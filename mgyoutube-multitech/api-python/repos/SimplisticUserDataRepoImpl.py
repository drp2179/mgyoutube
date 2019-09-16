from repos.UserDataRepo import UserDataRepo
from apimodel.User import User, cloneUser


class SimplisticUserDataRepoImpl(UserDataRepo):

    def __init__(self):
        self.usernameMap = dict()
        self.userIdMap = dict()
        self.parentChildrenMap = dict()
        self.nextUserId = 1

    def getUserByUsername(self, username: str) -> User:
        user = self.usernameMap.get(username)
        if (user != None):
            # cloning so modds after return do not affect maps
            return cloneUser(user)
        return user

    def addUser(self, user: User) -> User:
        # cloning so that we can mutate userId without affecting the input object
        addedUser = cloneUser(user)
        addedUser.userId = str(self.nextUserId)
        self.nextUserId = self.nextUserId + 1

        print("SimplisticUserDataRepoImpl.addUser, user being added",
              addedUser, "nextUserId", self.nextUserId)

        self.userIdMap[addedUser.userId] = addedUser
        self.usernameMap[addedUser.username] = addedUser

        # cloning so that mods after return do not mutate maps
        return cloneUser(addedUser)

    def removeUser(self, user: User):
        del self.userIdMap[user.userId]
        del self.usernameMap[user.username]

    def replaceUser(self, userId: str, user: User) -> User:
        existingUser = self.userIdMap[userId]

        if existingUser is not None:
            # // cloning so that we can mutate userId without affecting the input object
            replacingUser = cloneUser(user)
            replacingUser.userId = userId

            self.userIdMap[replacingUser.userId] = replacingUser
            self.usernameMap[replacingUser.username] = replacingUser

            # // cloning so that mods after return do not mutate maps
            return cloneUser(replacingUser)

        return None

    def addChildToParent(self, parentUserId: str, childUserId: int):
        childList = self.parentChildrenMap.get(parentUserId)
        if childList is None:
            self.parentChildrenMap[parentUserId] = list()

        childrenUserIds = self.parentChildrenMap[parentUserId]

        try:
            childrenUserIds.index(childUserId)
        except ValueError:
            childrenUserIds.append(childUserId)

    def getChildrenForParent(self, parentUserId: str) -> list:
        children = list()

        childrenUserIds = self.parentChildrenMap.get(parentUserId)
        if childrenUserIds is not None:
            for childUserId in childrenUserIds:
                user = self.getUserById(childUserId)
                if user is not None:
                    children.append(user)
                else:
                    print("unknown child userid ", childUserId)

        return children

    def getUserById(self, userId: str) -> User:
        return self.userIdMap.get(userId)
