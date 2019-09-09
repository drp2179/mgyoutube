import json
from wsgiref.simple_server import make_server
from pyramid.config import Configurator
from pyramid.response import Response
from pyramid.view import view_config
from pyramid.httpexceptions import HTTPNotFound, HTTPUnauthorized, HTTPMethodNotAllowed,  HTTPServerError
from modules import DefaultUserModuleImpl
from modules.UserNotFoundException import UserNotFoundException
from repos import SimplisticUserDataRepoImpl
from apimodel import User, UserCredential

userDataRepo = SimplisticUserDataRepoImpl.SimplisticUserDataRepoImpl()
userModule = DefaultUserModuleImpl.DefaultUserModuleImpl(userDataRepo)


def marshalUserCredentialFromJson(userCredentialJson: str) -> UserCredential:
    jsonUserCredentialJson = json.loads(userCredentialJson)
    print("jsonUserCredentialJson", jsonUserCredentialJson)
    userCredential = UserCredential.UserCredential()
    if ('username' in jsonUserCredentialJson):
        userCredential.username = jsonUserCredentialJson['username']
    if ('password' in jsonUserCredentialJson):
        userCredential.password = jsonUserCredentialJson['password']
    return userCredential


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


@view_config(route_name='support-getcreateupatedeleteuser', request_method='GET')
def support_getuser(request):
    username = request.matchdict['username']
    santizedUsername = username
    user = userModule.getUser(santizedUsername)
    print("support_getuser: user=", user)

    if (user is None):
        print("support_getuser: username=", username, " returning NOT_FOUND")
        raise HTTPNotFound()

    user.password = None

    responseJson = json.dumps(user.__dict__)

    print("support_getuser: username=", username, " returning OK")
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


@view_config(route_name='support-getcreateupatedeleteuser', request_method='DELETE')
def support_deleteuser(request):
    username = request.matchdict['username']
    santizedUsername = username
    oldUser = userModule.removeUser(santizedUsername)
    print("support_deleteuser: oldUser=", oldUser)

    if (oldUser is None):
        print("support_deleteuser: username=",
              username, " returning NOT_FOUND")
        raise HTTPNotFound()

    print("support_deleteuser: username=", username, " returning OK")
    return Response(status=200)


@view_config(route_name='support-getcreateupatedeleteuser', request_method='PUT')
def support_createuser(request):
    userJson = request.body
    print("createUser: userJson=", userJson)
    sanitizedUserJson = userJson
    user = marshalUserFromJson(sanitizedUserJson)
    print("createUser: user=", user)
    createdUser = userModule.createUser(user)
    createdUser.password = None

    responseJson = json.dumps(createdUser.__dict__)
    print("createUser: userJson=", userJson,
          " returning OK and ", responseJson)
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


@view_config(route_name='parents-auth', request_method='POST')
def authParentUser(request):
    userCredentialJson = request.body
    print("authParentUser: userCredentialJson=",  userCredentialJson)

    # TODO: need to sanitize payload input before using
    sanitizedUserCredentialJson = userCredentialJson
    userCredential = marshalUserCredentialFromJson(sanitizedUserCredentialJson)
    print("authParentUser: userCredential=", userCredential)

    authedUser = userModule.authUser(userCredential)

    if (authedUser is None):
        print("authParentUser: userCredentialJson=", userCredentialJson,
              " failed auth, returning UNAUTHORIZED")
        raise HTTPUnauthorized()
    elif (not authedUser.isParent):
        print("authParentUser: userCredentialJson=", userCredentialJson,
              " is not a parent, returning UNAUTHORIZED")
        raise HTTPUnauthorized()

    authedUser.password = None
    responseJson = json.dumps(authedUser.__dict__)

    print("authParentUser: userCredentialJson=",
          userCredentialJson, " returning OK")
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


@view_config(route_name='parents-children', request_method='GET')
def getChildrenForParent(request):

    # TODO: need to sanitize payload input before using
    parentUsername = request.matchdict['parentusername']
    sanitizedParentUsername = parentUsername
    print("getChildrenForParent: parentUsername=", parentUsername)

    try:
        children = userModule.getChildrenForParent(sanitizedParentUsername)
        print("getChildrenForParent: getChildrenForParent", children)

        for u in children:
            u.password = None

        responseJson = json.dumps(children, default=lambda x: x.__dict__)

        print("getChildrenForParent: parentUsername", parentUsername,
              " returning OK for size ", len(children), " and json ", responseJson)
        return Response(body=responseJson, content_type='application/json', charset="UTF-8")
    except UserNotFoundException as unfe:
        print("getChildrenForParent: unable to find user ",
              unfe.username, " returning NOT_FOUND")
        raise HTTPNotFound()


@view_config(route_name='parents-addupdatechildren', request_method='PUT')
def addUpdateChildToParent(request):

    # TODO: need to sanitize payload input before using
    parentUsername = request.matchdict['parentusername']
    sanitizedParentUsername = parentUsername
    childUsername = request.matchdict['childusername']
    # sanitizedChildUsername = childUsername
    childUserJson = request.body
    sanitizedChildUserJson = childUserJson
    print("addUpdateChildToParent: parentUsername=", parentUsername,
          ", childUsername=", childUsername, ", childUserJson=", childUserJson)

    childUser = marshalUserFromJson(sanitizedChildUserJson)
    print("addUpdateChildToParent: childUser=", childUser)

    try:
        addedUpdatedUser = userModule.addUpdateChildToParent(
            sanitizedParentUsername, childUser)

        addedUpdatedUser.password = None

        responseJson = json.dumps(addedUpdatedUser.__dict__)

        print("addUpdateChildToParent: parentUsername=", parentUsername,
              ", childUsername=", childUsername, " returning OK")
        return Response(body=responseJson, content_type='application/json', charset="UTF-8")
    except UserNotFoundException as unfe:
        print("addUpdateChildToParent: unable to find user ",
              unfe.username, " returning NOT_FOUND")
        raise HTTPNotFound()


@view_config(route_name='children-auth', request_method='POST')
def authChildrenUser(request):
    userCredentialJson = request.body
    print("authChildrenUser: userCredentialJson=",  userCredentialJson)

    # TODO: need to sanitize payload input before using
    sanitizedUserCredentialJson = userCredentialJson
    userCredential = marshalUserCredentialFromJson(sanitizedUserCredentialJson)
    print("authChildrenUser: userCredential=", userCredential)

    authedUser = userModule.authUser(userCredential)

    if (authedUser is None):
        print("authChildrenUser: userCredentialJson=", userCredentialJson,
              " failed auth, returning UNAUTHORIZED")
        raise HTTPUnauthorized()
    elif (authedUser.isParent):
        print("authChildrenUser: userCredentialJson=", userCredentialJson,
              " is a parent, returning UNAUTHORIZED")
        raise HTTPUnauthorized()

    authedUser.password = None
    responseJson = json.dumps(authedUser.__dict__)

    print("authChildrenUser: userCredentialJson=",
          userCredentialJson, " returning OK")
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


if __name__ == '__main__':
    with Configurator() as config:
        config.add_route('parents-auth',        '/api/parents/auth')
        config.add_route('parents-children',
                         '/api/parents/{parentusername}/children')
        config.add_route('parents-addupdatechildren',
                         '/api/parents/{parentusername}/children/{childusername}')
        config.add_route('children-auth',       '/api/children/auth')
        config.add_route('support-getcreateupatedeleteuser',
                         '/api/support/users/{username}')
        config.scan()
        app = config.make_wsgi_app()
    server = make_server('0.0.0.0', 6543, app)
    server.serve_forever()
