package repos

// SearchesDataRepo - interface for search data repositories
type SearchesDataRepo interface {
	RepositoryStartup()

	GetSearchesForParentUser(userID string) []string

	AddSearchToParentUser(parentUserID string, searchPhrase string)
}
