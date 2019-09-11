using System;
using System.IO;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;


namespace api_dotnet.aspnet
{

    public class RequestHelper
    {
        public static string GetRequestBody(HttpRequest request)
        {
            using (var reader = new StreamReader(request.Body))
            {
                return reader.ReadToEnd();
            }
        }

        public static T GetRouteValue<T>(HttpContext context, string key)
        {
            return (T)context.GetRouteValue(key);
        }

        public static string GetQueryStringField(HttpContext context, string key)
        {
            Microsoft.Extensions.Primitives.StringValues searchQuery;

            if (context.Request.Query.TryGetValue(key, out searchQuery))
            {
                if (searchQuery.Count >= 1)
                {
                    return searchQuery[0];
                }
            }

            return null;
        }
    }
}