package main

import (
	"encoding/json"
	"io/ioutil"
)

// YouTubeProperties - YouTube properties read from a config file
type YouTubeProperties struct {
	APIKey          string `json:"apikey"`
	ApplicationName string `json:"applicationame"`
}

// ReadYouTubeProperties - read the config file and return the YouTube properties
func ReadYouTubeProperties() (*YouTubeProperties, error) {

	youTubeProperties := YouTubeProperties{}

	file, errReadFile := ioutil.ReadFile("secrets/youtube.json")
	if errReadFile != nil {
		return nil, errReadFile
	}

	errUnmarshal := json.Unmarshal([]byte(file), &youTubeProperties)

	if errUnmarshal != nil {
		return nil, errUnmarshal
	}

	return &youTubeProperties, nil
}
