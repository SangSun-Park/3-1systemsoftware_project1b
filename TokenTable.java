import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	
	
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		//...
		tokenList = new ArrayList<Token>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
	}

	/**
	 * 초기화하면서 literalTable과 instTable을 링크시킨다.
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));

		if (tokenList.get(tokenList.size() - 1).operator.equals("LTORG")
				|| tokenList.get(tokenList.size() - 1).operator.equals("END")) {	// literal table 변경을 위해 location을 새로 계산
			int newLocation = tokenList.get(tokenList.size() - 2).location;
			boolean l = true;
			for (int i = 0; i < tokenList.size(); i++)
				if (tokenList.get(i).operand == null || tokenList.get(i).operand[0].equals(""))	
					continue;	
				else if (tokenList.get(i).operand[0].charAt(0) == '=') {		// literal이 존재하는지 찾음
					for (int j = 0; j < literalTab.literalList.size(); j++)
						if (literalTab.literalList.get(j).equals(tokenList.get(i).operand[0])
								&& !literalTab.locationList.get(j).equals(0)) {
							l = false;
							break;
						}
					if (l) {
						if (literalTab.literalList.size() != 0) {
							Token tokenb = tokenList.get(tokenList.size() - 2);
							if (tokenb.operator.equals("WORD"))
								newLocation += 3;
							else if (tokenb.operator.equals("BYTE"))
								newLocation += (int) ((tokenb.operand[0].length() - 3) / 2);
							else if (tokenb.operator.equals("RESW"))
								newLocation += Integer.parseInt(tokenb.operand[0]) * 3;
							else if (tokenb.operator.equals("RESB"))
								newLocation += Integer.parseInt(tokenb.operand[0]);
							else if (instTab.search(tokenb.operator)) {
								if (tokenb.operator.charAt(0) == '+')
									newLocation++;
								newLocation += instTab.getformat(tokenb.operator);
							}
						}
						literalTab.modifyLiteral(tokenList.get(i).operand[0], newLocation);	// 생성 되있는 literal table의 location 변경
					}
					l = true;
				}
			return;
		}
		
		Token token = tokenList.get(tokenList.size() - 1);
		if (tokenList.size() > 1) {
			Token tokenb = tokenList.get(tokenList.size() - 2);		
			if (tokenb.operator.equals("LTORG")) {				// literal table 확인하여 위치를 추가 계산
				token.location = literalTab.locationList.get(literalTab.locationList.size() - 1);
				String str = literalTab.literalList.get(literalTab.literalList.size() - 1);
				if (str.substring(0, 2).equals("=C"))
					token.location += str.substring(3, str.length() - 1).length();
				else
					token.location += (int) str.substring(3, str.length() - 1).length() / 2;
			}
			if(tokenb.operator.equals("STRAT") && tokenb.operator.equals("CSECT"))
				token.location += 0;
			else if(tokenb.operator.equals("WORD"))
				token.location += tokenb.location + 3;
			else if(tokenb.operator.equals("BYTE"))
				token.location += tokenb.location + (int)((tokenb.operand[0].length()-3) / 2);
			else if(tokenb.operator.equals("RESW"))
				token.location += tokenb.location + Integer.parseInt(tokenb.operand[0]) * 3;
			else if(tokenb.operator.equals("RESB"))
				token.location += tokenb.location + Integer.parseInt(tokenb.operand[0]);
			else if(instTab.search(tokenb.operator)){
				if (tokenb.operator.charAt(0) == '+')
					token.location ++;
				token.location += tokenb.location + instTab.getformat(tokenb.operator);
			}
			else token.location += tokenb.location;
			if (token.operator.equals("EQU")) {
				if(!token.operand[0].equals("*")) {
					if(token.operand[1].equals("+"))
						token.location = symTab.search(token.operand[0]) + symTab.search(token.operand[2]);
					else if(token.operand[1].equals("-"))
						token.location = symTab.search(token.operand[0]) - symTab.search(token.operand[2]);
					else if(token.operand[1].equals("*"))
						token.location = symTab.search(token.operand[0]) * symTab.search(token.operand[2]);
					else if(token.operand[1].equals("/"))
						token.location = symTab.search(token.operand[0]) / symTab.search(token.operand[2]);
				}
			}
		}
		else token.location = 0;
		
		if(!token.label.equals(""))
			symTab.modifySymbol(token.label, token.location); // symtable 수정 label
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		//...
		Token tmp = this.getToken(index);
		if(instTab.search(tmp.operator)) {
			int code = 0;
			tmp.objectCode = instTab.getopcode(tmp.operator);
			if(instTab.getformat(tmp.operator) == 2) {		// 2형식
				if(instTab.getopnum(tmp.operator) == 1) {
					if(tmp.operand[0].equals("A"))
						tmp.objectCode += "00";
					else if(tmp.operand[0].equals("S"))
						tmp.objectCode += "40";
					else if(tmp.operand[0].equals("T"))
						tmp.objectCode += "50";
					else if(tmp.operand[0].equals("X"))
						tmp.objectCode += "10";
				}
				else if(instTab.getopnum(tmp.operator) == 2) {
					for(int i = 0; i < 2; i++) {
						if(tmp.operand[i].equals("A"))
							tmp.objectCode += "0";
						else if(tmp.operand[i].equals("S"))
							tmp.objectCode += "4";
						else if(tmp.operand[i].equals("T"))
							tmp.objectCode += "5";
						else if(tmp.operand[i].equals("X"))
							tmp.objectCode += "1";
					}
				}
				tmp.byteSize = 2;
			}
			else if(tmp.getFlag(TokenTable.eFlag) == 1) {		// 4형식
				code += Integer.parseInt(instTab.getopcode(tmp.operator), 16) << 24;
				code += tmp.nixbpe << 20;
				if(tmp.getFlag(TokenTable.iFlag) == 16 && tmp.getFlag(TokenTable.nFlag) == 32) { // direct
					if(symTab.search(tmp.operand[0]) != 0) {
						code += symTab.search(tmp.operand[0]);
					}
					else if(literalTab.search(tmp.operand[0]) != 0){
						code += literalTab.search(tmp.operand[0]);
					}
				}
				else if(tmp.getFlag(TokenTable.nFlag) == 0 && tmp.getFlag(iFlag) == 16) { // immediate
					String str = tmp.operand[0].substring(1);
					code += Integer.parseInt(str);
				}
				else {  // indirect
					if(symTab.search(tmp.operand[0]) != 0) {
						Token stmp = new Token(null);
						for(int i = 0; i < tokenList.size(); i++) {
							stmp = tokenList.get(i);
							if(symTab.search(tmp.operand[0]) == stmp.location)
								break;
						}
						code += symTab.search(stmp.operand[0]);
					}
				}
				tmp.objectCode = String.format("%08X", code);
				tmp.byteSize = 4;
			}
			else {
				code += Integer.parseInt(instTab.getopcode(tmp.operator), 16) << 16; // 3형식
				code += tmp.nixbpe << 12;
				if (tmp.getFlag(TokenTable.iFlag) == 16 && tmp.getFlag(TokenTable.nFlag) == 32) { // 일반적 경
					if (symTab.search(tmp.operand[0]) != 0) {
						int loca = 0;
						if (tmp.operator.equals("WORD"))
							loca += tmp.location + 3;
						else if (tmp.operator.equals("BYTE"))
							loca += tmp.location + (int) ((tmp.operand[0].length() - 3) / 2);
						else if (tmp.operator.equals("RESW"))
							loca += tmp.location + Integer.parseInt(tmp.operand[0]) * 3;
						else if (tmp.operator.equals("RESB"))
							loca += tmp.location + Integer.parseInt(tmp.operand[0]);
						else if (instTab.search(tmp.operator)) {
							if (tmp.operator.charAt(0) == '+')
								loca++;
							loca += tmp.location + instTab.getformat(tmp.operator);
						}
						if (loca > symTab.search(tmp.operand[0]))
							code += 4096;
						code += symTab.search(tmp.operand[0]) - loca;
					} else if (literalTab.search(tmp.operand[0]) != 0) {
						int loca = 0;
						if (tmp.operator.equals("WORD"))
							loca += tmp.location + 3;
						else if (tmp.operator.equals("BYTE"))
							loca += tmp.location + (int) ((tmp.operand[0].length() - 3) / 2);
						else if (tmp.operator.equals("RESW"))
							loca += tmp.location + Integer.parseInt(tmp.operand[0]) * 3;
						else if (tmp.operator.equals("RESB"))
							loca += tmp.location + Integer.parseInt(tmp.operand[0]);
						else if (instTab.search(tmp.operator)) {
							if (tmp.operator.charAt(0) == '+')
								loca++;
							loca += tmp.location + instTab.getformat(tmp.operator);
						}
						if (loca > literalTab.search(tmp.operand[0]))
							code += 4096;
						code += literalTab.search(tmp.operand[0]) - loca;
					}
				}
				else if(tmp.getFlag(TokenTable.nFlag) == 0 && tmp.getFlag(iFlag) == 16) { // immediate
					String str = tmp.operand[0].substring(1);
					code += Integer.parseInt(str);
				}
				else {  // indirect
					if(symTab.search(tmp.operand[0]) != 0) {
						Token stmp = null;
						for(int i = 0; i < tokenList.size(); i++) {
							stmp = tokenList.get(i);
							if(symTab.search(tmp.operand[0]) == stmp.location)
								break;
						}
						code += symTab.search(stmp.operand[0]);
					}
				}
				tmp.objectCode = String.format("%06X", code);
				tmp.byteSize = 3;
			}
		}
		else if(tmp.operator.equals("WORD")) {
			tmp.objectCode = "000000";
			tmp.byteSize = 3;
		}
		else if(tmp.operator.equals("BYTE")) {
			if(tmp.operand[0].charAt(0) == 'X') {
				tmp.objectCode = tmp.operand[0].substring(2, tmp.operand[0].length()-1);
				tmp.byteSize =(int)((tmp.operand[0].length() - 3) / 2);
			}
			else {
				String str = tmp.operand[0].substring(2, tmp.operand[0].length()-1);
				for(int i = 0; i < str.length(); i++)
					tmp.objectCode = Integer.toHexString(str.charAt(i)).toUpperCase();
				tmp.byteSize = str.length();
			}
		}
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 占쌩곤옙
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String arr[] = line.split("\t", 4);
		label = arr[0];
		operator = arr[1];
		if(arr.length > 3)
			comment = arr[3];
		if(arr.length > 2)
			if(!arr[2].contentEquals("0")) { 	// operand 쪼개기
				String op[] = arr[2].split(",", 3);
				operand = new String[op.length];
				for(int i = 0; i < op.length; i++)
					operand[i] = op[i];
			}
		if(operator.equals("EQU") || operator.equals("WORD")) {	// operand 재처리 
			if(operand[0].equals("*"))
				return;
			char ch = 0;
			String op[] = null;
			boolean ca = false;
			for(int i = 0 ; i < operand[0].length(); i++) {	// 계산 문자가 있는지 확인
				ch = operand[0].charAt(i);
				if(ch == '+' || ch == '-' || ch == '*' || ch == '/') {
					ca = true;
					break;
				}
			}
			if(ca) {	// 있다면 다시 쪼개기
				op = operand[0].split(Character.toString(ch), 2);
				operand = new String[op.length+1];
				operand[0] = op[0];
				operand[1] = Character.toString(ch);
				operand[2] = op[1];
			}
		}
		nixbpe = 0;
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		nixbpe += flag;
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
