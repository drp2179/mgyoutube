package repos

import (
	"gofuncs"
)

// SimplisticSearchesDataRepoImpl - data for simple search data repository
type SimplisticSearchesDataRepoImpl struct {
	parentSearches map[string][]string
}

// RepositoryStartup - to initialize the repo
func (repo *SimplisticSearchesDataRepoImpl) RepositoryStartup() error {
	// nothing to do here
	return nil
}

// GetSearchesForParentUser - get the searches for the given parent
func (repo *SimplisticSearchesDataRepoImpl) GetSearchesForParentUser(parentUserID string) []string {
	existingSearches, exists := repo.parentSearches[parentUserID]
	if exists {
		// probably should clone this
		return existingSearches
	}
	return make([]string, 0)
}

// AddSearchToParentUser - add a search to the given parent
func (repo *SimplisticSearchesDataRepoImpl) AddSearchToParentUser(parentUserID string, searchPhrase string) {
	existingSearches, exists := repo.parentSearches[parentUserID]

	if !exists {
		existingSearches = make([]string, 0)
	}

	existingSearches = append(existingSearches, searchPhrase)
	repo.parentSearches[parentUserID] = existingSearches
}

// RemoveSearchFromParent - remove a search from the given parent
func (repo *SimplisticSearchesDataRepoImpl) RemoveSearchFromParent(parentUserID string, searchPhrase string) {
	existingSearches, exists := repo.parentSearches[parentUserID]

	if exists {
		index := gofuncs.FindStringIndexInStringSlice(existingSearches, searchPhrase)
		if index >= 0 {
			existingSearches = gofuncs.RemoveIndexFromSlice(existingSearches, index)

			repo.parentSearches[parentUserID] = existingSearches
		}
	}
}

// NewSimplisticSearchesDataRepoImpl - create a new instance of SimplisticSearchesDataRepoImpl
func NewSimplisticSearchesDataRepoImpl() *SimplisticSearchesDataRepoImpl {
	repo := SimplisticSearchesDataRepoImpl{}
	repo.parentSearches = make(map[string][]string)

	return &repo
}
