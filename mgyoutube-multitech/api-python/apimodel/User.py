
class User(object):

    def __init__(self):
        self.userId = 0
        self.username = None
        self.password = None
        self.isParent = False


def cloneUser(userToClone: User) -> User:
    clonedUser = User()
    clonedUser.userId = userToClone.userId
    clonedUser.username = userToClone.username
    clonedUser.password = userToClone.password
    clonedUser.isParent = userToClone.isParent
    return clonedUser
