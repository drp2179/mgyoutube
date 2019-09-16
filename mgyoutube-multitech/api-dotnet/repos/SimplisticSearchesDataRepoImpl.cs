using System.Collections.Generic;

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
        public void AddSearchToParentUser(string parentUserId, string searchPhrase)
        {
            if (!this.parentSearches.ContainsKey(parentUserId))
            {
                this.parentSearches[parentUserId] = new HashSet<string>();
            }

            this.parentSearches[parentUserId].Add(searchPhrase);
        }

        public List<string> GetSearchesForParentUser(string parentUserId)
        {
            HashSet<string> savedSearches;
            if (this.parentSearches.TryGetValue(parentUserId, out savedSearches))
            {
                return new List<string>(savedSearches);
            }

            return new List<string>();
        }
    }
}