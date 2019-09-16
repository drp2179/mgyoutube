
class User(object):

    def __init__(self):
        self.userId = None
        self.username = None
        self.password = None
        self.isParent = False

    def __str__(self):
        return "User(userId:"+str(self.userId) + ",username:" + str(self.username) + ",password:"+str(self.password) + ",isParent" + str(self.isParent) + ")"


def cloneUser(userToClone: User) -> User:
    clonedUser = User()
    clonedUser.userId = userToClone.userId
    clonedUser.username = userToClone.username
    clonedUser.password = userToClone.password
    clonedUser.isParent = userToClone.isParent
    return clonedUser
