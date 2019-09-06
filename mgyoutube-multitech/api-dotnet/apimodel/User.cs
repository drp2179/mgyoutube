namespace api_dotnet.apimodel
{
    public class User
    {
        public long userId;
        public string username;
        public string password;
        public bool isParent;
        public User() { }

        public User(User user)
        {
            this.userId = user.userId;
            this.username = user.username;
            this.password = user.password;
            this.isParent = user.isParent;
        }
    }
}