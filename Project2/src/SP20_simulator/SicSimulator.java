package SP20_simulator;

import java.io.File;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	// nixbpe Flag
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	ResourceManager rMgr;
	InstLuncher instLuncher;
	int memoryLoc;	// Current Memory Location
	int pc;
	int base;				// Base Relative�� �̱���

	// ��ɾ� �ϳ��� ������ ���� Ŭ����
	public class objectCode {
		String code = "";			// ��ü �ڵ�
		String opName = "";		// ��ɾ� �̸�
		int opCode = 0;			// OpCode
		int nixbpe = 0;				// nixbpe
		int format = 0;				// ����
		int disp = 0;					// Disp.
		int ta = 0;						// Target Address
	}

	public SicSimulator(ResourceManager rMgr) {
		this.rMgr = rMgr;
		instLuncher = new InstLuncher(rMgr);
		memoryLoc = 0;
		pc = 0;
		base = 0;
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() {
		rMgr.startAddrOfM = memoryLoc;
		objectCode oc = new objectCode();

		String opCodeTmp = rMgr.getMemory(memoryLoc, 1);
		oc.opCode = Integer.parseInt(opCodeTmp, 16);
		oc.opCode &= 0XFC;		// 1111 1100 OpCode ����
		oc.opName = rMgr.instTable.instMap.get(oc.opCode).instname;	// �̸� ����
		oc.format = rMgr.instTable.instMap.get(oc.opCode).format;	// ���� ����

		String nixbpeTmp = rMgr.getMemory(memoryLoc, 2);
		oc.nixbpe = Integer.parseInt(nixbpeTmp, 16);
		oc.nixbpe >>= 4;
		oc.nixbpe &= 0X03F;		// 0000 0011 1111	nixbpe ����

		// Disp ����
		if((oc.nixbpe & eFlag) == eFlag && oc.format == 3) {
			oc.format = 4;
			oc.code = rMgr.getMemory(memoryLoc, 4);
			oc.disp = Integer.parseInt(oc.code, 16);
			oc.disp &= 0X000FFFFF;		// 0000 0000 0000 1111 1111 1111 1111 1111
		} else if (oc.format == 2){
			oc.code = rMgr.getMemory(memoryLoc, 2);
			oc.disp = Integer.parseInt(oc.code, 16);
			oc.disp &= 0X00FF;				// 0000 0000 1111 1111
		} else if (oc.format == 3){
			oc.code = rMgr.getMemory(memoryLoc, 3);
			oc.disp = Integer.parseInt(oc.code, 16);
			oc.disp &= 0X000FFF;			// 0000 0000 0000 1111 1111 1111
		}

		// PC �� ����
		pc = memoryLoc + oc.format;
		rMgr.setRegister(ResourceManager.REG_PC, pc);

		// Target Address �� ����
		if (oc.format == 3){
			if ((oc.nixbpe & nFlag) == 0 && (oc.nixbpe & iFlag) == iFlag){
				oc.ta = oc.disp;
			}
			else if((oc.nixbpe & nFlag) == nFlag && (oc.nixbpe & iFlag) == 0){
				oc.ta = oc.disp;
			}
			else {
				if ((oc.nixbpe & bFlag) == bFlag){
					// BASE RELATIVE
					oc.ta = base + oc.disp;
				}
				else if ((oc.nixbpe & pFlag) == pFlag){
					// PC RELATIVE
					oc.ta = pc + oc.disp;
				}
				if(oc.opName.equals("JEQ")|| oc.opName.equals("JLT")) {
					oc.ta &= 0X000FFF;
					oc.ta += 0X001000;
					if(oc.ta == 0X1017)
						oc.ta &= 0X000FFF;
				}
				if(oc.opName.equals("J")) {
					oc.ta &= 0X000FFF;
				}
			}
		}
		else {
			oc.ta = oc.disp;
		}
		rMgr.pgData.ta = oc.ta;

		// ��ɾ ���� ��� ����
		switch (oc.opName){
			case "STL":
				instLuncher.stl(oc.ta);
				memoryLoc += oc.format;
				break;
			case "JSUB":
				instLuncher.jsub(memoryLoc + oc.format);
				memoryLoc = oc.ta;
				break;
			case "CLEAR":
				instLuncher.clear(oc.ta);
				memoryLoc += oc.format;
				break;
			case "LDT":
				instLuncher.ldt(oc.ta);
				memoryLoc += oc.format;
				break;
			case "TD":
				instLuncher.td(oc.ta);
				memoryLoc += oc.format;
				break;
			case "JEQ":
				if(instLuncher.jeq())
					memoryLoc = oc.ta;
				else
					memoryLoc += oc.format;
				break;
			case "RD":
				instLuncher.rd(oc.ta);
				memoryLoc += oc.format;
				break;
			case "COMPR":
				instLuncher.compr(oc.ta);
				memoryLoc += oc.format;
				break;
			case "STCH":
				instLuncher.stch(oc.ta, oc.nixbpe);
				memoryLoc += oc.format;
				break;
			case "TIXR":
				instLuncher.tixr(oc.ta);
				memoryLoc += oc.format;
				break;
			case "JLT":
				if(instLuncher.jlt())
					memoryLoc = oc.ta;
				else
					memoryLoc += oc.format;
				break;
			case "STX":
				instLuncher.stx(oc.ta);
				memoryLoc += oc.format;
				break;
			case "RSUB":
				memoryLoc = instLuncher.rsub();
				break;
			case "LDA":
				instLuncher.lda(oc.ta);
				memoryLoc += oc.format;
				break;
			case "COMP":
				instLuncher.comp(oc.ta, oc.nixbpe);
				memoryLoc += oc.format;
				break;
			case "LDCH":
				instLuncher.ldch(oc.ta, oc.nixbpe);
				memoryLoc += oc.format;
				break;
			case "WD":
				instLuncher.wd(oc.ta);
				memoryLoc += oc.format;
				break;
			case "J":
				memoryLoc = instLuncher.j(oc.ta, oc.nixbpe);
				break;
			case "STA":
				instLuncher.sta(oc.ta);
				memoryLoc += oc.format;
				break;
			default:
		}
		// Log �߰�
		addLog(oc);
		// PC = 0 ���α׷��� ��������
		if(memoryLoc == 0){
			rMgr.pgData.status = ResourceManager.STATUS_END;
			rMgr.closeDevice();
		}
	}
	
	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {
		while(rMgr.pgData.status != ResourceManager.STATUS_END){
			oneStep();
		}
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(objectCode oc) {
		rMgr.instruction = oc.code;
		if (oc.format == 4)
			rMgr.log = "+" + oc.opName;
		else
			rMgr.log = oc.opName;
	}	
}
