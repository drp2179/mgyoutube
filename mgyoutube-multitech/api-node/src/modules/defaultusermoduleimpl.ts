import { User } from '../apimodel/user';
import { UserCredential } from '../apimodel/usercredential';
import { UserNotFoundException } from './usernotfoundexception';
import { UserModule } from './usermodule';
import { UserDataRepo } from '../repos/userdatarepo';


export class DefaultUserModuleImpl implements UserModule {
    userDataRepo: UserDataRepo;

    constructor(aUserDataRepo: UserDataRepo) {
        this.userDataRepo = aUserDataRepo
    }

    authUser(userCredential: UserCredential): User | undefined {
        var user: User | undefined;

        try {
            user = this.userDataRepo.getUserByUsername(userCredential.username)

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

    createUser(user: User): User | undefined {
        if (user.userId === undefined || user.userId == 0) {
            const createdUser = this.userDataRepo.addUser(user)
            if (createdUser !== undefined) {
                return createdUser
            }
        }
        return undefined
    }

    updateUser(userId: number, user: User): User | undefined {
        return this.userDataRepo.replaceUser(userId, user);
    }

    getUser(username: string): User | undefined {
        try {
            return this.userDataRepo.getUserByUsername(username)
        }
        catch (e) {
            console.error(e)
            return undefined
        }
    }

    removeUser(username: string): User | undefined {
        const user = this.getUser(username);

        if (user && user.userId > 0) {
            this.userDataRepo.removeUser(user);
        }

        return user;
    }

    addUpdateChildToParent(parentUsername: string, childUser: User): User | undefined {

        const parentUser = this.getUser(parentUsername);
        if (parentUser === undefined) {
            throw new UserNotFoundException(parentUsername);
        }

        const existingChildUser = this.getUser(childUser.username);
        if (existingChildUser === undefined) {
            console.log("creating child ", childUser);
            const createdChildUser = this.createUser(childUser);
            if (createdChildUser === undefined) {
                console.error("creating child ", childUser, " failed ");
            }
            else {
                this.userDataRepo.addChildToParent(parentUser.userId, createdChildUser.userId);
            }
            return createdChildUser;
        } else {
            console.log("updating child ", childUser, " as ", existingChildUser.userId);
            this.userDataRepo.addChildToParent(parentUser.userId, existingChildUser.userId);
            return this.updateUser(existingChildUser.userId, childUser);
        }
    }

    getChildrenForParent(parentUsername: string): Array<User> {
        const parentUser = this.getUser(parentUsername);
        if (parentUser === undefined) {
            throw new UserNotFoundException(parentUsername);
        }

        const children = this.userDataRepo.getChildrenForParent(parentUser.userId);
        return children;
    }

}
