using System.Collections.Generic;

namespace api_dotnet.modules
{
    public interface SearchesModule
    {
        List<string> GetSavedSearchesForParent(string parentUsername);

        void AddSearchToParent(string parentUsername, string searchPhrase);

    }
}