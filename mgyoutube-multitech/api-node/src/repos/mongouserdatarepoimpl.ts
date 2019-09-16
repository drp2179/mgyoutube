import { User, cloneUser } from '../apimodel/user';
import { UserDataRepo } from './userdatarepo';
import { MongoClient, ObjectID, Db } from 'mongodb';

const USERS_COLLECTION_NAME = "users";
const USERS_COLLECTION_USERNAME_FIELDNAME = "username";
const USERS_COLLECTION_PASSWORD_FIELDNAME = "password";
const USERS_COLLECTION_PARENT_FIELDNAME = "isParent";

const PARENT_CHILD_COLLECTION_NAME = "parentChild";
const PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId";
const PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId";

const MONGO_ID_FIELDNAME = "_id";


export class MongoUserDataRepoImpl implements UserDataRepo {

    private client: MongoClient;
    private database: Db;
    private connectionString: string;
    private databaseName: string;

    constructor(connectionString: string, databaseName: string) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    async connectDb() {
        const paraThis = this;
        await MongoClient.connect(this.connectionString, { useNewUrlParser: true, useUnifiedTopology: true }).then(client => {
            paraThis.client = client;
            paraThis.database = paraThis.client.db(this.databaseName);
        }).catch(err => {
            throw new Error(err);
        });
    }

    async repositoryStartup(): Promise<void> {
        await this.connectDb();

        const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
        if (usersCollection != null) {
            const numDocs = await usersCollection.countDocuments();
            if (numDocs > 0) {
                await this.database.dropCollection(USERS_COLLECTION_NAME);
            }
        }
        const parentChildCollection = this.database.collection(PARENT_CHILD_COLLECTION_NAME);
        if (parentChildCollection != null) {
            const numDocs = await parentChildCollection.countDocuments();
            if (numDocs > 0) {
                await this.database.dropCollection(PARENT_CHILD_COLLECTION_NAME);
            }
        }
    }

    async getUserByUsername(username: string): Promise<User | undefined> {
        const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
        const filterDoc: any = {}
        filterDoc[USERS_COLLECTION_USERNAME_FIELDNAME] = username;

        const userDoc = await usersCollection.findOne(filterDoc);

        if (userDoc == null) {
            return undefined;
        }

        const user = new User();

        //const objectId: ObjectID = userDoc.getObjectId(MONGO_ID_FIELDNAME);
        const objectId: ObjectID = userDoc[MONGO_ID_FIELDNAME];
        user.userId = objectId.toHexString();

        user.username = userDoc[USERS_COLLECTION_USERNAME_FIELDNAME];
        user.password = userDoc[USERS_COLLECTION_PASSWORD_FIELDNAME];
        user.isParent = userDoc[USERS_COLLECTION_PARENT_FIELDNAME];

        console.log("MongoUserDataRepoImpl.getUserByUsername(", username, ") returning ", user);
        return user;
    }

    async addUser(user: User): Promise<User | undefined> {
        const newUserDoc: any = {}
        newUserDoc[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;
        newUserDoc[USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password;
        if (user.isParent === undefined) {
            newUserDoc[USERS_COLLECTION_PARENT_FIELDNAME] = false;
        }
        else {
            newUserDoc[USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent;
        }

        const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
        await usersCollection.insertOne(newUserDoc);

        return this.getUserByUsername(user.username);
    }

    async removeUser(user: User): Promise<void> {
        const docFilter: any = {}
        docFilter[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;

        const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
        await usersCollection.deleteOne(docFilter);
    }

    async replaceUser(userId: string, user: User): Promise<User | undefined> {

        const userById = await this.getUserById(userId);

        if (userById !== undefined) {
            const updatedUserDoc: any = {}
            updatedUserDoc[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;
            updatedUserDoc[USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password;
            if (user.isParent === undefined) {
                updatedUserDoc[USERS_COLLECTION_PARENT_FIELDNAME] = false;
            }
            else {
                updatedUserDoc[USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent;
            }

            const filterDoc: any = {}
            filterDoc[MONGO_ID_FIELDNAME] = ObjectID.createFromHexString(userById.userId)

            const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
            await usersCollection.replaceOne(filterDoc, updatedUserDoc);

            return this.getUserById(userId);
        }

        return undefined;
    }


    async addChildToParent(parentUserId: string, childUserId: string): Promise<void> {
        // TODO should probably worry about dups here...
        const document: any = {}
        document[PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId;
        document[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME] = childUserId;

        console.log("addChildToParent(" + parentUserId + ", " + childUserId + ")");
        const parentChildCollection = this.database.collection(PARENT_CHILD_COLLECTION_NAME);
        await parentChildCollection.insertOne(document);
    }

    // TODO: extract code for getting childrenIds into separate method to be used by addChildToParent to eliminate possible duplicate adds
    async getChildrenForParent(parentUserId: string): Promise<Array<User>> {
        const children: User[] = []
        const childrenIds: string[] = []
        const parThis = this;
        const filterDoc: any = {}
        filterDoc[PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId;

        console.log("getChildrenForParent(" + parentUserId + "), childrenIds before search:", childrenIds);
        const parentChildCollection = this.database.collection(PARENT_CHILD_COLLECTION_NAME);
        await parentChildCollection.find(filterDoc).forEach(d => {
            const childUserId = d[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME];
            childrenIds.push(childUserId);
        });

        console.log("getChildrenForParent(" + parentUserId + "), childrenIds after search:", childrenIds);

        // can't use childrenIds.forEach due to the lack of async calling
        for (var cid of childrenIds) {
            console.log("getChildrenForParent, getting child by id ", cid);
            const childUser = await this.getUserById(cid)
            if (childUser !== undefined) {
                children.push(childUser);
            }
            else {
                console.warn("unknown child userid ", cid);
            }
        }

        return children;
    }

    // probably will be public eventually
    async getUserById(userId: string): Promise<User | undefined> {
        console.log("getUserById(" + userId + ")")
        console.log("user id is ", userId.length, "chars long")

        const filterDoc: any = {}
        filterDoc[MONGO_ID_FIELDNAME] = ObjectID.createFromHexString(userId);

        const usersCollection = this.database.collection(USERS_COLLECTION_NAME);
        const userDoc = await usersCollection.findOne(filterDoc);

        if (userDoc == undefined) {
            return undefined;
        }

        const user = new User();

        //const objectId: ObjectID = userDoc.getObjectId(MONGO_ID_FIELDNAME);
        const objectId: ObjectID = userDoc[MONGO_ID_FIELDNAME];
        user.userId = objectId.toHexString();
        user.username = userDoc[USERS_COLLECTION_USERNAME_FIELDNAME];
        user.password = userDoc[USERS_COLLECTION_PASSWORD_FIELDNAME];
        user.isParent = userDoc[USERS_COLLECTION_PARENT_FIELDNAME];
        console.log("MongoUserDataRepoImpl.getUserById(", userId, ") returning ", user);
        return user;
    }
}
