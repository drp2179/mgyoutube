using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.repos
{
    public interface UserDataRepo
    {
        User GetUserByUsername(string username);
        User AddUser(User user);
        void RemoveUser(User user);
        void AddChildToParent(long userId1, long userId2);
        User ReplaceUser(long userId, User user);
        List<User> GetChildrenForParent(long userId);
    }
}