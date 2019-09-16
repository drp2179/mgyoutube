using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.modules
{

    public interface UserModule
    {
        User GetUser(string username);
        User CreateUpdateUser(User user);
        User CreateUser(User user);
        User RemoveUser(string username);
        User AuthUser(UserCredential userCredential);
        User AddUpdateChildToParent(string parentUsername, User childUser);
        List<User> GetChildrenForParent(string sanitizedParentUsername);
    }
}