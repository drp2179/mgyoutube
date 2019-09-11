package apimodel

// Video - for encapsulating videos
type Video struct {
	VideoID         string `json:"videoId"`
	Title           string `json:"title"`
	Description     string `json:"description"`
	ChannelID       string `json:"channelId"`
	ChannelTitle    string `json:"channelTitle"`
	PublishedAt     string `json:"publishedAt"`
	ThumbnailURL    string `json:"thumbnailUrl"`
	ThumbnailHeight int64  `json:"thumbnailHeight"`
	ThumbnailWidth  int64  `json:"thumbnailWidth"`
}
