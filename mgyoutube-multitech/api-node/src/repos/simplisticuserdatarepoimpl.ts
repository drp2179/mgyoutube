import { User, cloneUser } from '../apimodel/user';
import { UserDataRepo } from './userdatarepo';


export class SimplisticUserDataRepoImpl implements UserDataRepo {
    private usernameMap: Map<string, User> = new Map()
    private userIdMap: Map<string, User> = new Map()
    private parentChildrenMap: Map<string, Array<string>> = new Map()
    private nextUserId: number = 1

    constructor() { }

    async repositoryStartup(): Promise<void> {
        // nothing to do here
    }

    async getUserByUsername(username: string): Promise<User | undefined> {
        const user = this.usernameMap.get(username)
        if (user !== undefined) {
            // cloning so modds after return do not affect maps
            return cloneUser(user)
        }
        return user
    }

    async addUser(user: User): Promise<User | undefined> {
        // cloning so that we can mutate userId without affecting the input object
        const addedUser = cloneUser(user)
        addedUser.userId = this.nextUserId.toString();
        this.nextUserId++

        this.userIdMap.set(addedUser.userId, addedUser)
        this.usernameMap.set(addedUser.username, addedUser)

        // cloning so that mods after return do not mutate maps
        // getUserById does the cloning
        return this.getUserById(addedUser.userId);
    }

    async removeUser(user: User): Promise<void> {
        this.userIdMap.delete(user.userId);
        this.usernameMap.delete(user.username);
    }

    async replaceUser(userId: string, user: User): Promise<User | undefined> {

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


    async addChildToParent(parentUserId: string, childUserId: string): Promise<void> {
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

    async getChildrenForParent(parentUserId: string): Promise<Array<User>> {
        const children: Array<User> = []

        if (this.parentChildrenMap.has(parentUserId)) {
            const childrenUserIds = this.parentChildrenMap.get(parentUserId);

            if (childrenUserIds !== undefined) {
                for (var childUserId of childrenUserIds) {
                    const user = await this.getUserById(childUserId);
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
    async getUserById(userId: string): Promise<User | undefined> {
        const foundUser = this.userIdMap.get(userId);
        if (foundUser === undefined) {
            return undefined;
        }
        return cloneUser(foundUser);
    }
}
