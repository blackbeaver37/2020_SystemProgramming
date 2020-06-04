package SP20_simulator;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
    // SW Register Flag 값
    public static final int COMP_NOT=1;
    public static final int COMP_EQUAL=2;
    public static final int COMP_LESS=3;
    public static final int COMP_GREAT=4;

    ResourceManager rMgr;

    public InstLuncher(ResourceManager rMgr) {
        this.rMgr = rMgr;
    }

    // instruction 별로 동작을 수행하는 메소드를 정의
    public void stl(int ta){        // L의 값을 메모리에 저장
        String lData = String.format("%06X", rMgr.getRegister(ResourceManager.REG_L));
        rMgr.setMemory(ta, lData, 3);
    }
    public void jsub(int pc){       // L에 현재 PC를 저장
        rMgr.setRegister(ResourceManager.REG_L, pc);
    }
    public void clear(int ta){       //  Target Register의 값을 0으로 초기화
        int register = ta >> 4;
        rMgr.setRegister(register, 0);
    }
    public void ldt(int ta){            // 메모리의 값을 T로 저장
        int data = Integer.parseInt(rMgr.getMemory(ta, 3), 16);
        rMgr.setRegister(ResourceManager.REG_T, data);
    }
    public void td(int ta){             // Device Test (정상 작동시 SW에 NOT Flag 저장)
        String devName = rMgr.getMemory(ta, 1);
        rMgr.testDevice(devName);
    }
    public boolean jeq (){              // SW의 값이 EQUAL Flag일 때 동작
        if(rMgr.getRegister(ResourceManager.REG_SW) == COMP_EQUAL)
            return true;
        else
            return false;
    }
    public void rd(int ta){              // Device의 값을 A로 저장
        String buffer;
        String devName = rMgr.getMemory(ta, 1);
        buffer = rMgr.readDevice(devName, 1);
        rMgr.setRegister(ResourceManager.REG_A, buffer.charAt(0));
    }
    public void compr(int ta){      // 두 Register의 비교 연산 후 SW Flag를 저장
        int reg1 = ta>>4;
        int reg2 = ta & 0X0F;
        if (rMgr.getRegister(reg1) == rMgr.getRegister(reg2))
            rMgr.setRegister(ResourceManager.REG_SW, COMP_EQUAL);
        else
            rMgr.setRegister(ResourceManager.REG_SW, COMP_NOT);
    }
    public void stch(int ta, int nixbpe){       // A에 있는 캐릭터 값을 메모리에 저장
        int addr = ta;
        if ((nixbpe & SicSimulator.xFlag) == SicSimulator.xFlag){
            addr += rMgr.getRegister(ResourceManager.REG_X);
        }
        String data = String.format("%02X", rMgr.getRegister(ResourceManager.REG_A));
        rMgr.setMemory(addr, data, 1);
    }
    public void tixr(int ta){           // X와 타겟 Register의 연산 비교후 X += 1 및 SW Flag 저장
        int reg = ta>>4;
        rMgr.setRegister(ResourceManager.REG_X, rMgr.getRegister(ResourceManager.REG_X) + 1);
        if (rMgr.getRegister(reg) == rMgr.getRegister(ResourceManager.REG_X))
            rMgr.setRegister(ResourceManager.REG_SW, COMP_EQUAL);
        else if (rMgr.getRegister(reg) > rMgr.getRegister(ResourceManager.REG_X))
            rMgr.setRegister(ResourceManager.REG_SW, COMP_LESS);
        else if (rMgr.getRegister(reg) < rMgr.getRegister(ResourceManager.REG_X))
            rMgr.setRegister(ResourceManager.REG_SW, COMP_GREAT);
    }
    public boolean jlt (){      // SW LESS Flag 일 때 작동
        if(rMgr.getRegister(ResourceManager.REG_SW) == COMP_LESS)
            return true;
        else
            return false;
    }
    public void stx(int ta){        // X의 값을 메모리에 저장
        String xData = String.format("%06X", rMgr.getRegister(ResourceManager.REG_X));
        rMgr.setMemory(ta, xData, 3);
    }
    public int rsub(){                  // L에 저장된 위치로 돌아감
        return rMgr.getRegister(ResourceManager.REG_L);
    }
    public void lda(int ta){        // 메모리에 있는 값을 A에 저장
        int data;
        if(ta == 3)
            data = ta;
        else
            data = Integer.parseInt(rMgr.getMemory(ta, 3), 16);
        rMgr.setRegister(ResourceManager.REG_A, data);
    }
    public void comp(int ta, int nixbpe){       // Target 값과 A의 값을 비교 후 SW Flag 저장
        int compVal;
        if ((nixbpe & SicSimulator.iFlag) == SicSimulator.iFlag)
            compVal = ta;
        else
            compVal = Integer.parseInt(rMgr.getMemory(ta, 1), 16);
        if (rMgr.getRegister(ResourceManager.REG_A) == compVal)
            rMgr.setRegister(ResourceManager.REG_SW, COMP_EQUAL);
        else
            rMgr.setRegister(ResourceManager.REG_SW, COMP_NOT);
    }
    public void ldch(int ta, int nixbpe){       // 메모리에 있는 캐릭터 값을 A에 저장
        int addr = ta;
        if ((nixbpe & SicSimulator.xFlag) == SicSimulator.xFlag){
            addr += rMgr.getRegister(ResourceManager.REG_X);
        }
        int data = Integer.parseInt(rMgr.getMemory(addr, 1), 16);
        rMgr.setRegister(ResourceManager.REG_A, data);
    }
    public void wd(int ta){         // A의 값을 Device에 저장
        String buffer;
        buffer = String.format("%c", (char)rMgr.getRegister(ResourceManager.REG_A));
        String devName = rMgr.getMemory(ta, 1);
        rMgr.writeDevice(devName, buffer, 1);
    }
    public int j(int ta, int nixbpe){       // Target으로 이동
        int data;
        if ((nixbpe & SicSimulator.nFlag) == SicSimulator.nFlag)
            data = ta;
        else
            data = Integer.parseInt(rMgr.getMemory(ta, 3), 16);
        return data;
    }
    public void sta(int ta){                // A의 값을 메모리에 저장
        String aData = String.format("%06X", rMgr.getRegister(ResourceManager.REG_A));
        rMgr.setMemory(ta, aData, 3);
    }
}