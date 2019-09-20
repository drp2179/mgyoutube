from abc import ABC, abstractmethod


class SearchesModule(ABC):
    @abstractmethod
    def getSavedSearchesForParent(self,  parentUsername: str) -> list():
        pass

    @abstractmethod
    def addSearchToParent(self,  parentUsername: str, searchPhrase: str):
        pass

    @abstractmethod
    def removeSearchFromParent(self,  parentUsername: str, searchPhrase: str):
        pass
