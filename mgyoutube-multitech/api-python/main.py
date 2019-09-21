import json
from wsgiref.simple_server import make_server
from pyramid.config import Configurator
from modules import DefaultUserModuleImpl, DefaultVideoModuleImpl, DefaultSearchesModuleImpl
from repos import SimplisticUserDataRepoImpl, SimplisticSearchesDataRepoImpl, CouchSearchesDataRepoImpl, CouchUserDataRepoImpl
from webservices import ParentsWebService, ChildrenWebService, SupportWebService, VideoWebService
from webservices.ModuleRepoRegistry import ModuleRepoRegistry
import YouTubeProperties
import DatastoresProperties

if __name__ == '__main__':

    youTubeProperties = YouTubeProperties.YouTubeProperties()
    ModuleRepoRegistry.setVideoModule(
        DefaultVideoModuleImpl.DefaultVideoModuleImpl(youTubeProperties.apiKey, youTubeProperties.applicationName))

    datastoresProperties = DatastoresProperties.DatastoresProperties()

    #userDataRepo = SimplisticUserDataRepoImpl.SimplisticUserDataRepoImpl()
    userDataRepo = CouchUserDataRepoImpl.CouchUserDataRepoImpl(
        datastoresProperties.couchConnectionstring, datastoresProperties.couchUsername, datastoresProperties.couchPassword)
    userDataRepo.repositoryStartup()
    ModuleRepoRegistry.setUserDataRepo(userDataRepo)

    #serchesRepo = SimplisticSearchesDataRepoImpl.SimplisticSearchesDataRepoImpl()
    serchesRepo = CouchSearchesDataRepoImpl.CouchSearchesDataRepoImpl(
        datastoresProperties.couchConnectionstring, datastoresProperties.couchUsername, datastoresProperties.couchPassword)
    serchesRepo.repositoryStartup()

    searchesModule = DefaultSearchesModuleImpl.DefaultSearchesModuleImpl(
        userDataRepo, serchesRepo)
    ModuleRepoRegistry.setSearchesModule(searchesModule)

    config = Configurator()

    ParentsWebService.setupParentsWebService(config)
    ChildrenWebService.setupChildrensWebService(config)
    SupportWebService.setupSupportWebService(config)
    VideoWebService.setupVideoWebService(config)

    config.scan()
    config.scan(package='webservices')
    app = config.make_wsgi_app()
    port = 6543
    server = make_server('0.0.0.0', port, app)
    print("Listening on port", port, "...")
    server.serve_forever()
