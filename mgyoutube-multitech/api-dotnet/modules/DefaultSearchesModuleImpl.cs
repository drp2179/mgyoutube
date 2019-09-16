using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.repos;

namespace api_dotnet.modules
{
    public class DefaultSearchesModuleImpl : SearchesModule
    {
        public SearchesDataRepo SearchesDataRepo { get; set; }
        public UserDataRepo UserDataRepo { get; set; }
        public DefaultSearchesModuleImpl(UserDataRepo userDataRepo, SearchesDataRepo searchesDataRepo)
        {
            UserDataRepo = userDataRepo;
            SearchesDataRepo = searchesDataRepo;
        }

        public async Task AddSearchToParent(string parentUsername, string searchPhrase)
        {
            Console.WriteLine("AddSearchToParent, parentUsername=" + parentUsername + ", searchPhrase=" + searchPhrase);
            User parentUser = await this.UserDataRepo.GetUserByUsername(parentUsername);

            if (parentUser == null)
            {
                Console.WriteLine("AddSearchToParent, parentUsername=" + parentUsername + " parentUser is null so throwing UserNotFoundException");
                throw new UserNotFoundException(parentUsername);
            }

            Console.WriteLine("AddSearchToParent, parentUsername=" + parentUsername + ", found parent=" + parentUser);

            await this.SearchesDataRepo.AddSearchToParentUser(parentUser.userId, searchPhrase);
        }

        public async Task<List<string>> GetSavedSearchesForParent(string parentUsername)
        {
            User parentUser = await this.UserDataRepo.GetUserByUsername(parentUsername);

            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            List<string> searches = await this.SearchesDataRepo.GetSearchesForParentUser(parentUser.userId);

            Console.WriteLine("getSavedSearchesForParent for " + parentUsername + " returning searches.size " + searches.Count);

            return searches;
        }
    }
}