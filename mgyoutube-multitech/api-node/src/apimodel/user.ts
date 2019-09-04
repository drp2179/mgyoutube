

export class User {
    userId: number;
    username: string;
    password: string | undefined;
    isParent: boolean = false;

    // constructor() {
    //     // this.userId = 0;
    //     // this.username = undefined;
    //     // this.password = null;
    //     // this.isParent = false;
    // }
}

export function cloneUser(userToClone: User): User {
    const clonedUser = new User()

    if (userToClone.userId === undefined) {
        clonedUser.userId = 0;
    }
    else {
        clonedUser.userId = userToClone.userId
    }

    if (userToClone.username !== undefined) {
        clonedUser.username = userToClone.username
    }

    if (userToClone.password !== undefined) {
        clonedUser.password = userToClone.password
    }

    if (userToClone.isParent !== undefined) {
        clonedUser.isParent = userToClone.isParent
    }

    return clonedUser
}
