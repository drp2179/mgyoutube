

export class UserNotFoundException extends Error {
    static NAME: string = "UserNotFoundException";

    username: string;

    constructor(username: string, message?: string) {
        super(message);
        this.name = UserNotFoundException.NAME;
        this.username = username;
    }
}
