using System.Collections.Generic;

namespace api_dotnet.repos
{
    public interface SearchesDataRepo
    {
        void RepositoryStartup();

        List<string> GetSearchesForParentUser(string userId);

        void AddSearchToParentUser(string parentUserId, string searchPhrase);
    }
}