package apimodel

// UserCredential - for doing authentication
type UserCredential struct {
	Username string `json:"username"`
	Password string `json:"password"`
}
