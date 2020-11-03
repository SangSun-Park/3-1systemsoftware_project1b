import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과
 * 같다. 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 2) 사용자가 작성한 input 파일을
 * 읽어들인 후 저장한다. 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 4) 분석된 내용을
 * 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 * 작성중의 유의사항 : 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나
 * 완전히 대체하는 것은 안된다. 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한
 * 허용됨. 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능. 4) 파일, 또는 콘솔창에 한글을
 * 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. 필요한 경우 String 대신 별도의 클래스를
	 * 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.txt");
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20150235");
		assembler.printLiteralTable("literaltab_20150235");
		assembler.pass2();
		assembler.printObjectCode("output_20150235");

	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			File file = new File("./" + inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			while ((line = bufReader.readLine()) != null) {
				char ch = line.charAt(0);
				if (ch != '.')
					lineList.add(line);
			}
			bufReader.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * pass1 과정을 수행한다. 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성 2) label을 symbolTable에
	 * 정리
	 * 
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		int k = 0;
		int loc = 0; // section을 구분하기 위해서 사용하는 변수
		int size = lineList.size();
		for (int i = 0; i < size; k++) {
			symtabList.add(new SymbolTable());
			literaltabList.add(new LiteralTable());
			Token tmp = null;
			while (true) {
				tmp = new Token(lineList.get(i));
				i++;
				if ((tmp.operator.equals("CSECT") || tmp.operator.equals("END")) // 섹션 자르기
						&& symtabList.get(k).symbolList.size() > 1) {
					if (tmp.operator.equals("CSECT"))
						i--;
					break;
				}

				tmp.location = 0;

				if (tmp.label != null && !tmp.label.equals("")) { // symtable 만들기
					symtabList.get(k).putSymbol(tmp.label, tmp.location);
				}
				if (tmp.operand == null || tmp.operand[0].equals("")) {
				} else if (tmp.operand[0].charAt(0) == '=') { // literatable 만들기
					boolean l = true;
					for (int j = 0; j < literaltabList.get(k).literalList.size(); j++)
						if (literaltabList.get(k).literalList.get(j).equals(tmp.operand[0])) {
							l = false;
							break;
						}
					if (l) {
						literaltabList.get(k).putLiteral(tmp.operand[0], tmp.location);
					}
					l = true;
				}

			}
			TokenList.add(new TokenTable(symtabList.get(k), literaltabList.get(k), instTable)); // 섹션별 tokentable 생성
			for (; loc < i; loc++)
				TokenList.get(k).putToken(lineList.get(loc));
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File("./" + fileName);
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(filewriter);
			String line = "";
			for (int i = 0; i < symtabList.size(); i++) {
				for (int j = 0; j < symtabList.get(i).symbolList.size(); j++) {
					line = symtabList.get(i).symbolList.get(j) + "\t" // 가공하여서 출력
							+ Integer.toHexString(symtabList.get(i).locationList.get(j)).toUpperCase();
					bw.write(line);
					bw.newLine();
				}
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File("./" + fileName);
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(filewriter);
			String line = "";
			for (int i = 0; i < literaltabList.size(); i++) {
				if (literaltabList.get(i) == null)
					continue;
				for (int j = 0; j < literaltabList.get(i).literalList.size(); j++) {
					line = literaltabList.get(i).literalList.get(j).substring(3, // 가공하여서 출력
							literaltabList.get(i).literalList.get(j).length() - 1) + "\t"
							+ Integer.toHexString(literaltabList.get(i).locationList.get(j)).toUpperCase();
					bw.write(line);
					bw.newLine();
				}
			}
			bw.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	/**
	 * pass2 과정을 수행한다. 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		codeList = new ArrayList<String>();
		for (int i = 0; i < TokenList.size(); i++) { // nixbpe를 먼저 설정
			Token ope = null;
			for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				ope = TokenList.get(i).getToken(j);
				if (instTable.search(ope.operator)) {
					if (instTable.getformat(ope.operator) > 2) { // 3형식이상
						if (instTable.getopnum(ope.operator) == 0) { //operand가 없는 경우
							ope.setFlag(TokenTable.nFlag, 1);
							ope.setFlag(TokenTable.iFlag, 1);
						} else {
							if (ope.operand[0].charAt(0) == '@') // indirect
								ope.setFlag(TokenTable.nFlag, 1);
							else if (ope.operand[0].charAt(0) == '#') // immediate
								ope.setFlag(TokenTable.iFlag, 1);
							else {									
								ope.setFlag(TokenTable.nFlag, 1);	// 일반적인 3형식
								ope.setFlag(TokenTable.iFlag, 1);
							}
							if (ope.operator.charAt(0) == '+')	// 4형식 확인
								ope.setFlag(TokenTable.eFlag, 1);
							else if (ope.operand[0].charAt(0) != '#' && instTable.getopnum(ope.operator) != 0) 
								ope.setFlag(TokenTable.pFlag, 1);		// bflag는 사용하지 않아 별도로 구현하지 않음
							if (ope.operand.length >= 2 && ope.operand[1] != null)
								if (ope.operand[1].charAt(0) == 'X')
									ope.setFlag(TokenTable.xFlag, 1);
						}
					}
				}
				TokenList.get(i).makeObjectCode(j);		// objectcode 생성
				codeList.add(TokenList.get(i).getObjectCode(j));	// list에 저장
			}
		}
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		int extr = 0; // EXTREF의 위치 저장
		try {
			File file = new File("./" + fileName);
			FileWriter filewriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(filewriter);
			String line = "";
			int leng = 0;
			Token tmp = null;
			for (int i = 0; i < TokenList.size(); i++) {
				for (int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
					tmp = TokenList.get(i).getToken(j);
					if (tmp.operator.equals("START") || tmp.operator.equals("CSECT")) { // head recode 출력
						int lengh = 0;
						for (int h = TokenList.get(i).tokenList.size() - 1; h > 0; h--) {
							Token tmph = TokenList.get(i).getToken(h);
							if (!tmph.operator.equals("EQU")) {
								if (tmph.operator.equals("END") || tmph.operator.equals("LTORG")) { // literaltab을 참조
																									// location 계산
									lengh += literaltabList.get(i).locationList
											.get(literaltabList.get(i).locationList.size() - 1)
											+ (int) ((literaltabList.get(i).literalList
													.get(literaltabList.get(i).literalList.size() - 1).length() - 4)
													/ 2);
									break;
								} else { // head recode의 길이를 위해 location 계산
									if (tmph.operator.equals("WORD"))
										lengh += tmph.location + 3;
									else if (tmph.operator.equals("BYTE"))
										lengh += tmph.location + (int) ((tmp.operand[0].length() - 3) / 2);
									else if (tmph.operator.equals("RESW"))
										lengh += tmph.location + Integer.parseInt(tmph.operand[0]) * 3;
									else if (tmph.operator.equals("RESB"))
										lengh += tmph.location + Integer.parseInt(tmph.operand[0]);
									else if (TokenList.get(i).instTab.search(tmph.operator)) {
										if (tmph.operator.charAt(0) == '+')
											lengh++;
										lengh += tmp.location + TokenList.get(i).instTab.getformat(tmph.operator);
									}
									break;
								}
							}
						}
						line += "H" + String.format("%-6s%06X%06X", tmp.label, tmp.location, lengh);
						bw.write(line);
						bw.newLine();
						line = "";
					} else if (tmp.operator.equals("EXTDEF")) {
						line = "D";
						for (int ext = 0; ext < tmp.operand.length; ext++)
							line += String.format("%-6s%06X", tmp.operand[ext],
									TokenList.get(i).symTab.search(tmp.operand[ext]));
						bw.write(line);
						bw.newLine();
						line = "";
					} else if (tmp.operator.equals("EXTREF")) {
						extr = j;
						line = "R";
						for (int ext = 0; ext < tmp.operand.length; ext++)
							line += String.format("%-6s", tmp.operand[ext]);
						bw.write(line);
						bw.newLine();
						line = "";
					} else if (tmp.operator.equals("LTORG")) {	// literal에 담긴 값을 text recode로 출력
						if (!line.equals("")) {
							bw.write(line);
							line = "";
							bw.newLine();
						}
						for (int l = 0; l < literaltabList.get(i).literalList.size(); l++) {	// literal table 만큼 확인
							if (l == 0) {
								int lleng = 0;
								for (int ll = 0; ll < literaltabList.get(i).literalList.size(); ll++) {
									if (literaltabList.get(i).literalList.get(l).charAt(1) == 'X')
										lleng += (int) ((literaltabList.get(i).literalList.get(ll).length() - 4) / 2);
									else
										lleng += literaltabList.get(i).literalList.get(ll).length() - 4;
								}
								line += "T" + String.format("%06X%02X", literaltabList.get(i).locationList.get(l), lleng);
							}
							if (literaltabList.get(i).literalList.get(l).charAt(1) == 'X') {	
								line += String.format("%s", literaltabList.get(i).literalList.get(l).substring(3,
										literaltabList.get(i).literalList.get(l).length() - 1));
							} else {
								String str = literaltabList.get(i).literalList.get(l).substring(3,
										literaltabList.get(i).literalList.get(l).length() - 1);
								for (int sl = 0; sl < str.length(); sl++)
									line += String.format("%s", Integer.toHexString(str.charAt(sl)).toUpperCase());
							}
						}
						bw.write(line);
						bw.newLine();
						line = "";
					} else if (tmp.operator.equals("RESW") || tmp.operator.equals("RESB")
							|| tmp.operator.equals("EQU")) { // objectcode 없음
						continue;
					} else { // text recode 출력
						if (tmp.operator.equals("END")) { // END가 나오면 text recode에 이어 붙여서 출력
							for (int l = 0; l < literaltabList.get(i).literalList.size(); l++) {
								if (literaltabList.get(i).literalList.get(l).charAt(1) == 'X') {
									line += String.format("%s", literaltabList.get(i).literalList.get(l).substring(3,
											literaltabList.get(i).literalList.get(l).length() - 1));
								} else {
									String str = literaltabList.get(i).literalList.get(l).substring(3,
											literaltabList.get(i).literalList.get(l).length() - 1);
									for (int sl = 0; sl < str.length(); sl++)
										line += String.format("%s", Integer.toHexString(str.charAt(sl)).toUpperCase());
								}
							}
						} else {
							if (leng == 0 || leng + tmp.byteSize > 30) {
								if (leng != 0) {		// text recode의 입력 한계를 넘어서 출력
									bw.write(line);
									line = "";
									leng = 0;
									bw.newLine();
								}
								int lengt = tmp.byteSize;		// text recode의 길이를 체크하기 위해서 사용
								for (int t = j + 1; lengt < 30 && t < TokenList.get(i).tokenList.size(); t++) {
									Token tmpt = TokenList.get(i).getToken(t);
									if (tmpt.operator.equals("END")) {		// END가 나오면 literal 내용도 가져오기 위해
										for (int l = 0; l < literaltabList.get(i).literalList.size(); l++) {
											if (literaltabList.get(i).literalList.get(l).charAt(1) == 'X')
												lengt += (int) (literaltabList.get(i).literalList.get(l).length() - 4)
														/ 2;
											else
												lengt += literaltabList.get(i).literalList.get(l).length() - 4;
										}
									}
									lengt += tmpt.byteSize;
									if (lengt > 30) {		// 길이가 초과되면 멈춤
										lengt -= tmpt.byteSize;
										break;
									}
								}
								line = "T" + String.format("%06X%02X%s", tmp.location, lengt, tmp.objectCode);
								leng = tmp.byteSize;
							} else {
								line += tmp.objectCode;	
								leng += tmp.byteSize;
							}
						}
					}
				}
				if (!line.equals("")) {	// text recode의 남아있는 내용 출력
					bw.write(line);
					line = "";
					bw.newLine();
				}
				Token exttmp = TokenList.get(i).getToken(extr);
				for (int j = extr + 1; j < TokenList.get(i).tokenList.size(); j++) { // modification recode
					Token mtmp = TokenList.get(i).getToken(j);
					if (mtmp.objectCode != null)
						for (int fop = 0; fop < mtmp.operand.length; fop++) { // EXTREF를 활용하여서 작성
							boolean f = false;
							for (int exop = 0; exop < exttmp.operand.length; exop++) { // operand를 확인하여 같다면 작성
								if (mtmp.operand[fop].equals(exttmp.operand[exop])) {
									f = true;
									break;
								}
							}
							if (f) {
								if (mtmp.operator.equals("WORD")) {
									if (fop == 0)
										line = "M" + String.format("%06X06+%s", mtmp.location, mtmp.operand[fop]);
									else
										line = "M" + String.format("%06X06%s%s", mtmp.location, mtmp.operand[fop - 1],
												mtmp.operand[fop]);
								} else {
									line = "M" + String.format("%06X05+%s", mtmp.location + 1, mtmp.operand[fop]);
								}
								bw.write(line);
								line = "";
								bw.newLine();
							}
						}
				}
				if (TokenList.get(i).getToken(0).operator.equals("START")) // end recode
					line = "E000000";
				else
					line = "E";
				leng = 0;
				bw.write(line);
				line = "";
				bw.newLine();
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

}
