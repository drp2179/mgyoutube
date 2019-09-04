import { User } from '../apimodel/user';


export interface UserDataRepo {

    getUserByUsername(username: string): User | undefined;

    addUser(user: User): User;

    // updateUser(userId, user) {
    //     return this.userDataRepo.replaceUser(userId, user);
    // }


    removeUser(user: User): void;

    replaceUser(userId: number, user: User): User | undefined;


    addChildToParent(parentUserId: number, childUserId: number): void;


    getChildrenForParent(parentUserId: number): Array<User>;

}
