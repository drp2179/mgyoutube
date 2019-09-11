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
	apiKey          string
	applicationName string
}

// Search - search for videos
func (videoModule DefaultVideoModuleImpl) Search(searchTerms string) ([]*apimodel.Video, error) {

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
		Q(searchTerms).
		MaxResults(MaxNumberofVideosReturned).
		SafeSearch("strict").
		Type("video")

	response, errCallingYouTube := call.Do()

	if errCallingYouTube != nil {
		log.Println("Error searching YouTube for '", searchTerms, "', err: ", errCreateYouTube)
		return nil, errCallingYouTube
	}

	var videosToReturn []*apimodel.Video

	for _, searchResult := range response.Items {
		//log.Println("searchResult: ", searchResult)

		video := apimodel.Video{}
		video.VideoID = searchResult.Id.VideoId
		video.Title = searchResult.Snippet.Title
		video.Description = searchResult.Snippet.Description
		video.ChannelID = searchResult.Snippet.ChannelId
		video.ChannelTitle = searchResult.Snippet.ChannelTitle
		video.PublishedAt = searchResult.Snippet.PublishedAt
		video.ThumbnailURL = searchResult.Snippet.Thumbnails.Default.Url
		video.ThumbnailHeight = searchResult.Snippet.Thumbnails.Default.Height
		video.ThumbnailWidth = searchResult.Snippet.Thumbnails.Default.Width

		videosToReturn = append(videosToReturn, &video)
	}

	return videosToReturn, nil
}

// NewDefaultVideoModuleImpl - create a new DefaultVideoModuleImpl
func NewDefaultVideoModuleImpl(apiKey string, applicationName string) *DefaultVideoModuleImpl {
	module := DefaultVideoModuleImpl{}
	module.apiKey = apiKey
	module.applicationName = applicationName

	return &module
}
