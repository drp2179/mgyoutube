package couchfuncs

import (
	"log"

	"gopkg.in/couchbase/gocb.v1"
)

// DoesBucketExist - checsk the manager for the provided bucket name
func DoesBucketExist(clusterManager *gocb.ClusterManager, bucketName string) bool {

	buckets, getBucketsErr := clusterManager.GetBuckets()
	if getBucketsErr != nil {
		log.Println("Unable to get buckets from clusterManager:", getBucketsErr)
		return false
	}

	for _, bucket := range buckets {
		if bucket.Name == bucketName {
			return true
		}
	}

	return false
}

// CreateBasicBucket - create a basic bucket
func CreateBasicBucket(clusterManager *gocb.ClusterManager, bucketName string) error {
	bucketSettings := gocb.BucketSettings{}
	bucketSettings.Type = gocb.Couchbase
	bucketSettings.Name = bucketName
	bucketSettings.FlushEnabled = true
	bucketSettings.Quota = 128          // MB, necessary else it blows up for being less than 100MB
	bucketSettings.Password = ""        // without this,
	bucketSettings.Replicas = 1         // and this
	bucketSettings.IndexReplicas = true // and this, opening after insert results in "no access"

	insertBucketErr := clusterManager.InsertBucket(&bucketSettings)
	if insertBucketErr != nil {
		log.Println("Unable to insert", bucketName, " bucket into clusterManager:", insertBucketErr)
		return insertBucketErr
	}

	return nil
}
