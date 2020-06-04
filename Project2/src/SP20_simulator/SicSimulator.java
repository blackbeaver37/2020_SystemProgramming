package SP20_simulator;

import java.io.File;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
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
	int base;				// Base Relative는 미구현

	// 명령어 하나의 정보를 갖는 클래스
	public class objectCode {
		String code = "";			// 전체 코드
		String opName = "";		// 명령어 이름
		int opCode = 0;			// OpCode
		int nixbpe = 0;				// nixbpe
		int format = 0;				// 형식
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
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		rMgr.startAddrOfM = memoryLoc;
		objectCode oc = new objectCode();

		String opCodeTmp = rMgr.getMemory(memoryLoc, 1);
		oc.opCode = Integer.parseInt(opCodeTmp, 16);
		oc.opCode &= 0XFC;		// 1111 1100 OpCode 저장
		oc.opName = rMgr.instTable.instMap.get(oc.opCode).instname;	// 이름 저장
		oc.format = rMgr.instTable.instMap.get(oc.opCode).format;	// 형식 저장

		String nixbpeTmp = rMgr.getMemory(memoryLoc, 2);
		oc.nixbpe = Integer.parseInt(nixbpeTmp, 16);
		oc.nixbpe >>= 4;
		oc.nixbpe &= 0X03F;		// 0000 0011 1111	nixbpe 저장

		// Disp 저장
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

		// PC 값 저장
		pc = memoryLoc + oc.format;
		rMgr.setRegister(ResourceManager.REG_PC, pc);

		// Target Address 값 저장
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

		// 명령어에 따라 명령 수행
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
		// Log 추가
		addLog(oc);
		// PC = 0 프로그램이 끝났을때
		if(memoryLoc == 0){
			rMgr.pgData.status = ResourceManager.STATUS_END;
			rMgr.closeDevice();
		}
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		while(rMgr.pgData.status != ResourceManager.STATUS_END){
			oneStep();
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(objectCode oc) {
		rMgr.instruction = oc.code;
		if (oc.format == 4)
			rMgr.log = "+" + oc.opName;
		else
			rMgr.log = oc.opName;
	}	
}
