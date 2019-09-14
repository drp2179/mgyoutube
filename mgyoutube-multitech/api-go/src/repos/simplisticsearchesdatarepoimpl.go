package repos

// SimplisticSearchesDataRepoImpl - data for simple search data repository
type SimplisticSearchesDataRepoImpl struct {
	parentSearches map[int64][]string
}

// GetSearchesForParentUser - get the searches for the give parent
func (repo *SimplisticSearchesDataRepoImpl) GetSearchesForParentUser(parentUserID int64) []string {
	existingSearches, exists := repo.parentSearches[parentUserID]
	if exists {
		// probably should clone this
		return existingSearches
	}
	return make([]string, 0)
}

// AddSearchToParentUser - add a search to the give parent
func (repo *SimplisticSearchesDataRepoImpl) AddSearchToParentUser(parentUserID int64, searchPhrase string) {
	existingSearches, exists := repo.parentSearches[parentUserID]

	if !exists {
		existingSearches = make([]string, 0)
	}

	existingSearches = append(existingSearches, searchPhrase)
	repo.parentSearches[parentUserID] = existingSearches
}

// NewSimplisticSearchesDataRepoImpl - create a new instance of SimplisticSearchesDataRepoImpl
func NewSimplisticSearchesDataRepoImpl() *SimplisticSearchesDataRepoImpl {
	repo := SimplisticSearchesDataRepoImpl{}
	repo.parentSearches = make(map[int64][]string)

	return &repo
}
