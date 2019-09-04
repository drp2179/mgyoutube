import { UserCredential } from '../apimodel/usercredential';
import { User } from '../apimodel/user';


export interface UserModule {
    authUser(userCredential: UserCredential): User | undefined;


    getUser(username: string): User | undefined;
    createUser(user: User): User | undefined;
    updateUser(userId: number, user: User): User | undefined;
    removeUser(username: string): User | undefined;


    getChildrenForParent(parentUsername: string): Array<User>;
    addUpdateChildToParent(parentUsername: string, childUser: User): User | undefined;
}
