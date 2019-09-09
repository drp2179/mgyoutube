import { UserModule } from '../modules/usermodule';
import { ModuleRepoRegistry } from './modulereporegistry';
import { Helpers } from './helpers';
import { Request, Response, Next, Server, createServer } from 'restify'
import { NotFoundError, UnauthorizedError, InternalServerError } from 'restify-errors'
import { ParentsWebService } from './parentswebservice';


export class SupportWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.put('/api/support/users/:username', SupportWebService.supportCreateuser);
        server.get('/api/support/users/:username', SupportWebService.getUserByUsername);
        server.del('/api/support/users/:username', SupportWebService.removeUserByUsername);
    }

    private static supportCreateuser(req: Request, res: Response, next: Next): any {
        const userJson = req.body
        console.log("createUser: userJson=", userJson)

        const sanitizedUserJson = userJson
        const user = Helpers.marshalUserFromJson(sanitizedUserJson)
        console.log("createUser: user=", user)
        const createdUser = ModuleRepoRegistry.getUserModule().createUser(user)
        console.log("createUser: createdUser=", createdUser)
        if (createdUser === undefined) {
            console.error("createUser failed for " + sanitizedUserJson);
            const err500 = new InternalServerError();
            return next(err500);
        }
        createdUser.password = undefined;

        console.log("createUser: userJson=", userJson, " returning OK")
        res.setHeader("content-type", "application/json")
        res.send(createdUser)
        return next();
    }

    private static getUserByUsername(req: Request, res: Response, next: Next): any {

        const username = req.params.username
        console.log("getUserByUsername: username=", username)

        const santizedUsername = username
        const user = ModuleRepoRegistry.getUserModule().getUser(santizedUsername)
        console.log("getUserByUsername: user=", user)

        if (user == null) {
            console.warn("getUserByUsername: username=", username, " returning NOT_FOUND")
            const err404 = new NotFoundError();
            return next(err404);
        }

        user.password = undefined

        console.log("getUserByUsername: username=", username, " returning OK")

        res.setHeader("content-type", "application/json")
        res.send(user)

        return next();
    }

    private static removeUserByUsername(req: Request, res: Response, next: Next): any {
        const username = req.params.username
        console.log("removeUserByUsername: username=" + username);

        // TODO: need to sanitize payload input before using
        const santizedUsername = username;
        const oldUser = ModuleRepoRegistry.getUserModule().removeUser(santizedUsername);

        if (oldUser === undefined) {
            console.warn("deleteUserByUsername: username=", username, " returning NOT_FOUND");
            const err404 = new NotFoundError();
            return next(err404);
        }

        res.send(200)
        return next();
    }

}