using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.repos;

namespace api_dotnet.modules
{
    public class DefaultUserModuleImpl : UserModule
    {
        private UserDataRepo userDataRepo;

        public DefaultUserModuleImpl(UserDataRepo userDataRepo)
        {
            this.SetUserDataRepo(userDataRepo);
        }
        public void SetUserDataRepo(UserDataRepo userDataRepo)
        {
            this.userDataRepo = userDataRepo;
        }

        public async Task<User> AuthUser(UserCredential userCredential)
        {
            User user = null;
            try
            {
                user = await userDataRepo.GetUserByUsername(userCredential.username);

                if (user != null)
                {

                    if (user.password == null)
                    {
                        Console.WriteLine("user.password is null");
                        user = null;
                    }
                    else if (userCredential.password == null)
                    {
                        Console.WriteLine("userCredential.password is null");
                        user = null;
                    }
                    else if (!user.password.Equals(userCredential.password))
                    {
                        Console.WriteLine("user.password(" + user.password + ") != userCredential.password("
                                + userCredential.password + ")");
                        user = null;
                    }
                    else
                    {
                        Console.WriteLine("password match!");
                        // password match!
                    }
                }
                else
                {
                    Console.WriteLine("getUserByUsername(" + userCredential.username + ") returned null");
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                user = null;
            }

            return user;
        }

        public async Task<User> CreateUpdateUser(User user)
        {
            User existingUser = await this.userDataRepo.GetUserByUsername(user.username);
            if (existingUser != null)
            {
                if (user.userId == null)
                {
                    user.userId = existingUser.userId;
                }
                Console.WriteLine("createUpdateUser is updating existing user " + existingUser + " to be " + user);
                return await this.UpdateUser(existingUser.userId, user);
            }
            Console.WriteLine("createUpdateUser is creating new user " + user);
            return await this.CreateUser(user);
        }


        public async Task<User> CreateUser(User user)
        {
            if (user.userId == null)
            {
                User createdUser = await this.userDataRepo.AddUser(user);

                if (createdUser != null)
                {
                    return createdUser;
                }
            }

            return null;
        }

        private async Task<User> UpdateUser(string userId, User user)
        {
            if (userId == null)
            {
                throw new Exception("parameter 'userId' must not be null");
            }
            if (user == null)
            {
                throw new Exception("parameter 'user' must not be null");
            }
            if (!userId.Equals(user.userId))
            {
                if (user.userId != null)
                {
                    throw new Exception("user.userId is not null AND userId (" + userId + ") and user.userId ("
                            + user.userId + ") paremters do not match");
                }
                user.userId = userId;
            }

            return await this.userDataRepo.ReplaceUser(userId, user);
        }

        public async Task<User> AddUpdateChildToParent(string parentUsername, User childUser)
        {
            User parentUser = await this.GetUser(parentUsername);
            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            User existingChildUser = await this.GetUser(childUser.username);
            if (existingChildUser == null)
            {
                Console.WriteLine("creating child " + childUser);
                User createdChildUser = await this.CreateUser(childUser);
                await userDataRepo.AddChildToParent(parentUser.userId, createdChildUser.userId);
                return createdChildUser;
            }
            else
            {
                Console.WriteLine("updating child " + childUser + " as " + existingChildUser.userId);
                await userDataRepo.AddChildToParent(parentUser.userId, existingChildUser.userId);
                return await this.UpdateUser(existingChildUser.userId, childUser);
            }
        }

        public async Task<List<User>> GetChildrenForParent(string parentUsername)
        {
            User parentUser = await this.GetUser(parentUsername);
            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            List<User> children = await userDataRepo.GetChildrenForParent(parentUser.userId);

            return children;
        }

        public async Task<User> GetUser(string username)
        {
            try
            {
                return await this.userDataRepo.GetUserByUsername(username);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return null;
            }
        }

        public async Task<User> RemoveUser(string username)
        {
            User user = await this.GetUser(username);

            if (user != null)
            {
                await this.userDataRepo.RemoveUser(user);
            }

            return user;
        }
    }
}