import json
import urllib.parse
from pyramid.response import Response
from pyramid.request import Request
from pyramid.view import view_config
from pyramid.config import Configurator
from pyramid.httpexceptions import HTTPNotFound, HTTPUnauthorized, HTTPMethodNotAllowed,  HTTPServerError
from webservices.ModuleRepoRegistry import ModuleRepoRegistry
from webservices.Helpers import Helpers
from modules.UserNotFoundException import UserNotFoundException
from modules.UserModule import UserModule


@view_config(route_name='parents-auth', request_method='POST')
def authParentUser(request: Request) -> Response:
    userCredentialJson = request.body
    print("authParentUser: userCredentialJson=",  userCredentialJson)

    # TODO: need to sanitize payload input before using
    sanitizedUserCredentialJson = userCredentialJson
    userCredential = Helpers.marshalUserCredentialFromJson(
        sanitizedUserCredentialJson)
    print("authParentUser: userCredential=", userCredential)

    authedUser = ModuleRepoRegistry.getUserModule().authUser(userCredential)

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
def getChildrenForParent(request: Request) -> Response:

    # TODO: need to sanitize payload input before using
    parentUsername = request.matchdict['parentusername']
    sanitizedParentUsername = parentUsername
    print("getChildrenForParent: parentUsername=", parentUsername)

    try:
        children = ModuleRepoRegistry.getUserModule(
        ).getChildrenForParent(sanitizedParentUsername)
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
def addUpdateChildToParent(request: Request) -> Response:

    # TODO: need to sanitize payload input before using
    parentUsername = request.matchdict['parentusername']
    sanitizedParentUsername = parentUsername
    childUsername = request.matchdict['childusername']
    # sanitizedChildUsername = childUsername
    childUserJson = request.body
    sanitizedChildUserJson = childUserJson
    print("addUpdateChildToParent: parentUsername=", parentUsername,
          ", childUsername=", childUsername, ", childUserJson=", childUserJson)

    childUser = Helpers.marshalUserFromJson(sanitizedChildUserJson)
    print("addUpdateChildToParent: childUser=", childUser)

    try:
        addedUpdatedUser = ModuleRepoRegistry.getUserModule().addUpdateChildToParent(
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


@view_config(route_name='parents-searches', request_method='GET')
def getSavedSearches(request: Request) -> Response:

    parentUsername = request.matchdict['parentusername']
    print("getSavedSearches: parentUsername=", parentUsername)

    # // TODO: need to sanitize payload input before using
    sanitizedParentUsername = parentUsername

    try:
        searches = ModuleRepoRegistry.getSearchesModule().getSavedSearchesForParent(
            sanitizedParentUsername)
        print("getSavedSearches: getSavedSearchesForParent=", searches)

        responseJson = json.dumps(searches, default=lambda x: x.__dict__)

        print("getSavedSearches: parentUsername=", parentUsername,
              " returning OK for size ", len(searches), " and json=", responseJson)
        return Response(body=responseJson, content_type='application/json', charset="UTF-8")
    except UserNotFoundException as unfe:
        print("getSavedSearches: unable to find user ",
              unfe.username, " returning NOT_FOUND")
        raise HTTPNotFound()


@view_config(route_name='parents-addsearchphrase', request_method='PUT')
def saveSearch(request: Request) -> Response:
    parentUsername = request.matchdict['parentusername']
    searchPhrase = request.matchdict['searchphrase']
    decodedSearchPhrase = urllib.parse.unquote_plus(searchPhrase)
    print("saveSearch: parentUsername=", parentUsername, ", searchPhrase=",
          searchPhrase, ", decodedSearchPhrase=", decodedSearchPhrase)

    # // TODO: need to sanitize payload input before using
    sanitizedParentUsername = parentUsername
    sanitizedSearchPhrase = decodedSearchPhrase

    try:
        ModuleRepoRegistry.getSearchesModule().addSearchToParent(
            sanitizedParentUsername, sanitizedSearchPhrase)

        print("saveSearch: parentUsername=", sanitizedParentUsername,
              ", searchPhrase=", sanitizedSearchPhrase, " returning CREATED")
        location = "/parents/" + sanitizedParentUsername + "/searches"
        return Response(status=201, location=location)
    except UserNotFoundException as unfe:
        print("saveSearch: unable to find user ",
              unfe.username, " returning NOT_FOUND")
        return HTTPNotFound()


def setupParentsWebService(config: Configurator):
    config.add_route('parents-auth',        '/api/parents/auth')
    config.add_route('parents-children',
                     '/api/parents/{parentusername}/children')
    config.add_route('parents-addupdatechildren',
                     '/api/parents/{parentusername}/children/{childusername}')
    config.add_route('parents-searches',
                     '/api/parents/{parentusername}/searches')
    config.add_route('parents-addsearchphrase',
                     '/api/parents/{parentusername}/searches/{searchphrase}')
