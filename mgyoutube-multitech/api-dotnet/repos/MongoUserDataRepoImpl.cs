using System;
using System.Collections.Generic;
using api_dotnet.apimodel;
using MongoDB.Bson;
using MongoDB.Driver;

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

        public User GetUserByUsername(string username)
        {
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            IFindFluent<BsonDocument, BsonDocument> found = usersCollection.Find(new BsonDocument(USERS_COLLECTION_USERNAME_FIELDNAME, username));

            if (found.CountDocuments() <= 0)
            {
                return null;

            }

            BsonDocument userDoc = found.First();

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

        public User AddUser(User user)
        {
            Dictionary<string, object> documentFields = new Dictionary<string, object>();
            documentFields[USERS_COLLECTION_USERNAME_FIELDNAME] = user.username;
            documentFields[USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password;
            documentFields[USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent;
            BsonDocument document = new BsonDocument(documentFields);

            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            usersCollection.InsertOne(document);

            return this.GetUserByUsername(user.username);
        }

        public void RemoveUser(User user)
        {
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            usersCollection.DeleteOne(new BsonDocument(USERS_COLLECTION_USERNAME_FIELDNAME, user.username));
        }

        public User ReplaceUser(string userId, User user)
        {
            User userByUsername = this.GetUserByUsername(user.username);

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

                return this.GetUserById(user.userId);
            }

            return null;
        }

        public void AddChildToParent(string parentUserId, string childUserId)
        {
            // TODO should probably worry about dups here...
            Dictionary<string, object> documentFields = new Dictionary<string, object>();
            documentFields[PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId;
            documentFields[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME] = childUserId;
            BsonDocument document = new BsonDocument(documentFields);

            IMongoCollection<BsonDocument> parentChildCollection = this.database.GetCollection<BsonDocument>(PARENT_CHILD_COLLECTION_NAME);
            parentChildCollection.InsertOne(document);
        }

        public List<User> GetChildrenForParent(string parentUserId)
        {
            List<User> children = new List<User>();
            MongoUserDataRepoImpl udr = this;
            //     Consumer<Document> consumer = new Consumer<Document>() {

            //     @Override

            //     public void accept(final Document doc)
            //     {
            //         final String childUserId = doc.getString(PARENT_CHILD_COLLECTION_CHILD_FIELDNAME);

            //         final User user = udr.getUserById(childUserId);
            //         if (user != null)
            //         {
            //             children.add(user);
            //         }
            //         else
            //         {
            //             System.out.println("unknown child userid " + childUserId);
            //         }
            //     }
            // };

            IMongoCollection<BsonDocument> parentChildCollection = this.database.GetCollection<BsonDocument>(PARENT_CHILD_COLLECTION_NAME);
            parentChildCollection.Find(new BsonDocument(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, parentUserId)).ToList().ForEach(d =>
            {
                string childUserId = d[PARENT_CHILD_COLLECTION_CHILD_FIELDNAME].ToString();
                User user = this.GetUserById(childUserId);
                if (user != null)
                {
                    children.Add(user);
                }
                else
                {
                    Console.WriteLine("unknown child userid " + childUserId);
                }
            });

            return children;
        }

        // probably will be public eventually
        private User GetUserById(string userId)
        {
            IMongoCollection<BsonDocument> usersCollection = this.database.GetCollection<BsonDocument>(USERS_COLLECTION_NAME);
            BsonDocument userDoc = usersCollection.Find(new BsonDocument(MONGO_ID_FIELDNAME, new ObjectId(userId))).First();

            if (userDoc == null)
            {
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

            Console.WriteLine("MongoUserDataRepoImpl.getUserById(" + userId + ") returning " + user);
            return user;
        }
    }
}