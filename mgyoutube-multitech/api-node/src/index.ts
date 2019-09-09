import { SimplisticUserDataRepoImpl } from "./repos/simplisticuserdatarepoimpl";
import { ModuleRepoRegistry } from "./webservices/modulereporegistry";

import { Request, Response, Next, Server, createServer } from 'restify'
import { bodyParser } from 'restify-plugins'
import { SupportWebService } from './webservices/supportwebservice';
import { ChildrentWebService } from './webservices/childrenwebservice';
import { ParentsWebService } from './webservices/parentswebservice';


ModuleRepoRegistry.setUserDataRepo(new SimplisticUserDataRepoImpl());

const server: Server = createServer();
server.use(bodyParser());

ParentsWebService.setupRouter(server);
ChildrentWebService.setupRouter(server);
SupportWebService.setupRouter(server);

server.listen(8081, function () {
    console.log('%s listening at %s', server.name, server.url);
});