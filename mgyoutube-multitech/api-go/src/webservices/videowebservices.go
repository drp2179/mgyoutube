package webservices

import (
	"encoding/json"
	"log"
	"modules"
	"net/http"

	"github.com/gorilla/mux"
)

// VideoWebService - data for a video web service
type VideoWebService struct {
	//userModule modules.UserModule
	videosModule modules.VideoModule
}

func (webService VideoWebService) searchVideos(responseWriter http.ResponseWriter, request *http.Request) {
	searchQuery, ok := request.URL.Query()["search"]

	if !ok || len(searchQuery[0]) < 1 {
		log.Println("search term are blank failing as 400")
		//return Response.status(400).build()
		responseWriter.WriteHeader(http.StatusBadRequest)
		return
	}

	searchTerms := searchQuery[0]
	log.Println("videosSearch: searchTerms='", searchTerms, "'")

	// TODO: need to sanitize payload input before using
	sanitizedSearchTerms := searchTerms

	videos, errSearch := webService.videosModule.Search(sanitizedSearchTerms)
	log.Println("videosModule.search: videos=", videos, ", errSearch=", errSearch)

	//final String responseJson = GSON.toJson(videos);
	responseJSONBytes, _ := json.Marshal(videos)
	responseJSON := string(responseJSONBytes)

	log.Println("videosSearch: searchTerms=", searchTerms, " returning OK, ", responseJSON)
	//return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
	responseWriter.Header().Set("Content-Type", "application/json")
	responseWriter.WriteHeader(http.StatusOK)
	responseWriter.Write(responseJSONBytes)
}

// NewVideoWebService - returns a new VideoWebService
func NewVideoWebService(router *mux.Router, videoModule modules.VideoModule) *VideoWebService {

	webService := VideoWebService{}
	webService.videosModule = videoModule

	router.HandleFunc("/api/videos", webService.searchVideos).Methods("GET")

	return &webService
}
