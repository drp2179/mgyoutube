

export class User {
    userId: string;
    username: string;
    password: string | undefined;
    isParent: boolean = false;
}

export function cloneUser(userToClone: User): User {
    const clonedUser = new User()

    if (userToClone.userId !== undefined) {
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
