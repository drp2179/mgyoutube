package repos

import (
	"apimodel"
	"log"
	"strconv"
)

// SimpleUserDataRepoImpl - data for SimpleUserDataRepoImpl
type SimpleUserDataRepoImpl struct {
	usernameMap       map[string]*apimodel.User
	userIDMap         map[string]*apimodel.User
	parentChildrenMap map[string][]string
	nextUserID        int
}

// RepositoryStartup - to initialize the repo
func (userDataRepo *SimpleUserDataRepoImpl) RepositoryStartup() error {
	// nothing to do here
	return nil
}

// GetUserByUsername - get a user by username
func (userDataRepo SimpleUserDataRepoImpl) GetUserByUsername(username string) *apimodel.User {
	user, exists := userDataRepo.usernameMap[username]
	if exists {
		// cloning so modds after return do not affect maps
		clonedUser := apimodel.CloneUser(user)
		return clonedUser
	}
	return nil
}

// AddUser - add a user to the system
func (userDataRepo *SimpleUserDataRepoImpl) AddUser(user *apimodel.User) *apimodel.User {
	// cloning so that we can mutate userId without affecting the input object
	addedUser := apimodel.CloneUser(user)
	addedUser.UserID = strconv.Itoa(userDataRepo.nextUserID)
	userDataRepo.nextUserID++

	userDataRepo.userIDMap[addedUser.UserID] = addedUser
	userDataRepo.usernameMap[addedUser.Username] = addedUser

	// cloning so that mods after return do not mutate maps
	return apimodel.CloneUser(addedUser)
}

// RemoveUser - remove a user from the repo
func (userDataRepo SimpleUserDataRepoImpl) RemoveUser(user *apimodel.User) {
	delete(userDataRepo.userIDMap, user.UserID)
	delete(userDataRepo.usernameMap, user.Username)
}

// ReplaceUser - replace the user
func (userDataRepo SimpleUserDataRepoImpl) ReplaceUser(userID string, user *apimodel.User) *apimodel.User {
	_, existing := userDataRepo.userIDMap[userID]

	if existing {
		// cloning so that we can mutate userId without affecting the input object
		replacingUser := apimodel.CloneUser(user)
		replacingUser.UserID = userID

		userDataRepo.userIDMap[replacingUser.UserID] = replacingUser
		userDataRepo.usernameMap[replacingUser.Username] = replacingUser

		// cloning so that mods after return do not mutate maps
		clonedReplacingUser := apimodel.CloneUser(replacingUser)
		return clonedReplacingUser
	}

	return nil
}

// AddChildToParent - add a child to a parent
func (userDataRepo SimpleUserDataRepoImpl) AddChildToParent(parentUserID string, childUserID string) {
	_, existingParent := userDataRepo.parentChildrenMap[parentUserID]
	if !existingParent {
		userDataRepo.parentChildrenMap[parentUserID] = make([]string, 0)
	}

	childrenUserIds := userDataRepo.parentChildrenMap[parentUserID]

	hasChildID := checkArrayStringForValue(childrenUserIds, childUserID)
	if !hasChildID {
		childrenUserIds = append(childrenUserIds, childUserID)
		userDataRepo.parentChildrenMap[parentUserID] = childrenUserIds
	}
}

// GetChildrenForParent - get all of the children for a parent
func (userDataRepo SimpleUserDataRepoImpl) GetChildrenForParent(parentUserID string) []*apimodel.User {
	var children []*apimodel.User
	childrenUserIds, exists := userDataRepo.parentChildrenMap[parentUserID]

	if exists {

		for _, childUserID := range childrenUserIds {
			user := userDataRepo.getUserByID(childUserID)
			if user != nil {
				children = append(children, user)
			} else {
				log.Print("unknown child userid ", childUserID)
			}
		}
	}

	return children
}

func (userDataRepo SimpleUserDataRepoImpl) getUserByID(userID string) *apimodel.User {
	return userDataRepo.userIDMap[userID]
}

func checkArrayInt64ForValue(slice []int64, value int64) bool {
	for _, n := range slice {
		if n == value {
			return true
		}
	}

	return false
}

func checkArrayStringForValue(slice []string, value string) bool {
	for _, n := range slice {
		if n == value {
			return true
		}
	}

	return false
}

// NewSimpleUserDataRepoImpl - create a new SimpleUserDataRepoImpl
func NewSimpleUserDataRepoImpl() *SimpleUserDataRepoImpl {
	userDataRepo := SimpleUserDataRepoImpl{}

	userDataRepo.nextUserID = 1
	userDataRepo.userIDMap = make(map[string]*apimodel.User)
	userDataRepo.usernameMap = make(map[string]*apimodel.User)
	userDataRepo.parentChildrenMap = make(map[string][]string)

	return &userDataRepo
}
