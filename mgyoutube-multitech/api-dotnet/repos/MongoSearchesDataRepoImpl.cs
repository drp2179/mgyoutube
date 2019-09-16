using System.Collections.Generic;
using System.Threading.Tasks;
using MongoDB.Bson;
using MongoDB.Driver;

namespace api_dotnet.repos
{
    public class MongoSearchesDataRepoImpl : SearchesDataRepo
    {
        private readonly MongoClient client;
        private readonly IMongoDatabase database;

        public const string SEARCHES_COLLECTION_NAME = "searches";
        public const string SEARCHES_COLLECTION_PARENT_FIELDNAME = "parentUserId";
        public const string SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME = "phrase";

        public MongoSearchesDataRepoImpl(string connectionString, string databaseName)
        {
            this.client = new MongoClient(connectionString);
            this.database = client.GetDatabase(databaseName);
        }
        public void RepositoryStartup()
        {
            this.database.DropCollection(SEARCHES_COLLECTION_NAME);
        }

        public async Task AddSearchToParentUser(string parentUserId, string searchPhrase)
        {
            Dictionary<string, object> documentFields = new Dictionary<string, object>();
            documentFields[SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId;
            documentFields[SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME] = searchPhrase;
            BsonDocument document = new BsonDocument(documentFields);

            IMongoCollection<BsonDocument> searchesCollection = this.database.GetCollection<BsonDocument>(SEARCHES_COLLECTION_NAME);
            await searchesCollection.InsertOneAsync(document);
        }

        public async Task<List<string>> GetSearchesForParentUser(string parentUserId)
        {
            List<string> searches = new List<string>();

            IMongoCollection<BsonDocument> searchesCollection = this.database.GetCollection<BsonDocument>(SEARCHES_COLLECTION_NAME);
            var cursor = await searchesCollection.FindAsync(new BsonDocument(SEARCHES_COLLECTION_PARENT_FIELDNAME, parentUserId));

            await cursor.ForEachAsync(d =>
            {
                var x = d[SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME];
                searches.Add(x.ToString());
            });

            return searches;
        }
    }
}