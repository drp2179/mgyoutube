from couchbase.cluster import Cluster
from couchbase.bucket import Bucket


def doesBucketExist(bucketName: str, cluster: Cluster) -> bool:
    try:
        cluster.open_bucket(bucketName)
        return True
    except:
        return False


def createBasicBucket(bucketName: str, cluster: Cluster, timeout: float = 8.0) -> Bucket:
    cluster.cluster_manager().bucket_create(
        bucketName, 'couchbase', "", 0, 128, True)
    cluster.cluster_manager().wait_ready(bucketName, timeout)
    bucket = cluster.open_bucket(bucketName)
    bucket.bucket_manager().create_n1ql_primary_index()
    return bucket
