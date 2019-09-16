import { ModuleRepoRegistry } from './modulereporegistry';
import { Helpers } from './helpers';
import { Request, Response, Server } from 'restify'
import { UnauthorizedError } from 'restify-errors'
import { UseNext } from './whatsnext';


export class ChildrentWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.post('/api/children/auth', async (req, res, next) => {
            return UseNext.handleAsyncRestifyCall(await ChildrentWebService.authChildrenUser(req, res), next);
        })
    }

    private static async authChildrenUser(req: Request, res: Response): Promise<UseNext> {
        const userCredentialJson = req.body
        console.log("authChildrenUser: userCredentialJson=", userCredentialJson)

        // TODO: need to sanitize payload input before using
        const sanitizedUserCredentialJson = userCredentialJson
        const userCredential = Helpers.marshalUserCredentialFromJson(sanitizedUserCredentialJson)
        console.log("authChildrenUser: userCredential=", userCredential)

        const authedUser = await ModuleRepoRegistry.getUserModule().authUser(userCredential)

        if (authedUser == null) {
            console.warn("authChildrenUser: userCredentialJson=", userCredentialJson, " failed auth, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return new UseNext(err401);
        }
        else if (authedUser.isParent !== undefined && authedUser.isParent) {
            console.warn("authChildrenUser: userCredentialJson=", userCredentialJson, " is a parent, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return new UseNext(err401);
        }

        authedUser.password = undefined

        console.log("authChildrenUser: userCredentialJson=", userCredentialJson, " returning OK")

        res.setHeader("content-type", "application/json")
        res.send(authedUser)

        return UseNext.Nothing;
    }
}