using Microsoft.Extensions.Configuration;

namespace api_dotnet
{
    public class DatastoresProperties
    {
        public readonly string CouchConnectionString;
        public readonly string CouchUsername;
        public readonly string CouchPassword;
        public readonly string MongoConnectionString;
        public readonly string MongoDatabaseName;

        public DatastoresProperties(IConfiguration configuration)
        {
            IConfigurationSection youTubeSection = configuration.GetSection("DataStores");

            this.CouchConnectionString = youTubeSection.GetValue<string>("CouchConnectionString");
            this.CouchUsername = youTubeSection.GetValue<string>("CouchUsername");
            this.CouchPassword = youTubeSection.GetValue<string>("CouchPassword");
            this.MongoConnectionString = youTubeSection.GetValue<string>("MongoConnectionString");
            this.MongoDatabaseName = youTubeSection.GetValue<string>("MongoDatabaseName");
        }

    }
}