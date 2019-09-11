
class UserNotFoundException(Exception):

    def __init__(self, username, msg=None):
        if msg is None:
            msg = "Unable to find the user " + username
        super(UserNotFoundException, self).__init__(msg)
        self.username = username
