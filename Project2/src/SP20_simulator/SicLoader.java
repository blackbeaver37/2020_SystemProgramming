package SP20_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	ArrayList<sectionData> sectionList;								// ���Ǻ� ������ ����Ʈ
	int tmpAddr;

	// ���Ǻ� �����͸� ������ Ŭ����
	public class sectionData {
		String name = "";														// �� ������ �̸�
		int length = 0;															// �� ������ ����
		int startAddr = 0;														// �� ������ �����ּ�
		ArrayList<String> defName = new ArrayList<>();		// D���ڵ忡�� ������ Symbol �̸�
		ArrayList<Integer> defAddr = new ArrayList<>();		// D���ڵ忡�� ������ Symbol�� �ּ�
		ArrayList<String> refList = new ArrayList<>();			// R���ڵ忡�� ������ ������ Symbol �̸�
		ArrayList<Integer> tStartAddr = new ArrayList<>();	// ������ T���ڵ� ������ ���� �ּ�
		ArrayList<Integer> tLength = new ArrayList<>();		// ������ T���ڵ� ������ ����
		ArrayList<String> tCode = new ArrayList<>();			// ������ T���ڵ��� �ڵ�
		ArrayList<Integer> mAddr = new ArrayList<>();			// ������ M���ڵ��� ���� �� �ּ�
		ArrayList<Integer> mCount = new ArrayList<>();		// ������ M���ڵ��� ������ �ڵ� ��
		ArrayList<Character> mOper = new ArrayList<>();		// ������ M���ڵ��� ����
		ArrayList<String> mCode = new ArrayList<>();			// ������ M���ڵ��� �ڵ�
	}
	
	public SicLoader(ResourceManager rMgr) {
		setResourceManager(rMgr);
		sectionList = new ArrayList<>();
		tmpAddr = 0;
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager rMgr) {
		this.rMgr=rMgr;
	}

	/**
	 * �ڵ� ������ �Ľ��Ͽ� ���Ǻ� �����ͷ� �����Ѵ�.
	 * @param code : Section �ϳ��� ��ü object code
	 * @return sectionData���·� �Ľ̵� ������
	 */
	private sectionData parsingSectionData(String code){
		sectionData data = new sectionData();
		String tmpCode = code;

		// H Record �κ� �Ľ�
		tmpCode = tmpCode.substring(1);
		data.name = tmpCode.substring(0, 6).trim();	// �̸� ����

		tmpCode = tmpCode.substring(6);
		data.startAddr = Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr;		// ���� �ּ� ����

		tmpCode = tmpCode.substring(6);
		data.length = Integer.parseInt(tmpCode.substring(0, 6), 16);			// ���� ����

		tmpCode = tmpCode.substring(7);

		// D Record �κ� �Ľ�
		if(tmpCode.startsWith("D")){
			tmpCode = tmpCode.substring(1);
			while(!tmpCode.startsWith("\n")){
				data.defName.add(tmpCode.substring(0, 6).trim());										// �̸� ����
				data.defAddr.add(Integer.parseInt(tmpCode.substring(6, 12), 16) + tmpAddr);	// �ּ� ����
				tmpCode = tmpCode.substring(12);
			}
			tmpCode = tmpCode.substring(1);
		}

		// R Record �κ� �Ľ�
		if(tmpCode.startsWith("R")){
			tmpCode = tmpCode.substring(1);
			while(!tmpCode.startsWith("\n")){
				data.refList.add(tmpCode.substring(0, 6).trim());											// �̸� ����
				tmpCode = tmpCode.substring(6);
			}
			tmpCode = tmpCode.substring(1);
		}

		// T Record �κ� �Ľ�
		while(tmpCode.startsWith("T")){									// T Record Line ����ŭ �ݺ�
			tmpCode = tmpCode.substring(1);
			data.tStartAddr.add(Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr);		// �ش� ������ ���� �ּ� ����
			tmpCode = tmpCode.substring(6);
			data.tLength.add(Integer.parseInt(tmpCode.substring(0, 2), 16));	// �ش� ������ ���� ����
			tmpCode = tmpCode.substring(2);
			String str = "";
			while(!tmpCode.startsWith("\n")){
				str += tmpCode.substring(0, 1);
				tmpCode = tmpCode.substring(1);
			}
			data.tCode.add(str);												// �ش� Line�� �ڵ� ����
			tmpCode = tmpCode.substring(1);
		}

		// M Recode �κ� �Ľ�
		while(tmpCode.startsWith("M")){									// M Record Line ����ŭ �ݺ�
			tmpCode = tmpCode.substring(1);
			data.mAddr.add(Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr);		// �ش� ������ ���� �� �ּ� ����
			tmpCode = tmpCode.substring(6);
			data.mCount.add(Integer.parseInt(tmpCode.substring(0, 2), 16));	// �ش� ������ ������ �ڵ� �� ����
			tmpCode = tmpCode.substring(2);
			data.mOper.add(tmpCode.charAt(0));
			tmpCode = tmpCode.substring(1);
			String str = "";
			while(!tmpCode.startsWith("\n")){
				str += tmpCode.substring(0, 1);
				tmpCode = tmpCode.substring(1);
			}
			data.mCode.add(str.trim());										// �ش� �ڵ� ����
			tmpCode = tmpCode.substring(1);
		}
		tmpAddr += data.length;
		return data;
	}

	/**
	 * �ۼ��� SectionList�� SymbolTable�� �ۼ�
	 * @param sectionList : �ۼ��� sectionList
	 */
	private void makeSymTab(ArrayList<sectionData> sectionList){
		for(sectionData data : sectionList){
			rMgr.symbolTable.putSymbol(data.name, data.startAddr);
			for(int i = 0; i < data.defName.size(); i++){
				rMgr.symbolTable.putSymbol(data.defName.get(i), data.defAddr.get(i));
			}
		}
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode){
		// ��ü �ڵ带 String�� ����
		String allCode = "";
		try {
			Scanner scanner = new Scanner(objectCode);
			while (scanner.hasNextLine()) {
				allCode += scanner.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// ��ü �ڵ带 ���Ǻ��� ������ String �迭�� ����
		String[] codeList = allCode.split("\n\n");
		// ���Ǻ� �ڵ�� �Ľ��� �ǽ��Ͽ� SectionData List �ۼ�
		for(int index = 0; index < codeList.length; index++){
			sectionData data = parsingSectionData(codeList[index]);
			sectionList.add(data);
		}

		// VisualSimulator��¿� �ʿ��� ���� ResourceManager�� ����
		rMgr.pgData.name = sectionList.get(0).name;
		rMgr.pgData.startAddr = sectionList.get(0).startAddr;
		rMgr.pgData.length = tmpAddr;

		// SymbolTable �ۼ�
		makeSymTab(sectionList);

		// �޸𸮷� �ڵ� Load
		for(sectionData data : sectionList){
			for(int i=0; i<data.tCode.size(); i++){
				rMgr.setMemory(data.tStartAddr.get(i), data.tCode.get(i), data.tLength.get(i));
			}
			for(int i=0; i<data.mCount.size(); i++){
				int inputValue = rMgr.symbolTable.search(data.mCode.get(i));
				int targetValue = Integer.parseInt(rMgr.getMemory(data.mAddr.get(i), 3), 16);
				int value = 0;
				if(data.mCount.get(i) == 5){
					inputValue &= 0X0FFFFF;
				}
				switch (data.mOper.get(i)){
					case '+':
						value = targetValue + inputValue;
						break;
					case '-':
						value = targetValue - inputValue;
						break;
					default:
				}
				rMgr.setMemory(data.mAddr.get(i), String.format("%06X", value), 3);
			}
		}
	}
}
