using System;
using api_dotnet.apimodel;
using Newtonsoft.Json;

namespace api_dotnet.webservices
{
    class Helpers
    {
        public static User MarshalUserFromJson(string userJson)
        {
            User user = JsonConvert.DeserializeObject<User>(userJson);

            // TODO: do additional content validation

            return user;
        }

        public static UserCredential MarshalUserCredentialFromJson(string userCredentialJson)
        {
            UserCredential userCredential = JsonConvert.DeserializeObject<UserCredential>(userCredentialJson);

            //
            // post object create validation
            //
            if (userCredential.password == null || userCredential.username == null)
            {
                Console.WriteLine("userCredential username or password is null, returning null");
                return null;
            }

            return userCredential;
        }

    }
}