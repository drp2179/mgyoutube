using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.repos
{
    public interface UserDataRepo
    {
        void RepositoryStartup();

        User GetUserByUsername(string username);
        User AddUser(User user);
        void RemoveUser(User user);
        void AddChildToParent(string parentUserId, string childUserId);
        User ReplaceUser(string userId, User user);
        List<User> GetChildrenForParent(string userId);
    }
}