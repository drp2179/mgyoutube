from abc import ABC, abstractmethod


class SearchesDataRepo(ABC):
    @abstractmethod
    def repositoryStartup(self):
        pass

    @abstractmethod
    def getSearchesForParentUser(self, parentUserId: str) -> list:
        pass

    @abstractmethod
    def addSearchToParentUser(self, parentUserId: str, searchPhrase: str):
        pass

    @abstractmethod
    def removeSearchFromParentUser(self, parentUserId: str, searchPhrase: str):
        pass
