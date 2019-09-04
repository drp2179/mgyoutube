import { User } from './apimodel/user';
import { UserCredential } from './apimodel/usercredential';
import { UserNotFoundException } from './modules/usernotfoundexception';
import { UserDataRepo } from './repos/userdatarepo';
import { SimplisticUserDataRepoImpl } from "./repos/simplisticuserdatarepoimpl";
import { UserModule } from './modules/usermodule';
import { DefaultUserModuleImpl } from './modules/defaultusermoduleimpl';

import { Request, Response, Next, Server, createServer } from 'restify'
import { NotFoundError, UnauthorizedError, InternalServerError } from 'restify-errors'
import { bodyParser } from 'restify-plugins'

const userDataRepo: UserDataRepo = new SimplisticUserDataRepoImpl()
const userModule: UserModule = new DefaultUserModuleImpl(userDataRepo)


function marshalUserCredentialFromJson(userCredentialJson: object): UserCredential {
    //const jsonUserCredentialJson = JSON.parse(userCredentialJson)
    const jsonUserCredentialJson: any = userCredentialJson
    //console.log("jsonUserCredentialJson", jsonUserCredentialJson)
    const userCredential = new UserCredential()
    userCredential.username = jsonUserCredentialJson.username;
    userCredential.password = jsonUserCredentialJson.password
    return userCredential
}


function marshalUserFromJson(userJson: object): User {
    //const jsonUser = JSON.parse(userJson)
    const jsonUser: any = userJson
    //console.log("jsonUser", jsonUser)
    const user = new User()
    user.username = jsonUser.username;
    user.password = jsonUser.password;
    user.isParent = jsonUser.isParent;
    user.userId = jsonUser.userId;
    return user
}


function getUserByUsername(req: Request, res: Response, next: Next): any {

    const username = req.params.username
    console.log("getUserByUsername: username=", username)

    const santizedUsername = username
    const user = userModule.getUser(santizedUsername)
    console.log("getUserByUsername: user=", user)

    if (user == null) {
        console.warn("getUserByUsername: username=", username, " returning NOT_FOUND")
        const err404 = new NotFoundError();
        return next(err404);
    }

    user.password = undefined
    //const responseJson = JSON.stringify(user)

    console.log("getUserByUsername: username=", username, " returning OK")

    res.setHeader("content-type", "application/json")
    //res.send(responseJson)
    res.send(user)

    return next();
}

function supportCreateuser(req: Request, res: Response, next: Next): any {
    const userJson = req.body
    console.log("createUser: userJson=", userJson)

    const sanitizedUserJson = userJson
    const user = marshalUserFromJson(sanitizedUserJson)
    console.log("createUser: user=", user)
    const createdUser = userModule.createUser(user)
    console.log("createUser: createdUser=", createdUser)
    if (createdUser === undefined) {
        console.error("createUser failed for " + sanitizedUserJson);
        const err500 = new InternalServerError();
        return next(err500);
    }
    const location = "/users/" + createdUser.userId
    console.log("createUser: userJson=", userJson, " returning CREATED")

    res.setHeader("location", location)
    res.send(201)
    return next();
}

function authParentUser(req: Request, res: Response, next: Next): any {
    const userCredentialJson = req.body
    console.log("authParentUser: userCredentialJson=", userCredentialJson)

    // TODO: need to sanitize payload input before using
    const sanitizedUserCredentialJson = userCredentialJson
    const userCredential = marshalUserCredentialFromJson(sanitizedUserCredentialJson)
    console.log("authParentUser: userCredential=", userCredential)

    const authedUser = userModule.authUser(userCredential)

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
    //const responseJson = JSON.stringify(authedUser)

    console.log("authParentUser: userCredentialJson=", userCredentialJson, " returning OK")

    res.setHeader("content-type", "application/json")
    //res.send(responseJson)
    res.send(authedUser)
    return next();
}

function authChildrenUser(req: Request, res: Response, next: Next): any {
    const userCredentialJson = req.body
    console.log("authChildrenUser: userCredentialJson=", userCredentialJson)

    // TODO: need to sanitize payload input before using
    const sanitizedUserCredentialJson = userCredentialJson
    const userCredential = marshalUserCredentialFromJson(sanitizedUserCredentialJson)
    console.log("authChildrenUser: userCredential=", userCredential)

    const authedUser = userModule.authUser(userCredential)

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
    //const responseJson = JSON.stringify(authedUser)

    console.log("authChildrenUser: userCredentialJson=", userCredentialJson, " returning OK")

    res.setHeader("content-type", "application/json")
    //res.send(responseJson)
    res.send(authedUser)

    return next();
}

function getChildrenForParent(req: Request, res: Response, next: Next): any {
    const parentUsername = req.params.parentusername
    console.log("getChildrenForParent: parentUsername=", parentUsername);

    // TODO: need to sanitize payload input before using
    const sanitizedParentUsername = parentUsername;

    try {
        const children = userModule.getChildrenForParent(sanitizedParentUsername);
        console.log("getChildrenForParent: getChildrenForParent=", children);

        // children.forEach(u -> {
        //     u.password = null;
        // });

        const responseJson = JSON.stringify(children);

        console.log("getChildrenForParent: parentUsername=", parentUsername, " returning OK for size "
            , children.length + " and json=", responseJson);
        //return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
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

function addUpdateChildToParent(req: Request, res: Response, next: Next): any {
    const parentUsername = req.params.parentusername
    const childUsername = req.params.childusername
    const childUserJson = req.body
    console.log("addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername="
        , childUsername, ", childUserJson=", childUserJson);

    // TODO: need to sanitize payload input before using
    const sanitizedParentUsername = parentUsername;
    // final String sanitizedChildUsername = childUsername;
    const sanitizedChildUserJson = childUserJson;

    const childUser = marshalUserFromJson(sanitizedChildUserJson);
    console.log("addUpdateChildToParent: childUser=", childUser);

    try {
        const addedUpdatedUser = userModule.addUpdateChildToParent(sanitizedParentUsername, childUser);

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

function removeUserByUsername(req: Request, res: Response, next: Next): any {
    const username = req.params.username
    console.log("removeUserByUsername: username=" + username);

    // TODO: need to sanitize payload input before using
    const santizedUsername = username;
    const oldUser = userModule.removeUser(santizedUsername);

    if (oldUser === undefined) {
        console.warn("deleteUserByUsername: username=", username, " returning NOT_FOUND");
        const err404 = new NotFoundError();
        return next(err404);
    }

    res.send(200)
    return next();
}


const server: Server = createServer();
server.use(bodyParser());
server.use(bodyParser());
server.post('/api/parents/auth', authParentUser);
server.get('/api/parents/:parentusername/children', getChildrenForParent);
server.put('/api/parents/:parentusername/children/:childusername', addUpdateChildToParent);

server.post('/api/children/auth', authChildrenUser);

server.post('/api/support/user', supportCreateuser);
server.get('/api/support/user/:username', getUserByUsername);
server.del('/api/support/user/:username', removeUserByUsername);

server.listen(8081, function () {
    console.log('%s listening at %s', server.name, server.url);
});