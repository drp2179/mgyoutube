from abc import ABC, abstractmethod
from apimodel.UserCredential import UserCredential
from apimodel.User import User


class UserModule(ABC):
    @abstractmethod
    def authUser(self, userCredential: UserCredential) -> User:
        pass

    @abstractmethod
    def createUser(self, user: User) -> User:
        pass

    @abstractmethod
    def getUser(self, username: str) -> User:
        pass

    @abstractmethod
    def removeUser(self, username: str) -> User:
        pass

    @abstractmethod
    def updateUser(self, userId: str, user: User) -> User:
        pass

    @abstractmethod
    def getChildrenForParent(self, parentUsername: str) -> list:
        pass

    @abstractmethod
    def addUpdateChildToParent(self, parentUsername: str, childUser: User) -> User:
        pass
