package repos

import (
	"couchfuncs"
	"fmt"
	"log"

	"github.com/google/uuid"
	"gopkg.in/couchbase/gocb.v1"
)

const (
	searchesBucketName                  = "searches"
	searchesBucketParentIndexName       = "searches-parentuserid-idx"
	searchesBucketParentFieldname       = "parentUserId"
	searchesBucketSearchPhraseFieldname = "phrase"
	searchesFindPhraseByParent          = "SELECT phrase FROM `searches` WHERE parentUserId = $1"
	searchesDeletePhraseAndParent       = "DELETE FROM `searches` WHERE parentUserId = $1 AND phrase = $2"
)

type couchParentPhrase struct {
	ParentUserID string `json:"parentUserId"`
	Phrase       string `json:"phrase"`
}

// CouchSearchesDataRepoImpl - data for using CouchDB for the search data repository
type CouchSearchesDataRepoImpl struct {
	cluster  *gocb.Cluster
	username string
	password string
}

// RepositoryStartup - to initialize the repo
func (repo *CouchSearchesDataRepoImpl) RepositoryStartup() error {

	clusterManager := repo.cluster.Manager(repo.username, repo.password)
	if !couchfuncs.DoesBucketExist(clusterManager, searchesBucketName) {

		createError := couchfuncs.CreateBasicBucket(clusterManager, searchesBucketName)
		if createError != nil {
			log.Println("Unable to create bucket", searchesBucketName, ":", createError)
			return createError
		}

		searchesBucket, openBucketErr := repo.cluster.OpenBucket(searchesBucketName, "")
		if openBucketErr != nil {
			log.Println("Unable to open", searchesBucketName, "bucket:", openBucketErr)
			return openBucketErr
		}

		bucketManager := searchesBucket.Manager(repo.username, repo.password)

		createPrimaryIndexError := bucketManager.CreatePrimaryIndex("", true, true)
		if createPrimaryIndexError != nil {
			log.Println("Unable to create primary index for", searchesBucketName, "bucket:", createPrimaryIndexError)
			return createPrimaryIndexError
		}

		createParentIndexError := bucketManager.CreateIndex(searchesBucketParentIndexName, []string{searchesBucketParentFieldname}, true, true)
		if createParentIndexError != nil {
			log.Println("Unable to create index", searchesBucketParentIndexName, " for", searchesBucketName, "bucket:", createParentIndexError)
			return createParentIndexError
		}
	} else {
		searchesBucket, openBucketErr := repo.cluster.OpenBucket(searchesBucketName, "")
		if openBucketErr != nil {
			log.Println("Unable to open", searchesBucketName, "bucket:", openBucketErr)
			return openBucketErr
		}

		bucketManager := searchesBucket.Manager(repo.username, repo.password)
		flushErr := bucketManager.Flush()

		if flushErr != nil {
			log.Println("Unable to flush", searchesBucketName, "bucket:", flushErr)
			return flushErr
		}
	}

	return nil
}

// GetSearchesForParentUser - get the searches for the given parent
func (repo *CouchSearchesDataRepoImpl) GetSearchesForParentUser(parentUserID string) []string {
	searches := make([]string, 0)

	searchesBucket, openBucketErr := repo.cluster.OpenBucket(searchesBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", searchesBucketName, "bucket", openBucketErr)
	}

	myQuery := gocb.NewN1qlQuery(searchesFindPhraseByParent)
	myQuery.Consistency(gocb.RequestPlus)
	myQueryParams := []interface{}{parentUserID}

	rows, err := searchesBucket.ExecuteN1qlQuery(myQuery, myQueryParams)
	if err != nil {
		log.Println("error searching for phrases", err)
	}

	var row couchParentPhrase
	for rows.Next(&row) {
		phrase := row.Phrase
		searches = append(searches, phrase)
	}

	if err = rows.Close(); err != nil {
		fmt.Printf("Couldn't get all the rows: %s\n", err)
	}

	//log.Println("Metrics: ErrorCount=", rows.Metrics().ErrorCount, "ResultCount=", rows.Metrics().ResultCount, "WarningCount=", rows.Metrics().WarningCount)

	log.Println("Couchdb searching", searchesBucketName, "for parentUserId", parentUserID, "returning", len(searches), "phrases")

	return searches
}

// AddSearchToParentUser - add a search to the given parent
func (repo *CouchSearchesDataRepoImpl) AddSearchToParentUser(parentUserID string, searchPhrase string) {
	log.Println("AddSearchToParentUser parentUserID", parentUserID, "searchPhrase", searchPhrase)

	content := couchParentPhrase{}
	content.ParentUserID = parentUserID
	content.Phrase = searchPhrase

	id := uuid.New().String()

	searchesBucket, openBucketErr := repo.cluster.OpenBucket(searchesBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", searchesBucketName, "bucket", openBucketErr)
	}

	insertedCas, insertErr := searchesBucket.Insert(id, &content, 0)
	log.Println("Couchdb inserted parentUserId", parentUserID, "and searchPhrase", searchPhrase, ", docId:", insertedCas, "insertErr", insertErr)
}

// RemoveSearchFromParent - remove a search from the given parent
func (repo *CouchSearchesDataRepoImpl) RemoveSearchFromParent(parentUserID string, searchPhrase string) {
	log.Println("RemoveSearchFromParent parentUserID", parentUserID, "searchPhrase", searchPhrase)

	searchesBucket, openBucketErr := repo.cluster.OpenBucket(searchesBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", searchesBucketName, "bucket", openBucketErr)
	}

	myQuery := gocb.NewN1qlQuery(searchesDeletePhraseAndParent)
	myQuery.Consistency(gocb.RequestPlus)
	myQueryParams := []interface{}{parentUserID, searchPhrase}

	rows, err := searchesBucket.ExecuteN1qlQuery(myQuery, myQueryParams)

	log.Println("Couchdb deleted parentUserId ", parentUserID, " and searchPhrase ", searchPhrase, ": rows", rows, "err", err)
}

// NewCouchSearchesDataRepoImpl - create a new instance of CouchSearchesDataRepoImpl
func NewCouchSearchesDataRepoImpl(connectionString string, username string, password string) *CouchSearchesDataRepoImpl {
	repo := CouchSearchesDataRepoImpl{}

	cluster, _ := gocb.Connect(connectionString)
	cluster.Authenticate(gocb.PasswordAuthenticator{
		Username: username,
		Password: password,
	})

	repo.cluster = cluster
	repo.username = username
	repo.password = password

	return &repo
}
