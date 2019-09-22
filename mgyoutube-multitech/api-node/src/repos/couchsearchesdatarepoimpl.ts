import { SearchesDataRepo } from "./searchesdatarepo";
import { Cluster, Bucket, N1qlQuery } from "couchbase";

import uuidv4 = require('uuid/v4');
import { CouchbaseUtils } from "../couchutils/couchutils";



const SEARCHES_BUCKET_NAME = "searches";
const SEARCHES_PARENT_INDEX_NAME = "searches-parentuserid-idx";
const SEARCHES_BUCKET_PARENT_FIELDNAME = "parentUserId";
const SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME = "phrase";

const SEARCHES_FIND_PHRASE_BY_PARENT = "SELECT phrase FROM `searches` WHERE parentUserId = $1";
const SEARCHES_DELETE_PHRASE_AND_PARENT = "DELETE FROM `searches` WHERE parentUserId = $1 AND phrase = $2";

export class CouchSearchesDataRepoImpl implements SearchesDataRepo {
    private cluster: Cluster;

    constructor(connectionString: string, username: string, password: string) {
        this.cluster = new Cluster(connectionString);
        this.cluster.authenticate(username, password);
    }

    async repositoryStartup(): Promise<void> {
        const paraThis = this;

        return new Promise<void>(async (resolve, reject) => {
            const bucketExists = await CouchbaseUtils.doesBucketExistAsync(SEARCHES_BUCKET_NAME, paraThis.cluster)
            if (!bucketExists) {
                const searchsBucket = await CouchbaseUtils.createBasicBucketAsync(SEARCHES_BUCKET_NAME, paraThis.cluster);
                searchsBucket.manager().createIndex(SEARCHES_PARENT_INDEX_NAME, [SEARCHES_BUCKET_PARENT_FIELDNAME], (err) => {
                    if (err) {
                        reject("createIndex for " + SEARCHES_PARENT_INDEX_NAME + " failed:" + err);
                    }
                    else {
                        resolve();
                    }
                })
            }
            else {
                await CouchbaseUtils.flushBucketAsync(SEARCHES_BUCKET_NAME, paraThis.cluster);
                resolve();
            }
        });
    }

    async getSearchesForParentUser(parentUserId: string): Promise<string[]> {
        return new Promise<string[]>((resolve, reject) => {
            const query = N1qlQuery.fromString(SEARCHES_FIND_PHRASE_BY_PARENT);
            query.consistency(N1qlQuery.Consistency.REQUEST_PLUS);

            const searchsBucket: Bucket = this.cluster.openBucket(SEARCHES_BUCKET_NAME);
            searchsBucket.query(query, [parentUserId], (err, rows) => {
                const searches: string[] = [];
                if (err) {
                    reject(err);
                    return;
                }

                if (!rows) {
                    reject(new Error("no error but rows is not defined for getSearchesForParentUser " + parentUserId));
                    return
                }

                rows.forEach((row: any) => {
                    const phrase: string = row.phrase;
                    searches.push(phrase);
                });

                resolve(searches);
            });
        });
    };

    async addSearchToParentUser(parentUserId: string, searchPhrase: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            const id = uuidv4();

            const document: any = {}
            document[SEARCHES_BUCKET_PARENT_FIELDNAME] = parentUserId;
            document[SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME] = searchPhrase;

            const searchesBucket: Bucket = this.cluster.openBucket(SEARCHES_BUCKET_NAME);
            searchesBucket.insert(id, document, (err) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve();
            });
        });
    }

    async removeSearchFromParentUser(parentUserId: string, searchPhrase: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            const query = N1qlQuery.fromString(SEARCHES_DELETE_PHRASE_AND_PARENT);
            query.consistency(N1qlQuery.Consistency.REQUEST_PLUS);

            const searchsBucket: Bucket = this.cluster.openBucket(SEARCHES_BUCKET_NAME);
            searchsBucket.query(query, [parentUserId, searchPhrase], (err) => {
                if (err) {
                    reject(err);
                    return;
                }

                resolve();
            });
        });
    }
}