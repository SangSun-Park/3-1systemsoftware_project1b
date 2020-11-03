import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap = new HashMap<>();
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instfile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instfile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
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
	
	
	//get, set, search ���� �Լ��� ���� ����

}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	String name;
	int format;
	String opcode;
	int opnum;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		String arr[] = line.split("\t", 4);
		name = arr[0];
		format = Integer.parseInt(arr[1]);
		opcode = arr[2];
		opnum = Integer.parseInt(arr[3]);
	}
	
		
	//�� �� �Լ� ���� ����
	
	
}
