using Couchbase.Configuration.Server.Serialization;
using Couchbase.Management;

namespace api_dotnet.couchutils
{


    public class CouchUtils
    {
        public static bool DoesBucketExist(IClusterManager clusterManager, string bucketName)
        {
            var bucketsResult = clusterManager.ListBuckets();
            foreach (BucketConfig b in bucketsResult.Value)
            {
                if (b.Name == bucketName)
                {
                    return true;
                }
            }

            return false;
        }
    }

}