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
