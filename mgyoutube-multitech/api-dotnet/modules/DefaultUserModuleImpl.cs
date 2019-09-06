using System;
using System.Collections.Generic;
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

        public User AuthUser(UserCredential userCredential)
        {
            User user = null;
            try
            {
                user = userDataRepo.GetUserByUsername(userCredential.username);

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

        public User CreateUser(User user)
        {
            if (user.userId == 0)
            {
                User createdUser = this.userDataRepo.AddUser(user);

                if (createdUser != null)
                {
                    return createdUser;
                }
            }

            return null;
        }

        public User AddUpdateChildToParent(string parentUsername, User childUser)
        {
            User parentUser = this.GetUser(parentUsername);
            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            User existingChildUser = this.GetUser(childUser.username);
            if (existingChildUser == null)
            {
                Console.WriteLine("creating child " + childUser);
                User createdChildUser = this.CreateUser(childUser);
                userDataRepo.AddChildToParent(parentUser.userId, createdChildUser.userId);
                return createdChildUser;
            }
            else
            {
                Console.WriteLine("updating child " + childUser + " as " + existingChildUser.userId);
                userDataRepo.AddChildToParent(parentUser.userId, existingChildUser.userId);
                return this.UpdateUser(existingChildUser.userId, childUser);
            }
        }

        private User UpdateUser(long userId, User user)
        {
            return this.userDataRepo.ReplaceUser(userId, user);
        }

        public List<User> GetChildrenForParent(string parentUsername)
        {
            User parentUser = this.GetUser(parentUsername);
            if (parentUser == null)
            {
                throw new UserNotFoundException(parentUsername);
            }

            List<User> children = userDataRepo.GetChildrenForParent(parentUser.userId);

            return children;
        }

        public User GetUser(string username)
        {
            try
            {
                return this.userDataRepo.GetUserByUsername(username);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                return null;
            }
        }

        public User RemoveUser(string username)
        {
            User user = this.GetUser(username);

            if (user != null)
            {
                this.userDataRepo.RemoveUser(user);
            }

            return user;
        }
    }
}