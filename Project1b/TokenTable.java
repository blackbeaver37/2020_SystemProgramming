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
	public TokenTable(SymbolTable symTab, InstTable instTab) {

		this.symTab = symTab;
		this.instTab = instTab;
		tokenList = new ArrayList<>();
		
	}

	/**
	 * 초기화하면서 literalTable과 instTable을 링크시킨다.
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(LiteralTable literalTab, InstTable instTab) {

		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<>();
		
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
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
		
		// 인덱스 값으로 토큰 하나씩 접근
		Token t = getToken(index);
		
		// n, i 비트 계산
		if(t.operand[0].startsWith("@")) {		// Indirect Addressing 
			t.setFlag(nFlag, 1);
			t.setFlag(iFlag, 0);
		}
		else if(t.operand[0].startsWith("#")) {	// Immediate Addressing
			t.setFlag(nFlag, 0);
			t.setFlag(iFlag, 1);
		}
		else if(t.byteSize == 2 ) {			// 2형직
			t.setFlag(nFlag, 0);
			t.setFlag(iFlag, 0);
		}
		else {
			t.setFlag(nFlag, 1);
			t.setFlag(iFlag, 1);
		}
		
		// x 비트 계산
		if(t.operand.length > 1 && t.operand[1].equals("X")) {	// 피연산자에 X가 온 경우
			t.setFlag(xFlag, 1);
		}
		else {
			t.setFlag(xFlag, 0);
		}
		
		// b 비트 계산
		t.setFlag(bFlag, 0);
		
		// p 비트 계산
		if(t.byteSize == 4) {				// 4형식
			t.setFlag(pFlag, 0);
		} // 4형식인 경우
		else if(t.byteSize == 2 ) {			// 2형식
			t.setFlag(pFlag, 0);
		} // 2형식인 경우
		else if(t.operand.length != 0 && t.operand[0].startsWith("#")) {	// Direct Addressing
			t.setFlag(pFlag, 0);
		}
		else if(t.operator.equals("RSUB")) {	// RSUB 연산인 경우
			t.setFlag(pFlag, 0);
		}
		else {
			t.setFlag(pFlag, 1);
		}
		
		// e 비트 계산
		if(t.byteSize == 4) {				// 4형식
			t.setFlag(eFlag, 1);
		}
		else {
			t.setFlag(eFlag, 0);
		}
		
		// objectCode 계산
		// 2형식인 경우
		if(t.byteSize == 2) {
			int code = instTab.instMap.get(t.operator).opcode << 8;
			switch(t.operand[0]) {		// 피연산자의 레지스터 종류 구분
			case "A":
				code += 0 << 4;
				break;
			case "X":
				code += 1 << 4;
				break;
			case "L":
				code += 2 << 4;
				break;
			case "B":
				code += 3 << 4;
				break;
			case "S":
				code += 4 << 4;
				break;
			case "T":
				code += 5 << 4;
				break;
			}
			if(t.operand.length == 2) {	// 피연산자가 2개인 경우
				switch(t.operand[1]) {
				case "A":
					code += 0;
					break;
				case "X":
					code += 1;
					break;
				case "L":
					code += 2;
					break;
				case "B":
					code += 3;
					break;
				case "S":
					code += 4;
					break;
				case "T":
					code += 5;
					break;
				}
			}
			
			// 생성된 objectCode를 16진수로 저장
			t.objectCode = String.format("%04X", code);
		}
		
		// 3형식인 경우
		else if(t.byteSize == 3) {
			
			// 해당 명령어의 opcode값을 임시 변수에 저장
			int code = instTab.instMap.get(t.operator).opcode << 16;
			
			// nixbpe 값을 추가
			code += t.nixbpe << 12;
			
			// RSUB 명령어인 경우
			if(t.operator.equals("RSUB")) {
				code += 0;
			}
			
			// PC relative인 경우
			else if(t.getFlag(pFlag) == pFlag) {
				int ta = 0;
				// PC = 다음 토큰의 주소값
				int pc = getToken(index+1).location;
				
				// LiteralTable과 SymbolTable에서 값을 탐색하여 TA에 주소값 저장
				if(literalTab.literalList.indexOf(t.operand[0]) != -1) {
					ta = literalTab.locationList.get(literalTab.literalList.indexOf(t.operand[0]));
				}
				else if(symTab.symbolList.indexOf(t.operand[0].replace("@", "")) != -1) {
					ta = symTab.locationList.get(symTab.symbolList.indexOf(t.operand[0].replace("@", "")));
				}
				
				// TA - PC 값의 하위 12개 비트값 추가
				code += (ta - pc) & 0XFFF;
			}
			
			// Direct Addressing인 경우
			else if(t.getFlag(bFlag | pFlag) == 0) {
				code += Integer.parseInt(t.operand[0].replace("#", ""));
			}
			
			// 생성된 objectCode를 16진수로 저장
			t.objectCode = String.format("%06X", code);
		}
		
		// 4형식인 경우
		else if(t.byteSize == 4) {
			int code = instTab.instMap.get(t.operator.replace("+", "")).opcode << 24;
			code += t.nixbpe << 20;
			
			// 생성된 objectCode를 16진수로 저장
			t.objectCode = String.format("%08X", code);
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
		//initialize 추가
		label = "";
		operator = "";
		operand = new String[0];
		comment = "";
		objectCode = "";
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		
		String[] strArray = line.split("\t");
		
		switch(strArray.length) {
		case 4:
			comment = strArray[3];
		case 3:
			operand = strArray[2].split(",");
		case 2:
			operator = strArray[1];
		case 1:
			label = strArray[0];
		default:
			break;
		}
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
		if(value == 1) {
			nixbpe |= flag;
		}
		else {
			nixbpe &= 127 - flag;
		}
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
