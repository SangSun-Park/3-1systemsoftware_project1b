import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public SymbolTable() {
		symbolList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();
	}
	
	
	
	public void putSymbol(String symbol, int location) {
		symbolList.add(symbol);
		locationList.add(location);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		for(int i = 0; i < symbolList.size(); i++){
			if (symbol.equals(symbolList.get(i))) {
					locationList.set(i, newLocation);
			}
		}
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		for(int i = 0; i < symbolList.size(); i++){
			if (symbol.equals(symbolList.get(i))) {
					return locationList.get(i);
			}
		}
		return address;
	}
	
	
	
}
