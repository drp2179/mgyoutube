package modules

import "apimodel"

// VideoModule - defines the interface for a VideoModule
type VideoModule interface {
	Search(searchTerms string) ([]*apimodel.Video, error)
}
