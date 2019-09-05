package repos

import "apimodel"

// UserDataRepo - interface definition for a UserDataRepo
type UserDataRepo interface {
	GetUserByUsername(username string) *apimodel.User

	AddUser(user *apimodel.User) *apimodel.User

	RemoveUser(user *apimodel.User)

	ReplaceUser(userID int64, user *apimodel.User) *apimodel.User

	AddChildToParent(parentUserID int64, childUserID int64)

	GetChildrenForParent(parentUserID int64) []*apimodel.User
}
