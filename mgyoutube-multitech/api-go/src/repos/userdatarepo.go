package repos

import "apimodel"

// UserDataRepo - interface definition for a UserDataRepo
type UserDataRepo interface {
	RepositoryStartup() error

	GetUserByUsername(username string) *apimodel.User

	AddUser(user *apimodel.User) *apimodel.User

	RemoveUser(user *apimodel.User)

	ReplaceUser(userID string, user *apimodel.User) *apimodel.User

	AddChildToParent(parentUserID string, childUserID string)

	GetChildrenForParent(parentUserID string) []*apimodel.User
}
