using System.Collections.Generic;

namespace api_dotnet.repos
{
    public class SimplisticSearchesDataRepoImpl : SearchesDataRepo
    {
        private readonly Dictionary<long, HashSet<string>> parentSearches = new Dictionary<long, HashSet<string>>();
        public void AddSearchToParentUser(long parentUserId, string searchPhrase)
        {
            if (!this.parentSearches.ContainsKey(parentUserId))
            {
                this.parentSearches[parentUserId] = new HashSet<string>();
            }

            this.parentSearches[parentUserId].Add(searchPhrase);
        }

        public List<string> GetSearchesForParentUser(long userId)
        {
            HashSet<string> savedSearches;
            if (this.parentSearches.TryGetValue(userId, out savedSearches))
            {
                return new List<string>(savedSearches);
            }

            return new List<string>();
        }
    }
}