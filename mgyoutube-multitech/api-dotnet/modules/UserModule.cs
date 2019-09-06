using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.modules
{

    interface UserModule
    {
        User GetUser(string username);
        User CreateUser(User user);
        User RemoveUser(string username);
        User AuthUser(UserCredential userCredential);
        User AddUpdateChildToParent(string parentUsername, User childUser);
        List<User> GetChildrenForParent(string sanitizedParentUsername);
    }
}