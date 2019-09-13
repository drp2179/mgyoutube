from repos.SearchesDataRepo import SearchesDataRepo


class SimplisticSearchesDataRepoImpl(SearchesDataRepo):

    def __init__(self):
        self.parentSearches = dict()  # <userId:int, set(str)>

    def getSearchesForParentUser(self, parentUserId: int) -> list:
        if (parentUserId in self.parentSearches):
            return list(self.parentSearches[parentUserId])
        return list()

    def addSearchToParentUser(self, parentUserId: int, searchPhrase: str):
        if parentUserId not in self.parentSearches:
            self.parentSearches[parentUserId] = set()
        existingSet = self.parentSearches[parentUserId]
        existingSet.add(searchPhrase)
