package modules

import (
	"errors"
	"log"
	"repos"
)

// DefaultSearchesModuleImpl - default searches module implementation
type DefaultSearchesModuleImpl struct {
	userDataRepo     repos.UserDataRepo
	searchesDataRepo repos.SearchesDataRepo
}

// GetSavedSearchesForParent - get the saved searches for the parent
func (module *DefaultSearchesModuleImpl) GetSavedSearchesForParent(parentUsername string) ([]string, error) {
	parentUser := module.userDataRepo.GetUserByUsername(parentUsername)

	if parentUser == nil {
		return nil, errors.New("unable to find user " + parentUsername)
	}

	searches := module.searchesDataRepo.GetSearchesForParentUser(parentUser.UserID)

	log.Println(
		"getSavedSearchesForParent for ", parentUsername, " returning searches.size ", len(searches))

	return searches, nil
}

// AddSearchToParent - add the search to the named parent
func (module *DefaultSearchesModuleImpl) AddSearchToParent(parentUsername string, searchPhrase string) error {
	parentUser := module.userDataRepo.GetUserByUsername(parentUsername)

	if parentUser == nil {
		return errors.New("unable to find user " + parentUsername)
	}

	module.searchesDataRepo.AddSearchToParentUser(parentUser.UserID, searchPhrase)

	return nil
}

// NewDefaultSearchesModuleImpl - create a new instance of DefaultSearchesModuleImpl
func NewDefaultSearchesModuleImpl(userDataRepo repos.UserDataRepo, searchesDataRepo repos.SearchesDataRepo) *DefaultSearchesModuleImpl {
	module := DefaultSearchesModuleImpl{}
	module.userDataRepo = userDataRepo
	module.searchesDataRepo = searchesDataRepo

	return &module
}
