import { User } from '../apimodel/user';


export interface UserDataRepo {

    repositoryStartup(): Promise<void>;

    getUserByUsername(username: string): Promise<User | undefined>;

    addUser(user: User): Promise<User | undefined>;

    // updateUser(userId, user) {
    //     return this.userDataRepo.replaceUser(userId, user);
    // }


    removeUser(user: User): Promise<void>;

    replaceUser(userId: string, user: User): Promise<User | undefined>;


    addChildToParent(parentUserId: string, childUserId: string): Promise<void>;


    getChildrenForParent(parentUserId: string): Promise<Array<User>>;

}
