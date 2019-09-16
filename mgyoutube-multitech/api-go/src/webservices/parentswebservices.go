package webservices

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"modules"
	"net/http"
	"net/url"

	"github.com/gorilla/mux"
)

// ParentsWebService - a parents web service data structure
type ParentsWebService struct {
	userModule     modules.UserModule
	searchesModule modules.SearchesModule
}

func (webService ParentsWebService) authParentUser(responseWriter http.ResponseWriter, request *http.Request) {
	defer request.Body.Close()
	userCredentialJSONBytes, err := ioutil.ReadAll(request.Body)
	log.Println("authParentUser: userCredentialJson=", string(userCredentialJSONBytes))

	if err != nil {
		log.Println("authParentUser: getting body failed", err)
		responseWriter.WriteHeader(http.StatusInternalServerError)
	} else {
		// TODO: need to sanitize payload input before using
		userCredentialJSON := string(userCredentialJSONBytes)
		sanitizedUserCredentialJSONBytes := []byte(userCredentialJSON)
		sanitizedUserCredentialJSON := userCredentialJSON

		userCredential := MarshalUserCredentialFromJSON(sanitizedUserCredentialJSONBytes)
		log.Println("authParentUser: userCredential=", userCredential)

		authedUser := webService.userModule.AuthUser(userCredential)

		if authedUser == nil {
			log.Println("authParentUser: userCredentialJson=", sanitizedUserCredentialJSON, " failed auth, returning UNAUTHORIZED")
			responseWriter.WriteHeader(http.StatusUnauthorized)
		} else if !authedUser.IsParent {
			log.Println("authParentUser: userCredentialJson=", sanitizedUserCredentialJSON, " is not a parent returning UNAUTHORIZED")
			responseWriter.WriteHeader(http.StatusUnauthorized)
		} else {

			authedUser.Password = ""

			responseJSONBytes, _ := json.Marshal(authedUser)
			responseJSON := string(responseJSONBytes)

			log.Println("authParentUser: userCredentialJson=", responseJSON, " returning OK")

			responseWriter.Header().Set("Content-Type", "application/json")
			responseWriter.WriteHeader(http.StatusOK)
			responseWriter.Write(responseJSONBytes)
		}
	}
}

func (webService ParentsWebService) getChildrenForParent(responseWriter http.ResponseWriter, request *http.Request) {

	httpParams := mux.Vars(request)

	parentUsername := httpParams["parentusername"]
	log.Println("getChildrenForParent: parentUsername=", parentUsername)

	// TODO: need to sanitize payload input before using
	sanitizedParentUsername := parentUsername

	children, err := webService.userModule.GetChildrenForParent(sanitizedParentUsername)
	if err == nil {
		log.Println("getChildrenForParent: getChildrenForParent=", children)

		for _, child := range children {
			//log.Println("getChildrenForParent: child=", child)
			child.Password = ""
			//log.Println("getChildrenForParent: child=", child)
		}
		// children.forEach(u -> {
		//     u.password = null;
		// });
		log.Println("getChildrenForParent: getChildrenForParent=", children)

		var responseJSON string
		var responseJSONBytes []byte
		var marshalErr error

		if len(children) > 0 {
			log.Println("pre marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
			responseJSONBytes, marshalErr = json.Marshal(children)
			log.Println("post marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes, "marshalErr", marshalErr)
			if marshalErr != nil {
				log.Println("getChildrenForParent, error marshalling children", marshalErr)
			}
			responseJSON = string(responseJSONBytes)
			log.Println("post post marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes, "marshalErr", marshalErr)
		} else {
			log.Println("pre children count <=0 responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
			responseJSON = "[]"
			responseJSONBytes = []byte(responseJSON)
			log.Println("post children count <=0 responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
		}

		log.Println("getChildrenForParent: parentUsername=", parentUsername, " returning OK for size ", len(children), " and json=", responseJSON, " and bytes ", responseJSONBytes)

		responseWriter.Header().Set("Content-Type", "application/json")
		responseWriter.WriteHeader(http.StatusOK)
		responseWriter.Write(responseJSONBytes)
	} else {
		log.Println("getChildrenForParent: unable to find user ", sanitizedParentUsername, " returning NOT_FOUND ", err)
		responseWriter.WriteHeader(http.StatusNotFound)
	}
}

func (webService ParentsWebService) addUpdateChildToParent(responseWriter http.ResponseWriter, request *http.Request) {
	httpParams := mux.Vars(request)

	parentUsername := httpParams["parentusername"]
	childUsername := httpParams["childusername"]
	defer request.Body.Close()
	childUserJSONBytes, err := ioutil.ReadAll(request.Body)
	childUserJSON := string(childUserJSONBytes)

	if err != nil {
		log.Println("ParentsWebService.addUpdateChildToParent for ", parentUsername, " and ", childUsername, " failed to extract message body ", err)
		responseWriter.WriteHeader(http.StatusBadRequest)
		return
	}

	log.Println("ParentsWebService.addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername=", childUsername, ", childUserJson=", childUserJSON)

	// TODO: need to sanitize payload input before using
	sanitizedParentUsername := parentUsername
	// final String sanitizedChildUsername = childUsername;
	sanitizedChildUserJSONBytes := []byte(childUserJSON)
	//sanitizedChildUserJSON := childUserJSON

	childUser := MarshalUserFromJSON(sanitizedChildUserJSONBytes)
	log.Println("ParentsWebService.addUpdateChildToParent: childUser=", childUser)

	addedUpdatedUser, err := webService.userModule.AddUpdateChildToParent(sanitizedParentUsername, childUser)

	if err != nil {
		log.Println("ParentsWebService.userModule.addUpdateChildToParent for ", sanitizedParentUsername, " and ", childUser, " failed ", err)
		responseWriter.WriteHeader(http.StatusNotFound)
	} else if addedUpdatedUser == nil {
		log.Println("ParentsWebService.userModule.addUpdateChildToParent for ", sanitizedParentUsername, " and ", childUser, " failed and return nil for addedUpdatedUser")
		responseWriter.WriteHeader(http.StatusInternalServerError)
	} else {
		addedUpdatedUser.Password = ""

		responseJSONBytes, _ := json.Marshal(addedUpdatedUser)
		responseJSON := string(responseJSONBytes)

		log.Println("ParentsWebService.addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername=", childUsername, " returning OK", responseJSON)
		responseWriter.Header().Set("Content-Type", "application/json")
		responseWriter.WriteHeader(http.StatusOK)
		responseWriter.Write(responseJSONBytes)
	}
}

func (webService ParentsWebService) getSavedSearches(responseWriter http.ResponseWriter, request *http.Request) {

	httpParams := mux.Vars(request)

	parentUsername := httpParams["parentusername"]
	log.Println("getSavedSearches: parentUsername=", parentUsername)

	// TODO: need to sanitize payload input before using
	sanitizedParentUsername := parentUsername

	searches, err := webService.searchesModule.GetSavedSearchesForParent(sanitizedParentUsername)
	if err == nil {
		log.Println("getSavedSearches: GetSavedSearchesForParent=", searches)

		var responseJSON string
		var responseJSONBytes []byte
		var marshalErr error

		if len(searches) > 0 {
			//log.Println("pre marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
			responseJSONBytes, marshalErr = json.Marshal(searches)
			//log.Println("post marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes, "marshalErr", marshalErr)
			if marshalErr != nil {
				log.Println("getSavedSearches, error marshalling children", marshalErr)
			}
			responseJSON = string(responseJSONBytes)
			//log.Println("post post marshal responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes, "marshalErr", marshalErr)
		} else {
			//log.Println("pre children count <=0 responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
			responseJSON = "[]"
			responseJSONBytes = []byte(responseJSON)
			//log.Println("post children count <=0 responseJSON", responseJSON, "responseJSONBytes", responseJSONBytes)
		}

		log.Println("getSavedSearches: parentUsername=", sanitizedParentUsername, " returning OK for size ", len(searches), " and json=", responseJSON, " and bytes ", responseJSONBytes)

		responseWriter.Header().Set("Content-Type", "application/json")
		responseWriter.WriteHeader(http.StatusOK)
		responseWriter.Write(responseJSONBytes)
	} else {
		log.Println("getSavedSearches: unable to find user ", sanitizedParentUsername, " returning NOT_FOUND ", err)
		responseWriter.WriteHeader(http.StatusNotFound)
	}
}

func (webService ParentsWebService) saveSearch(responseWriter http.ResponseWriter, request *http.Request) {
	httpParams := mux.Vars(request)

	parentUsername := httpParams["parentusername"]
	searchPhrase := httpParams["searchphrase"]
	decodedSearchPhrase, decodingError := url.QueryUnescape(searchPhrase)

	log.Println("saveSearch: parentUsername=", parentUsername, ", searchPhrase=", searchPhrase, ", decodedSearchPhrase=", decodedSearchPhrase, ", decodingError=", decodingError)

	if decodingError != nil {
		responseWriter.WriteHeader(http.StatusInternalServerError)
		return
	}

	// TODO: need to sanitize payload input before using
	sanitizedParentUsername := parentUsername
	sanitizedSearchPhrase := decodedSearchPhrase

	addErr := webService.searchesModule.AddSearchToParent(sanitizedParentUsername, sanitizedSearchPhrase)

	if addErr != nil {
		log.Println("searchesModule.AddSearchToParent for ", sanitizedParentUsername, " and ", sanitizedSearchPhrase, " failed ", addErr)
		responseWriter.WriteHeader(http.StatusNotFound)
	} else {
		log.Println("saveSearch: parentUsername=", sanitizedParentUsername, ", searchPhrase=", sanitizedSearchPhrase, " returning CREATED")
		responseWriter.WriteHeader(http.StatusCreated)
		responseWriter.Write([]byte(""))
	}
}

// NewParentWebService - creates and returns a new ParentsWebService
func NewParentWebService(router *mux.Router, userModule modules.UserModule, searchesModule modules.SearchesModule) *ParentsWebService {

	webService := ParentsWebService{}
	webService.userModule = userModule
	webService.searchesModule = searchesModule

	router.HandleFunc("/api/parents/auth", webService.authParentUser).Methods("POST")
	router.HandleFunc("/api/parents/{parentusername}/children", webService.getChildrenForParent).Methods("GET")
	router.HandleFunc("/api/parents/{parentusername}/children/{childusername}", webService.addUpdateChildToParent).Methods("PUT")
	router.HandleFunc("/api/parents/{parentusername}/searches", webService.getSavedSearches).Methods("GET")
	router.HandleFunc("/api/parents/{parentusername}/searches/{searchphrase}", webService.saveSearch).Methods("PUT")

	return &webService
}
