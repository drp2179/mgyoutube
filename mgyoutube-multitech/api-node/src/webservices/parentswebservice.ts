import { UserModule } from '../modules/usermodule';
import { UserNotFoundException } from '../modules/usernotfoundexception';
import { ModuleRepoRegistry } from './modulereporegistry';
import { Helpers } from './helpers';
import { Request, Response, Next, Server } from 'restify'
import { NotFoundError, UnauthorizedError, InternalServerError } from 'restify-errors'


export class ParentsWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.post('/api/parents/auth', ParentsWebService.authParentUser);
        server.get('/api/parents/:parentusername/children', ParentsWebService.getChildrenForParent);
        server.put('/api/parents/:parentusername/children/:childusername', ParentsWebService.addUpdateChildToParent);
    }

    private static authParentUser(req: Request, res: Response, next: Next): any {
        const userCredentialJson = req.body
        console.log("authParentUser: userCredentialJson=", userCredentialJson)

        // TODO: need to sanitize payload input before using
        const sanitizedUserCredentialJson = userCredentialJson
        const userCredential = Helpers.marshalUserCredentialFromJson(sanitizedUserCredentialJson)
        console.log("authParentUser: userCredential=", userCredential)

        const authedUser = ModuleRepoRegistry.getUserModule().authUser(userCredential)

        if (authedUser == null) {
            console.warn("authParentUser: userCredentialJson=", userCredentialJson, " failed auth, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return next(err401);
        }
        else if (authedUser.isParent === undefined || !authedUser.isParent) {
            console.warn("authParentUser: userCredentialJson=", userCredentialJson, " is not a parent, returning UNAUTHORIZED")
            const err401 = new UnauthorizedError();
            return next(err401);
        }

        authedUser.password = undefined

        console.log("authParentUser: userCredentialJson=", userCredentialJson, " returning OK")

        res.setHeader("content-type", "application/json")
        res.send(authedUser)
        return next();
    }

    private static getChildrenForParent(req: Request, res: Response, next: Next): any {
        const parentUsername = req.params.parentusername
        console.log("getChildrenForParent: parentUsername=", parentUsername);

        // TODO: need to sanitize payload input before using
        const sanitizedParentUsername = parentUsername;

        try {
            const children = ModuleRepoRegistry.getUserModule().getChildrenForParent(sanitizedParentUsername);
            console.log("getChildrenForParent: getChildrenForParent=", children);

            children.forEach((c) => { c.password = undefined });

            const responseJson = JSON.stringify(children);

            console.log("getChildrenForParent: parentUsername=", parentUsername, " returning OK for size "
                , children.length + " and json=", responseJson);
            res.setHeader("content-type", "application/json")
            res.send(children)
            return next();
        } catch (e) {
            if (e.name == UserNotFoundException.NAME) {
                console.warn("getChildrenForParent: unable to find user ", e.username, " returning NOT_FOUND");
                const err404 = new NotFoundError();
                return next(err404);
            }
            else {
                console.error("getChildrenForParent exception", e);
                const err500 = new InternalServerError();
                return next(err500);
            }

        }
    }

    private static addUpdateChildToParent(req: Request, res: Response, next: Next): any {
        const parentUsername = req.params.parentusername
        const childUsername = req.params.childusername
        const childUserJson = req.body
        console.log("addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername="
            , childUsername, ", childUserJson=", childUserJson);

        // TODO: need to sanitize payload input before using
        const sanitizedParentUsername = parentUsername;
        // final String sanitizedChildUsername = childUsername;
        const sanitizedChildUserJson = childUserJson;

        const childUser = Helpers.marshalUserFromJson(sanitizedChildUserJson);
        console.log("addUpdateChildToParent: childUser=", childUser);

        try {
            const addedUpdatedUser = ModuleRepoRegistry.getUserModule().addUpdateChildToParent(sanitizedParentUsername, childUser);

            if (addedUpdatedUser === undefined) {
                throw new Error("userModule.addUpdateChildToParent for " + sanitizedParentUsername + " and " + JSON.stringify(childUser) + " failed")
            }
            else {
                addedUpdatedUser.password = undefined;

                const responseJson = JSON.stringify(addedUpdatedUser);

                console.log("addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername="
                    , childUsername, " returning OK");
                res.setHeader("content-type", "application/json")
                res.send(addedUpdatedUser)
                return next()
            }
        } catch (e) {
            if (e.name == UserNotFoundException.NAME) {
                console.warn("addUpdateChildToParent: unable to find user ", e.username, " returning NOT_FOUND");
                const err404 = new NotFoundError();
                return next(err404);
            }
            else {
                console.error("addUpdateChildToParent exception", e);
                const err500 = new InternalServerError();
                return next(err500);
            }
        }
    }

}