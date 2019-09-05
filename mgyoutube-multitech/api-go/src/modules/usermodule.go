package modules

import "apimodel"

// UserModule - defines the interface for a UserModule
type UserModule interface {
	AuthUser(userCredential *apimodel.UserCredential) *apimodel.User

	GetUser(username string) *apimodel.User

	CreateUser(user *apimodel.User) *apimodel.User
	UpdateUser(userID int64, user *apimodel.User) *apimodel.User
	RemoveUser(username string) *apimodel.User

	GetChildrenForParent(parentUsername string) ([]*apimodel.User, error)
	AddUpdateChildToParent(parentUsername string, childUser *apimodel.User) (*apimodel.User, error)
}
