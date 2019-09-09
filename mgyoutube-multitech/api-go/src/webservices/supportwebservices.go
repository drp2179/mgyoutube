package webservices

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"modules"
	"net/http"

	"github.com/gorilla/mux"
)

// SupportWebService - data structure for the support web service
type SupportWebService struct {
	userModule modules.UserModule
}

func (webService SupportWebService) createUpdateUser(responseWriter http.ResponseWriter, request *http.Request) {
	defer request.Body.Close()
	userJSONBytes, err := ioutil.ReadAll(request.Body)
	userJSON := string(userJSONBytes)

	if err != nil {
		log.Println("supportCreateuser: getting body failed", err)
		responseWriter.WriteHeader(http.StatusInternalServerError)
		return
	}

	log.Println("supportCreateuser: userJson=", userJSON)

	sanitizedUserJSON := userJSON
	sanitizedUserJSONBytes := []byte(sanitizedUserJSON)

	marsheledUser := MarshalUserFromJSON(sanitizedUserJSONBytes)
	log.Println("supportCreateuser: marsheledUser=", marsheledUser)

	createdUser := webService.userModule.CreateUser(marsheledUser)
	log.Println("supportCreateuser: createdUser=", createdUser)

	if createdUser == nil {
		log.Println("createUser failed for ", sanitizedUserJSON)
		responseWriter.WriteHeader(http.StatusInternalServerError)
		return
	}

	createdUser.Password = ""
	responseJSONBytes, _ := json.Marshal(createdUser)
	responseJSON := string(responseJSONBytes)

	log.Println("supportCreateuser: userJson=", sanitizedUserJSON, " returning OK and ", responseJSON)

	responseWriter.Header().Set("Content-Type", "application/json")
	responseWriter.WriteHeader(http.StatusOK)
	responseWriter.Write(responseJSONBytes)
}

func (webService SupportWebService) getCreateUpdateDeleteUserByUsername(responseWriter http.ResponseWriter, request *http.Request) {

	if request.Method == "DELETE" {
		webService.removeUserByUsername(responseWriter, request)
	} else if request.Method == "GET" {
		webService.getUserByUsername(responseWriter, request)
	} else if request.Method == "PUT" {
		webService.createUpdateUser(responseWriter, request)
	} else {
		responseWriter.WriteHeader(http.StatusBadRequest)
	}
}

func (webService SupportWebService) getUserByUsername(responseWriter http.ResponseWriter, request *http.Request) {
	httpParams := mux.Vars(request)

	username := httpParams["username"]
	log.Println("getUserByUsername: username=", username)

	santizedUsername := username
	user := webService.userModule.GetUser(santizedUsername)
	log.Println("getUserByUsername: user=", user)

	if user == nil {
		log.Println("getUserByUsername: username=", username, " returning NOT_FOUND")
		responseWriter.WriteHeader(http.StatusNotFound)
		return
	}

	user.Password = ""
	responseJSONBytes, _ := json.Marshal(user)
	responseJSON := string(responseJSONBytes)

	log.Println("getUserByUsername: username=", username, " returning OK ", responseJSON)

	responseWriter.Header().Set("Content-Type", "application/json")
	responseWriter.WriteHeader(http.StatusOK)
	responseWriter.Write(responseJSONBytes)
}

func (webService SupportWebService) removeUserByUsername(responseWriter http.ResponseWriter, request *http.Request) {
	httpParams := mux.Vars(request)

	username := httpParams["username"]
	log.Println("removeUserByUsername: username=", username)

	// TODO: need to sanitize payload input before using
	santizedUsername := username
	oldUser := webService.userModule.RemoveUser(santizedUsername)

	if oldUser == nil {
		log.Println("deleteUserByUsername: username=", username, " returning NOT_FOUND")
		responseWriter.WriteHeader(http.StatusNotFound)
		return
	}

	responseWriter.WriteHeader(http.StatusOK)
}

// NewSupportWebService - setup and return a new SupportWebService
func NewSupportWebService(router *mux.Router, userModule modules.UserModule) *SupportWebService {

	supportWebService := SupportWebService{}
	supportWebService.userModule = userModule

	router.HandleFunc("/api/support/users/{username}", supportWebService.getCreateUpdateDeleteUserByUsername).Methods("GET", "DELETE", "PUT")

	return &supportWebService
}
