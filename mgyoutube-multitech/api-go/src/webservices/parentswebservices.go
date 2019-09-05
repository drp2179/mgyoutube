package webservices

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"modules"
	"net/http"

	"github.com/gorilla/mux"
)

// ParentsWebService - a parents web service data structure
type ParentsWebService struct {
	userModule modules.UserModule
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
		log.Println("addUpdateChildToParent for ", parentUsername, " and ", childUsername, " failed to extract message body ", err)
		responseWriter.WriteHeader(http.StatusBadRequest)
		return
	}

	log.Println("addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername=", childUsername, ", childUserJson=", childUserJSON)

	// TODO: need to sanitize payload input before using
	sanitizedParentUsername := parentUsername
	// final String sanitizedChildUsername = childUsername;
	sanitizedChildUserJSONBytes := []byte(childUserJSON)
	//sanitizedChildUserJSON := childUserJSON

	childUser := MarshalUserFromJSON(sanitizedChildUserJSONBytes)
	log.Println("addUpdateChildToParent: childUser=", childUser)

	addedUpdatedUser, err := webService.userModule.AddUpdateChildToParent(sanitizedParentUsername, childUser)

	if err != nil {
		log.Println("userModule.addUpdateChildToParent for ", sanitizedParentUsername, " and ", childUser, " failed ", err)
		responseWriter.WriteHeader(http.StatusNotFound)
	} else {
		addedUpdatedUser.Password = ""

		responseJSONBytes, _ := json.Marshal(addedUpdatedUser)
		responseJSON := string(responseJSONBytes)

		log.Println("addUpdateChildToParent: parentUsername=", parentUsername, ", childUsername=", childUsername, " returning OK", responseJSON)
		responseWriter.Header().Set("Content-Type", "application/json")
		responseWriter.WriteHeader(http.StatusOK)
		responseWriter.Write(responseJSONBytes)
	}
}

// NewParentWebService - creates and returns a new ParentsWebService
func NewParentWebService(router *mux.Router, userModule modules.UserModule) *ParentsWebService {

	webService := ParentsWebService{}
	webService.userModule = userModule

	router.HandleFunc("/api/parents/auth", webService.authParentUser).Methods("POST")
	router.HandleFunc("/api/parents/{parentusername}/children", webService.getChildrenForParent).Methods("GET")
	router.HandleFunc("/api/parents/{parentusername}/children/{childusername}", webService.addUpdateChildToParent).Methods("PUT")

	return &webService
}
