using System.Collections.Generic;
using System.Threading.Tasks;

namespace api_dotnet.repos
{
    public interface SearchesDataRepo
    {
        void RepositoryStartup();

        Task<List<string>> GetSearchesForParentUser(string userId);

        Task AddSearchToParentUser(string parentUserId, string searchPhrase);
    }
}