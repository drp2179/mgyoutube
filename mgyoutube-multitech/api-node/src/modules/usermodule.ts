import { UserCredential } from '../apimodel/usercredential';
import { User } from '../apimodel/user';
import { UserDataRepo } from '../repos/userdatarepo';


export interface UserModule {

    setUserDataRepo(udr: UserDataRepo): void;

    authUser(userCredential: UserCredential): User | undefined;


    getUser(username: string): User | undefined;
    createUser(user: User): User | undefined;
    updateUser(userId: number, user: User): User | undefined;
    removeUser(username: string): User | undefined;


    getChildrenForParent(parentUsername: string): Array<User>;
    addUpdateChildToParent(parentUsername: string, childUser: User): User | undefined;
}
