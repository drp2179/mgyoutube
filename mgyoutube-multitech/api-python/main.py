import json
from wsgiref.simple_server import make_server
from pyramid.config import Configurator
from modules import DefaultUserModuleImpl, DefaultVideoModuleImpl, DefaultSearchesModuleImpl
from repos import SimplisticUserDataRepoImpl, SimplisticSearchesDataRepoImpl
from webservices import ParentsWebService, ChildrenWebService, SupportWebService, VideoWebService
from webservices.ModuleRepoRegistry import ModuleRepoRegistry
import YouTubeProperties

if __name__ == '__main__':

    youTubeProperties = YouTubeProperties.YouTubeProperties()
    ModuleRepoRegistry.setVideoModule(
        DefaultVideoModuleImpl.DefaultVideoModuleImpl(youTubeProperties.apiKey, youTubeProperties.applicationName))

    userDataRepo = SimplisticUserDataRepoImpl.SimplisticUserDataRepoImpl()
    ModuleRepoRegistry.setUserDataRepo(userDataRepo)

    serchesRepo = SimplisticSearchesDataRepoImpl.SimplisticSearchesDataRepoImpl()
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
