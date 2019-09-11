package modules

import (
	"apimodel"
	"log"
	"net/http"

	"google.golang.org/api/googleapi/transport"
	"google.golang.org/api/youtube/v3"
)

// MaxNumberofVideosReturned - must be between [0,50], should be 64...
const MaxNumberofVideosReturned int64 = 50

// DefaultVideoModuleImpl = structure for default video module impl
type DefaultVideoModuleImpl struct {
	//userDataRepo repos.UserDataRepo
	apiKey          string
	applicationName string
}

// Search - search for videos
func (videoModule DefaultVideoModuleImpl) Search(searchTerms string) ([]*apimodel.Video, error) {
	//query = flag.String("query", "Google", "Search term")
	//maxResults = flag.Int64("max-results", 50, "Max YouTube results")

	//flag.Parse()

	client := &http.Client{
		Transport: &transport.APIKey{Key: videoModule.apiKey},
	}

	service, errCreateYouTube := youtube.New(client)
	if errCreateYouTube != nil {
		log.Println("Error creating new YouTube client: ", errCreateYouTube)
		return nil, errCreateYouTube
	}

	// Make the API call to YouTube.
	call := service.Search.List("id,snippet").
		//Q(*query).
		Q(searchTerms).
		MaxResults(MaxNumberofVideosReturned).
		SafeSearch("strict").
		Type("video")
	// MaxResults(*maxResults)
	response, errCallingYouTube := call.Do()

	if errCallingYouTube != nil {
		log.Println("Error searching YouTube for '", searchTerms, "', err: ", errCreateYouTube)
		return nil, errCallingYouTube
	}

	var videosToReturn []*apimodel.Video

	for _, searchResult := range response.Items {
		// switch item.Id.Kind {
		// case "youtube#video":

		log.Println("searchResult: ", searchResult)
		// final ResourceId resourceId = searchResult.getId(); // id
		// final ThumbnailDetails thumbnails = snippet.getThumbnails(); // thumbnails

		video := apimodel.Video{}
		// video.setVideoId(resourceId.getVideoId());
		video.Title = searchResult.Snippet.Title
		video.Description = searchResult.Snippet.Description
		// video.setThumbnailUrl(thumbnails.getDefault().getUrl());
		// video.setChannelId(snippet.getChannelId());
		// video.setChannelTitle(snippet.getChannelTitle());
		// video.setPublishedAt(snippet.getPublishedAt());

		videosToReturn = append(videosToReturn, &video)
	}

	//final YouTube.Search.List search = this.getYouTube().search().list("id,snippet");

	//search.setType("video")

	// To increase efficiency, only retrieve the fields that the
	// application uses.
	// search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
	// @See https://developers.google.com/youtube/v3/getting-started#partial

	// System.out.println("searching for " + searchTerms);
	// final SearchListResponse searchResponse = search.execute();
	// final List<SearchResult> searchResultList = searchResponse.getItems();

	// //final List<Video> videosToReturn = new ArrayList<Video>();

	// if (searchResultList != null) {
	// 	for (SearchResult searchResult : searchResultList) {
	// 		System.out.println("searchResult " + searchResult);
	// 		// final ResourceId resourceId = searchResult.getId(); // id
	// 		final SearchResultSnippet snippet = searchResult.getSnippet(); // snippet
	// 		// final ThumbnailDetails thumbnails = snippet.getThumbnails(); // thumbnails

	// 		final Video video = new Video();
	// 		// video.setVideoId(resourceId.getVideoId());
	// 		video.title = snippet.getTitle();
	// 		video.description = snippet.getDescription();
	// 		// video.setThumbnailUrl(thumbnails.getDefault().getUrl());
	// 		// video.setChannelId(snippet.getChannelId());
	// 		// video.setChannelTitle(snippet.getChannelTitle());
	// 		// video.setPublishedAt(snippet.getPublishedAt());

	// 		videosToReturn.add(video);
	// 	}
	// }

	return videosToReturn, nil
}

// NewDefaultVideoModuleImpl - create a new DefaultVideoModuleImpl
func NewDefaultVideoModuleImpl(apiKey string, applicationName string) *DefaultVideoModuleImpl {
	module := DefaultVideoModuleImpl{}
	module.apiKey = apiKey
	module.applicationName = applicationName

	return &module
}
