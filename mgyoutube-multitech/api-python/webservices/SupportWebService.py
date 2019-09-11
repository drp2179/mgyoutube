import json
from pyramid.response import Response
from pyramid.request import Request
from pyramid.view import view_config
from pyramid.config import Configurator
from pyramid.httpexceptions import HTTPNotFound, HTTPUnauthorized, HTTPMethodNotAllowed,  HTTPServerError
from webservices.Helpers import Helpers
from webservices.ModuleRepoRegistry import ModuleRepoRegistry


@view_config(route_name='support-getcreateupatedeleteuser', request_method='GET')
def support_getuser(request):
    username = request.matchdict['username']
    santizedUsername = username
    user = ModuleRepoRegistry.getUserModule().getUser(santizedUsername)
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
    oldUser = ModuleRepoRegistry.getUserModule().removeUser(santizedUsername)
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
    user = Helpers.marshalUserFromJson(sanitizedUserJson)
    print("createUser: user=", user)
    createdUser = ModuleRepoRegistry.getUserModule().createUser(user)
    createdUser.password = None

    responseJson = json.dumps(createdUser.__dict__)
    print("createUser: userJson=", userJson,
          " returning OK and ", responseJson)
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


def setupSupportWebService(config: Configurator):
    config.add_route('support-getcreateupatedeleteuser',
                     '/api/support/users/{username}')
