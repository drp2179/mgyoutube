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
    public class VideoWebService
    {
        private VideoModule videosModule;

        public VideoWebService()
        {
            Console.WriteLine("Creating VideoWebService");
            this.videosModule = ModuleRepoRegistry.TheVideoModule;
        }

        public void SetupRoutes(IRouteBuilder routeBuilder)
        {
            routeBuilder.MapGet("/api/videos", VideosSearch);
        }

        public Task VideosSearch(HttpContext context)
        {
            try
            {
                string searchTerms = RequestHelper.GetQueryStringField(context, "search");
                searchTerms = searchTerms == null ? searchTerms : searchTerms.Trim();

                Console.WriteLine("videosSearch: searchTerms='" + searchTerms + "'");

                if (string.IsNullOrEmpty(searchTerms))
                {
                    Console.WriteLine("search term are blank failing as 400");
                    return ResponseHelper.BadRequest(context);
                }

                // TODO: need to sanitize payload input before using
                string sanitizedSearchTerms = searchTerms;

                List<Video> videos = videosModule.search(sanitizedSearchTerms);

                String responseJson = JsonConvert.SerializeObject(videos);

                Console.WriteLine("videosSearch: searchTerms=" + searchTerms + " returning OK");
                return ResponseHelper.Ok(context.Response, responseJson, MediaType.APPLICATION_JSON);
            }
            catch (Exception e)
            {
                Console.WriteLine("VideosSearch, unhandled exception", e);
                return ResponseHelper.InternalServerError(context);
            }
        }
    }
}