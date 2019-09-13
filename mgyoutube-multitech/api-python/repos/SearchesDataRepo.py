from abc import ABC, abstractmethod


class SearchesDataRepo(ABC):

    @abstractmethod
    def getSearchesForParentUser(self,  parentUserId: int) -> list:
        pass

    @abstractmethod
    def addSearchToParentUser(self,  parentUserId: int,  searchPhrase: str):
        pass
