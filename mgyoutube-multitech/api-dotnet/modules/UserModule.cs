using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;

namespace api_dotnet.modules
{

    public interface UserModule
    {
        Task<User> GetUser(string username);
        Task<User> CreateUpdateUser(User user);
        Task<User> CreateUser(User user);
        Task<User> RemoveUser(string username);
        Task<User> AuthUser(UserCredential userCredential);
        Task<User> AddUpdateChildToParent(string parentUsername, User childUser);
        Task<List<User>> GetChildrenForParent(string sanitizedParentUsername);
    }
}