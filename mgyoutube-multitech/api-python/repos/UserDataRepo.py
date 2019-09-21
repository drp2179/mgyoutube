from abc import ABC, abstractmethod
from apimodel.User import User


class UserDataRepo(ABC):

    @abstractmethod
    def repositoryStartup(self):
        pass

    @abstractmethod
    def getUserByUsername(self,  username: str) -> User:
        pass

    @abstractmethod
    def addUser(self,  user: User) -> User:
        pass

    @abstractmethod
    def removeUser(self,  user: User):
        pass

    @abstractmethod
    def replaceUser(self, userId: str, user: User) -> User:
        pass

    @abstractmethod
    def addChildToParent(self, parentUserId: str, childUserId: str):
        pass

    @abstractmethod
    def getChildrenForParent(self, userId: str) -> list:
        pass
