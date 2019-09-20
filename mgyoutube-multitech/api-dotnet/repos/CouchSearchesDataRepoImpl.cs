using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.couchutils;
using Couchbase;
using Couchbase.Authentication;
using Couchbase.Configuration.Client;
using Couchbase.Core;
using Couchbase.Core.Buckets;
using Couchbase.Management;
using Couchbase.N1QL;

namespace api_dotnet.repos
{
    public class CouchSearchesDataRepoImpl : SearchesDataRepo
    {
        private readonly Cluster cluster;
        private readonly string username;
        private readonly string password;

        public const string SEARCHES_BUCKET_NAME = "searches";
        public const string SEARCHES_PARENT_INDEX_NAME = "searches-parentuserid-idx";
        public const string SEARCHES_BUCKET_PARENT_FIELDNAME = "parentUserId";
        public const string SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME = "phrase";

        public const string SEARCHES_FIND_PHRASE_BY_PARENT = "SELECT phrase FROM `searches` WHERE parentUserId = $1";
        public const string SEARCHES_DELETE_PHRASE_AND_PARENT = "DELETE FROM `searches` WHERE parentUserId = $1 AND phrase = $2";

        public CouchSearchesDataRepoImpl(string connectionString, string username, string password)
        {
            this.username = username;
            this.password = password;
            this.cluster = new Cluster(new ClientConfiguration
            {
                Servers = new List<Uri> { new Uri(connectionString) }
            });

            IAuthenticator authenticator = new PasswordAuthenticator(username, password);
            cluster.Authenticate(authenticator);
        }
        public void RepositoryStartup()
        {
            Console.WriteLine("CouchSearchesDataRepoImpl.RepositoryStartup");

            IClusterManager clusterManager = this.cluster.CreateManager(this.username, this.password);

            if (!CouchUtils.DoesBucketExist(clusterManager, SEARCHES_BUCKET_NAME))
            {
                IResult createBucketResults = clusterManager.CreateBucket(SEARCHES_BUCKET_NAME,
                    bucketType: BucketTypeEnum.Couchbase,
                    ramQuota: 128, // megabytes
                    replicaNumber: ReplicaNumber.One,
                    indexReplicas: true,
                    flushEnabled: true);

                if (!createBucketResults.Success)
                {
                    throw new Exception(createBucketResults.Message);
                }
                IBucket searchesBucket = cluster.OpenBucket(SEARCHES_BUCKET_NAME);
                IBucketManager searchesBucketManager = searchesBucket.CreateManager(this.username, this.password);
                IResult createPrimaryIndexResult = searchesBucketManager.CreateN1qlPrimaryIndex(false);
                if (!createPrimaryIndexResult.Success)
                {
                    throw new Exception(createPrimaryIndexResult.Message);
                }
                string[] fields = { SEARCHES_BUCKET_PARENT_FIELDNAME };
                IResult createParentsIndexResult = searchesBucketManager.CreateN1qlIndex(SEARCHES_PARENT_INDEX_NAME, false, fields);
                if (!createParentsIndexResult.Success)
                {
                    throw new Exception(createParentsIndexResult.Message);
                }
            }
            else
            {
                IBucket searchesBucket = cluster.OpenBucket(SEARCHES_BUCKET_NAME);
                IBucketManager searchesBucketManager = searchesBucket.CreateManager(this.username, this.password);
                Console.WriteLine("flushing " + SEARCHES_BUCKET_NAME);
                IResult flushResults = searchesBucketManager.Flush();
                if (!flushResults.Success)
                {
                    throw new Exception(flushResults.Message);
                }
            }
        }

        public async Task AddSearchToParentUser(string parentUserId, string searchPhrase)
        {
            string id = Guid.NewGuid().ToString();
            Document<dynamic> document = new Document<dynamic> { Id = id, Content = new { parentUserId = parentUserId, phrase = searchPhrase } };

            IBucket searchesBucket = cluster.OpenBucket(SEARCHES_BUCKET_NAME);
            IDocumentResult result = await searchesBucket.InsertAsync(document);

            Console.WriteLine("Couchdb inserted parentUserId " + parentUserId + " and searchPhrase " + searchPhrase
                    + ", docId: " + id + " result.Message: " + result.Message);
        }

        public async Task<List<string>> GetSearchesForParentUser(string parentUserId)
        {
            Console.WriteLine("Couchdb searching " + SEARCHES_BUCKET_NAME + " for parentUserId " + parentUserId);

            List<string> searches = new List<string>();

            QueryRequest query = new QueryRequest(SEARCHES_FIND_PHRASE_BY_PARENT);
            query.ScanConsistency(ScanConsistency.RequestPlus);
            query.AddPositionalParameter(parentUserId);

            IBucket searchesBucket = cluster.OpenBucket(SEARCHES_BUCKET_NAME);
            using (IQueryResult<dynamic> results = await searchesBucket.QueryAsync<dynamic>(query))
            {
                if (!results.Success)
                {
                    Console.WriteLine("Couchdb failed to search for " + parentUserId + " Message: " + results.Message + " Status:"
                            + results.Status);
                    results.Errors.ForEach(e =>
                    {
                        Console.WriteLine("Error Code: " + e.Code + " Severity: " + e.Severity + " Name: " + e.Name + " Message: " + e.Message);
                    });

                    Console.WriteLine("results.Exception: " + results.Exception);
                    if (results.Exception != null)
                    {
                        throw results.Exception;
                    }
                    throw new Exception("Unable to get any searches for parentUserId " + parentUserId);
                }

                foreach (dynamic row in results)
                {
                    string phrase = row.phrase;
                    searches.Add(phrase);
                }
            }

            Console.WriteLine("Couchdb searching " + SEARCHES_BUCKET_NAME + " for parentUserId " + parentUserId
                    + " returning " + searches.Count + " phrases");
            return searches;
        }

        public async Task RemoveSearchFromParentUser(string parentUserId, string searchPhrase)
        {
            QueryRequest query = new QueryRequest(SEARCHES_DELETE_PHRASE_AND_PARENT);
            query.ScanConsistency(ScanConsistency.RequestPlus);
            query.AddPositionalParameter(parentUserId);
            query.AddPositionalParameter(searchPhrase);
            IBucket searchesBucket = cluster.OpenBucket(SEARCHES_BUCKET_NAME);

            using (IQueryResult<dynamic> results = await searchesBucket.QueryAsync<dynamic>(query))
            {
                Console.WriteLine("Couchdb deleted parentUserId " + parentUserId + " and searchPhrase " + searchPhrase + ": "
                        + results.Status);
            }
        }
    }
}