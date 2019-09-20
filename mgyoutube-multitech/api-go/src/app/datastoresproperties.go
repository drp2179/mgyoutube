package main

import (
	"encoding/json"
	"io/ioutil"
)

// DatastoresProperties - Datastores properties read from a config file
type DatastoresProperties struct {
	MongoConnectionString string `json:"mongoConnectionString"`
	MongoDatabaseName     string `json:"mongoDatabaseName"`
	CouchConnectionString string `json:"couchConnectionString"`
	CouchUsername         string `json:"couchUsername"`
	CouchPassword         string `json:"couchPassword"`
}

// ReadDatastoresProperties - read the config file and return the YouTube properties
func ReadDatastoresProperties() (*DatastoresProperties, error) {

	properties := DatastoresProperties{}

	file, errReadFile := ioutil.ReadFile("secrets/datastores.json")
	if errReadFile != nil {
		return nil, errReadFile
	}

	errUnmarshal := json.Unmarshal([]byte(file), &properties)

	if errUnmarshal != nil {
		return nil, errUnmarshal
	}

	return &properties, nil
}
