package SP20_simulator;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	// 최댓값 지정
	public static final int MAX_MEMORY_SIZE=65536;
	public static final int NUM_OF_REGISTER=10;
	// 레지스터 번호 지정
	public static final int REG_A=0;
	public static final int REG_X=1;
	public static final int REG_L=2;
	public static final int REG_B=3;
	public static final int REG_S=4;
	public static final int REG_T=5;
	public static final int REG_F=6;
	public static final int REG_PC=8;
	public static final int REG_SW=9;
	// Program 또는 Device의 상태를 저장하기위한 값
	public static final int STATUS_INITIALIZED=0;
	public static final int STATUS_READY=1;
	public static final int STATUS_RUNNING=2;
	public static final int STATUS_END=3;

	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	//HashMap<String,Object> deviceManager = new HashMap<>();
	//char[] memory = new char[MAX_MEMORY_SIZE];
	String[] memory = new String[MAX_MEMORY_SIZE];
	int[] register = new int[NUM_OF_REGISTER];		// 레지스터내 정보를 저장할 배열
	double register_F;
	
	SymbolTable symbolTable;
	InstTable instTable;

	String log;
	String instruction;
	int startAddrOfM;

	deviceData data;

	// Program의 상태를 저장하기 위한 클래스
	// VisualSimulator에 특별히 출력을 해야하는 값들 역시 저장
	programData pgData;
	public class programData {
		String name = "";									// 이름
		int startAddr = 0;									// 시작 주소
		int length = 0;										// 길이
		int ta = 0;												// Target Address
		String recentDev = "";							// 최근 사용한 Device
		char status = STATUS_INITIALIZED;	// 상태
	}

	// Device의 정보를 저장하기위한 클래스
	// Device의 이름을 Key로 갖는 HashMap
	HashMap<String, deviceData> deviceManager;
	public class deviceData {
		String name = "";											// 이름
		String content = "";										// 장치(파일) 내 내용
		String mod = "";											// 읽기 모드시 "R", 쓰기 모드시 "W"
		int curserLoc = 0;											// 읽기 모드시 읽고 있는 위치 값 저장, 쓰기 모드시 쓰여진 글자수 저장
		char devStatus = STATUS_INITIALIZED;		// 상태
	}

	/**
	 * ResourceManager 클래스 초기화
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
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		for(String s : memory)
			s = "";
		for(int i : register)
			i = 0;
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 * 추가로 쓰기 모드로 사용한 Device의 내용을 실질적으로 File에 작성한다.
	 */
	public void closeDevice() {
		// deviceManager 내에  모든 장치를 점검
		for(String key : deviceManager.keySet()){
			deviceData data = deviceManager.get(key);
			// 쓰기 모드로 사용되었던 장치라면 파일에 내용을 저장
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
		// 후에 장치에 대한 정보 초기화
		pgData.recentDev = "";
		deviceManager = new HashMap<>();
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * 또한 test 시 파일의 내용을 전부 읽어 deviceManager 내 content에 저장한다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 */
	public void testDevice(String devName) {
		// 장치(파일)를 열어서 deviceManager에 정보 저장 및 상태 변경
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
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 */
	public String readDevice(String devName, int num){
		String loadContent = "";
		if(deviceManager.containsKey(devName)) {
			deviceData data = deviceManager.get(devName);
			switch (data.devStatus) {
				// 장치가 처음 쓰여지는 경우 모드 설정 및 상태 변경
				case STATUS_READY:
					data.mod = "R";
					data.devStatus = STATUS_RUNNING;
				case STATUS_RUNNING:
					int index = data.curserLoc;
					// Content를 다 읽은 상태라면 NULL값을 리턴한다.
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
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param content 보내는 데이터
	 * @param num 보내는 글자의 개수
	 */
	public void writeDevice(String devName, String content, int num){
		if(deviceManager.containsKey(devName)){
			deviceData data = deviceManager.get(devName);
			switch (data.devStatus) {
				// 장치가 처음 쓰여지는 경우 모드 설정 및 상태 변경
				case STATUS_READY:
					data.mod = "W";
					data.content = "";		// 읽기 모드인 경우 내용을 초기화
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
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public String getMemory(int location, int num){
		String loadData = "";
		for(int i = 0; i < num; i++){
			loadData += memory[location+i];
		}
		return loadData;
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, String data, int num){
		for(int i = 0; i <num; i++){
			memory[locate+i] = data.substring(i*2, i*2+2);
		}
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
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
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
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