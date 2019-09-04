import { User, cloneUser } from '../apimodel/user';
import { UserDataRepo } from './userdatarepo';


export class SimplisticUserDataRepoImpl implements UserDataRepo {
    private usernameMap: Map<string, User> = new Map()
    private userIdMap: Map<number, User> = new Map()
    private parentChildrenMap: Map<number, Array<number>> = new Map()
    private nextUserId: number = 1

    // constructor() {
    //     this.usernameMap = new Map()
    //     this.userIdMap = new Map()
    //     this.parentChildrenMap = new Map()
    //     this.nextUserId = 1
    // }

    getUserByUsername(username: string): User | undefined {
        const user = this.usernameMap.get(username)
        if (user !== undefined) {
            // cloning so modds after return do not affect maps
            return cloneUser(user)
        }
        return user
    }

    addUser(user: User): User {
        // cloning so that we can mutate userId without affecting the input object
        const addedUser = cloneUser(user)
        addedUser.userId = this.nextUserId++

        this.userIdMap.set(addedUser.userId, addedUser)
        this.usernameMap.set(addedUser.username, addedUser)

        // cloning so that mods after return do not mutate maps
        return cloneUser(addedUser)
    }

    removeUser(user: User): void {
        this.userIdMap.delete(user.userId);
        this.usernameMap.delete(user.username);
    }

    replaceUser(userId: number, user: User): User | undefined {

        const existingUser = this.userIdMap.get(userId);

        if (existingUser !== undefined) {
            // cloning so that we can mutate userId without affecting the input object
            const replacingUser = cloneUser(user);
            replacingUser.userId = userId;

            this.userIdMap.set(replacingUser.userId, replacingUser);
            this.usernameMap.set(replacingUser.username, replacingUser);

            // cloning so that mods after return do not mutate maps
            return cloneUser(replacingUser);
        }

        return undefined;
    }


    addChildToParent(parentUserId: number, childUserId: number) {
        if (!this.parentChildrenMap.has(parentUserId)) {
            this.parentChildrenMap.set(parentUserId, []);
        }

        const childrenUserIds = this.parentChildrenMap.get(parentUserId);

        if (childrenUserIds === undefined) {
            console.error("unable to find child user ides for parent ", parentUserId);
        }
        else if (!childrenUserIds.includes(childUserId)) {
            childrenUserIds.push(childUserId);
        }
    }

    getChildrenForParent(parentUserId: number): Array<User> {
        const children: Array<User> = []

        if (this.parentChildrenMap.has(parentUserId)) {
            const childrenUserIds = this.parentChildrenMap.get(parentUserId);

            if (childrenUserIds !== undefined) {
                for (var childUserId of childrenUserIds) {
                    const user = this.getUserById(childUserId);
                    if (user !== undefined) {
                        children.push(user);
                    } else {
                        console.warn("unknown child userid ", childUserId);
                    }
                }
            }
        }

        return children;
    }

    // probably will be public eventually
    getUserById(userId: number): User | undefined {
        return this.userIdMap.get(userId);
    }
}
