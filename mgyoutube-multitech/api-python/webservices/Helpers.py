import json
from apimodel import User, UserCredential


class Helpers(object):

    @staticmethod
    def marshalUserCredentialFromJson(userCredentialJson: str) -> UserCredential:
        jsonUserCredentialJson = json.loads(userCredentialJson)
        print("jsonUserCredentialJson", jsonUserCredentialJson)
        userCredential = UserCredential.UserCredential()
        if ('username' in jsonUserCredentialJson):
            userCredential.username = jsonUserCredentialJson['username']
        if ('password' in jsonUserCredentialJson):
            userCredential.password = jsonUserCredentialJson['password']
        return userCredential

    @staticmethod
    def marshalUserFromJson(userJson: str) -> User:
        jsonUser = json.loads(userJson)
        print("jsonUser", jsonUser)
        user = User.User()
        if ('username' in jsonUser):
            user.username = jsonUser['username']
        if ('password' in jsonUser):
            user.password = jsonUser['password']
        if ('isParent' in jsonUser):
            user.isParent = jsonUser['isParent']
        if ('userId' in jsonUser):
            user.userId = jsonUser['userId']
        return user
