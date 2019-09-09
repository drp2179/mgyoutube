using System;
using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.repos
{
    public class SimplisticUserDataRepoImpl : UserDataRepo
    {
        private Dictionary<string, User> usernameMap = new Dictionary<string, User>();
        private Dictionary<long, User> userIdMap = new Dictionary<long, User>();
        private Dictionary<long, List<long>> parentChildrenMap = new Dictionary<long, List<long>>();
        private long nextUserId = 1;
        private readonly object mutationLock = new object();

        public User GetUserByUsername(string username)
        {
            User user = null;
            if (this.usernameMap.TryGetValue(username, out user))
            {
                // cloning so mods after return do not affect maps
                return new User(user);
            }
            return null;
        }

        public User AddUser(User user)
        {
            lock (mutationLock)
            {
                // cloning so that we can mutate userId without affecting the input object
                User addedUser = new User(user);
                addedUser.userId = nextUserId++;

                this.userIdMap[addedUser.userId] = addedUser;
                this.usernameMap[addedUser.username] = addedUser;

                // cloning so that mods after return do not mutate maps
                return new User(addedUser);
            }
        }

        public void RemoveUser(User user)
        {
            lock (mutationLock)
            {
                this.userIdMap.Remove(user.userId);
                this.usernameMap.Remove(user.username);
            }
        }

        public User ReplaceUser(long userId, User user)
        {
            lock (mutationLock)
            {
                User existingUser;

                if (this.userIdMap.TryGetValue(userId, out existingUser))
                {
                    // cloning so that we can mutate userId without affecting the input object
                    User replacingUser = new User(user);
                    replacingUser.userId = userId;

                    this.userIdMap[replacingUser.userId] = replacingUser;
                    this.usernameMap[replacingUser.username] = replacingUser;

                    // cloning so that mods after return do not mutate maps
                    return new User(replacingUser);
                }

                return null;
            }
        }

        public void AddChildToParent(long parentUserId, long childUserId)
        {
            lock (mutationLock)
            {
                if (!this.parentChildrenMap.ContainsKey(parentUserId))
                {
                    this.parentChildrenMap[parentUserId] = new List<long>();
                }

                List<long> childrenUserIds = this.parentChildrenMap[parentUserId];

                if (!childrenUserIds.Contains(childUserId))
                {
                    childrenUserIds.Add(childUserId);
                }
            }
        }

        public List<User> GetChildrenForParent(long parentUserId)
        {
            List<User> children = new List<User>();

            if (this.parentChildrenMap.ContainsKey(parentUserId))
            {
                List<long> childrenUserIds = this.parentChildrenMap[parentUserId];

                foreach (long childUserId in childrenUserIds)
                {
                    User user = this.GetUserById(childUserId);
                    if (user != null)
                    {
                        children.Add(user);
                    }
                    else
                    {
                        Console.WriteLine("unknown child userid " + childUserId);
                    }
                }
            }

            return children;
        }

        // probably will be public eventually
        private User GetUserById(long userId)
        {
            return this.userIdMap[userId];
        }
    }
}