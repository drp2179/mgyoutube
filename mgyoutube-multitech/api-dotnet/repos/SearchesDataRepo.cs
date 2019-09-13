using System.Collections.Generic;

namespace api_dotnet.repos
{
    public interface SearchesDataRepo
    {
        List<string> GetSearchesForParentUser(long userId);

        void AddSearchToParentUser(long parentUserId, string searchPhrase);
    }
}