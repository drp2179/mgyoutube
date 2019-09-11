import json
from pyramid.response import Response
from pyramid.request import Request
from pyramid.view import view_config
from pyramid.config import Configurator
from pyramid.httpexceptions import HTTPNotFound, HTTPUnauthorized, HTTPMethodNotAllowed,  HTTPServerError
from webservices.Helpers import Helpers
from webservices.ModuleRepoRegistry import ModuleRepoRegistry


@view_config(route_name='children-auth', request_method='POST')
def authChildrenUser(request: Request) -> Response:
    userCredentialJson = request.body
    print("authChildrenUser: userCredentialJson=",  userCredentialJson)

    # TODO: need to sanitize payload input before using
    sanitizedUserCredentialJson = userCredentialJson
    userCredential = Helpers.marshalUserCredentialFromJson(
        sanitizedUserCredentialJson)
    print("authChildrenUser: userCredential=", userCredential)

    authedUser = ModuleRepoRegistry.getUserModule().authUser(userCredential)

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


def setupChildrensWebService(config: Configurator):
    config.add_route('children-auth', '/api/children/auth')
