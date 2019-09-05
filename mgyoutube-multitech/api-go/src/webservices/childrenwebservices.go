package webservices

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"modules"
	"net/http"

	"github.com/gorilla/mux"
)

// ChildrenWebService - data for a childrens web service
type ChildrenWebService struct {
	userModule modules.UserModule
}

func (webService ChildrenWebService) authChildrenUser(responseWriter http.ResponseWriter, request *http.Request) {
	defer request.Body.Close()
	userCredentialJSONBytes, err := ioutil.ReadAll(request.Body)
	log.Println("authChildrenUser: userCredentialJson=", string(userCredentialJSONBytes))

	if err != nil {
		log.Println("authChildrenUser: getting body failed", err)
		responseWriter.WriteHeader(http.StatusInternalServerError)
		return
	}
	// TODO: need to sanitize payload input before using
	userCredentialJSON := string(userCredentialJSONBytes)
	sanitizedUserCredentialJSONBytes := []byte(userCredentialJSON)
	sanitizedUserCredentialJSON := userCredentialJSON

	userCredential := MarshalUserCredentialFromJSON(sanitizedUserCredentialJSONBytes)
	log.Println("authChildrenUser: userCredential=", userCredential)

	authedUser := webService.userModule.AuthUser(userCredential)

	if authedUser == nil {
		log.Println("authChildrenUser: userCredentialJson=", sanitizedUserCredentialJSON, " failed auth, returning UNAUTHORIZED")
		responseWriter.WriteHeader(http.StatusUnauthorized)
	} else if authedUser.IsParent {
		log.Println("authChildrenUser: userCredentialJson=", sanitizedUserCredentialJSON, " is a parent returning UNAUTHORIZED")
		responseWriter.WriteHeader(http.StatusUnauthorized)
	} else {

		authedUser.Password = ""

		responseJSONBytes, _ := json.Marshal(authedUser)
		responseJSON := string(responseJSONBytes)

		log.Println("authChildrenUser: userCredentialJson=", responseJSON, " returning OK")

		responseWriter.Header().Set("Content-Type", "application/json")
		responseWriter.WriteHeader(http.StatusOK)
		responseWriter.Write(responseJSONBytes)
	}
}

// NewChildrenWebService - returns a new ChildrensWebService
func NewChildrenWebService(router *mux.Router, userModule modules.UserModule) *ChildrenWebService {

	webService := ChildrenWebService{}
	webService.userModule = userModule

	router.HandleFunc("/api/children/auth", webService.authChildrenUser).Methods("POST")

	return &webService
}
