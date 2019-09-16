using System.Collections.Generic;
using System.Threading.Tasks;

namespace api_dotnet.modules
{
    public interface SearchesModule
    {
        Task<List<string>> GetSavedSearchesForParent(string parentUsername);

        Task AddSearchToParent(string parentUsername, string searchPhrase);

    }
}