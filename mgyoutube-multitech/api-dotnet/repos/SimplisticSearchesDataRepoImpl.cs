using System.Collections.Generic;
using System.Threading.Tasks;

namespace api_dotnet.repos
{
    public class SimplisticSearchesDataRepoImpl : SearchesDataRepo
    {
        private readonly Dictionary<string, HashSet<string>> parentSearches = new Dictionary<string, HashSet<string>>();

        public SimplisticSearchesDataRepoImpl() { }
        public void RepositoryStartup()
        {
            // nothing to do here
        }
        public async Task AddSearchToParentUser(string parentUserId, string searchPhrase)
        {
            await Task.Run(() =>
            {
                if (!this.parentSearches.ContainsKey(parentUserId))
                {
                    this.parentSearches[parentUserId] = new HashSet<string>();
                }

                this.parentSearches[parentUserId].Add(searchPhrase);
            });
        }

        public async Task<List<string>> GetSearchesForParentUser(string parentUserId)
        {
            return await Task.Run(() =>
            {
                HashSet<string> savedSearches;
                if (this.parentSearches.TryGetValue(parentUserId, out savedSearches))
                {
                    return new List<string>(savedSearches);
                }

                return new List<string>();
            });
        }

        public async Task RemoveSearchFromParentUser(string parentUserId, string searchPhrase)
        {
            await Task.Run(() =>
            {
                if (this.parentSearches.ContainsKey(parentUserId))
                {
                    var s = this.parentSearches[parentUserId];

                    if (s.Contains(searchPhrase))
                    {
                        s.Remove(searchPhrase);
                    }
                }
            });
        }
    }
}