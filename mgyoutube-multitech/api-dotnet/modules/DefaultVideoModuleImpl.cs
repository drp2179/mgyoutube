using System;
using System.Collections.Generic;
using api_dotnet.apimodel;
using api_dotnet.repos;
using Google.Apis.Services;
using Google.Apis.YouTube.v3;
using Google.Apis.YouTube.v3.Data;

namespace api_dotnet.modules
{
    public class DefaultVideoModuleImpl : VideoModule
    {
        // must be between [0,50], should be 64...
        public const long MAX_NUMBER_OF_VIDEOS_RETURNED = 50;
        private readonly String apiKey;
        private readonly String applicationName;
        //private readonly YouTubeService youTube;

        public DefaultVideoModuleImpl(string apiKey, string applicationName)
        {
            this.apiKey = apiKey;
            this.applicationName = applicationName;
        }

        // public void SetYouTube(YouTubeService youTube)
        // {
        //     this.youTube = youTube;
        // }

        protected YouTubeService getYouTube()
        {
            return new YouTubeService(new BaseClientService.Initializer()
            {
                ApiKey = this.apiKey,
                ApplicationName = this.applicationName
            });
        }


        public List<apimodel.Video> search(string searchTerms)
        {
            YouTubeService youtubeService = getYouTube();
            SearchResource.ListRequest search = youtubeService.Search.List("id,snippet");

            search.Q = searchTerms;
            search.MaxResults = MAX_NUMBER_OF_VIDEOS_RETURNED;
            search.SafeSearch = SearchResource.ListRequest.SafeSearchEnum.Strict;

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.Type = "video";

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            // search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            // @See https://developers.google.com/youtube/v3/getting-started#partial

            Console.WriteLine("searching for " + searchTerms);
            SearchListResponse searchListResponse = search.Execute();

            List<apimodel.Video> videosToReturn = new List<apimodel.Video>();

            if (searchListResponse.Items != null)
            {
                foreach (var searchResult in searchListResponse.Items)
                {
                    //Console.WriteLine("searchResult " + searchResult);
                    ResourceId resourceId = searchResult.Id;
                    SearchResultSnippet snippet = searchResult.Snippet;
                    ThumbnailDetails thumbnails = snippet.Thumbnails;

                    apimodel.Video video = new apimodel.Video();
                    video.videoId = resourceId.VideoId;
                    video.title = snippet.Title;
                    video.description = snippet.Description;
                    video.thumbnailUrl = thumbnails.Default__.Url;
                    video.thumbnailHeight = thumbnails.Default__.Height;
                    video.thumbnailWidth = thumbnails.Default__.Width;
                    video.channelId = snippet.ChannelId;
                    video.channelTitle = snippet.ChannelTitle;
                    video.publishedAt = snippet.PublishedAtRaw;

                    videosToReturn.Add(video);
                }
            }

            return videosToReturn;
        }
    }
}