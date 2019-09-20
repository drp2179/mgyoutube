package repos

import (
	"apimodel"
	"couchfuncs"
	"fmt"
	"log"

	"github.com/google/uuid"
	"gopkg.in/couchbase/gocb.v1"
)

const (
	usersBucketName              = "users"
	usersBucketUsernameFieldname = "username"
	usersBucketPasswordFieldname = "password"
	usersBucketParentFieldname   = "isParent"
	usersBucketFindByUsername    = "SELECT META(users).id, users.* FROM users WHERE username = $1"
	usersBucketUsernameIndexName = "users-username-idx"

	parentChildBucketName            = "parentChild"
	parentChildBucketParentFieldname = "parentUserId"
	parentChildBucketChildFieldname  = "childUserId"
	parentChildBucketFindByParent    = "SELECT childUserId FROM parentChild WHERE parentUserId = $1"
	parentChildBucketParentIndexName = "parnetchild-parent-idx"
)

type couchUserCollectionDoc struct {
	ID       string `json:"id,omitempty"`
	Username string `json:"username"`
	Password string `json:"password"`
	IsParent bool   `json:"isParent"`
}

type couchParentChildCollectionDoc struct {
	//ID           string `json:"id,omitempty"`
	ParentUserID string `json:"parentUserId"`
	ChildUserID  string `json:"childUserId"`
}

// CouchUserDataRepoImpl - data for CouchUserDataRepoImpl
type CouchUserDataRepoImpl struct {
	cluster  *gocb.Cluster
	username string
	password string
}

// RepositoryStartup - to initialize the repo
func (repo *CouchUserDataRepoImpl) RepositoryStartup() error {
	clusterManager := repo.cluster.Manager(repo.username, repo.password)

	{
		if !couchfuncs.DoesBucketExist(clusterManager, usersBucketName) {
			createError := couchfuncs.CreateBasicBucket(clusterManager, usersBucketName)
			if createError != nil {
				log.Println("Unable to create bucket", usersBucketName, ":", createError)
				return createError
			}

			usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")
			if openBucketErr != nil {
				return openBucketErr
			}

			usersBucketManager := usersBucket.Manager(repo.username, repo.password)

			createPrimaryIndexError := usersBucketManager.CreatePrimaryIndex("", true, false)
			if createPrimaryIndexError != nil {
				return createPrimaryIndexError
			}

			createUsersUsernameIndexError := usersBucketManager.CreateIndex(usersBucketUsernameIndexName, []string{usersBucketUsernameFieldname}, true, false)
			if createUsersUsernameIndexError != nil {
				return createUsersUsernameIndexError
			}
		} else {
			usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")
			if openBucketErr != nil {
				return openBucketErr
			}

			usersBucketManager := usersBucket.Manager(repo.username, repo.password)
			flushErr := usersBucketManager.Flush()

			if flushErr != nil {
				return flushErr
			}
		}
	}
	{
		if !couchfuncs.DoesBucketExist(clusterManager, parentChildBucketName) {
			createError := couchfuncs.CreateBasicBucket(clusterManager, parentChildBucketName)
			if createError != nil {
				log.Println("Unable to create bucket", parentChildBucketName, ":", createError)
				return createError
			}
			parentChildBucket, openBucketErr := repo.cluster.OpenBucket(parentChildBucketName, "")
			if openBucketErr != nil {
				return openBucketErr
			}

			parentChildBucketManager := parentChildBucket.Manager(repo.username, repo.password)

			createPrimaryIndexError := parentChildBucketManager.CreatePrimaryIndex("", true, false)
			if createPrimaryIndexError != nil {
				return createPrimaryIndexError
			}

			createParentIndexError := parentChildBucketManager.CreateIndex(parentChildBucketParentIndexName, []string{parentChildBucketParentFieldname}, true, false)
			if createParentIndexError != nil {
				return createParentIndexError
			}
		} else {
			parentChildBucket, openBucketErr := repo.cluster.OpenBucket(parentChildBucketName, "")
			if openBucketErr != nil {
				return openBucketErr
			}

			parentChildBucketManager := parentChildBucket.Manager(repo.username, repo.password)
			flushErr := parentChildBucketManager.Flush()

			if flushErr != nil {
				return flushErr
			}
		}
	}

	return nil
}

// GetUserByUsername - get a user by username
func (repo CouchUserDataRepoImpl) GetUserByUsername(username string) *apimodel.User {
	log.Println("CouchUserDataRepoImpl.GetUserByUsername(", username, ")")

	myQuery := gocb.NewN1qlQuery(usersBucketFindByUsername)
	myQuery.Consistency(gocb.RequestPlus)
	myQueryParams := []interface{}{username}

	usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", usersBucketName, "bucket", openBucketErr)
		return nil
	}

	rows, err := usersBucket.ExecuteN1qlQuery(myQuery, myQueryParams)
	if err != nil {
		log.Println("error searching for phrases", err)
		return nil
	}

	var row couchUserCollectionDoc
	fetchError := rows.One(&row)
	if fetchError != nil {
		log.Println("error fetching by username ", username, fetchError)
		return nil
	}

	log.Println("row", row)

	user := apimodel.User{}
	user.UserID = row.ID
	user.Username = row.Username
	user.Password = row.Password
	user.IsParent = row.IsParent

	if err = rows.Close(); err != nil {
		fmt.Printf("Couldn't get all the rows: %s\n", err)
	}

	log.Println("CouchUserDataRepoImpl.getUserByUsername(", username, ") returning ", user)
	return &user
}

// AddUser - add a user to the system
func (repo *CouchUserDataRepoImpl) AddUser(user *apimodel.User) *apimodel.User {
	content := couchUserCollectionDoc{}
	content.ID = uuid.New().String()
	content.Username = user.Username
	content.Password = user.Password
	content.IsParent = user.IsParent

	usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", usersBucketName, "bucket", openBucketErr)
		return nil
	}

	insertedCas, insertErr := usersBucket.Insert(content.ID, &content, 0)
	log.Println("Couchdb inserted user", user.Username, ", ID", content.ID, "insertedCas", insertedCas, "insertErr", insertErr)

	//return repo.GetUserByUsername(user.Username)
	return repo.getUserByID(content.ID)
}

// RemoveUser - remove a user from the repo
func (repo CouchUserDataRepoImpl) RemoveUser(user *apimodel.User) {

	usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", usersBucketName, "bucket", openBucketErr)
		return
	}

	var content interface{}
	cas, getError := usersBucket.Get(user.UserID, &content)

	if getError != nil {
		log.Println("Error while getting (before removing)", user.UserID, "from", usersBucketName, "bucket", getError)
		return
	}

	_, removeError := usersBucket.Remove(user.UserID, cas)

	if removeError != nil {
		log.Println("Error while removing", user.UserID, "from", usersBucketName, "bucket", removeError)
		return
	}
}

// ReplaceUser - replace the user
func (repo CouchUserDataRepoImpl) ReplaceUser(userID string, user *apimodel.User) *apimodel.User {
	log.Println("CouchUserDataRepoImpl.ReplaceUser(", userID, ") with ", user)

	usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", usersBucketName, "bucket", openBucketErr)
		return nil
	}

	content := couchUserCollectionDoc{}
	cas, getError := usersBucket.Get(userID, &content)
	if getError != nil {
		log.Println("Error while getting document for", userID, "in", usersBucketName, "bucket", getError)
		return nil
	}

	content.Username = user.Username
	content.Password = user.Password
	content.IsParent = user.IsParent

	_, replaceError := usersBucket.Replace(userID, &content, cas, 0)

	if replaceError != nil {
		log.Println("Error while replacing", userID, "from", usersBucketName, "bucket", replaceError)
		return nil
	}

	return repo.getUserByID(userID)
}

// AddChildToParent - add a child to a parent
func (repo CouchUserDataRepoImpl) AddChildToParent(parentUserID string, childUserID string) {
	// TODO should probably worry about dups here...
	contentID := uuid.New().String()
	content := couchParentChildCollectionDoc{}
	content.ParentUserID = parentUserID
	content.ChildUserID = childUserID

	parentChildBucket, openBucketErr := repo.cluster.OpenBucket(parentChildBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", parentChildBucketName, "bucket", openBucketErr)
		return
	}

	insertedCas, insertErr := parentChildBucket.Insert(contentID, &content, 0)
	log.Println("Couchdb inserted parentUserID", parentUserID, ",childUserID", childUserID, ", docId", contentID, "insertedCas:", insertedCas, "insertErr", insertErr)
}

// GetChildrenForParent - get all of the children for a parent
func (repo CouchUserDataRepoImpl) GetChildrenForParent(parentUserID string) []*apimodel.User {
	var children []*apimodel.User

	parentChildBucket, openBucketErr := repo.cluster.OpenBucket(parentChildBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", parentChildBucketName, "bucket", openBucketErr)
		return nil
	}

	myQuery := gocb.NewN1qlQuery(parentChildBucketFindByParent)
	myQuery.Consistency(gocb.RequestPlus)
	myQueryParams := []interface{}{parentUserID}

	rows, err := parentChildBucket.ExecuteN1qlQuery(myQuery, myQueryParams)
	if err != nil {
		log.Println("error searching for parent", err)
		return nil
	}

	var row couchParentChildCollectionDoc
	for rows.Next(&row) {
		childUser := repo.getUserByID(row.ChildUserID)
		children = append(children, childUser)
	}

	if err = rows.Close(); err != nil {
		fmt.Printf("Couldn't get all the rows: %s\n", err)
	}

	return children
}

func (repo CouchUserDataRepoImpl) getUserByID(userID string) *apimodel.User {
	log.Println("CouchUserDataRepoImpl.getUserByID(", userID, ")")

	usersBucket, openBucketErr := repo.cluster.OpenBucket(usersBucketName, "")

	if openBucketErr != nil {
		log.Println("Error while opening", usersBucketName, "bucket", openBucketErr)
		return nil
	}

	row := couchUserCollectionDoc{}
	cas, getError := usersBucket.Get(userID, &row)
	if getError != nil {
		log.Println("error getting ", userID, getError)
		return nil
	}

	user := apimodel.User{}
	user.UserID = row.ID
	user.Username = row.Username
	user.Password = row.Password
	user.IsParent = row.IsParent

	log.Println("CouchUserDataRepoImpl.getUserById(", userID, "), CAS", cas, "returning ", user)
	return &user
}

// NewCouchUserDataRepoImpl - create a new CouchUserDataRepoImpl
func NewCouchUserDataRepoImpl(connectionString string, username string, password string) *CouchUserDataRepoImpl {
	repo := CouchUserDataRepoImpl{}

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
