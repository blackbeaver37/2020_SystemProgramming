package SP20_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	ArrayList<sectionData> sectionList;								// 섹션별 데이터 리스트
	int tmpAddr;

	// 섹션별 데이터를 저장할 클래스
	public class sectionData {
		String name = "";														// 각 섹션의 이름
		int length = 0;															// 각 섹션의 길이
		int startAddr = 0;														// 각 섹션의 시작주소
		ArrayList<String> defName = new ArrayList<>();		// D레코드에서 정의한 Symbol 이름
		ArrayList<Integer> defAddr = new ArrayList<>();		// D레코드에서 정의한 Symbol의 주소
		ArrayList<String> refList = new ArrayList<>();			// R레코드에서 선언한 참소할 Symbol 이름
		ArrayList<Integer> tStartAddr = new ArrayList<>();	// 각각의 T레코드 라인의 시작 주소
		ArrayList<Integer> tLength = new ArrayList<>();		// 각각의 T레코드 라인의 길이
		ArrayList<String> tCode = new ArrayList<>();			// 각각의 T레코드의 코드
		ArrayList<Integer> mAddr = new ArrayList<>();			// 각각의 M레코드의 수정 할 주소
		ArrayList<Integer> mCount = new ArrayList<>();		// 각각의 M레코드의 수정할 코드 수
		ArrayList<Character> mOper = new ArrayList<>();		// 각각의 M레코드의 연산
		ArrayList<String> mCode = new ArrayList<>();			// 각각의 M레코드의 코드
	}
	
	public SicLoader(ResourceManager rMgr) {
		setResourceManager(rMgr);
		sectionList = new ArrayList<>();
		tmpAddr = 0;
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager rMgr) {
		this.rMgr=rMgr;
	}

	/**
	 * 코드 정보를 파싱하여 섹션별 데이터로 저장한다.
	 * @param code : Section 하나의 전체 object code
	 * @return sectionData형태로 파싱된 데이터
	 */
	private sectionData parsingSectionData(String code){
		sectionData data = new sectionData();
		String tmpCode = code;

		// H Record 부분 파싱
		tmpCode = tmpCode.substring(1);
		data.name = tmpCode.substring(0, 6).trim();	// 이름 저장

		tmpCode = tmpCode.substring(6);
		data.startAddr = Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr;		// 시작 주소 저장

		tmpCode = tmpCode.substring(6);
		data.length = Integer.parseInt(tmpCode.substring(0, 6), 16);			// 길이 저장

		tmpCode = tmpCode.substring(7);

		// D Record 부분 파싱
		if(tmpCode.startsWith("D")){
			tmpCode = tmpCode.substring(1);
			while(!tmpCode.startsWith("\n")){
				data.defName.add(tmpCode.substring(0, 6).trim());										// 이름 저장
				data.defAddr.add(Integer.parseInt(tmpCode.substring(6, 12), 16) + tmpAddr);	// 주소 저장
				tmpCode = tmpCode.substring(12);
			}
			tmpCode = tmpCode.substring(1);
		}

		// R Record 부분 파싱
		if(tmpCode.startsWith("R")){
			tmpCode = tmpCode.substring(1);
			while(!tmpCode.startsWith("\n")){
				data.refList.add(tmpCode.substring(0, 6).trim());											// 이름 저장
				tmpCode = tmpCode.substring(6);
			}
			tmpCode = tmpCode.substring(1);
		}

		// T Record 부분 파싱
		while(tmpCode.startsWith("T")){									// T Record Line 수만큼 반복
			tmpCode = tmpCode.substring(1);
			data.tStartAddr.add(Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr);		// 해당 라인의 시작 주소 저장
			tmpCode = tmpCode.substring(6);
			data.tLength.add(Integer.parseInt(tmpCode.substring(0, 2), 16));	// 해당 라인의 길이 저장
			tmpCode = tmpCode.substring(2);
			String str = "";
			while(!tmpCode.startsWith("\n")){
				str += tmpCode.substring(0, 1);
				tmpCode = tmpCode.substring(1);
			}
			data.tCode.add(str);												// 해당 Line의 코드 저장
			tmpCode = tmpCode.substring(1);
		}

		// M Recode 부분 파싱
		while(tmpCode.startsWith("M")){									// M Record Line 수만큼 반복
			tmpCode = tmpCode.substring(1);
			data.mAddr.add(Integer.parseInt(tmpCode.substring(0, 6), 16) + tmpAddr);		// 해당 라인의 수정 할 주소 저장
			tmpCode = tmpCode.substring(6);
			data.mCount.add(Integer.parseInt(tmpCode.substring(0, 2), 16));	// 해당 라인의 수정할 코드 수 저장
			tmpCode = tmpCode.substring(2);
			data.mOper.add(tmpCode.charAt(0));
			tmpCode = tmpCode.substring(1);
			String str = "";
			while(!tmpCode.startsWith("\n")){
				str += tmpCode.substring(0, 1);
				tmpCode = tmpCode.substring(1);
			}
			data.mCode.add(str.trim());										// 해당 코드 저장
			tmpCode = tmpCode.substring(1);
		}
		tmpAddr += data.length;
		return data;
	}

	/**
	 * 작성된 SectionList로 SymbolTable을 작성
	 * @param sectionList : 작성된 sectionList
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
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		// 전체 코드를 String에 저장
		String allCode = "";
		try {
			Scanner scanner = new Scanner(objectCode);
			while (scanner.hasNextLine()) {
				allCode += scanner.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// 전체 코드를 섹션별로 나누어 String 배열에 저장
		String[] codeList = allCode.split("\n\n");
		// 섹션별 코드로 파싱을 실시하여 SectionData List 작성
		for(int index = 0; index < codeList.length; index++){
			sectionData data = parsingSectionData(codeList[index]);
			sectionList.add(data);
		}

		// VisualSimulator출력에 필요한 값들 ResourceManager로 전달
		rMgr.pgData.name = sectionList.get(0).name;
		rMgr.pgData.startAddr = sectionList.get(0).startAddr;
		rMgr.pgData.length = tmpAddr;

		// SymbolTable 작성
		makeSymTab(sectionList);

		// 메모리로 코드 Load
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
