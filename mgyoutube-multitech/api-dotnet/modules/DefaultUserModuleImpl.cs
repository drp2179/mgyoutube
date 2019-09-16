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

        public User CreateUpdateUser(User user)
        {
            User existingUser = this.userDataRepo.GetUserByUsername(user.username);
            if (existingUser != null)
            {
                if (user.userId == null)
                {
                    user.userId = existingUser.userId;
                }
                Console.WriteLine("createUpdateUser is updating existing user " + existingUser + " to be " + user);
                return this.UpdateUser(existingUser.userId, user);
            }
            Console.WriteLine("createUpdateUser is creating new user " + user);
            return this.CreateUser(user);
        }


        public User CreateUser(User user)
        {
            if (user.userId == null)
            {
                User createdUser = this.userDataRepo.AddUser(user);

                if (createdUser != null)
                {
                    return createdUser;
                }
            }

            return null;
        }

        private User UpdateUser(string userId, User user)
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

            return this.userDataRepo.ReplaceUser(userId, user);
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