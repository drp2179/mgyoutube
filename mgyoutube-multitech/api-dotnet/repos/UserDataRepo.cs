using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;

namespace api_dotnet.repos
{
    public interface UserDataRepo
    {
        void RepositoryStartup();

        Task<User> GetUserByUsername(string username);
        Task<User> AddUser(User user);
        Task RemoveUser(User user);
        Task AddChildToParent(string parentUserId, string childUserId);
        Task<User> ReplaceUser(string userId, User user);
        Task<List<User>> GetChildrenForParent(string userId);
    }
}