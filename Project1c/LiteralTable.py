class LiteralTable:
    literalList = []
    locationList = []

    def __init__(self):
        self.literalList = []
        self.locationList = []

    # 새로운 Literal을 table에 추가한다.
    # @param literal : 새로 추가되는 literal의 label
    # @param location : 해당 literal이 가지는 주소값
    def putLiteral(self, literal, location):
        if self.search(literal) == -1:
            self.literalList.append(literal)
            self.locationList.append((location))

    # 기존에 존재하는 literal 값에 대해서 가리키는 주소값을 변경한다.
    # @param literal : 변경을 원하는 literal의 label
    # @param newLocation : 새로 바꾸고자 하는 주소값
    def modifyLiteral(self, literal, newLocation):
        try:
            self.locationList[self.literalList.index(literal)] = newLocation
        except ValueError:
            return

    # 인자로 전달된 literal이 어떤 주소를 지칭하는지 알려준다.
    # @param literal : 검색을 원하는 literal의 label
    # @return literal이 가지고 있는 주소값. 해당 literal이 없을 경우 -1 리턴
    def search(self, literal):
        address = 0
        try:
            index = self.literalList.index(literal)
        except ValueError:
            return -1

        address = self.locationList[index]

        return address