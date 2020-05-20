class SymbolTable:
    symbolList = []
    locationList = []

    def __init__(self):
        self.symbolList = []
        self.locationList = []

    # 새로운 Symbol을 table에 추가한다.
    # @param symbol : 새로 추가되는 symbol의 label
    # @param location : 해당 symbol이 가지는 주소값
    def putSymbol(self, symbol, location):
        self.symbolList.append(symbol)
        self.locationList.append(location)

    # 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
    # @param symbol : 변경을 원하는 symbol의 label
    # @param newLocation : 새로 바꾸고자 하는 주소값
    def modifySymbol(self, symbol, newLocation):
        try:
            self.locationList[self.symbolList.index(symbol)] = newLocation
        except ValueError:
            return

    # 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다.
    # @param symbol : 검색을 원하는 symbol의 label
    # @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
    def search(self, symbol):
        address = 0
        try:
            index = self.symbolList.index(symbol)
        except ValueError:
            return -1

        address = self.locationList[index]

        return address