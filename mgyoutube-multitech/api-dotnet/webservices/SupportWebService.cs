using System;
using System.IO;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.aspnet;
using api_dotnet.modules;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Newtonsoft.Json;

namespace api_dotnet.webservices
{
    public class SupportWebService
    {
        private UserModule userModule;

        public SupportWebService()
        {
            Console.WriteLine("Creating SupportWebService");
            this.userModule = ModuleRepoRegistry.TheUserModule;
        }

        public void SetupRoutes(IRouteBuilder routeBuilder)
        {
            routeBuilder.MapPut("/api/support/users/{username}", CreateUser);
            routeBuilder.MapGet("/api/support/users/{username}", GetUserByUsername);
            routeBuilder.MapDelete("/api/support/users/{username}", DeleteUserByUsername);
        }

        public Task CreateUser(HttpContext context)
        {
            string userJson = RequestHelper.GetRequestBody(context.Request);
            Console.WriteLine("createUser: userJson=" + userJson);

            // TODO: need to sanitize payload input before using
            string sanitizedUserJson = userJson;
            User user = Helpers.MarshalUserFromJson(sanitizedUserJson);
            Console.WriteLine("createUser: user=" + user);

            User createdUser = userModule.CreateUpdateUser(user);
            createdUser.password = null;

            string responseJson = JsonConvert.SerializeObject(createdUser);

            Console.WriteLine("createUser: userJson=" + userJson + " returning OK and " + responseJson);
            return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
        }


        public Task GetUserByUsername(HttpContext context)
        {
            string username = (string)context.GetRouteValue("username");
            Console.WriteLine("getUserByUsername: username=" + username);

            // TODO: need to sanitize payload input before using
            string santizedUsername = username;
            User user = userModule.GetUser(santizedUsername);
            Console.WriteLine("getUserByUsername: user=" + user);

            if (user == null)
            {
                Console.WriteLine("getUserByUsername: username=" + username + " returning NOT_FOUND");
                return ResponseHelper.NotFound(context);
            }

            user.password = null;

            string responseJson = JsonConvert.SerializeObject(user);

            Console.WriteLine("getUserByUsername: username=" + username + " returning OK with " + responseJson);
            return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
        }


        public Task DeleteUserByUsername(HttpContext context)
        {
            string username = (string)context.GetRouteValue("username");
            Console.WriteLine("deleteUserByUsername: username=" + username);

            // TODO: need to sanitize payload input before using
            string santizedUsername = username;
            User oldUser = userModule.RemoveUser(santizedUsername);

            if (oldUser == null)
            {
                Console.WriteLine("deleteUserByUsername: username=" + username + " returning NOT_FOUND");
                return ResponseHelper.NotFound(context);
            }

            return ResponseHelper.Ok(context);
        }
    }
}
