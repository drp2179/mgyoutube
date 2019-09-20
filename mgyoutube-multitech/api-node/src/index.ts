import { SimplisticUserDataRepoImpl } from "./repos/simplisticuserdatarepoimpl";
import { ModuleRepoRegistry } from "./webservices/modulereporegistry";

import { Server, createServer } from 'restify'
import { bodyParser, queryParser } from 'restify-plugins'
import { SupportWebService } from './webservices/supportwebservice';
import { ChildrentWebService } from './webservices/childrenwebservice';
import { ParentsWebService } from './webservices/parentswebservice';
import { VideoWebService } from './webservices/videowebservice';
import { DefaultVideoModuleImpl } from "./modules/defaultvideomoduleimpl";
import { YouTubeProperties } from "./youtubeproperties";
import { SimplisticSearchesDataRepoImpl } from "./repos/simplisticsearchesdatarepoimpl";
import { DefaultSearchesModuleImpl } from "./modules/defaultsearchesmoduleimpl";
import { MongoSearchesDataRepoImpl } from "./repos/mongosearchesdatarepoimpl";
import { SearchesDataRepo } from "./repos/searchesdatarepo";
import { UserDataRepo } from "./repos/userdatarepo";
import { MongoUserDataRepoImpl } from "./repos/mongouserdatarepoimpl";


async function setupModulesAndRepos() {
    const youTubeProperties = new YouTubeProperties();
    const videoModule = new DefaultVideoModuleImpl(youTubeProperties.apiKey, youTubeProperties.applicationName);
    ModuleRepoRegistry.setVideosModule(videoModule);

    const connectionString = "mongodb://localhost:27017"
    const databaseName = "mgyoutube"

    //const userDataRepo = new SimplisticUserDataRepoImpl()
    const userDataRepo: UserDataRepo = new MongoUserDataRepoImpl(connectionString, databaseName)
    userDataRepo.repositoryStartup();
    ModuleRepoRegistry.setUserDataRepo(userDataRepo);

    //const searchesDataRepo = new SimplisticSearchesDataRepoImpl();
    const searchesDataRepo: SearchesDataRepo = new MongoSearchesDataRepoImpl(connectionString, databaseName);
    searchesDataRepo.repositoryStartup();

    const searchesModule = new DefaultSearchesModuleImpl(userDataRepo, searchesDataRepo)
    ModuleRepoRegistry.setSearchesModule(searchesModule);
}

setupModulesAndRepos().then(() => {

    const server: Server = createServer();
    server.use(bodyParser());
    server.use(queryParser());

    ParentsWebService.setupRouter(server);
    ChildrentWebService.setupRouter(server);
    SupportWebService.setupRouter(server);
    VideoWebService.setupRouter(server);

    server.listen(8081, function () {
        console.log('%s listening at %s', server.name, server.url);
    });
}).catch(err => {
    console.error(err);
})