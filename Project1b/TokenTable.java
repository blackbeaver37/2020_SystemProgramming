import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {

		this.symTab = symTab;
		this.instTab = instTab;
		tokenList = new ArrayList<>();
		
	}

	/**
	 * �ʱ�ȭ�ϸ鼭 literalTable�� instTable�� ��ũ��Ų��.
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(LiteralTable literalTab, InstTable instTab) {

		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<>();
		
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		
		// �ε��� ������ ��ū �ϳ��� ����
		Token t = getToken(index);
		
		// n, i ��Ʈ ���
		if(t.operand[0].startsWith("@")) {		// Indirect Addressing 
			t.setFlag(nFlag, 1);
			t.setFlag(iFlag, 0);
		}
		else if(t.operand[0].startsWith("#")) {	// Immediate Addressing
			t.setFlag(nFlag, 0);
			t.setFlag(iFlag, 1);
		}
		else if(t.byteSize == 2 ) {			// 2����
			t.setFlag(nFlag, 0);
			t.setFlag(iFlag, 0);
		}
		else {
			t.setFlag(nFlag, 1);
			t.setFlag(iFlag, 1);
		}
		
		// x ��Ʈ ���
		if(t.operand.length > 1 && t.operand[1].equals("X")) {	// �ǿ����ڿ� X�� �� ���
			t.setFlag(xFlag, 1);
		}
		else {
			t.setFlag(xFlag, 0);
		}
		
		// b ��Ʈ ���
		t.setFlag(bFlag, 0);
		
		// p ��Ʈ ���
		if(t.byteSize == 4) {				// 4����
			t.setFlag(pFlag, 0);
		} // 4������ ���
		else if(t.byteSize == 2 ) {			// 2����
			t.setFlag(pFlag, 0);
		} // 2������ ���
		else if(t.operand.length != 0 && t.operand[0].startsWith("#")) {	// Direct Addressing
			t.setFlag(pFlag, 0);
		}
		else if(t.operator.equals("RSUB")) {	// RSUB ������ ���
			t.setFlag(pFlag, 0);
		}
		else {
			t.setFlag(pFlag, 1);
		}
		
		// e ��Ʈ ���
		if(t.byteSize == 4) {				// 4����
			t.setFlag(eFlag, 1);
		}
		else {
			t.setFlag(eFlag, 0);
		}
		
		// objectCode ���
		// 2������ ���
		if(t.byteSize == 2) {
			int code = instTab.instMap.get(t.operator).opcode << 8;
			switch(t.operand[0]) {		// �ǿ������� �������� ���� ����
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
			if(t.operand.length == 2) {	// �ǿ����ڰ� 2���� ���
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
			
			// ������ objectCode�� 16������ ����
			t.objectCode = String.format("%04X", code);
		}
		
		// 3������ ���
		else if(t.byteSize == 3) {
			
			// �ش� ��ɾ��� opcode���� �ӽ� ������ ����
			int code = instTab.instMap.get(t.operator).opcode << 16;
			
			// nixbpe ���� �߰�
			code += t.nixbpe << 12;
			
			// RSUB ��ɾ��� ���
			if(t.operator.equals("RSUB")) {
				code += 0;
			}
			
			// PC relative�� ���
			else if(t.getFlag(pFlag) == pFlag) {
				int ta = 0;
				// PC = ���� ��ū�� �ּҰ�
				int pc = getToken(index+1).location;
				
				// LiteralTable�� SymbolTable���� ���� Ž���Ͽ� TA�� �ּҰ� ����
				if(literalTab.literalList.indexOf(t.operand[0]) != -1) {
					ta = literalTab.locationList.get(literalTab.literalList.indexOf(t.operand[0]));
				}
				else if(symTab.symbolList.indexOf(t.operand[0].replace("@", "")) != -1) {
					ta = symTab.locationList.get(symTab.symbolList.indexOf(t.operand[0].replace("@", "")));
				}
				
				// TA - PC ���� ���� 12�� ��Ʈ�� �߰�
				code += (ta - pc) & 0XFFF;
			}
			
			// Direct Addressing�� ���
			else if(t.getFlag(bFlag | pFlag) == 0) {
				code += Integer.parseInt(t.operand[0].replace("#", ""));
			}
			
			// ������ objectCode�� 16������ ����
			t.objectCode = String.format("%06X", code);
		}
		
		// 4������ ���
		else if(t.byteSize == 4) {
			int code = instTab.instMap.get(t.operator.replace("+", "")).opcode << 24;
			code += t.nixbpe << 20;
			
			// ������ objectCode�� 16������ ����
			t.objectCode = String.format("%08X", code);
		}
	}
	
	/** 
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		label = "";
		operator = "";
		operand = new String[0];
		comment = "";
		objectCode = "";
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
