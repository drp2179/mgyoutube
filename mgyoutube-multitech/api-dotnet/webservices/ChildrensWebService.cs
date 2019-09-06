using System;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.aspnet;
using api_dotnet.modules;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Newtonsoft.Json;

namespace api_dotnet.webservices
{
    public class ChildrenWebService
    {
        private UserModule userModule;

        public ChildrenWebService()
        {
            Console.WriteLine("Creating ChildrenWebService");
            this.userModule = ModuleRepoRegistry.GetUserModule();
        }

        public void SetupRoutes(IRouteBuilder routeBuilder)
        {
            routeBuilder.MapPost("/api/children/auth", AuthChildUser);
        }

        public Task AuthChildUser(HttpContext context)
        {
            string userCredentialJson = RequestHelper.GetRequestBody(context.Request);
            Console.WriteLine("authChildUser: userCredentialJson=" + userCredentialJson);

            // TODO: need to sanitize payload input before using
            String sanitizedUserCredentialJson = userCredentialJson;
            UserCredential userCredential = Helpers.MarshalUserCredentialFromJson(sanitizedUserCredentialJson);
            Console.WriteLine("authChildUser: userCredential=" + userCredential);

            User authedUser = userModule.AuthUser(userCredential);

            if (authedUser == null)
            {
                Console.WriteLine("authChildUser: userCredentialJson=" + userCredentialJson + " failed auth, returning UNAUTHORIZED");
                return ResponseHelper.Unauthorized(context);
            }
            else if (authedUser.isParent)
            {
                Console.WriteLine("authChildUser: userCredentialJson=" + userCredentialJson + " is a parent, returning UNAUTHORIZED");
                return ResponseHelper.Unauthorized(context);
            }

            authedUser.password = null;

            String responseJson = JsonConvert.SerializeObject(authedUser);

            Console.WriteLine("authUser: userCredentialJson=" + userCredentialJson + " returning OK and " + responseJson);
            return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
        }
    }
}