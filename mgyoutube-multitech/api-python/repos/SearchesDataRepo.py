from abc import ABC, abstractmethod


class SearchesDataRepo(ABC):

    @abstractmethod
    def getSearchesForParentUser(self,  parentUserId: str) -> list:
        pass

    @abstractmethod
    def addSearchToParentUser(self,  parentUserId: str,  searchPhrase: str):
        pass
