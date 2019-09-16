package apimodel

// User - someone in the system
type User struct {
	UserID   string  `json:"userId"`
	Username string `json:"username"`
	Password string `json:"password,omitempty"`
	IsParent bool   `json:"isParent"`
}

// CloneUser - clone the provided user
func CloneUser(srcUser *User) *User {
	newUser := User{}

	newUser.UserID = srcUser.UserID
	newUser.Username = srcUser.Username
	newUser.Password = srcUser.Password
	newUser.IsParent = srcUser.IsParent

	return &newUser
}
