import { User, cloneUser } from '../apimodel/user';
import { UserDataRepo } from './userdatarepo';
import { Cluster, Bucket, N1qlQuery } from 'couchbase';
import uuidv4 = require('uuid/v4');

const USERS_BUCKET_NAME = "users";
const USERS_FIND_BY_USERNAME = "SELECT META(users).id, users.* FROM users WHERE username = $1";
const USERS_USERNAME_INDEX_NAME = "users-username-idx";
const USERS_BUCKET_ID_FIELDNAME = "id";
const USERS_BUCKET_USERNAME_FIELDNAME = "username";
const USERS_BUCKET_PASSWORD_FIELDNAME = "password";
const USERS_BUCKET_PARENT_FIELDNAME = "isParent";

const PARENT_CHILD_BUCKET_NAME = "parentChild";
const PARENT_CHILD_FIND_BY_PARENT = "SELECT childUserId FROM parentChild WHERE parentUserId = $1";
const PARENT_CHILD_BUCKET_PARENT_INDEX_NAME = "parnetchild-parent-idx";
const PARENT_CHILD_BUCKET_PARENT_FIELDNAME = "parentUserId";
const PARENT_CHILD_BUCKET_CHILD_FIELDNAME = "childUserId";


export class CouchUserDataRepoImpl implements UserDataRepo {

    private cluster: Cluster;

    constructor(connectionString: string, username: string, password: string) {
        this.cluster = new Cluster(connectionString);
        this.cluster.authenticate(username, password);
    }

    async repositoryStartup(): Promise<void> {
        const paraThis = this;

        return new Promise<void>((resolve, reject) => {
            const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
            console.log("Flushing ", USERS_BUCKET_NAME);
            usersBucket.manager().flush(() => {
                console.log("flush of ", USERS_BUCKET_NAME, " finished.");

                const parentChildBucket: Bucket = paraThis.cluster.openBucket(PARENT_CHILD_BUCKET_NAME);
                console.log("Flushing ", PARENT_CHILD_BUCKET_NAME);
                parentChildBucket.manager().flush(() => {
                    console.log("flush of ", PARENT_CHILD_BUCKET_NAME, " finished.");
                    resolve();
                });
            });
        });
    }

    async getUserByUsername(username: string): Promise<User | undefined> {
        const paraThis = this;

        return new Promise<User | undefined>((resolve, reject) => {
            const query = N1qlQuery.fromString(USERS_FIND_BY_USERNAME);
            query.consistency(N1qlQuery.Consistency.REQUEST_PLUS);

            const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
            usersBucket.query(query, [username], (err, rows) => {

                if (err) {
                    reject(err);
                    return;
                }

                if (!rows) {
                    reject(new Error("no error but rows is undefined in getUserByUsername " + username));
                    return;
                }

                if (rows.length === 0) {
                    resolve(undefined);
                } else {
                    const row = rows[0];

                    const user = new User();

                    user.userId = row[USERS_BUCKET_ID_FIELDNAME];
                    user.username = row[USERS_BUCKET_USERNAME_FIELDNAME];
                    user.password = row[USERS_BUCKET_PASSWORD_FIELDNAME];
                    user.isParent = row[USERS_BUCKET_PARENT_FIELDNAME];

                    console.log("CouchUserDataRepoImpl.getUserByUsername(", username, ") returning ", user);
                    resolve(user);
                }
            });
        });
    }

    async addUser(user: User): Promise<User | undefined> {
        const paraThis = this;

        return new Promise<User | undefined>((resolve, reject) => {
            const id = uuidv4();
            const content: any = {}
            content[USERS_BUCKET_USERNAME_FIELDNAME] = user.username;
            content[USERS_BUCKET_PASSWORD_FIELDNAME] = user.password;
            content[USERS_BUCKET_PARENT_FIELDNAME] = user.isParent;

            const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
            usersBucket.insert(id, content, async (err) => {
                console.log("CouchUserDataRepoImpl.addUser " + user.username + ", docId: " + id + " err:", err);

                if (err) {
                    reject(err);
                }
                else {
                    const foundAddedUser = await paraThis.getUserByUsername(user.username);
                    resolve(foundAddedUser);
                }
            });
        });
    }

    async removeUser(user: User): Promise<void> {
        const paraThis = this;

        return new Promise<void>((resolve, reject) => {
            const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
            usersBucket.remove(user.userId, (err) => {
                if (err) {
                    reject(err);
                }
                else {
                    resolve();
                }
            });
        });
    }

    async replaceUser(userId: string, user: User): Promise<User | undefined> {
        const paraThis = this;

        const userById = await this.getUserById(userId);

        if (userById !== undefined) {
            const updatedUserDoc: any = {}
            updatedUserDoc[USERS_BUCKET_USERNAME_FIELDNAME] = user.username;
            updatedUserDoc[USERS_BUCKET_PASSWORD_FIELDNAME] = user.password;
            if (user.isParent === undefined) {
                updatedUserDoc[USERS_BUCKET_PARENT_FIELDNAME] = false;
            }
            else {
                updatedUserDoc[USERS_BUCKET_PARENT_FIELDNAME] = user.isParent;
            }

            return new Promise<User | undefined>((resolve, reject) => {
                const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
                usersBucket.replace(userId, updatedUserDoc, async (err) => {
                    if (err) {
                        reject(err);
                    } else {
                        const foundReplacedUser = await paraThis.getUserById(userId);
                        resolve(foundReplacedUser);
                    }
                });
            });
        }

        return undefined;
    }


    async addChildToParent(parentUserId: string, childUserId: string): Promise<void> {
        const paraThis = this;

        return new Promise<void>((resolve, reject) => {
            // TODO should probably worry about dups here...
            const id = uuidv4();
            const document: any = {}
            document[PARENT_CHILD_BUCKET_PARENT_FIELDNAME] = parentUserId;
            document[PARENT_CHILD_BUCKET_CHILD_FIELDNAME] = childUserId;

            console.log("CouchUserDataRepoImpl.addChildToParent(" + parentUserId + ", " + childUserId + ")");
            const parentChildBucket: Bucket = paraThis.cluster.openBucket(PARENT_CHILD_BUCKET_NAME);
            parentChildBucket.insert(id, document, (err) => {
                if (err) {
                    reject(err);
                } else {
                    resolve();
                }
            });
        });
    }

    async getChildrenForParent(parentUserId: string): Promise<Array<User>> {
        const paraThis = this;
        return new Promise<User[]>((resolve, reject) => {
            const children: User[] = []
            const childrenIds: string[] = []

            const query = N1qlQuery.fromString(PARENT_CHILD_FIND_BY_PARENT);
            query.consistency(N1qlQuery.Consistency.REQUEST_PLUS);

            const parentChildBucket: Bucket = paraThis.cluster.openBucket(PARENT_CHILD_BUCKET_NAME);
            parentChildBucket.query(query, [parentUserId], async (err, rows) => {
                if (err) {
                    reject(err);
                    return;
                } else if (!rows) {
                    reject(new Error("no error but rows undefined in getChildrenForParent " + parentUserId));
                    return;
                }

                console.log("CouchUserDataRepoImpl.getChildrenForParent", parentUserId, "rows", rows);

                rows.forEach((row) => {
                    const childUserId = row[PARENT_CHILD_BUCKET_CHILD_FIELDNAME];
                    childrenIds.push(childUserId);
                });

                for (var cid of childrenIds) {
                    console.log("CouchUserDataRepoImpl.getChildrenForParent, getting child by id ", cid);
                    const childUser = await paraThis.getUserById(cid)
                    if (childUser) {
                        children.push(childUser);
                    }
                    else {
                        console.warn("unknown child userid ", cid);
                    }
                }

                resolve(children);
            });
        });
    }

    // probably will be public eventually
    async getUserById(userId: string): Promise<User | undefined> {
        const paraThis = this;

        return new Promise<User | undefined>((resolve, reject) => {
            console.log("CouchUserDataRepoImpl.getUserById(" + userId + ")")

            const usersBucket: Bucket = paraThis.cluster.openBucket(USERS_BUCKET_NAME);
            usersBucket.get(userId, (err, result) => {
                if (err) {
                    reject(err);
                    return;
                }

                if (!result) {
                    console.log("unable to find a result for userid " + userId);
                    resolve(undefined);
                    return;
                }
                if (!result.value) {
                    console.log("unable to find a result.value for userid " + userId);
                    resolve(undefined);
                    return;
                }

                const user = new User();

                user.userId = userId;
                user.username = result.value[USERS_BUCKET_USERNAME_FIELDNAME];
                user.password = result.value[USERS_BUCKET_PASSWORD_FIELDNAME];
                user.isParent = result.value[USERS_BUCKET_PARENT_FIELDNAME];
                console.log("CouchUserDataRepoImpl.getUserById(", userId, ") returning ", user);
                resolve(user);
            });
        });
    }
}
