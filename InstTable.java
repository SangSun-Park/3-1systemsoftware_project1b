import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap = new HashMap<>();
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instfile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instfile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File("./" + fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			while((line = bufReader.readLine()) != null){
				Instruction inst = new Instruction(line);
				instMap.put(inst.name, inst);
			}
			bufReader.close();
		}catch(Exception e) {
			e.getStackTrace();
		}
	}
	
	public String getName(String inst) {
		if (inst.charAt(0) == '+')
			inst = inst.substring(1,inst.length());
		return instMap.get(inst).name;
		}
	
	public int getformat(String inst) {
		if (inst.charAt(0) == '+')
		inst = inst.substring(1,inst.length());
		return instMap.get(inst).format;
		}
	
	public String getopcode(String inst) {
		if (inst.charAt(0) == '+')
			inst = inst.substring(1,inst.length());
		return instMap.get(inst).opcode;
		}
	
	public int getopnum(String inst) {
		if (inst.charAt(0) == '+')
			inst = inst.substring(1,inst.length());
		return instMap.get(inst).opnum;
		}
	
	public boolean search(String inst) {
		if (inst.charAt(0) == '+')
			inst = inst.substring(1,inst.length());
		if (instMap.get(inst) != null)
			return true;
		else 
			return false;
		}
	
	
	//get, set, search 등의 함수는 자유 구현

}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	/* 
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	String name;
	int format;
	String opcode;
	int opnum;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		String arr[] = line.split("\t", 4);
		name = arr[0];
		format = Integer.parseInt(arr[1]);
		opcode = arr[2];
		opnum = Integer.parseInt(arr[3]);
	}
	
		
	//그 외 함수 자유 구현
	
	
}
