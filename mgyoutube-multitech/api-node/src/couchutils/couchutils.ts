import { Cluster, Bucket, N1qlQuery } from "couchbase";


export class CouchbaseUtils {

    static async doesBucketExistAsync(bucketName: string, cluster: Cluster): Promise<boolean> {
        return new Promise<boolean>((resolve, reject) => {
            cluster.manager().listBuckets((err: any, results: any[]) => {
                if (err) {
                    reject(err);
                } else {
                    for (var bucket of results) {
                        if (bucket.name === bucketName) {
                            resolve(true);
                        }
                    }

                    resolve(false);
                }
            });
        })
    }

    static async flushBucketAsync(bucketName: string, cluster: Cluster): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            const bucket: Bucket = cluster.openBucket(bucketName);
            console.log("Flushing", bucketName);
            bucket.manager().flush((err: any, results: any) => {
                console.log("Flush of", bucketName, "finished");
                if (err) {
                    reject("Flushing of bucket " + bucketName + " failed: " + err)
                }
                else {
                    resolve();
                }
            });
        });
    }

    static async createBasicBucketAsync(bucketName: string, cluster: Cluster): Promise<Bucket> {
        return new Promise<Bucket>((resolve, reject) => {
            const bucketOps = {
                //authType: 'sasl',
                bucketType: 'couchbase',
                ramQuotaMB: 128,
                replicaNumber: 0,
                saslPassword: '',
                flushEnabled: 1 // not true!?!
            };

            const username = "";
            const password = "";
            cluster.manager(username, password).createBucket(bucketName, bucketOps, (err: any, results: any) => {
                if (err) {
                    reject("Creating bucket '" + bucketName + "' failed: " + err);
                    return;
                }
                console.log("Created bucket", bucketName);
                // const bucket: Bucket = cluster.openBucket(bucketName, 'testing123', (err: any) => {
                const bucket: Bucket = cluster.openBucket(bucketName, '', (err: any) => {
                    if (err) {
                        reject("Opening bucket '" + bucketName + "' failed: " + err);
                        return;
                    }

                    // const queryStr = "CREATE PRIMARY INDEX on `" + bucketName + "`"
                    // bucket.query(N1qlQuery.fromString(queryStr), (err, rows) => {
                    //     if (err) {
                    //         reject("Creating primary index for '" + bucketName + "' failed: " + err);
                    //     }
                    //     else {
                    //         resolve(bucket)
                    //     }
                    // })
                    // const bucketManager = bucket.manager();
                    // console.log("bucketManager", bucketManager);
                    const indexOpts = {
                        name: '',
                        ignoreIfExists: true,
                        deferred: false
                    }
                    const bucketManager = bucket.manager();
                    bucketManager.createPrimaryIndex(indexOpts, (err) => {
                        if (err) {
                            reject("Creating primary index for '" + bucketName + "' failed: " + err);
                        } else {
                            resolve(bucket);
                        }
                    });
                });
            });
        });
    }
}