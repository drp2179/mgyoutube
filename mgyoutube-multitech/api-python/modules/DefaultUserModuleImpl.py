
from modules.UserModule import UserModule
from apimodel.User import User
from apimodel.UserCredential import UserCredential
from repos.UserDataRepo import UserDataRepo
from modules.UserNotFoundException import UserNotFoundException


class DefaultUserModuleImpl(UserModule):

    def __init__(self, aUserDataRepo: UserDataRepo):
        self.userDataRepo = aUserDataRepo

    def authUser(self, userCredential: UserCredential) -> User:
        user = None
        try:
            user = self.userDataRepo.getUserByUsername(userCredential.username)

            if (user is not None):
                if (user.password is None):
                    print("user.password is None")
                    user = None
                elif (userCredential.password is None):
                    print("userCredential.password is None")
                    user = None
                elif (user.password != userCredential.password):
                    print("user.password(", user.password,
                          ") != userCredential.password(", userCredential.password, ")")
                    user = None
                else:
                    print("password match!")
                    # password match!
            else:
                print("getUserByUsername(", userCredential.username, ") returned None")
        except Exception as e:
            print("", e)
            user = None

        return user

    def createUser(self, user: User) -> User:
        if (user.userId == 0):
            createdUser = self.userDataRepo.addUser(user)
            if (createdUser != None):
                return createdUser
        return None

    def getUser(self, username: str) -> User:
        try:
            return self.userDataRepo.getUserByUsername(username)
        except Exception as e:
            print("", e)
            return None

    def removeUser(self, username: str) -> User:
        user = self.getUser(username)
        if user is not None:
            self.userDataRepo.removeUser(user)
        return user

    def updateUser(self, userId: int,  user: User) -> User:
        return self.userDataRepo.replaceUser(userId, user)

    def getChildrenForParent(self, parentUsername: str) -> list:
        parentUser = self.getUser(parentUsername)
        if (parentUser is None):
            raise UserNotFoundException(parentUsername)

        children = self.userDataRepo.getChildrenForParent(parentUser.userId)
        return children

    def addUpdateChildToParent(self, parentUsername: str, childUser: User) -> User:
        parentUser = self.getUser(parentUsername)
        if parentUser is None:
            raise UserNotFoundException(parentUsername)

        existingChildUser = self.getUser(childUser.username)
        if existingChildUser is None:
            print("creating child ", childUser)
            createdChildUser = self.createUser(childUser)
            self.userDataRepo.addChildToParent(
                parentUser.userId, createdChildUser.userId)
            return createdChildUser
        else:
            print("updating child ", childUser,
                  " as ", existingChildUser.userId)
            self.userDataRepo.addChildToParent(
                parentUser.userId, existingChildUser.userId)
            return self.updateUser(existingChildUser.userId, childUser)
