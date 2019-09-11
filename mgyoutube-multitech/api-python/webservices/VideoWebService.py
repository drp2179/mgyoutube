import json
from pyramid.response import Response
from pyramid.request import Request
from pyramid.view import view_config
from pyramid.config import Configurator
from pyramid.httpexceptions import HTTPNotFound, HTTPUnauthorized, HTTPMethodNotAllowed,  HTTPServerError, HTTPBadRequest
from webservices.ModuleRepoRegistry import ModuleRepoRegistry


@view_config(route_name='videos-search', request_method='GET')
def videosSearch(request: Request) -> Response:
    searchTerms: str = None
    try:
        searchTerms = request.params.getone("search")
        print("videosSearch: searchTerms='", searchTerms, "'")
    except KeyError:
        print("no 'search' query parameter, failing as 400")
        raise HTTPBadRequest()

    if (searchTerms is None):
        print("search term is None, failing as 400")
        raise HTTPBadRequest()

    searchTerms = searchTerms.strip()
    if (searchTerms == ""):
        print("search term are blank failing as 400")
        raise HTTPBadRequest()

    # // TODO: need to sanitize payload input before using
    sanitizedSearchTerms = searchTerms

    videos = ModuleRepoRegistry.getVideoModule().search(sanitizedSearchTerms)
    print("videosModule.search: videos=", videos)

    responseJson = json.dumps(videos, default=lambda x: x.__dict__)

    print("videosSearch: searchTerms=",
          sanitizedSearchTerms, " returning OK, ", responseJson)
    return Response(body=responseJson, content_type='application/json', charset="UTF-8")


def setupVideoWebService(config: Configurator):
    config.add_route('videos-search', '/api/videos')
