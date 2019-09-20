package repos

import (
	"context"
	"log"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

// SearchesCollectionName - the name of the searches collection
const SearchesCollectionName string = "searches"

// SearchesCollectionParentFieldname - the name of the searches parent user id field
const SearchesCollectionParentFieldname string = "parentUserId"

// SearchesCollectionSearchPhraseFieldname - the name of the searches search phrase field
const SearchesCollectionSearchPhraseFieldname string = "phrase"

type mongoParentChild struct {
	ParentUserID string `bson:"parentUserId"`
	Phrase       string `bson:"phrase"`
}

// MongoSearchesDataRepoImpl - data for using Mongo for the search data repository
type MongoSearchesDataRepoImpl struct {
	client   *mongo.Client
	database *mongo.Database
}

// RepositoryStartup - to initialize the repo
func (repo *MongoSearchesDataRepoImpl) RepositoryStartup() error {
	ctx, contextCancel := context.WithTimeout(context.Background(), 16*time.Second)
	defer contextCancel()

	searchesCollection := repo.database.Collection(SearchesCollectionName)
	if searchesCollection != nil {
		dropErr := searchesCollection.Drop(ctx)
		if dropErr != nil {
			return dropErr
		}
	}

	return nil
}

// GetSearchesForParentUser - get the searches for the given parent
func (repo *MongoSearchesDataRepoImpl) GetSearchesForParentUser(parentUserID string) []string {
	var searches = make([]string, 0)

	filter := bson.M{}
	filter[SearchesCollectionParentFieldname] = parentUserID

	ctx, contextCancel := context.WithTimeout(context.Background(), 16*time.Second)
	defer contextCancel()

	searchesCollection := repo.database.Collection(SearchesCollectionName)
	cur, err := searchesCollection.Find(ctx, filter)
	defer cur.Close(ctx)

	if err != nil {
		log.Println("GetSearchesForParentUser unexpected error when searching for ", parentUserID, ":", err)
	} else {

		for cur.Next(ctx) {
			var rec = mongoParentChild{}
			err := cur.Decode(&rec)
			if err != nil {
				log.Println("GetSearchesForParentUser unexpected error when searching for ", parentUserID, ":", err)
				break
			}
			searches = append(searches, rec.Phrase)
		}
	}

	return searches
}

// AddSearchToParentUser - add a search to the given parent
func (repo *MongoSearchesDataRepoImpl) AddSearchToParentUser(parentUserID string, searchPhrase string) {
	log.Println("AddSearchToParentUser parentUserID", parentUserID, "searchPhrase", searchPhrase)

	rec := mongoParentChild{}
	rec.ParentUserID = parentUserID
	rec.Phrase = searchPhrase

	ctx, cancelContext := context.WithTimeout(context.Background(), 4*time.Second)
	defer cancelContext()

	searchesCollection := repo.database.Collection(SearchesCollectionName)
	insertResult, err := searchesCollection.InsertOne(ctx, rec)
	if err != nil {
		log.Println("AddSearchToParentUser unexpectedly failed", err)
	}

	id := insertResult.InsertedID
	log.Println("AddSearchToParentUser parentUserID", parentUserID, "searchPhrase", searchPhrase, "inserted id", id)
}

// RemoveSearchFromParent - remove a search from the given parent
func (repo *MongoSearchesDataRepoImpl) RemoveSearchFromParent(parentUserID string, searchPhrase string) {
	log.Println("RemoveSearchFromParent parentUserID", parentUserID, "searchPhrase", searchPhrase)

	filter := bson.M{}
	filter[SearchesCollectionParentFieldname] = parentUserID
	filter[SearchesCollectionSearchPhraseFieldname] = searchPhrase

	ctx, cancelContext := context.WithTimeout(context.Background(), 4*time.Second)
	defer cancelContext()

	searchesCollection := repo.database.Collection(SearchesCollectionName)
	deleteResult, err := searchesCollection.DeleteOne(ctx, filter)
	if err != nil {
		log.Println("RemoveSearchFromParent unexpectedly failed", err)
	}

	log.Println("RemoveSearchFromParent parentUserID", parentUserID, "searchPhrase", searchPhrase, "deleted count", deleteResult.DeletedCount)
}

// NewMongoSearchesDataRepoImpl - create a new instance of MongoSearchesDataRepoImpl
func NewMongoSearchesDataRepoImpl(connectionString string, databaseName string) *MongoSearchesDataRepoImpl {
	repo := MongoSearchesDataRepoImpl{}

	client, err := mongo.NewClient(options.Client().ApplyURI(connectionString))
	if err != nil {
		log.Println("failed to get Mongo client", err)
		return nil
	}

	repo.client = client

	ctx, cancelContext := context.WithTimeout(context.Background(), 8*time.Second)
	defer cancelContext()

	err = repo.client.Connect(ctx)
	if err != nil {
		log.Println("failed to connect Mongo client", err)
		return nil
	}

	err = repo.client.Ping(ctx, nil)
	if err != nil {
		log.Println("failed to ping Mongo client", err)
		return nil
	}

	repo.database = repo.client.Database(databaseName)

	return &repo
}
