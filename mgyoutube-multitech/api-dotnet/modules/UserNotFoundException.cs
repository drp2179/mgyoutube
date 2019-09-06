namespace api_dotnet.modules
{
    public class UserNotFoundException : System.Exception
    {
        public readonly string username;

        public UserNotFoundException(string username) : base()
        {
            this.username = username;
        }
        public UserNotFoundException(string username, string message) : base(message)
        {
            this.username = username;
        }
    }
}