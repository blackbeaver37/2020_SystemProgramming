import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		
		assembler.pass1();

		assembler.printSymbolTable("symtab_20160274.txt");
		assembler.printLiteralTable("literaltab_20160274.txt");
		
		assembler.pass2();
		
		assembler.printObjectCode("output_20160274.txt");
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		
		// 파일 객체 생성
        Path path = Paths.get(inputFile);
        Charset cs = StandardCharsets.UTF_8;
        
        // 파일 전체 내용을 lineList에 추가
        try{
        	lineList.addAll(Files.readAllLines(path,cs));
        }catch(IOException e){
            e.printStackTrace();
        }
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		
		// 섹션별로 테이블을 저장하기위한 카운트 변수
		int sectcount = 0;
		
		// 테이블 생성
		LiteralTable literalTable = new LiteralTable();
		SymbolTable symbolTable = new SymbolTable();
		TokenTable tokenTable = new TokenTable(literalTable, instTable);
		
		// 생성된 테이블을 리스트에 저장
		literaltabList.add(literalTable);
		symtabList.add(symbolTable);
		TokenList.add(tokenTable);
		
		TokenList.get(sectcount++).symTab = symbolTable;
		
		// lineList에 저장되어 있는 내용을 라인별로 접근
		for(String readLine : lineList){
			
			// 주석문인 경우 저장하지않고 넘어감
			if(readLine.startsWith(".")) 
				continue;
			
			// 라인의 operator자리에 CSECT인 경우
			// 새로운 테이블을 생성하고 리스트에 추가
			if(readLine.split("\t")[1].equals("CSECT")) {
				literalTable = new LiteralTable();
				symbolTable = new SymbolTable();
				tokenTable = new TokenTable(literalTable, instTable);
				
				literaltabList.add(literalTable);
				symtabList.add(symbolTable);
				TokenList.add(tokenTable);
				
				TokenList.get(sectcount++).symTab = symbolTable;
			}
            tokenTable.putToken(readLine);
        }
		
		// 토큰화 작업
		// 토큰 테이블별 접급
		for(TokenTable tokenT : TokenList) {
			
			// 주소값을 저장하기위한 변수
			int location = 0;
			
			// 토큰 하나씩 접근 (한줄씩 접근)
			for(Token t : tokenT.tokenList) {
				
				// 주소값 저장
				t.location = location;
				
				// label이 있는 경우 SymbolTable에 추가
				if( !t.label.isEmpty() ) {
					tokenT.symTab.putSymbol(t.label, location);
				}
				
				// operand가 =로 시작하면 LiteralTable에 추가
				if( t.operand.length != 0 && t.operand[0].startsWith("=")) {
					tokenT.literalTab.putLiteral(t.operand[0], 0);
				}
				
				// operator 자리에 명령어가 온 경우 그 명령어의 포맷별로 주소증가값 증가
				if(instTable.instMap.containsKey(t.operator.replace("+", ""))) {
					location += instTable.instMap.get(t.operator.replace("+", "")).format;
					
					if(t.operator.startsWith("+")) {
						location++;
					}
				}
				
				// operator 자리에 명령어 외의 단어들이 온 경우의 주소증가값 계산
				switch(t.operator) {
				case "RESW":
					location += Integer.parseInt(t.operand[0]) * 3;		// operand에 온 수 x 3
					break;
					
				case "RESB":
					location += Integer.parseInt(t.operand[0]);			// operand에 온 수
					break;
					
				case "BYTE":
					// 정수형인 경우
					if(t.operand[0].startsWith("X")) {
						location += ( t.operand[0].length() - 3 ) / 2;	// operand의 길이 에서 X와 '의 개수인 3을 빼고 계산	
					}
					// 캐릭터형인 경우
					else if(t.operand[0].startsWith("C")) {
						location += ( t.operand[0].length() - 3 );		// operand의 길이 에서 C와 '의 개수인 3을 빼고 계산	
					}
					break;
					
				case "WORD":
					location += 3;
					break;
					
				// LTORG와 END가 온경우 사용 되었던 Literal문자를 선언해야 함
				// LiteralTab에 존재하는 문자 수만큼 반복 하여 주소 증가값 계산
				case "LTORG":
				case "END":
					for(int i = 0; i < tokenT.literalTab.locationList.size(); i++) {
						tokenT.literalTab.modifyLiteral(tokenT.literalTab.literalList.get(i), location);
						
						if(tokenT.literalTab.literalList.get(i).startsWith("=X")) {
							location += ( tokenT.literalTab.literalList.get(i).length() - 4 ) / 2;
						}
						else if(tokenT.literalTab.literalList.get(i).startsWith("=C")) {
							location += ( tokenT.literalTab.literalList.get(i).length() - 4 );
						}
					}
					
					break;
					
				case "EQU":
					// operand에 *이 온경우는 넘어감
					if(!t.operand[0].equals("*")) {
						String[] strArray = t.operand[0].split("-");
						tokenT.symTab.modifySymbol(t.label, tokenT.symTab.search(strArray[0]) - tokenT.symTab.search(strArray[1]));
					}
					break;
				} // switch문 종료
			} // 토큰별 접근 for문 종료
		} // 테이블별 접근 for문 종료
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		
		try{
            //파일 객체 생성
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// 저장된 symbolTable 수만큼 반복
            	for(int i=0; i<symtabList.size(); i++) {
            		
            		// Table별 저장된 Symbol 수만큼 반복
            		for(int j=0; j<symtabList.get(i).symbolList.size(); j++) {
            			
            			// 형식에 맞게 파일에 출력
            			bufferedWriter.write(String.format("%-6s\t%04X\n",
            								symtabList.get(i).symbolList.get(j),
            								symtabList.get(i).locationList.get(j)));		
            		}
            		bufferedWriter.newLine();
            	}
                bufferedWriter.close();
            }
        }catch (IOException e) {
            System.out.println(e);
        }
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		
		try{
            //파일 객체 생성
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// 저장된 literalTable 수만큼 반복
            	for(int i=0; i<literaltabList.size(); i++) {
            		
            		// Table안에 저장된 Literal 수만큼 반복
            		for(int j=0; j<literaltabList.get(i).literalList.size(); j++) {
            			
            			// 형식에 맞게 파일에 출력
            			bufferedWriter.write(String.format("%-6s\t%04X\n",
            								literaltabList.get(i).literalList.get(j).replace("=C", "").replace("=X", "").replace("'", ""),
            								literaltabList.get(i).locationList.get(j)));		
            			
            		}
            	}
                bufferedWriter.close();
            }
        }catch (IOException e) {
            System.out.println(e);
        }
	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		
		// 첫번째 섹션인지 판단하는 변수
		boolean isFirstSec = true;
		
		// TokenTable 하나씩 접근
		for(TokenTable tokenT : TokenList) {
			
			// 각 섹션에서 참조해야 하는 변수 목록을 저장할 리스트
			// = EXTREF에서 선언되어 있는 목록 저장
			ArrayList<String> refList = new ArrayList<>();
			
			// M레코드를 작성하기 위해 값을 저장해둘 리스트
			ArrayList<String> mOperList = new ArrayList<>();	// M레코드에 추가해야하는 라인의 operator 리스트
			ArrayList<String> mNameList = new ArrayList<>();	// M레코드에 추가해야하는 변수 리스트
			ArrayList<Integer> mLocList = new ArrayList<>();	// M레코드에 추가해야하는 라인의 주소값 리스트
			
			// 각 라인의 길이를 저장할 변수
			// T레코드의 코드 길이 계산을 위해 사용
			int codeLen = 0;
			
			// Table에 저장된 Token 하나씩 접근 하여 codeList 작성
			for(Token t : tokenT.tokenList) {
				
				// 연산자에 오는 키워드 별로 구분
				switch(t.operator) {
				case "CSECT":
					putNextLine();		// 섹션 구분
				case "START":
					// codeList에 시작 부분 작성
					codeList.add("H");
					codeList.add(String.format("%-6s", t.label));
					codeList.add(String.format("%06X", t.location));
					codeList.add("LENGTHTMP");					// 코드의 총 길이 위치에 임시 내용 저장
					putNextLine();
					break;
					
					// codeList에 EXTDEF부분 작성
				case "EXTDEF":
					codeList.add("D");
					for(String defName : t.operand) {
						codeList.add(String.format("%-6s", defName));
						codeList.add(String.format("%06X", tokenT.symTab.locationList.get(tokenT.symTab.symbolList.indexOf(defName))));
					}
					putNextLine();
					break;
					
					// codeList에 EXTREF부분 작성 및 refList작성
				case "EXTREF":
					codeList.add("R");
					for(String refName : t.operand) {
						codeList.add(String.format("%-6s", refName));
						refList.add(refName);		// 섹션별 REF리스트에 저장
					}
					putNextLine();
					break;
					
				default:
					
					// 명령어가 온 경우
					if(instTable.instMap.containsKey(t.operator.replace("+", ""))) {
						
						// T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
						if(t.operator.startsWith("+") && codeLen+4 > 29) {
							codeLen = putNextLine(codeLen);
						} // 4형식인 경우
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 3 && codeLen+3 > 0X1D) {
							codeLen = putNextLine(codeLen);
						} // 3형식인 경우
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 2 && codeLen+2 > 0X1D) {
							codeLen = putNextLine(codeLen);
						} // 2형식인 경우
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 1 && codeLen+1 > 0X1D){
							codeLen = putNextLine(codeLen);
						} // 1형식인 경우
						
						// 새로운 줄에 T레코드를 작성하는 경우
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");		// 코드의 길이자리에 임시값 저장
						}
						
						// T레코드 뒷 부분 작성
						if(t.operator.startsWith("+")) {									// 4형식
							t.byteSize = 4;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 4;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 3) {	// 3형식
							t.byteSize = 3;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 3;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 2) {	// 2형식
							t.byteSize = 2;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 2;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 1) {	// 1형식
							t.byteSize = 1;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 1;
						}
					}
					
					// operator에 BYTE가 온 경우
					else if(t.operator.equals("BYTE")) {
						
						// T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
						if(codeLen+1 > 29) {
							codeLen = putNextLine(codeLen);
						}
						
						// 새로운 줄에 T레코드를 작성하는 경우
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");			// 코드의 길이자리에 임시값 저장
						}
						
						// T레코드 뒷 부분 작성
						codeList.add(t.operand[0].replace("X", "").replace("'", ""));
						t.byteSize = 1;
						codeLen += 1;
					}
					
					// operator에 WORD가 온 경우
					else if(t.operator.equals("WORD")) {
						
						// T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
						if(codeLen+3 > 29) {
							codeLen = putNextLine(codeLen);
						}
						
						// 새로운 줄에 T레코드를 작성하는 경우
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");			// 코드의 길이자리에 임시값 저장
						}
						
						// T레코드 뒷 부분 작성
						codeList.add(String.format("%06X", 0X000000));
						t.byteSize = 3;
						codeLen += 3;
					}
					
					// operator에 LTORG가 온 경우 개행하여 T레코드 작성
					else if(t.operator.equals("LTORG")) {
						codeLen = putNextLine(codeLen);
						codeList.add("T");
						codeList.add(String.format("%06X", t.location));
						codeList.add("CL");			// 코드의 길이자리에 임시값 저장
						
						// LiteralTable에 저장되어있던 값들 T레코드에 작성
						for(int i = 0; i < tokenT.literalTab.literalList.size(); i++) {
							
							// 캐릭터형인 경우
							if(tokenT.literalTab.literalList.get(i).startsWith("=C")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=C", "").replace("'", "");
								t.byteSize = 0;
								for(int j = 0; j < str.length(); j++) {
									codeList.add(String.format("%02X", (int)str.charAt(j)));
									t.byteSize++;
									codeLen++;
								}
							}
							
							// 정수형인 경우
							else if(tokenT.literalTab.literalList.get(i).startsWith("=X")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=X", "").replace("'", "");
								codeList.add(String.format("%02X", Integer.parseInt(str)));
								t.byteSize = 1;
								codeLen++;
							}
						}
					}
					
					// operator에 END가 온 경우 개행하지 않고 T레코드 작성
					else if(t.operator.equals("END")) {
						
						// LiteralTable에 저장되어있던 값들 T레코드에 작성
						for(int i = 0; i < tokenT.literalTab.literalList.size(); i++) {
							
							// 캐릭터형인 경우
							if(tokenT.literalTab.literalList.get(i).startsWith("=C")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=C", "").replace("'", "");
								t.byteSize = 0;
								for(int j = 0; j < str.length(); j++) {
									codeList.add(String.format("%02X", (int)str.charAt(j)));
									t.byteSize++;
									codeLen++;
								}
							}
							
							// 정수형인 경우
							else if(tokenT.literalTab.literalList.get(i).startsWith("=X")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=X", "").replace("'", "");
								codeList.add(String.format("%02X", Integer.parseInt(str)));
								t.byteSize = 1;
								codeLen++;
							}
						}
					}
				}
				
				// 토큰 한줄 마다 읽으면서, M레코드를 작성하기 위해 값을 저장해둘 리스트 작성
				if( !t.operator.equals("LTORG") && !t.operator.equals("RSUB") && !t.operator.equals("EXTREF") && !t.operator.equals("CSECT")) {
					
					// operand에 오는 String을 "-"로 구분하여 새로운 문자열 배열에 저장
					String[] operArr = t.operand[0].split("-");
					
					// 구분되어 생성된 문자열 배열 수만큼 반복
					for(int i = 0; i < operArr.length; i++) {
						
						// refList에 내용 수만큼 반복하여 생성된 문자열과 비교
						for(int j = 0; j < refList.size(); j++) {	
							
							// 같은 문자가 있다면
							if(operArr[i].equals(refList.get(j))) {
								// 앞에 있는 문자 또는 하나의 문자인 경우
								if(i==0) {
									mOperList.add(t.operator);
									mNameList.add("+" + operArr[i]);
									mLocList.add(t.location);
								}
								
								// "-" 다음에 오는 문자라면
								else {
									mOperList.add(t.operator);
									mNameList.add("-" + operArr[i]);
									mLocList.add(t.location);
								}
							}
						}
					}
				} // M레코드 리스트 작성 완료
			} // Table에 저장된 Token 하나씩 접근 하여 codeList 작성 하는 for문 종료
			
			// T레코드 작성이 한 줄을 다 채우지 못하고 끝난경우가 생기면 개행
			if(codeLen != 0)
				codeLen = putNextLine(codeLen);
			
			// 저장된 List이용하여 M레코드 한줄씩 작성
			for(int i = 0; i < mNameList.size(); i++) {
				codeList.add("M");
				if(mOperList.get(i).equals("WORD")) {
					codeList.add(String.format("%06X", mLocList.get(i)));
					codeList.add("06");
				}
				else {
				codeList.add(String.format("%06X", mLocList.get(i) + 1));
				codeList.add("05");
				}
				codeList.add(String.format("%-7s", mNameList.get(i)));
				putNextLine();
			}
			
			// 마지막 E부분 작성
			codeList.add("E");
			
			// 첫번째 섹션이라면 프로그램의 시작주소 작성
			if(isFirstSec) {
				codeList.add(codeList.get(2));
				isFirstSec = false;
			}
			
			// 섹션별 작성 종료시에 섹션별 길이 임시값"LENGTHTMP"을 실제 값으로 작성
			int lastLoc = tokenT.tokenList.get(tokenT.tokenList.size()-1).location;
			int lastByt = tokenT.tokenList.get(tokenT.tokenList.size()-1).byteSize;
			codeList.set(codeList.indexOf("LENGTHTMP"), String.format("%06X", lastLoc + lastByt));
			
			putNextLine();
		} // TokenTable 하나씩 접근하는 for문 종료
	}
	
	/**
	 * 개행을 진행하고 파라미터 값이 있다면 T레코드의 코드길이 값으로 저장되어있는 
	 * 임시값"CL"을 codeLen값으로 바꾸고 codeLen을 초기화 하기위한 0리턴
	 * @param fileName : 저장되는 파일 이름
	 * @return 0 
	 */
	private void putNextLine() {
		codeList.add("\n");
	}
	private int putNextLine(int length) {
		codeList.add("\n");
		codeList.set(codeList.indexOf("CL"), String.format("%02X", length));
		
		return 0;
	}
	
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try{
            //파일 객체 생성
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// 파일 작성
            	for(int i=0; i<codeList.size(); i++) {
            		bufferedWriter.write(codeList.get(i));
            	}
                bufferedWriter.close();
            }
        }catch (IOException e) {
            System.out.println(e);
        }
	}
}
