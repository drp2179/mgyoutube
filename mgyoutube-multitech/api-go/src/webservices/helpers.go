package webservices

import (
	"apimodel"
	"encoding/json"
	"log"
)

// MarshalUserCredentialFromJSON - returns a UserCredential based on the provided byte array
func MarshalUserCredentialFromJSON(userCredentialJSONBytes []byte) *apimodel.UserCredential {
	var userCredential apimodel.UserCredential
	err := json.Unmarshal(userCredentialJSONBytes, &userCredential)
	if err != nil {
		log.Println("error:", err)
		return nil
	}
	log.Println("userCredential:", userCredential)
	return &userCredential
}

// MarshalUserFromJSON - returns a User based on the provided byte array
func MarshalUserFromJSON(userJSONBytes []byte) *apimodel.User {
	var user apimodel.User
	err := json.Unmarshal(userJSONBytes, &user)
	if err != nil {
		log.Println("error:", err)
		return nil
	}
	return &user
}
