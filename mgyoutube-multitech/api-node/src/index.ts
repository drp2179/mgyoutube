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


var youTubeProperties = new YouTubeProperties();
var videoModule = new DefaultVideoModuleImpl(youTubeProperties.apiKey, youTubeProperties.applicationName);
ModuleRepoRegistry.setVideosModule(videoModule);
ModuleRepoRegistry.setUserDataRepo(new SimplisticUserDataRepoImpl());

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