import { User } from '../apimodel/user';
import { UserCredential } from '../apimodel/usercredential';
import { UserNotFoundException } from './usernotfoundexception';
import { UserModule } from './usermodule';
import { UserDataRepo } from '../repos/userdatarepo';


export class DefaultUserModuleImpl implements UserModule {
    userDataRepo: UserDataRepo;

    constructor(aUserDataRepo: UserDataRepo) {
        this.setUserDataRepo(aUserDataRepo);
    }

    setUserDataRepo(udr: UserDataRepo): void {
        this.userDataRepo = udr;
    }

    async authUser(userCredential: UserCredential): Promise<User | undefined> {
        var user: User | undefined;

        try {
            user = await this.userDataRepo.getUserByUsername(userCredential.username)

            if (user != null) {
                if (user.password == null) {
                    console.log("user.password is None")
                    user = undefined
                }
                else if (userCredential.password == null) {
                    console.warn("userCredential.password is None")
                    user = undefined
                }
                else if (user.password != userCredential.password) {
                    console.warn("user.password(", user.password,
                        ") != userCredential.password(", userCredential.password, ")")
                    user = undefined
                }
                else {
                    console.log("password match!")
                    // password match!
                }
            }
            else {
                console.warn("getUserByUsername(", userCredential.username, ") returned None")
            }
        }
        catch (e) {
            console.error(e)
            user = undefined
        }

        return user
    }

    async createUpdateUser(user: User): Promise<User | undefined> {
        const existingUser = await this.userDataRepo.getUserByUsername(user.username);
        if (existingUser !== undefined) {
            if (user.userId === undefined) {
                user.userId = existingUser.userId;
            }
            console.log("createUpdateUser is updating existing user ", existingUser, " to be ", user);
            return this.updateUser(existingUser.userId, user);
        }
        console.log("createUpdateUser is creating new user ", user);
        return this.createUser(user);
    }

    async createUser(user: User): Promise<User | undefined> {
        if (user.userId === undefined) {
            const createdUser = this.userDataRepo.addUser(user)
            //if (createdUser !== undefined) {
            return createdUser
            //}
        }
        return undefined
    }

    async updateUser(userId: string, user: User): Promise<User | undefined> {
        return this.userDataRepo.replaceUser(userId, user);
    }

    async getUser(username: string): Promise<User | undefined> {
        try {
            return this.userDataRepo.getUserByUsername(username)
        }
        catch (e) {
            console.error(e)
            return undefined
        }
    }

    async removeUser(username: string): Promise<User | undefined> {
        const user = await this.getUser(username);

        if (user && user.userId) {
            await this.userDataRepo.removeUser(user);
        }

        return user;
    }

    async addUpdateChildToParent(parentUsername: string, childUser: User): Promise<User | undefined> {

        const parentUser = await this.getUser(parentUsername);
        if (parentUser === undefined) {
            throw new UserNotFoundException(parentUsername);
        }

        const existingChildUser = await this.getUser(childUser.username);
        if (existingChildUser === undefined) {
            console.log("addUpdateChildToParent, creating child ", childUser);
            const createdChildUser = await this.createUser(childUser);
            if (createdChildUser === undefined) {
                console.error("addUpdateChildToParent, creating child ", childUser, " failed ");
            }
            else {
                await this.userDataRepo.addChildToParent(parentUser.userId, createdChildUser.userId);
            }
            return createdChildUser;
        } else {
            console.log("addUpdateChildToParent, updating child ", childUser, " as ", existingChildUser.userId);
            this.userDataRepo.addChildToParent(parentUser.userId, existingChildUser.userId);
            return this.updateUser(existingChildUser.userId, childUser);
        }
    }

    async getChildrenForParent(parentUsername: string): Promise<Array<User>> {
        const parentUser = await this.getUser(parentUsername);
        if (parentUser === undefined) {
            throw new UserNotFoundException(parentUsername);
        }
        console.log("getChildrenForParent(" + parentUsername + "), using parent ", parentUser);

        const children = this.userDataRepo.getChildrenForParent(parentUser.userId);
        return children;
    }

}
