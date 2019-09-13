using System;
using System.Collections.Generic;
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

        public void AddSearchToParent(string parentUsername, string searchPhrase)
        {
            User parentUser = this.UserDataRepo.GetUserByUsername(parentUsername);

            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            this.SearchesDataRepo.AddSearchToParentUser(parentUser.userId, searchPhrase);
        }

        public List<string> GetSavedSearchesForParent(string parentUsername)
        {
            User parentUser = this.UserDataRepo.GetUserByUsername(parentUsername);

            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            List<string> searches = this.SearchesDataRepo.GetSearchesForParentUser(parentUser.userId);

            Console.WriteLine("getSavedSearchesForParent for " + parentUsername + " returning searches.size " + searches.Count);

            return searches;
        }
    }
}