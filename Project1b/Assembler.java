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
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */

public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
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
	 * ������� ���� ��ƾ
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
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		
		// ���� ��ü ����
        Path path = Paths.get(inputFile);
        Charset cs = StandardCharsets.UTF_8;
        
        // ���� ��ü ������ lineList�� �߰�
        try{
        	lineList.addAll(Files.readAllLines(path,cs));
        }catch(IOException e){
            e.printStackTrace();
        }
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		
		// ���Ǻ��� ���̺��� �����ϱ����� ī��Ʈ ����
		int sectcount = 0;
		
		// ���̺� ����
		LiteralTable literalTable = new LiteralTable();
		SymbolTable symbolTable = new SymbolTable();
		TokenTable tokenTable = new TokenTable(literalTable, instTable);
		
		// ������ ���̺��� ����Ʈ�� ����
		literaltabList.add(literalTable);
		symtabList.add(symbolTable);
		TokenList.add(tokenTable);
		
		TokenList.get(sectcount++).symTab = symbolTable;
		
		// lineList�� ����Ǿ� �ִ� ������ ���κ��� ����
		for(String readLine : lineList){
			
			// �ּ����� ��� ���������ʰ� �Ѿ
			if(readLine.startsWith(".")) 
				continue;
			
			// ������ operator�ڸ��� CSECT�� ���
			// ���ο� ���̺��� �����ϰ� ����Ʈ�� �߰�
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
		
		// ��ūȭ �۾�
		// ��ū ���̺� ����
		for(TokenTable tokenT : TokenList) {
			
			// �ּҰ��� �����ϱ����� ����
			int location = 0;
			
			// ��ū �ϳ��� ���� (���پ� ����)
			for(Token t : tokenT.tokenList) {
				
				// �ּҰ� ����
				t.location = location;
				
				// label�� �ִ� ��� SymbolTable�� �߰�
				if( !t.label.isEmpty() ) {
					tokenT.symTab.putSymbol(t.label, location);
				}
				
				// operand�� =�� �����ϸ� LiteralTable�� �߰�
				if( t.operand.length != 0 && t.operand[0].startsWith("=")) {
					tokenT.literalTab.putLiteral(t.operand[0], 0);
				}
				
				// operator �ڸ��� ��ɾ �� ��� �� ��ɾ��� ���˺��� �ּ������� ����
				if(instTable.instMap.containsKey(t.operator.replace("+", ""))) {
					location += instTable.instMap.get(t.operator.replace("+", "")).format;
					
					if(t.operator.startsWith("+")) {
						location++;
					}
				}
				
				// operator �ڸ��� ��ɾ� ���� �ܾ���� �� ����� �ּ������� ���
				switch(t.operator) {
				case "RESW":
					location += Integer.parseInt(t.operand[0]) * 3;		// operand�� �� �� x 3
					break;
					
				case "RESB":
					location += Integer.parseInt(t.operand[0]);			// operand�� �� ��
					break;
					
				case "BYTE":
					// �������� ���
					if(t.operand[0].startsWith("X")) {
						location += ( t.operand[0].length() - 3 ) / 2;	// operand�� ���� ���� X�� '�� ������ 3�� ���� ���	
					}
					// ĳ�������� ���
					else if(t.operand[0].startsWith("C")) {
						location += ( t.operand[0].length() - 3 );		// operand�� ���� ���� C�� '�� ������ 3�� ���� ���	
					}
					break;
					
				case "WORD":
					location += 3;
					break;
					
				// LTORG�� END�� �°�� ��� �Ǿ��� Literal���ڸ� �����ؾ� ��
				// LiteralTab�� �����ϴ� ���� ����ŭ �ݺ� �Ͽ� �ּ� ������ ���
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
					// operand�� *�� �°��� �Ѿ
					if(!t.operand[0].equals("*")) {
						String[] strArray = t.operand[0].split("-");
						tokenT.symTab.modifySymbol(t.label, tokenT.symTab.search(strArray[0]) - tokenT.symTab.search(strArray[1]));
					}
					break;
				} // switch�� ����
			} // ��ū�� ���� for�� ����
		} // ���̺� ���� for�� ����
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		
		try{
            //���� ��ü ����
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// ����� symbolTable ����ŭ �ݺ�
            	for(int i=0; i<symtabList.size(); i++) {
            		
            		// Table�� ����� Symbol ����ŭ �ݺ�
            		for(int j=0; j<symtabList.get(i).symbolList.size(); j++) {
            			
            			// ���Ŀ� �°� ���Ͽ� ���
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
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		
		try{
            //���� ��ü ����
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// ����� literalTable ����ŭ �ݺ�
            	for(int i=0; i<literaltabList.size(); i++) {
            		
            		// Table�ȿ� ����� Literal ����ŭ �ݺ�
            		for(int j=0; j<literaltabList.get(i).literalList.size(); j++) {
            			
            			// ���Ŀ� �°� ���Ͽ� ���
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
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		
		// ù��° �������� �Ǵ��ϴ� ����
		boolean isFirstSec = true;
		
		// TokenTable �ϳ��� ����
		for(TokenTable tokenT : TokenList) {
			
			// �� ���ǿ��� �����ؾ� �ϴ� ���� ����� ������ ����Ʈ
			// = EXTREF���� ����Ǿ� �ִ� ��� ����
			ArrayList<String> refList = new ArrayList<>();
			
			// M���ڵ带 �ۼ��ϱ� ���� ���� �����ص� ����Ʈ
			ArrayList<String> mOperList = new ArrayList<>();	// M���ڵ忡 �߰��ؾ��ϴ� ������ operator ����Ʈ
			ArrayList<String> mNameList = new ArrayList<>();	// M���ڵ忡 �߰��ؾ��ϴ� ���� ����Ʈ
			ArrayList<Integer> mLocList = new ArrayList<>();	// M���ڵ忡 �߰��ؾ��ϴ� ������ �ּҰ� ����Ʈ
			
			// �� ������ ���̸� ������ ����
			// T���ڵ��� �ڵ� ���� ����� ���� ���
			int codeLen = 0;
			
			// Table�� ����� Token �ϳ��� ���� �Ͽ� codeList �ۼ�
			for(Token t : tokenT.tokenList) {
				
				// �����ڿ� ���� Ű���� ���� ����
				switch(t.operator) {
				case "CSECT":
					putNextLine();		// ���� ����
				case "START":
					// codeList�� ���� �κ� �ۼ�
					codeList.add("H");
					codeList.add(String.format("%-6s", t.label));
					codeList.add(String.format("%06X", t.location));
					codeList.add("LENGTHTMP");					// �ڵ��� �� ���� ��ġ�� �ӽ� ���� ����
					putNextLine();
					break;
					
					// codeList�� EXTDEF�κ� �ۼ�
				case "EXTDEF":
					codeList.add("D");
					for(String defName : t.operand) {
						codeList.add(String.format("%-6s", defName));
						codeList.add(String.format("%06X", tokenT.symTab.locationList.get(tokenT.symTab.symbolList.indexOf(defName))));
					}
					putNextLine();
					break;
					
					// codeList�� EXTREF�κ� �ۼ� �� refList�ۼ�
				case "EXTREF":
					codeList.add("R");
					for(String refName : t.operand) {
						codeList.add(String.format("%-6s", refName));
						refList.add(refName);		// ���Ǻ� REF����Ʈ�� ����
					}
					putNextLine();
					break;
					
				default:
					
					// ��ɾ �� ���
					if(instTable.instMap.containsKey(t.operator.replace("+", ""))) {
						
						// T���ڵ��� ���̸� ��� �Ͽ� ������ �ϰų� �����ʰ� ����
						if(t.operator.startsWith("+") && codeLen+4 > 29) {
							codeLen = putNextLine(codeLen);
						} // 4������ ���
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 3 && codeLen+3 > 0X1D) {
							codeLen = putNextLine(codeLen);
						} // 3������ ���
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 2 && codeLen+2 > 0X1D) {
							codeLen = putNextLine(codeLen);
						} // 2������ ���
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 1 && codeLen+1 > 0X1D){
							codeLen = putNextLine(codeLen);
						} // 1������ ���
						
						// ���ο� �ٿ� T���ڵ带 �ۼ��ϴ� ���
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");		// �ڵ��� �����ڸ��� �ӽð� ����
						}
						
						// T���ڵ� �� �κ� �ۼ�
						if(t.operator.startsWith("+")) {									// 4����
							t.byteSize = 4;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 4;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 3) {	// 3����
							t.byteSize = 3;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 3;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 2) {	// 2����
							t.byteSize = 2;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 2;
						}
						else if(instTable.instMap.get(t.operator.replace("+", "")).format == 1) {	// 1����
							t.byteSize = 1;
							tokenT.makeObjectCode(tokenT.tokenList.indexOf(t));
							codeList.add(t.objectCode);
							codeLen += 1;
						}
					}
					
					// operator�� BYTE�� �� ���
					else if(t.operator.equals("BYTE")) {
						
						// T���ڵ��� ���̸� ��� �Ͽ� ������ �ϰų� �����ʰ� ����
						if(codeLen+1 > 29) {
							codeLen = putNextLine(codeLen);
						}
						
						// ���ο� �ٿ� T���ڵ带 �ۼ��ϴ� ���
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");			// �ڵ��� �����ڸ��� �ӽð� ����
						}
						
						// T���ڵ� �� �κ� �ۼ�
						codeList.add(t.operand[0].replace("X", "").replace("'", ""));
						t.byteSize = 1;
						codeLen += 1;
					}
					
					// operator�� WORD�� �� ���
					else if(t.operator.equals("WORD")) {
						
						// T���ڵ��� ���̸� ��� �Ͽ� ������ �ϰų� �����ʰ� ����
						if(codeLen+3 > 29) {
							codeLen = putNextLine(codeLen);
						}
						
						// ���ο� �ٿ� T���ڵ带 �ۼ��ϴ� ���
						if(codeLen == 0) {
							codeList.add("T");
							codeList.add(String.format("%06X", t.location));
							codeList.add("CL");			// �ڵ��� �����ڸ��� �ӽð� ����
						}
						
						// T���ڵ� �� �κ� �ۼ�
						codeList.add(String.format("%06X", 0X000000));
						t.byteSize = 3;
						codeLen += 3;
					}
					
					// operator�� LTORG�� �� ��� �����Ͽ� T���ڵ� �ۼ�
					else if(t.operator.equals("LTORG")) {
						codeLen = putNextLine(codeLen);
						codeList.add("T");
						codeList.add(String.format("%06X", t.location));
						codeList.add("CL");			// �ڵ��� �����ڸ��� �ӽð� ����
						
						// LiteralTable�� ����Ǿ��ִ� ���� T���ڵ忡 �ۼ�
						for(int i = 0; i < tokenT.literalTab.literalList.size(); i++) {
							
							// ĳ�������� ���
							if(tokenT.literalTab.literalList.get(i).startsWith("=C")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=C", "").replace("'", "");
								t.byteSize = 0;
								for(int j = 0; j < str.length(); j++) {
									codeList.add(String.format("%02X", (int)str.charAt(j)));
									t.byteSize++;
									codeLen++;
								}
							}
							
							// �������� ���
							else if(tokenT.literalTab.literalList.get(i).startsWith("=X")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=X", "").replace("'", "");
								codeList.add(String.format("%02X", Integer.parseInt(str)));
								t.byteSize = 1;
								codeLen++;
							}
						}
					}
					
					// operator�� END�� �� ��� �������� �ʰ� T���ڵ� �ۼ�
					else if(t.operator.equals("END")) {
						
						// LiteralTable�� ����Ǿ��ִ� ���� T���ڵ忡 �ۼ�
						for(int i = 0; i < tokenT.literalTab.literalList.size(); i++) {
							
							// ĳ�������� ���
							if(tokenT.literalTab.literalList.get(i).startsWith("=C")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=C", "").replace("'", "");
								t.byteSize = 0;
								for(int j = 0; j < str.length(); j++) {
									codeList.add(String.format("%02X", (int)str.charAt(j)));
									t.byteSize++;
									codeLen++;
								}
							}
							
							// �������� ���
							else if(tokenT.literalTab.literalList.get(i).startsWith("=X")) {
								String str = tokenT.literalTab.literalList.get(i).replace("=X", "").replace("'", "");
								codeList.add(String.format("%02X", Integer.parseInt(str)));
								t.byteSize = 1;
								codeLen++;
							}
						}
					}
				}
				
				// ��ū ���� ���� �����鼭, M���ڵ带 �ۼ��ϱ� ���� ���� �����ص� ����Ʈ �ۼ�
				if( !t.operator.equals("LTORG") && !t.operator.equals("RSUB") && !t.operator.equals("EXTREF") && !t.operator.equals("CSECT")) {
					
					// operand�� ���� String�� "-"�� �����Ͽ� ���ο� ���ڿ� �迭�� ����
					String[] operArr = t.operand[0].split("-");
					
					// ���еǾ� ������ ���ڿ� �迭 ����ŭ �ݺ�
					for(int i = 0; i < operArr.length; i++) {
						
						// refList�� ���� ����ŭ �ݺ��Ͽ� ������ ���ڿ��� ��
						for(int j = 0; j < refList.size(); j++) {	
							
							// ���� ���ڰ� �ִٸ�
							if(operArr[i].equals(refList.get(j))) {
								// �տ� �ִ� ���� �Ǵ� �ϳ��� ������ ���
								if(i==0) {
									mOperList.add(t.operator);
									mNameList.add("+" + operArr[i]);
									mLocList.add(t.location);
								}
								
								// "-" ������ ���� ���ڶ��
								else {
									mOperList.add(t.operator);
									mNameList.add("-" + operArr[i]);
									mLocList.add(t.location);
								}
							}
						}
					}
				} // M���ڵ� ����Ʈ �ۼ� �Ϸ�
			} // Table�� ����� Token �ϳ��� ���� �Ͽ� codeList �ۼ� �ϴ� for�� ����
			
			// T���ڵ� �ۼ��� �� ���� �� ä���� ���ϰ� ������찡 ����� ����
			if(codeLen != 0)
				codeLen = putNextLine(codeLen);
			
			// ����� List�̿��Ͽ� M���ڵ� ���پ� �ۼ�
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
			
			// ������ E�κ� �ۼ�
			codeList.add("E");
			
			// ù��° �����̶�� ���α׷��� �����ּ� �ۼ�
			if(isFirstSec) {
				codeList.add(codeList.get(2));
				isFirstSec = false;
			}
			
			// ���Ǻ� �ۼ� ����ÿ� ���Ǻ� ���� �ӽð�"LENGTHTMP"�� ���� ������ �ۼ�
			int lastLoc = tokenT.tokenList.get(tokenT.tokenList.size()-1).location;
			int lastByt = tokenT.tokenList.get(tokenT.tokenList.size()-1).byteSize;
			codeList.set(codeList.indexOf("LENGTHTMP"), String.format("%06X", lastLoc + lastByt));
			
			putNextLine();
		} // TokenTable �ϳ��� �����ϴ� for�� ����
	}
	
	/**
	 * ������ �����ϰ� �Ķ���� ���� �ִٸ� T���ڵ��� �ڵ���� ������ ����Ǿ��ִ� 
	 * �ӽð�"CL"�� codeLen������ �ٲٰ� codeLen�� �ʱ�ȭ �ϱ����� 0����
	 * @param fileName : ����Ǵ� ���� �̸�
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
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		try{
            //���� ��ü ����
            File file = new File(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            
            if(file.isFile() && file.canWrite()){
            	
            	// ���� �ۼ�
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