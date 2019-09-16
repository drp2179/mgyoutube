import { UserCredential } from '../apimodel/usercredential';
import { User } from '../apimodel/user';
import { UserDataRepo } from '../repos/userdatarepo';


export interface UserModule {

    setUserDataRepo(udr: UserDataRepo): void;

    authUser(userCredential: UserCredential): Promise<User | undefined>;

    getUser(username: string): Promise<User | undefined>;

    createUpdateUser(user: User): Promise<User | undefined>
    createUser(user: User): Promise<User | undefined>;
    updateUser(userId: string, user: User): Promise<User | undefined>;
    removeUser(username: string): Promise<User | undefined>;

    getChildrenForParent(parentUsername: string): Promise<Array<User>>;
    addUpdateChildToParent(parentUsername: string, childUser: User): Promise<User | undefined>;
}
