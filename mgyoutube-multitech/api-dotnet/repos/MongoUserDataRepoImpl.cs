using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using MongoDB.Bson;
using MongoDB.Driver;
using Newtonsoft.Json;

namespace api_dotnet.repos
{
    public class MongoUserDataRepoImpl : UserDataRepo
    {
        private readonly MongoClient client;
        private readonly IMongoDatabase database;

        public const string USERS_COLLECTION_NAME = "users";
        public const string USERS_COLLECTION_USERNAME_FIELDNAME = "username";
        public const string USERS_COLLECTION_PASSWORD_FIELDNAME = "password";
        public const string USERS_COLLECTION_PARENT_FIELDNAME = "isParent";

        public const string PARENT_CHILD_COLLECTION_NAME = "parentChild";
        public const string PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId";
        public const string PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId";
        public const string MONGO_ID_FIELDNAME = "_id";

        public MongoUserDataRepoImpl(string connectionString, string databaseName)
        {
            this.client = new MongoClient(connectionString);
            this.database = client.GetDatabase(databaseName);
        }

        public void RepositoryStartup()
        {
            this.database.DropCollection(USERS_COLLECTION_NAME);
            this.database.DropCollection(PARENT_CHILD_COLLECTION_NAME);
        }

        public async Task<User> GetUserByUsername(string username)
        {
            Console.WriteLine("MongoUserDataRepoImpl.GetUserByUsername(" + username + ")");
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            using (IAsyncCursor<BsonDocument> cursor = await usersCollection.FindAsync(new BsonDocument(USERS_COLLECTION_USERNAME_FIELDNAME, username)))
            {
                if (!await cursor.MoveNextAsync())
                {
                    Console.WriteLine("GetUserByUsername MoveNextAsync failed");
                    return null;
                }

                IEnumerator<BsonDocument> docEnum = cursor.Current.GetEnumerator();
                if (!docEnum.MoveNext())
                {
                    Console.WriteLine("GetUserByUsername MoveNext failed");
                    return null;
                }

                BsonDocument userDoc = docEnum.Current;

                if (userDoc == null)
                {
                    Console.WriteLine("GetUserByUsername.userDoc is null");
                    return null;
                }

                User user = new User();

                ObjectId objectId = (ObjectId)userDoc[MONGO_ID_FIELDNAME];
                user.userId = objectId.ToString();
                user.username = userDoc[USERS_COLLECTION_USERNAME_FIELDNAME].ToString();
                user.password = userDoc[USERS_COLLECTION_PASSWORD_FIELDNAME].ToString();
                object isParentObj = userDoc[USERS_COLLECTION_PARENT_FIELDNAME];
                if (isParentObj != null)
                {
                    user.isParent = ((BsonBoolean)isParentObj).ToBoolean();
                }
                else
                {
                    user.isParent = false;
                }

                Console.WriteLine("MongoUserDataRepoImpl.getUserByUsername(" + username + ") returning " + user);
                return user;
            }
        }

        public async Task<User> AddUser(User user)
        {
            Dictionary<string, object> documentFields = new Dictionary<string, object>();
            documentFields[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;
            documentFields[USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password;
            documentFields[USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent;
            BsonDocument document = new BsonDocument(documentFields);

            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            await usersCollection.InsertOneAsync(document);

            return await this.GetUserByUsername(user.username);
        }

        public async Task RemoveUser(User user)
        {
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            await usersCollection.DeleteOneAsync(new BsonDocument(USERS_COLLECTION_USERNAME_FIELDNAME, user.username));
        }

        public async Task<User> ReplaceUser(string userId, User user)
        {
            User userByUsername = await this.GetUserByUsername(user.username);

            if (userByUsername != null)
            {
                Dictionary<string, object> documentFields = new Dictionary<string, object>();
                documentFields[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;
                documentFields[USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password;
                documentFields[USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent;
                BsonDocument document = new BsonDocument(documentFields);

                IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
                usersCollection.ReplaceOne(new BsonDocument(MONGO_ID_FIELDNAME, new ObjectId(userByUsername.userId)),
                        document);

                return await this.GetUserById(user.userId);
            }

            return null;
        }

        public async Task AddChildToParent(string parentUserId, string childUserId)
        {
            // TODO should probably worry about dups here...
            Dictionary<string, object> documentFields = new Dictionary<string, object>();
            documentFields[PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId;
            documentFields[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME] = childUserId;
            BsonDocument document = new BsonDocument(documentFields);

            IMongoCollection<BsonDocument> parentChildCollection = this.database.GetCollection<BsonDocument>(PARENT_CHILD_COLLECTION_NAME);
            await parentChildCollection.InsertOneAsync(document);
        }

        public async Task<List<User>> GetChildrenForParent(string parentUserId)
        {
            List<User> children = new List<User>();
            List<string> childrenIds = new List<string>();
            MongoUserDataRepoImpl udr = this;

            IMongoCollection<BsonDocument> parentChildCollection = this.database.GetCollection<BsonDocument>(PARENT_CHILD_COLLECTION_NAME);
            using (var cursor = await parentChildCollection.FindAsync(new BsonDocument(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, parentUserId)))
            {
                await cursor.ForEachAsync(d =>
                {
                    string childUserId = d[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME].ToString();
                    childrenIds.Add(childUserId);
                });

                foreach (var childUserId in childrenIds)
                {
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
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            using (IAsyncCursor<BsonDocument> cursor = await usersCollection.FindAsync(new BsonDocument(MONGO_ID_FIELDNAME, new ObjectId(userId))))
            {
                if (!await cursor.MoveNextAsync())
                {
                    return null;
                }

                IEnumerator<BsonDocument> docEnum = cursor.Current.GetEnumerator();
                if (!docEnum.MoveNext())
                {
                    return null;
                }

                BsonDocument userDoc = docEnum.Current;

                User user = new User();

                ObjectId objectId = (ObjectId)userDoc[MONGO_ID_FIELDNAME];
                user.userId = objectId.ToString();
                user.username = userDoc[USERS_COLLECTION_USERNAME_FIELDNAME].ToString();
                user.password = userDoc[USERS_COLLECTION_PASSWORD_FIELDNAME].ToString();
                object isParentObj = userDoc[USERS_COLLECTION_PARENT_FIELDNAME];
                if (isParentObj != null)
                {
                    user.isParent = ((BsonBoolean)isParentObj).ToBoolean();
                }
                else
                {
                    user.isParent = false;
                }

                Console.WriteLine("MongoUserDataRepoImpl.getUserById(" + userId + ") returning " + user);
                return user;
            }
        }
    }
}