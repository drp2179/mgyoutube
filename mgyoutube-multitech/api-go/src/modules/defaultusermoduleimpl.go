package modules

import (
	"apimodel"
	"errors"
	"log"
	"repos"
)

// DefaultUserModuleImpl = structure for default user module impl
type DefaultUserModuleImpl struct {
	userDataRepo repos.UserDataRepo
}

//AuthUser - authenticate the user
func (userModule DefaultUserModuleImpl) AuthUser(userCredential *apimodel.UserCredential) *apimodel.User {
	user := userModule.userDataRepo.GetUserByUsername(userCredential.Username)

	if user != nil {
		if len(user.Password) == 0 {
			log.Println("user.password is empty")
			user = nil
		} else if len(userCredential.Password) == 0 {
			log.Println("userCredential.password is empty")
			user = nil
		} else if user.Password != userCredential.Password {
			log.Println("user.password(", user.Password, ") != userCredential.password(", userCredential.Password, ")")
			user = nil
		} else {
			log.Println("password match!")
			// password match!
		}
	} else {
		log.Println("getUserByUsername(", userCredential.Username, ") returned None")
	}

	return user
}

// GetUser - returns the user for the username
func (userModule DefaultUserModuleImpl) GetUser(username string) *apimodel.User {
	return userModule.userDataRepo.GetUserByUsername(username)
}

// CreateUser - creates a user based on the provided model
func (userModule DefaultUserModuleImpl) CreateUser(user *apimodel.User) *apimodel.User {
	if user.UserID == 0 {
		//log.Println("CreateUser, user", user)
		createdUser := userModule.userDataRepo.AddUser(user)
		//log.Println("CreateUser, createdUser", createdUser)
		return createdUser
	}
	//log.Println("CreateUser, returning nil")
	return nil
}

// UpdateUser - updates the associated user with the provided data
func (userModule DefaultUserModuleImpl) UpdateUser(userID int64, user *apimodel.User) *apimodel.User {
	return userModule.userDataRepo.ReplaceUser(userID, user)
}

// RemoveUser - removes the identified user
func (userModule DefaultUserModuleImpl) RemoveUser(username string) *apimodel.User {
	user := userModule.GetUser(username)

	if user != nil && user.UserID > 0 {
		userModule.userDataRepo.RemoveUser(user)
	}

	return user
}

// GetChildrenForParent - returns the children for the provided parent
func (userModule DefaultUserModuleImpl) GetChildrenForParent(parentUsername string) ([]*apimodel.User, error) {
	parentUser := userModule.GetUser(parentUsername)
	if parentUser == nil {
		return nil, errors.New("unable to find parent")
	}

	children := userModule.userDataRepo.GetChildrenForParent(parentUser.UserID)
	return children, nil
}

// AddUpdateChildToParent -
func (userModule DefaultUserModuleImpl) AddUpdateChildToParent(parentUsername string, childUser *apimodel.User) (*apimodel.User, error) {
	parentUser := userModule.GetUser(parentUsername)
	if parentUser == nil {
		return nil, errors.New("unable to find parent")
	}

	existingChildUser := userModule.GetUser(childUser.Username)
	if existingChildUser == nil {
		log.Println("creating child ", childUser)

		createdChildUser := userModule.CreateUser(childUser)
		if createdChildUser == nil {
			log.Println("creating child ", childUser, " failed ")
			return nil, errors.New("Creating child failed")
		}

		userModule.userDataRepo.AddChildToParent(parentUser.UserID, createdChildUser.UserID)
		return createdChildUser, nil
	}

	log.Println("updating child ", childUser, " as ", existingChildUser.UserID)
	userModule.userDataRepo.AddChildToParent(parentUser.UserID, existingChildUser.UserID)
	updatedUser := userModule.UpdateUser(existingChildUser.UserID, childUser)
	return updatedUser, nil
}

// NewDefaultUserModuleImpl - create a new NewDefaultUserModuleImpl
func NewDefaultUserModuleImpl(userDataRepo repos.UserDataRepo) *DefaultUserModuleImpl {
	defUserModuleImpl := DefaultUserModuleImpl{}
	defUserModuleImpl.userDataRepo = userDataRepo
	return &defUserModuleImpl
}
