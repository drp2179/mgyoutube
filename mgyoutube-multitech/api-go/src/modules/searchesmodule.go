package modules

// SearchesModule - the interface for a search module
type SearchesModule interface {
	GetSavedSearchesForParent(parentUsername string) ([]string, error)

	AddSearchToParent(parentUsername string, searchPhrase string) error

	RemoveSearchFromParent(parentUsername string, searchPhrase string) error
}
