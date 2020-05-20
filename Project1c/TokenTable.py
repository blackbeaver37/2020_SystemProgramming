from InstTable import InstTable
from LiteralTable import LiteralTable
from SymbolTable import SymbolTable

class TokenTable:
    MAX_OPERAND = 3;

    # bit 조작의 가독성을 위한 선언
    nFlag = 32
    iFlag = 16
    xFlag = 8
    bFlag = 4
    pFlag = 2
    eFlag = 1

    # Token을 다룰 때 필요한 테이블들을 링크시킨다.
    symTab = SymbolTable
    literalTab = LiteralTable
    instTab = InstTable

    # 각 line을 의미별로 분할하고 분석하는 공간.
    tokenList = []

    def __init__(self, symTab, literalTab, instTab):
        self.symTab = symTab
        self.literalTab = literalTab
        self.instTab = instTab
        self.tokenList = []

    # 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
    # @param line : 분리되지 않은 일반 문자열
    def putToken(self, line):
        self.tokenList.append(Token(line))

    # tokenList에서 index에 해당하는 Token을 리턴한다.
    # @param index
    # @return : index번호에 해당하는 코드를 분석한 Token 클래스
    def getToken(self, index):
        return self.tokenList[index]

    # instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
    # @param index
    def makeObjectCode(self, index):
        # 인덱스 값으로 토큰 하나씩 접근
        t = self.getToken(index)

        # n, i 비트 계산
        if t.operator == "RSUB":
            t.setFlag(self.nFlag, 1)
            t.setFlag(self.iFlag, 1)
        elif t.operand[0][0] == "@":      # Indirect Addressing
            t.setFlag(self.nFlag, 1)
            t.setFlag(self.iFlag, 0)
        elif t.operand[0][0] == "#":     # Immediate Addressing
            t.setFlag(self.nFlag, 0)
            t.setFlag(self.iFlag, 1)
        elif t.byteSize == 2:           # 2형식
            t.setFlag(self.nFlag, 0)
            t.setFlag(self.iFlag, 0)
        else:
            t.setFlag(self.nFlag, 1)
            t.setFlag(self.iFlag, 1)

        # x 비트 계산
        if len(t.operand) > 1 and t.operand[1] == "X":
            t.setFlag(self.xFlag, 1)
        else:
            t.setFlag(self.xFlag, 0)

        # b 비트 계산
        t.setFlag(self.bFlag, 0)

        # p 비트 계산
        if t.byteSize == 4:             # 4형식
            t.setFlag(self.pFlag, 0)
        elif t.byteSize == 2:            # 2형식
            t.setFlag(self.pFlag, 0)
        elif t.operator == "RSUB":       # RSUB 연산
            t.setFlag(self.pFlag, 0)
        elif len(t.operand) != 0 and t.operand[0][0] == "#":
            t.setFlag(self.pFlag, 0)      # Immediate Addressing
        else:
            t.setFlag(self.pFlag, 1)

        # e 비트 계산
        if t.byteSize == 4:             # 4형식
            t.setFlag(self.eFlag, 1)
        else:
            t.setFlag(self.eFlag, 0)

        # ObjectCode 계산
        # 2형식
        if t.byteSize == 2:
            # 해당 명령어의 opcode값을 임시 변수에 저장
            code = self.instTab.instMap.get(t.operator).opcode << 8
            # 레지스터 종류 구분
            if t.operand[0] == "A":
                code += 0 << 4
            elif t.operand[0] == "X":
                code += 1 << 4
            elif t.operand[0] == "L":
                code += 2 << 4
            elif t.operand[0] == "B":
                code += 3 << 4
            elif t.operand[0] == "S":
                code += 4 << 4
            elif t.operand[0] == "T":
                code += 5 << 4

            # 피연산자가 2개인 경우
            if len(t.operator) == 2:
                if t.operand[1] == "A":
                    code += 0
                elif t.operand[1] == "X":
                    code += 1
                elif t.operand[1] == "L":
                    code += 2
                elif t.operand[1] == "B":
                    code += 3
                elif t.operand[1] == "S":
                    code += 4
                elif t.operand[1] == "T":
                    code += 5

            # 생성된 objectCode를 16진수 문자열로 저장
            t.objectCode = "%04X" % code

        # 3형식
        elif t.byteSize == 3:
            # 해당 명령어의 opcode값을 임시 변수에 저장
            code = self.instTab.instMap.get(t.operator).opcode << 16

            # nixbpe 값을 추가
            code += t.nixbpe << 12

            # RSUB 연산인 경우
            if t.operator == "RSUB":
                code += 0

            # PC relative인 경우
            elif t.getFlag(self.pFlag) == self.pFlag:
                ta = 0
                pc = self.getToken(index+1).location

                # LiteralTable과 SymbolTable에서 값을 탐색하여 TA에 주소값 저장
                try:
                    if self.literalTab.literalList.index(t.operand[0]) != -1:
                        ta = self.literalTab.locationList[self.literalTab.literalList.index(t.operand[0])]
                except ValueError:
                    try:
                        if self.symTab.symbolList.index(t.operand[0].replace("@", "")) != -1:
                            ta = self.symTab.locationList[self.symTab.symbolList.index(t.operand[0].replace("@", ""))]
                    except ValueError:
                        ta = 0

                # TA - PC 값의 하위 12개 비트값 추가
                code += (ta - pc) & 0XFFF

            # Direct Addressing인 경우
            elif t.getFlag(self.bFlag | self.pFlag) == 0:
                code += int(t.operand[0].replace("#", ""))

            # 생성된 objectCode를 16진수로 저장
            t.objectCode = "%06X" % code

        # 4형식
        elif t.byteSize == 4:
            # 해당 명령어의 opcode값을 임시 변수에 저장
            code = self.instTab.instMap.get(t.operator.replace("+", "")).opcode << 24
            # nixbpe 값을 추가
            code += t.nixbpe << 20
            # 생성된 objectCode를 16진수로 저장
            t.objectCode = "%08X" % hex(code)

    # index번호에 해당하는 object code를 리턴한다.
    # @param index
    # @return : object code
    def getObjectCode(self, index):
        return self.tokenList[index].objectCode;

# 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다.
class Token:
    # 의미 분석 단계에서 사용되는 변수들
    location = 0
    label = ""
    operator = ""
    operand = []
    comment = ""
    nixbpe = 0

    # object code 생성 단계에서 사용되는 변수들
    objectCode = ""
    byteSize = 0

    # 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
    # @param line 문장단위로 저장된 프로그램 코드
    def __init__(self, line):
        self.parsing(line)

    # line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
    # @param line 문장단위로 저장된 프로그램 코드
    def parsing(self, line):
        tmpLine = line.split("\t")

        if len(tmpLine) == 4:
            self.comment = tmpLine[3]
        if len(tmpLine) >= 3:
            self.operand = tmpLine[2].split(',')
        if len(tmpLine) >= 2:
            self.operator = tmpLine[1]
        if len(tmpLine) >= 1:
            self.label = tmpLine[0]

    # n,i,x,b,p,e flag를 설정한다.
    # @param flag : 원하는 비트 위치
    # @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
    def setFlag(self, flag, value):
        if value == 1:
            self.nixbpe |= flag;
        else:
            self.nixbpe &= 127 - flag;

    # 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다
    # @param flags : 값을 확인하고자 하는 비트 위치
    # @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
    def getFlag(self, flags):
        return self.nixbpe & flags;