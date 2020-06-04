package SP20_simulator;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	// �ִ� ����
	public static final int MAX_MEMORY_SIZE=65536;
	public static final int NUM_OF_REGISTER=10;
	// �������� ��ȣ ����
	public static final int REG_A=0;
	public static final int REG_X=1;
	public static final int REG_L=2;
	public static final int REG_B=3;
	public static final int REG_S=4;
	public static final int REG_T=5;
	public static final int REG_F=6;
	public static final int REG_PC=8;
	public static final int REG_SW=9;
	// Program �Ǵ� Device�� ���¸� �����ϱ����� ��
	public static final int STATUS_INITIALIZED=0;
	public static final int STATUS_READY=1;
	public static final int STATUS_RUNNING=2;
	public static final int STATUS_END=3;

	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	//HashMap<String,Object> deviceManager = new HashMap<>();
	//char[] memory = new char[MAX_MEMORY_SIZE];
	String[] memory = new String[MAX_MEMORY_SIZE];
	int[] register = new int[NUM_OF_REGISTER];		// �������ͳ� ������ ������ �迭
	double register_F;
	
	SymbolTable symbolTable;
	InstTable instTable;

	String log;
	String instruction;
	int startAddrOfM;

	deviceData data;

	// Program�� ���¸� �����ϱ� ���� Ŭ����
	// VisualSimulator�� Ư���� ����� �ؾ��ϴ� ���� ���� ����
	programData pgData;
	public class programData {
		String name = "";									// �̸�
		int startAddr = 0;									// ���� �ּ�
		int length = 0;										// ����
		int ta = 0;												// Target Address
		String recentDev = "";							// �ֱ� ����� Device
		char status = STATUS_INITIALIZED;	// ����
	}

	// Device�� ������ �����ϱ����� Ŭ����
	// Device�� �̸��� Key�� ���� HashMap
	HashMap<String, deviceData> deviceManager;
	public class deviceData {
		String name = "";											// �̸�
		String content = "";										// ��ġ(����) �� ����
		String mod = "";											// �б� ���� "R", ���� ���� "W"
		int curserLoc = 0;											// �б� ���� �а� �ִ� ��ġ �� ����, ���� ���� ������ ���ڼ� ����
		char devStatus = STATUS_INITIALIZED;		// ����
	}

	/**
	 * ResourceManager Ŭ���� �ʱ�ȭ
	 */
	public ResourceManager(){
		instTable = new InstTable("inst.data");
		initializeResource();
		symbolTable = new SymbolTable();
		pgData = new programData();
		deviceManager = new HashMap<>();
		log = "";
		instruction = "";
		startAddrOfM = 0;
	}

	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		for(String s : memory)
			s = "";
		for(int i : register)
			i = 0;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 * �߰��� ���� ���� ����� Device�� ������ ���������� File�� �ۼ��Ѵ�.
	 */
	public void closeDevice() {
		// deviceManager ����  ��� ��ġ�� ����
		for(String key : deviceManager.keySet()){
			deviceData data = deviceManager.get(key);
			// ���� ���� ���Ǿ��� ��ġ��� ���Ͽ� ������ ����
			if(data.mod == "W"){
				try{
					File file = new File(data.name + ".txt");
					FileWriter fw = new FileWriter(file);
					fw.write(data.content);
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// �Ŀ� ��ġ�� ���� ���� �ʱ�ȭ
		pgData.recentDev = "";
		deviceManager = new HashMap<>();
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * ���� test �� ������ ������ ���� �о� deviceManager �� content�� �����Ѵ�.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		// ��ġ(����)�� ��� deviceManager�� ���� ���� �� ���� ����
		setRegister(REG_SW, InstLuncher.COMP_NOT);
		if(deviceManager.containsKey(devName))
			return;
		try {
			File file = new File(devName + ".txt");
			Scanner scanner = new Scanner(file);
			data = new deviceData();
			String devContent = "";
			devContent += scanner.nextLine();
			while (scanner.hasNextLine()) {
				devContent += "\n" + scanner.nextLine();
			}
			data.name = devName;
			data.content = devContent;
			data.devStatus = STATUS_READY;
			deviceManager.put(devName, data);
			pgData.recentDev = devName;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 */
	public String readDevice(String devName, int num){
		String loadContent = "";
		if(deviceManager.containsKey(devName)) {
			deviceData data = deviceManager.get(devName);
			switch (data.devStatus) {
				// ��ġ�� ó�� �������� ��� ��� ���� �� ���� ����
				case STATUS_READY:
					data.mod = "R";
					data.devStatus = STATUS_RUNNING;
				case STATUS_RUNNING:
					int index = data.curserLoc;
					// Content�� �� ���� ���¶�� NULL���� �����Ѵ�.
					if(data.curserLoc >= data.content.length()){
						loadContent = "\0";
						return loadContent;
					}
					loadContent = data.content.substring(index, index+num);
					data.curserLoc += num;
					break;
				default:
			}
		}
		return loadContent;
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param content ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, String content, int num){
		if(deviceManager.containsKey(devName)){
			deviceData data = deviceManager.get(devName);
			switch (data.devStatus) {
				// ��ġ�� ó�� �������� ��� ��� ���� �� ���� ����
				case STATUS_READY:
					data.mod = "W";
					data.content = "";		// �б� ����� ��� ������ �ʱ�ȭ
					data.devStatus = STATUS_RUNNING;
				case STATUS_RUNNING:
					for (int i = 0; i < num; i++) {
						data.content += content;
						data.curserLoc++;
					}
					break;
				default:
			}
		}
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public String getMemory(int location, int num){
		String loadData = "";
		for(int i = 0; i < num; i++){
			loadData += memory[location+i];
		}
		return loadData;
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, String data, int num){
		for(int i = 0; i <num; i++){
			memory[locate+i] = data.substring(i*2, i*2+2);
		}
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		int tmpInt = data;
		int length = (int)(Math.log10(tmpInt)+1);
		char[] tmpChar = new char[length];
		for(int i = 0; i < length; i++){
			tmpChar[length - (i+1)] = Character.forDigit(tmpInt%10, 10);
			tmpInt /= 10;
		}
		return tmpChar;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int charToInt(char[] data){
		int tmpInt = 0;
		int length = data.length;
		for(int i = 0; i < length; i++){
			tmpInt += (data[i] - '0');
			if(i + 1 != length){
				tmpInt *= 10;
			}
		}
		return tmpInt;
	}
}