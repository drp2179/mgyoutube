using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.couchutils;
using Couchbase;
using Couchbase.Authentication;
using Couchbase.Configuration.Client;
using Couchbase.Core;
using Couchbase.Core.Buckets;
using Couchbase.N1QL;

namespace api_dotnet.repos
{
    public class CouchUserDataRepoImpl : UserDataRepo
    {
        private readonly Cluster cluster;
        private readonly string username;
        private readonly string password;

        public const string USERS_BUCKET_NAME = "users";
        public const string USERS_FIND_BY_USERNAME = "SELECT META(users).id, users.* FROM users WHERE username = $1";
        public const string USERS_USERNAME_INDEX_NAME = "users-username-idx";
        public const string USERS_BUCKET_ID_FIELDNAME = "id";
        public const string USERS_BUCKET_USERNAME_FIELDNAME = "username";
        public const string USERS_BUCKET_PASSWORD_FIELDNAME = "password";
        public const string USERS_BUCKET_PARENT_FIELDNAME = "isParent";

        public const string PARENT_CHILD_BUCKET_NAME = "parentChild";
        public const string PARENT_CHILD_FIND_BY_PARENT = "SELECT childUserId FROM parentChild WHERE parentUserId = $1";
        public const string PARENT_CHILD_PARENT_INDEX_NAME = "parnetchild-parent-idx";
        public const string PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId";
        public const string PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId";

        public CouchUserDataRepoImpl(string connectionString, string username, string password)
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
            Console.WriteLine("CouchUserDataRepoImpl.RepositoryStartup");
            Couchbase.Management.IClusterManager clusterManager = this.cluster.CreateManager(this.username, this.password);

            {
                if (!CouchUtils.DoesBucketExist(clusterManager, USERS_BUCKET_NAME))
                {
                    IResult createBucketResults = clusterManager.CreateBucket(USERS_BUCKET_NAME,
                        bucketType: BucketTypeEnum.Couchbase,
                        ramQuota: 128, // megabytes
                        replicaNumber: Couchbase.Management.ReplicaNumber.One,
                        indexReplicas: true,
                        flushEnabled: true);

                    if (!createBucketResults.Success)
                    {
                        throw new Exception(createBucketResults.Message);
                    }
                    IBucket usersBucket = cluster.OpenBucket(USERS_BUCKET_NAME);
                    Couchbase.Management.IBucketManager usersBucketManager = usersBucket.CreateManager(this.username, this.password);
                    IResult createPrimaryIndexResult = usersBucketManager.CreateN1qlPrimaryIndex(false);
                    if (!createPrimaryIndexResult.Success)
                    {
                        throw new Exception(createPrimaryIndexResult.Message);
                    }
                    string[] fields = { USERS_BUCKET_USERNAME_FIELDNAME };
                    IResult createParentsIndexResult = usersBucketManager.CreateN1qlIndex(USERS_USERNAME_INDEX_NAME, false, fields);
                    if (!createParentsIndexResult.Success)
                    {
                        throw new Exception(createParentsIndexResult.Message);
                    }
                }
                else
                {
                    IBucket usersBucket = cluster.OpenBucket(USERS_BUCKET_NAME);
                    Couchbase.Management.IBucketManager usersBucketManager = usersBucket.CreateManager(this.username, this.password);
                    Console.WriteLine("flushing " + USERS_BUCKET_NAME);
                    IResult flushResults = usersBucketManager.Flush();
                    if (!flushResults.Success)
                    {
                        throw new Exception(flushResults.Message);
                    }
                }
            }
            {
                if (!CouchUtils.DoesBucketExist(clusterManager, PARENT_CHILD_BUCKET_NAME))
                {
                    IResult createBucketResults = clusterManager.CreateBucket(PARENT_CHILD_BUCKET_NAME,
                        bucketType: BucketTypeEnum.Couchbase,
                        ramQuota: 128, // megabytes
                        replicaNumber: Couchbase.Management.ReplicaNumber.One,
                        indexReplicas: true,
                        flushEnabled: true);

                    if (!createBucketResults.Success)
                    {
                        throw new Exception(createBucketResults.Message);
                    }
                    IBucket parentChildBucket = cluster.OpenBucket(PARENT_CHILD_BUCKET_NAME);
                    Couchbase.Management.IBucketManager parentChildBucketManager = parentChildBucket.CreateManager(this.username, this.password);
                    IResult createPrimaryIndexResult = parentChildBucketManager.CreateN1qlPrimaryIndex(false);
                    if (!createPrimaryIndexResult.Success)
                    {
                        throw new Exception(createPrimaryIndexResult.Message);
                    }
                    string[] fields = { PARENT_CHILD_PARENT_INDEX_NAME };
                    IResult createParentsIndexResult = parentChildBucketManager.CreateN1qlIndex(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, false, fields);
                    if (!createParentsIndexResult.Success)
                    {
                        throw new Exception(createParentsIndexResult.Message);
                    }
                }
                else
                {
                    IBucket parentChildBucket = cluster.OpenBucket(PARENT_CHILD_BUCKET_NAME);
                    Couchbase.Management.IBucketManager parentChildBucketManager = parentChildBucket.CreateManager(this.username, this.password);
                    Console.WriteLine("flushing " + PARENT_CHILD_BUCKET_NAME);
                    IResult flushResults = parentChildBucketManager.Flush();
                    if (!flushResults.Success)
                    {
                        throw new Exception(flushResults.Message);
                    }
                }
            }
        }

        public async Task<User> GetUserByUsername(string username)
        {
            Console.WriteLine("CouchUserDataRepoImpl.GetUserByUsername(" + username + ")");

            QueryRequest query = new QueryRequest(USERS_FIND_BY_USERNAME);
            query.ScanConsistency(ScanConsistency.RequestPlus);
            query.AddPositionalParameter(username);

            IBucket usersBucket = cluster.OpenBucket(USERS_BUCKET_NAME);

            using (IQueryResult<dynamic> results = await usersBucket.QueryAsync<dynamic>(query))
            {
                if (!results.Success)
                {
                    Console.WriteLine("Couchdb failed to search for " + username + " Message: " + results.Message + " Status:"
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
                    throw new Exception("Unable to get any users for username " + username);
                }

                User user = null;

                var enumerator = results.GetEnumerator();
                if (enumerator.MoveNext())
                {
                    var row = enumerator.Current;
                    user = new User();
                    user.userId = row.id;
                    user.username = row.username;
                    user.password = row.password;
                    user.isParent = row.isParent;
                }

                Console.WriteLine("Couch.getUserByUsername(" + username + ") returning " + user);
                return user;
            }
        }

        private User createUserFromRow(dynamic row)
        {
            User user = new User();
            user.userId = row.id;
            user.username = row.username;
            user.password = row.password;
            user.isParent = row.isParent;

            return user;
        }


        public async Task<User> AddUser(User user)
        {
            string id = Guid.NewGuid().ToString();
            var document = new Document<dynamic> { Id = id, Content = new { username = user.username, password = user.password, isParent = user.isParent } };

            IBucket usersBucket = this.cluster.OpenBucket(USERS_BUCKET_NAME);
            IDocumentResult<dynamic> insertResults = await usersBucket.InsertAsync(document);
            Console.WriteLine("Couchdb.addUser " + user.username + ", docId: " + id);

            return await this.GetUserByUsername(user.username);
        }

        public async Task RemoveUser(User user)
        {
            IBucket usersBucket = this.cluster.OpenBucket(USERS_BUCKET_NAME);
            await usersBucket.RemoveAsync(user.userId);
        }

        public async Task<User> ReplaceUser(string userId, User user)
        {
            User userByUsername = await this.GetUserByUsername(user.username);

            if (userByUsername != null)
            {
                var document = new Document<dynamic> { Id = userId, Content = new { username = user.username, password = user.password, isParent = user.isParent } };

                IBucket usersBucket = this.cluster.OpenBucket(USERS_BUCKET_NAME);
                IDocumentResult<dynamic> insertResults = await usersBucket.ReplaceAsync(document);
                Console.WriteLine("Couchdb.ReplaceUser " + user.username + ", docId: " + userId);

                return await this.GetUserById(userId);
            }

            return null;
        }

        public async Task AddChildToParent(string parentUserId, string childUserId)
        {
            string id = Guid.NewGuid().ToString();
            var document = new Document<dynamic> { Id = id, Content = new { parentUserId = parentUserId, childUserId = childUserId } };

            IBucket parentChildBucket = this.cluster.OpenBucket(PARENT_CHILD_BUCKET_NAME);
            IDocumentResult<dynamic> insertResults = await parentChildBucket.InsertAsync(document);
            Console.WriteLine("Couchdb.AddChildToParent " + parentUserId + " and " + childUserId + ", docId: " + id);
        }

        public async Task<List<User>> GetChildrenForParent(string parentUserId)
        {
            List<User> children = new List<User>();
            List<string> childrenIds = new List<string>();

            QueryRequest query = new QueryRequest(PARENT_CHILD_FIND_BY_PARENT);
            query.ScanConsistency(ScanConsistency.RequestPlus);
            query.AddPositionalParameter(parentUserId);

            IBucket parentChildBucket = this.cluster.OpenBucket(PARENT_CHILD_BUCKET_NAME);

            using (IQueryResult<dynamic> results = await parentChildBucket.QueryAsync<dynamic>(query))
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
                    throw new Exception("Unable to get any children for parentUserId " + parentUserId);
                }

                foreach (dynamic row in results)
                {
                    string childUserId = row.childUserId;
                    User user = await this.GetUserById(childUserId);
                    if (user != null)
                    {
                        children.Add(user);
                    }
                    else
                    {
                        Console.WriteLine("unknown child userid " + childUserId);
                    }
                }
            }

            return children;
        }

        // probably will be public eventually
        private async Task<User> GetUserById(string userId)
        {
            IBucket usersBucket = this.cluster.OpenBucket(USERS_BUCKET_NAME);

            IOperationResult<dynamic> getResults = await usersBucket.GetAsync<dynamic>(userId);
            if (!getResults.Success)
            {
                Console.WriteLine("Couchdb failed to search for " + userId + " Message: " + getResults.Message + " Status:"
                        + getResults.Status);

                Console.WriteLine("results.Exception: " + getResults.Exception);
                if (getResults.Exception != null)
                {
                    throw getResults.Exception;
                }
                throw new Exception("Unable to get any users for userid " + userId);
            }

            User user = new User();

            user.userId = getResults.Id;
            user.username = getResults.Value.username;
            user.password = getResults.Value.password;
            user.isParent = getResults.Value.isParent;

            Console.WriteLine("MongoUserDataRepoImpl.getUserById(" + userId + ") returning " + user);
            return user;
        }
    }
}