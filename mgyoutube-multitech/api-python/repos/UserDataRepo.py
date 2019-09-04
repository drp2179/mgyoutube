from abc import ABC, abstractmethod
from apimodel.User import User


class UserDataRepo(ABC):

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
    def replaceUser(self, userId: int, user: User) -> User:
        pass

    @abstractmethod
    def addChildToParent(self, parentUserId: int, childUserId: int):
        pass

    @abstractmethod
    def getChildrenForParent(self, userId) -> list:
        pass
