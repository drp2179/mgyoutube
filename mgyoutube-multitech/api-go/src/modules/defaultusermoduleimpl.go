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

// GetUserByUsername - returns the user for the username
func (userModule DefaultUserModuleImpl) GetUserByUsername(username string) *apimodel.User {
	return userModule.userDataRepo.GetUserByUsername(username)
}

// CreateUpdateUser - create or update based on if the username already exists
func (userModule *DefaultUserModuleImpl) CreateUpdateUser(user *apimodel.User) *apimodel.User {
	existingUser := userModule.userDataRepo.GetUserByUsername(user.Username)
	if existingUser != nil {
		if len(user.UserID) == 0 {
			user.UserID = existingUser.UserID
		}

		log.Println("createUpdateUser is updating existing user ", existingUser, " to be ", user)
		return userModule.UpdateUser(existingUser.UserID, user)
	}

	log.Println("createUpdateUser is creating new user ", user)
	return userModule.CreateUser(user)
}

// CreateUser - creates a user based on the provided model
func (userModule DefaultUserModuleImpl) CreateUser(user *apimodel.User) *apimodel.User {
	if len(user.UserID) == 0 {
		//log.Println("CreateUser, user", user)
		createdUser := userModule.userDataRepo.AddUser(user)
		//log.Println("CreateUser, createdUser", createdUser)
		return createdUser
	}
	//log.Println("CreateUser, returning nil")
	return nil
}

// UpdateUser - updates the associated user with the provided data
func (userModule DefaultUserModuleImpl) UpdateUser(userID string, user *apimodel.User) *apimodel.User {
	log.Println("DefaultUserModuleImpl.UpdateUser, userID", userID, "as user ", user)
	return userModule.userDataRepo.ReplaceUser(userID, user)
}

// RemoveUser - removes the identified user
func (userModule DefaultUserModuleImpl) RemoveUser(username string) *apimodel.User {
	user := userModule.GetUserByUsername(username)

	if user != nil && len(user.UserID) != 0 {
		userModule.userDataRepo.RemoveUser(user)
	}

	return user
}

// GetChildrenForParent - returns the children for the provided parent
func (userModule DefaultUserModuleImpl) GetChildrenForParent(parentUsername string) ([]*apimodel.User, error) {
	parentUser := userModule.GetUserByUsername(parentUsername)
	if parentUser == nil {
		return nil, errors.New("unable to find parent")
	}

	children := userModule.userDataRepo.GetChildrenForParent(parentUser.UserID)
	return children, nil
}

// AddUpdateChildToParent -
func (userModule DefaultUserModuleImpl) AddUpdateChildToParent(parentUsername string, childUser *apimodel.User) (*apimodel.User, error) {
	log.Println("DefaultUserModuleImpl.AddUpdateChildToParent, parentUsername", parentUsername, " childUser ", childUser)
	parentUser := userModule.GetUserByUsername(parentUsername)
	if parentUser == nil {
		return nil, errors.New("unable to find parent")
	}

	existingChildUser := userModule.GetUserByUsername(childUser.Username)
	if existingChildUser == nil {
		log.Println("DefaultUserModuleImpl.AddUpdateChildToParent, creating child ", childUser)

		createdChildUser := userModule.CreateUser(childUser)
		if createdChildUser == nil {
			log.Println("DefaultUserModuleImpl.AddUpdateChildToParent, creating child ", childUser, " failed ")
			return nil, errors.New("Creating child failed")
		}

		userModule.userDataRepo.AddChildToParent(parentUser.UserID, createdChildUser.UserID)
		return createdChildUser, nil
	}

	log.Println("DefaultUserModuleImpl.AddUpdateChildToParent, updating child ", childUser, " as ", existingChildUser.UserID)
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
