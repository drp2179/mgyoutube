import { ModuleRepoRegistry } from './modulereporegistry';
import { Helpers } from './helpers';
import { Request, Response, Next, Server, createServer } from 'restify'
import { NotFoundError, UnauthorizedError, InternalServerError } from 'restify-errors'


export class ChildrentWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.post('/api/children/auth', ChildrentWebService.authChildrenUser);
    }

    private static authChildrenUser(req: Request, res: Response, next: Next): any {
        const userCredentialJson = req.body
        console.log("authChildrenUser: userCredentialJson=", userCredentialJson)

        // TODO: need to sanitize payload input before using
        const sanitizedUserCredentialJson = userCredentialJson
        const userCredential = Helpers.marshalUserCredentialFromJson(sanitizedUserCredentialJson)
        console.log("authChildrenUser: userCredential=", userCredential)

        const authedUser = ModuleRepoRegistry.getUserModule().authUser(userCredential)

        if (authedUser == null) {
            console.warn("authChildrenUser: userCredentialJson=", userCredentialJson, " failed auth, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return next(err401);
        }
        else if (authedUser.isParent !== undefined && authedUser.isParent) {
            console.warn("authChildrenUser: userCredentialJson=", userCredentialJson, " is a parent, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return next(err401);
        }

        authedUser.password = undefined

        console.log("authChildrenUser: userCredentialJson=", userCredentialJson, " returning OK")

        res.setHeader("content-type", "application/json")
        res.send(authedUser)

        return next();
    }
}