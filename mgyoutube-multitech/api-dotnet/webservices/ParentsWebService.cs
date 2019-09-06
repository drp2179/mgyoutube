using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using api_dotnet.apimodel;
using api_dotnet.aspnet;
using api_dotnet.modules;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using Newtonsoft.Json;

namespace api_dotnet.webservices
{
    public class ParentsWebService
    {
        private UserModule userModule;

        public ParentsWebService()
        {
            Console.WriteLine("Creating ParentsWebService");
            this.userModule = ModuleRepoRegistry.GetUserModule();
        }

        public void SetupRoutes(IRouteBuilder routeBuilder)
        {
            routeBuilder.MapPost("/api/parents/auth", AuthParentUser);
            routeBuilder.MapGet("/api/parents/{parentUsername}/children", GetChildrenForParent);
            routeBuilder.MapPut("/api/parents/{parentUsername}/children/{childUsername}", AddUpdateChildToParent);
        }


        public Task AuthParentUser(HttpContext context)
        {
            string userCredentialJson = RequestHelper.GetRequestBody(context.Request);
            Console.WriteLine("authParentUser: userCredentialJson=" + userCredentialJson);

            // TODO: need to sanitize payload input before using
            String sanitizedUserCredentialJson = userCredentialJson;
            UserCredential userCredential = Helpers.MarshalUserCredentialFromJson(sanitizedUserCredentialJson);
            Console.WriteLine("authParentUser: userCredential=" + userCredential);

            User authedUser = userModule.AuthUser(userCredential);

            if (authedUser == null)
            {
                Console.WriteLine("authParentUser: userCredentialJson=" + userCredentialJson + " failed auth, returning UNAUTHORIZED");
                return ResponseHelper.Unauthorized(context);
            }
            else if (!authedUser.isParent)
            {
                Console.WriteLine("authParentUser: userCredentialJson=" + userCredentialJson + " is not a parent, returning UNAUTHORIZED");
                return ResponseHelper.Unauthorized(context);
            }

            authedUser.password = null;

            string responseJson = JsonConvert.SerializeObject(authedUser);

            Console.WriteLine("authParentUser: userCredentialJson=" + userCredentialJson + " returning OK and " + responseJson);
            return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
        }

        public Task GetChildrenForParent(HttpContext context)
        {
            string parentUsername = (string)context.GetRouteValue("parentUsername");
            Console.WriteLine("getChildrenForParent: parentUsername=" + parentUsername);

            // TODO: need to sanitize payload input before using
            string sanitizedParentUsername = parentUsername;

            try
            {
                List<User> children = userModule.GetChildrenForParent(sanitizedParentUsername);
                Console.WriteLine("getChildrenForParent: getChildrenForParent=" + children);

                children.ForEach(u => u.password = null);

                string responseJson = JsonConvert.SerializeObject(children);

                Console.WriteLine("getChildrenForParent: parentUsername=" + parentUsername + " returning OK for size " + children.Count + " with " + responseJson);
                return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
            }
            catch (UserNotFoundException unfe)
            {
                Console.WriteLine("getChildrenForParent: unable to find user " + unfe.username + " returning NOT_FOUND");
                return ResponseHelper.NotFound(context);
            }
        }

        public Task AddUpdateChildToParent(HttpContext context)
        {
            string parentUsername = RequestHelper.GetRouteValue<string>(context, "parentUsername");
            string childUsername = RequestHelper.GetRouteValue<string>(context, "childUsername");
            string childUserJson = RequestHelper.GetRequestBody(context.Request);
            Console.WriteLine("addUpdateChildToParent: parentUsername=" + parentUsername + ", childUsername="
                    + childUsername + ", childUserJson=" + childUserJson);

            // TODO: need to sanitize payload input before using
            string sanitizedParentUsername = parentUsername;
            // string sanitizedChildUsername = childUsername;
            string sanitizedChildUserJson = childUserJson;

            User childUser = Helpers.MarshalUserFromJson(sanitizedChildUserJson);
            Console.WriteLine("addUpdateChildToParent: childUser=" + childUser);

            try
            {
                User addedUpdatedUser = userModule.AddUpdateChildToParent(sanitizedParentUsername, childUser);

                addedUpdatedUser.password = null;

                string responseJson = JsonConvert.SerializeObject(addedUpdatedUser);

                Console.WriteLine("addUpdateChildToParent: parentUsername=" + parentUsername + ", childUsername="
                        + childUsername + " returning OK  and " + responseJson);
                return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
            }
            catch (UserNotFoundException unfe)
            {
                Console.WriteLine("addUpdateChildToParent: unable to find user " + unfe.username + " returning NOT_FOUND");
                return ResponseHelper.NotFound(context);
            }
        }

    }
}