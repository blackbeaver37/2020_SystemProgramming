class InstTable:
    # inst.data 파일을 불러와 저장하는 공간
    instMap = dict()

    # 클래스 초기화. 파싱을 동시에 처리한다.
    # @param instFile : instuction에 대한 명세가 저장된 파일 이름
    def __init__(self, instFile):
        self.openFile(instFile)

    # 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
    def openFile(self, fileName):
        file = open(fileName, 'r')
        while True:
            line = file.readline()
            if not line: break
            inst = Instruction(line)
            self.instMap[inst.instname] = inst
        file.close()


#  명령어 하나하나의 구체적 정보를 담는 class
class Instruction:
    instname = ""    # 명령어 이름
    format = 0      # 명령어 포맷
    opcode = 0     # 명령어 Opcode
    opnum = 0     # 피연산자의 수

    # 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
    # @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
    def __init__(self,line):
        self.parsing(line)

    # 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
    # @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
    def parsing(self,line):
        tmpLine = line.replace("\n", "").split()
        self.instname = tmpLine[0]
        self.format = int(tmpLine[1])
        self.opcode = int(tmpLine[2], 16)
        self.opnum = int(tmpLine[3])