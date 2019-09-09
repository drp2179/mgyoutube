import { User } from "../apimodel/user"
import { UserCredential } from "../apimodel/usercredential"

export class Helpers {

    public static marshalUserCredentialFromJson(userCredentialJson: object): UserCredential {
        const jsonUserCredentialJson: any = userCredentialJson
        const userCredential = new UserCredential()
        userCredential.username = jsonUserCredentialJson.username;
        userCredential.password = jsonUserCredentialJson.password
        return userCredential
    }


    public static marshalUserFromJson(userJson: object): User {
        const jsonUser: any = userJson
        const user = new User()
        user.username = jsonUser.username;
        user.password = jsonUser.password;
        user.isParent = jsonUser.isParent;
        user.userId = jsonUser.userId;
        return user
    }

}