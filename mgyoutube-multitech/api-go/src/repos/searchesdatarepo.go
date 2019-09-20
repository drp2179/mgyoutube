package repos

// SearchesDataRepo - interface for search data repositories
type SearchesDataRepo interface {
	RepositoryStartup() error

	GetSearchesForParentUser(userID string) []string

	AddSearchToParentUser(parentUserID string, searchPhrase string)

	RemoveSearchFromParent(parentUserID string, searchPhrase string)
}
