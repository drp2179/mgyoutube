package repos

// SearchesDataRepo - interface for search data repositories
type SearchesDataRepo interface {
	GetSearchesForParentUser(userID int64) []string

	AddSearchToParentUser(parentUserID int64, searchPhrase string)
}
