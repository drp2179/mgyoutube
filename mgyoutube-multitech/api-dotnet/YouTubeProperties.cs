using Microsoft.Extensions.Configuration;

namespace api_dotnet
{
    public class YouTubeProperties
    {
        public readonly string ApiKey;
        public readonly string ApplicationName;

        public YouTubeProperties(IConfiguration configuration)
        {
            IConfigurationSection youTubeSection = configuration.GetSection("YouTube");

            this.ApiKey = youTubeSection.GetValue<string>("ApiKey");
            this.ApplicationName = youTubeSection.GetValue<string>("ApplicationName");
        }

    }
}