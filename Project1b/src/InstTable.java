import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {

		 try{
	            //파일 객체 생성
	            File file = new File(fileName);
	            FileReader filereader = new FileReader(file);
	            BufferedReader bufReader = new BufferedReader(filereader);
	            
	            String line = "";
	            
	            // 라인별로 파일을 읽어서 inst에 저장하고 그 값을 instMap에 저장
	            while((line = bufReader.readLine()) != null){
	                Instruction inst = new Instruction(line);
	                instMap.put(inst.instname, inst);
	            }            
	            bufReader.close();
	        }catch(IOException e){
	            System.out.println(e);
	        }
	}
}

/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {

	String instname;	// 명령어 이름
	int format;		// 명령어 포맷
	int opcode;		// 명령어 Opcode
	int opnum;		// 피연산자의 수
	
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
		
		String[] strArray = line.split("\t");
		instname = strArray[0];
		format = Integer.parseInt(strArray[1]);
		opcode = Integer.parseInt(strArray[2], 16);
		opnum = Integer.parseInt(strArray[3]);
		
	}
}
