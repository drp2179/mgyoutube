import { ModuleRepoRegistry } from './modulereporegistry';
import { Helpers } from './helpers';
import { Request, Response, Next, Server, createServer } from 'restify'
import { NotFoundError, UnauthorizedError, InternalServerError } from 'restify-errors'
import { ParentsWebService } from './parentswebservice';
import { UseNext } from './whatsnext';


export class SupportWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.put('/api/support/users/:username', async (req, res, next) => {
            return UseNext.handleAsyncRestifyCall(await SupportWebService.createUpdateUserByUsername(req, res), next)
        });
        server.get('/api/support/users/:username', async (req, res, next) => {
            return UseNext.handleAsyncRestifyCall(await SupportWebService.getUserByUsername(req, res), next)
        });
        server.del('/api/support/users/:username', async (req, res, next) => {
            return UseNext.handleAsyncRestifyCall(await SupportWebService.removeUserByUsername(req, res), next)
        });
    }

    private static async createUpdateUserByUsername(req: Request, res: Response): Promise<UseNext> {
        const userJson = req.body
        console.log("createUpdateUserByUsername: userJson=", userJson)

        const sanitizedUserJson = userJson
        const user = Helpers.marshalUserFromJson(sanitizedUserJson)
        console.log("createUpdateUserByUsername: user=", user)
        const createdUser = await ModuleRepoRegistry.getUserModule().createUpdateUser(user)
        console.log("createUpdateUserByUsername: createdUser=", createdUser)
        if (createdUser === undefined) {
            console.error("createUpdateUserByUsername failed for " + sanitizedUserJson);
            const err500 = new InternalServerError();
            return new UseNext(err500);
        }
        createdUser.password = undefined;

        console.log("createUpdateUserByUsername: userJson=", userJson, " returning OK")
        res.setHeader("content-type", "application/json")
        res.send(createdUser)
        return UseNext.Nothing;
    }

    private static async getUserByUsername(req: Request, res: Response): Promise<UseNext> {

        const username = req.params.username
        console.log("getUserByUsername: username=", username)

        const santizedUsername = username
        const user = await ModuleRepoRegistry.getUserModule().getUser(santizedUsername)
        console.log("getUserByUsername: user=", user)

        if (user == null) {
            console.warn("getUserByUsername: username=", username, " returning NOT_FOUND")
            const err404 = new NotFoundError();
            return new UseNext(err404);
        }

        user.password = undefined

        console.log("getUserByUsername: username=", username, " returning OK")

        res.setHeader("content-type", "application/json")
        res.send(user)

        return UseNext.Nothing;
    }

    private static async removeUserByUsername(req: Request, res: Response): Promise<UseNext> {
        const username = req.params.username
        console.log("removeUserByUsername: username=" + username);

        // TODO: need to sanitize payload input before using
        const santizedUsername = username;
        const oldUser = await ModuleRepoRegistry.getUserModule().removeUser(santizedUsername);

        if (oldUser === undefined) {
            console.warn("deleteUserByUsername: username=", username, " returning NOT_FOUND");
            const err404 = new NotFoundError();
            return new UseNext(err404);
        }

        res.send(200)
        return UseNext.Nothing;
    }

}