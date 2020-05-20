from InstTable import InstTable
from LiteralTable import LiteralTable
from SymbolTable import SymbolTable
from TokenTable import TokenTable, Token

from multipledispatch import dispatch

class Assembler:
    # instruction 명세를 저장한 공간
    instTable = ""
    # 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간.
    lineList = []
    # 프로그램의 section별로 symbol table을 저장하는 공간
    symtabList = []
    # 프로그램의 section별로 literal table을 저장하는 공간
    literaltabList = []
    # 프로그램의 section별로 프로그램을 저장하는 공간
    TokenList = []
    # Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.
    codeList = []

    # 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
    # @param instFile : instruction 명세를 작성한 파일 이름.
    def __init__(self,instFile):
        self.instTable = InstTable(instFile)

    # inputFile을 읽어들여서 lineList에 저장한다.
    # @param inputFile : input 파일 이름.
    def loadInputFile(self, inputFile):
        file = open(inputFile, 'r')
        while True:
            line = file.readline()
            if not line: break
            self.lineList.append(line)
        file.close()

    # pass1 과정을 수행한다.
    def pass1(self):
        # 섹션별로 테이블을 저장하기위한 카운트 변수
        sectCount = 0

        # 테이블 생성
        literalTable = LiteralTable()
        symbolTable = SymbolTable()
        tokenTable = TokenTable(symbolTable, literalTable, self.instTable)

        # 생성된 테이블을 리스트에 저장
        self.literaltabList.append(literalTable)
        self.symtabList.append(symbolTable)
        self.TokenList.append(tokenTable)

        self.TokenList[sectCount].symTab = symbolTable;
        sectCount += 1

        # lineList에 저장되어 있는 내용을 라인별로 접근
        for i in range(len(self.lineList)):
            line = self.lineList[i].replace("\n", "")

            # 주석문인 경우 저장하지않고 넘어감
            if line[0] == ".":
                continue

            # 라인의 operator자리에 CSECT인 경우
            # 새로운 테이블을 생성하고 리스트에 추가
            if line.split("\t")[1] == "CSECT":

                literalTable = LiteralTable()
                symbolTable = SymbolTable()
                tokenTable = TokenTable(symbolTable, literalTable, self.instTable)

                self.literaltabList.append(literalTable)
                self.symtabList.append(symbolTable)
                self.TokenList.append(tokenTable)

                self.TokenList[sectCount].symTab = symbolTable;
                sectCount += 1
                print(literalTable)

            tokenTable.putToken(line)

        # 주소값 계산
        # 토큰 테이블별 접급
        for i in range(len(self.TokenList)):
            tokenT = self.TokenList[i]

            # 주소값을 저장하기위한 변수
            location = 0

            # 토큰 하나씩 접근 (한줄씩 접근)
            for j in range(len(tokenT.tokenList)):
                t = tokenT.tokenList[j]

                # 주소값 저장
                t.location = location

                # label이 있는 경우 SymbolTable에 추가
                if len(t.label) != 0:
                    tokenT.symTab.putSymbol(t.label, location)

                # operand가 =로 시작하면 LiteralTable에 추가
                if len(t.operand) != 0 and t.operand[0].startswith("="):
                    tokenT.literalTab.putLiteral(t.operand[0], 0)

                # operator 자리에 명령어가 온 경우 그 명령어의 포맷별로 주소증가값 증가
                if t.operator.replace("+", "") in self.instTable.instMap:
                    location += self.instTable.instMap.get(t.operator.replace("+", "")).format

                    if t.operator[0] == "+":
                        location += 1

                # operator 자리에 RESW가 온 경우의 주소증가값 계산
                if t.operator == "RESW":
                    location += int(t.operand[0]) * 3

                # operator 자리에 RESB가 온 경우의 주소증가값 계산
                if t.operator == "RESB":
                    location += int(t.operand[0])

                # operator 자리에 BYTE가 온 경우의 주소증가값 계산
                if t.operator == "BYTE":
                    # 정수형인 경우
                    if t.operand[0][0] == "X":
                        location += (len(t.operand[0]) - 3) // 2     # operand의 길이 에서 X와 '의 개수인 3을 빼고 계산
                    # 캐릭터형인 경우
                    elif t.operand[0][0] == "C":
                        location += (len(t.operand[0]) - 3)         # operand의 길이 에서 C와 '의 개수인 3을 빼고 계산

                # operator 자리에 WORD가 온 경우의 주소증가값 계산
                if t.operator == "WORD":
                    location += 3

                # operator 자리에 LTORG와 END가 온경우 사용 되었던 Literal문자를 선언해야 함
                # LiteralTab에 존재하는 문자 수만큼 반복 하여 주소 증가값 계산
                if t.operator == "END" or t.operator == "LTORG":
                    for k in range(len(tokenT.literalTab.locationList)):
                        tokenT.literalTab.modifyLiteral(tokenT.literalTab.literalList[k], location)

                        if tokenT.literalTab.literalList[k][0:2] == "=X":
                            location += (len(tokenT.literalTab.literalList[k]) - 4) // 2
                        elif tokenT.literalTab.literalList[k][0:2] == "=C":
                            location += (len(tokenT.literalTab.literalList[k]) - 4)

                # operator 자리에 EQU가 온 경우의 주소증가값 계산
                if t.operator == "EQU":
                    # operand에 *이 온경우는 넘어감
                    if t.operand[0][0] != "*":
                        tmpStrAry = []
                        tmpIndex = 0
                        for k in range(len(t.operand[0].split("-"))):
                            tmpStrAry.append(t.operand[0].split("-")[k])
                            if k == 0:
                                tmpIndex = tokenT.symTab.search(tmpStrAry[k])
                            else:
                                tmpIndex -= tokenT.symTab.search(tmpStrAry[k])
                        tokenT.symTab.modifySymbol(t.label, tmpIndex)

    # 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
    # @param fileName : 저장되는 파일 이름
    def printSymbolTable(self, fileName):
        file = open(fileName, 'w')
        for i in range(len(self.TokenList)):
            for j in range(len(self.TokenList[i].symTab.symbolList)):
                file.write("%-6s\t%04X\n" % (self.TokenList[i].symTab.symbolList[j], self.TokenList[i].symTab.locationList[j]))
            file.write("\n")
        file.close()

    # 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
    # @param fileName : 저장되는 파일 이름
    def printLiteralTable(self, fileName):
        file = open(fileName, 'w')
        for i in range(len(self.literaltabList)):
            for j in range(len(self.literaltabList[i].literalList)):
                file.write("%-6s\t%04X\n" % (self.literaltabList[i].literalList[j].replace("=C", "").replace("=X", "").replace("'", ""),
                                           self.literaltabList[i].locationList[j]))
        file.close()

    # pass2 과정을 수행한다.
    def pass2(self):
        # 첫번째 섹션인지 판단하는 변수
        isFirstSec = True

        # TokenTable 하나씩 접근
        for i in range(len(self.TokenList)):
            tokenT = self.TokenList[i]

            # 각 섹션에서 참조해야 하는 변수 목록을 저장할 리스트
            # EXTREF에서 선언되어 있는 목록 저장
            refList = []
            # M레코드를 작성하기 위해 값을 저장해둘 리스트
            mOperList = []       # M레코드에 추가해야하는 라인의 operator 리스트
            mNameList = []      # M레코드에 추가해야하는 변수 리스트
            mLocList = []       # M레코드에 추가해야하는 라인의 주소값 리스트

            # 각 라인의 길이를 저장할 변수
            # T레코드의 코드 길이 계산을 위해 사용
            codeLen = 0

            # Table에 저장된 Token 하나씩 접근 하여 codeList 작성
            for j in range(len(tokenT.tokenList)):
                t = tokenT.tokenList[j]

                # 연산자에 CSECT가 온 경우
                if t.operator == "CSECT":
                    self.putNextLine()
                    self.codeList.append("H")
                    self.codeList.append("%-6s" % t.label)
                    self.codeList.append("%06X" % t.location)
                    self.codeList.append("LENGTHTMP")  # 코드의 총 길이 위치에 임시 내용 저장
                    self.putNextLine()

                # 연산자에 START가 온 경우
                elif t.operator == "START":
                    self.codeList.append("H")
                    self.codeList.append("%-6s" % t.label)
                    self.codeList.append("%06X" % t.location)
                    self.codeList.append("LENGTHTMP")       # 코드의 총 길이 위치에 임시 내용 저장
                    self.putNextLine()

                # 연산자에 EXTDEF가 온 경우
                elif t.operator == "EXTDEF":
                    self.codeList.append("D")
                    for k in range(len(t.operand)):
                        defName = t.operand[k]
                        self.codeList.append("%-6s" % defName)
                        self.codeList.append("%06X" % tokenT.symTab.locationList[tokenT.symTab.symbolList.index(defName)])
                    self.putNextLine()

                # 연산자에 EXTREF가 온 경우, 추가로 refList 작성
                elif t.operator == "EXTREF":
                    self.codeList.append("R")
                    for k in range(len(t.operand)):
                        refName = t.operand[k]
                        self.codeList.append("%-6s" % refName)
                        refList.append(refName)
                    self.putNextLine()

                # 연산자에 명령어가 온 경우, 추가로 refList 작성
                elif t.operator in self.instTable.instMap:

                    # T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
                    if t.operator[0] == "+" and codeLen+4 > 0X1D:
                        codeLen = self.putNextLine(codeLen)
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 3 and codeLen+3 > 0X1D:
                        codeLen = self.putNextLine(codeLen)
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 2 and codeLen+2 > 0X1D:
                        codeLen = self.putNextLine(codeLen)
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 1 and codeLen+1 > 0X1D:
                        codeLen = self.putNextLine(codeLen)

                    # 새로운 줄에 T레코드를 작성하는 경우
                    if codeLen == 0:
                        self.codeList.append("T")
                        self.codeList.append("%06X" % t.location)
                        self.codeList.append("CL")      # 코드의 길이자리에 임시값 저장

                    # T레코드 뒷 부분 작성
                    if t.operator[0] == "+":                                        # 4형식
                        t.byteSize = 4
                        tokenT.makeObjectCode(tokenT.tokenList.index(t))
                        self.codeList.append(t.objectCode)
                        codeLen += 4
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 3:   # 3형식
                        t.byteSize = 3
                        tokenT.makeObjectCode(tokenT.tokenList.index(t))
                        self.codeList.append(t.objectCode)
                        codeLen += 3
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 2:   # 2형식
                        t.byteSize = 2
                        tokenT.makeObjectCode(tokenT.tokenList.index(t))
                        self.codeList.append(t.objectCode)
                        codeLen += 2
                    elif self.instTable.instMap.get(t.operator.replace("+", "")).format == 1:   # 1형식
                        t.byteSize = 1
                        tokenT.makeObjectCode(tokenT.tokenList.index(t))
                        self.codeList.append(t.objectCode)
                        codeLen += 1

                # 연산자에 BYTE가 온 경우
                elif t.operator == "BYTE":
                    # T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
                    if codeLen+1 > 0X1D:
                        codeLen = self.putNextLine(codeLen)

                    # 새로운 줄에 T레코드를 작성하는 경우
                    if codeLen == 0:
                        self.codeList.append("T")
                        self.codeList.append("%06X" % hex(t.location))
                        self.codeList.append("CL")      # 코드의 길이자리에 임시값 저장

                    # T레코드 뒷 부분 작성
                    self.codeList.append(t.operand[0].replace("X", "").replace("'", ""))
                    t.byteSize = 1
                    codeLen += 1

                # 연산자에 WORD가 온 경우
                elif t.operator == "WORD":
                    # T레코드의 길이를 계산 하여 개행을 하거나 하지않고 진행
                    if codeLen + 3 > 0X1D:
                        codeLen = self.putNextLine(codeLen)

                    # 새로운 줄에 T레코드를 작성하는 경우
                    if codeLen == 0:
                        self.codeList.append("T")
                        self.codeList.append("%06X" % hex(t.location))
                        self.codeList.append("CL")  # 코드의 길이자리에 임시값 저장

                    # T레코드 뒷 부분 작성
                    self.codeList.append("%06X" % 0X000000)
                    t.byteSize = 3
                    codeLen += 3

                # operator에 LTORG가 온 경우 개행하여 T레코드 작성
                elif t.operator == "LTORG":
                    codeLen = self.putNextLine(codeLen)
                    self.codeList.append("T")
                    self.codeList.append("%06X" % t.location)
                    self.codeList.append("CL")  # 코드의 길이자리에 임시값 저장

                    # LiteralTable에 저장되어있던 값들 T레코드에 작성
                    for k in range(len(tokenT.literalTab.literalList)):
                        # 캐릭터형인 경우
                        if tokenT.literalTab.literalList[k][0:2] == "=C":
                            str = tokenT.literalTab.literalList[k].replace("=C", "").replace("'", "")
                            t.byteSize = 0
                            for l in range(len(str)):
                                self.codeList.append("%02X" % ord(str[l]))
                                t.byteSize += 1
                                codeLen += 1

                        # 정수형인 경우
                        if tokenT.literalTab.literalList[k][0:2] == "=X":
                            str = tokenT.literalTab.literalList[k].replace("=X", "").replace("'", "")
                            self.codeList.append("%02X" % int(str))
                            t.byteSize = 1
                            codeLen += 1

                # operator에 END가 온 경우 개행하지 않고 T레코드 작성
                elif t.operator == "END":

                    # LiteralTable에 저장되어있던 값들 T레코드에 작성
                    for k in range(len(tokenT.literalTab.literalList)):
                        # 캐릭터형인 경우
                        if tokenT.literalTab.literalList[k][0:2] == "=C":
                            str = tokenT.literalTab.literalList[k].replace("=C", "").replace("'", "")
                            t.byteSize = 0
                            for l in range(len(str)):
                                self.codeList.append("%02X" % hex(int(str[l])))
                                t.byteSize += 1
                                codeLen += 1

                        # 정수형인 경우
                        if tokenT.literalTab.literalList[k][0:2] == "=X":
                            str = tokenT.literalTab.literalList[k].replace("=X", "").replace("'", "")
                            self.codeList.append("%02X" % int(str))
                            t.byteSize = 1
                            codeLen += 1

                # 토큰 한줄 마다 읽으면서, M레코드를 작성하기 위해 값을 저장해둘 리스트 작성
                if t.operator != "LTORG" and t.operator != "RSUB" and t.operator != "EXTREF" and t.operator != "CSECT":
                    # operand에 오는 String을 "-"로 구분하여 새로운 문자열 배열에 저장
                    operArr = []
                    for k in range(len(t.operand[0].split("-"))):
                        operArr.append(t.operand[0].split("-")[k])

                    # 구분되어 생성된 문자열 배열 수만큼 반복
                    for k in range(len(operArr)):
                        # refList에 내용 수만큼 반복하여 생성된 문자열과 비교
                        for l in range(len(refList)):
                            # 같은 문자가 있다면
                            if operArr[k] == refList[l]:
                                # 앞에 있는 문자 또는 하나의 문자인 경우
                                if k == 0:
                                    mOperList.append(t.operator)
                                    mNameList.append("+" + operArr[k])
                                    mLocList.append(t.location)
                                # "-" 다음에 오는 문자라면
                                else:
                                    mOperList.append(t.operator)
                                    mNameList.append("-" + operArr[k])
                                    mLocList.append(t.location)

            # T레코드 작성이 한 줄을 다 채우지 못하고 끝난경우가 생기면 개행
            if codeLen != 0:
                codeLen = self.putNextLine(codeLen)

            # 저장된 List이용하여 M레코드 한줄씩 작성
            for j in range(len(mNameList)):
                self.codeList.append("M")
                if mOperList[j] == "WORD":
                    self.codeList.append("%06X" % mLocList[j])
                    self.codeList.append("06")
                else:
                    self.codeList.append("%06X" % (mLocList[j] + 1))
                    self.codeList.append("05")
                self.codeList.append("%-7s" % mNameList[j])
                self.putNextLine()

            # 마지막 E부분 작성
            self.codeList.append("E")
            # 첫번째 섹션이라면 프로그램의 시작주소 작성
            if isFirstSec:
                self.codeList.append(self.codeList[2])
                isFirstSec = False

            # 섹션별 작성 종료시에 섹션별 길이 임시값"LENGTHTMP"을 실제 값으로 작성
            lastLoc = tokenT.tokenList[len(tokenT.tokenList) - 1].location
            lastByt = tokenT.tokenList[len(tokenT.tokenList) - 1].byteSize
            self.codeList[self.codeList.index("LENGTHTMP")] = "%06X" % (lastLoc + lastByt)

            self.putNextLine()

    # 개행을 진행하고 파라미터 값이 있다면 T레코드의 코드길이 값으로 저장되어있는
    # 임시값"CL"을 codeLen값으로 바꾸고 codeLen을 초기화 하기위한 0리턴
    # @param fileName : 저장되는 파일 이름
    # @return 0
    @dispatch()
    def putNextLine(self):
        self.codeList.append("\n")
    @dispatch(int)
    def putNextLine(self, length):
        self.codeList.append("\n")
        self.codeList[self.codeList.index("CL")] = "%02X" % length
        return 0

    # 작성된 codeList를 출력형태에 맞게 출력한다.
    # @param fileName : 저장되는 파일 이름
    def printObjectCode(self, fileName):
        file = open(fileName, 'w')
        for i in range(len(self.codeList)):
            tmpCode = self.codeList[i]
            file.write(tmpCode)
        file.close()

# 실행문
assembler = Assembler("inst.data")
assembler.loadInputFile("input.txt")

assembler.pass1()

assembler.printSymbolTable("symtab_20160274.txt")
assembler.printLiteralTable("literaltab_20160274.txt")

assembler.pass2()

assembler.printObjectCode("output_20160274.txt");