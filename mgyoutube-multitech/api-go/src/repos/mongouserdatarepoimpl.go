package repos

import (
	"apimodel"
	"context"
	"log"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

const (
	// UsersCollectionName - the name of the users collection
	UsersCollectionName = "users"
	//UsersCollectionUsernameFieldname - the name of the username field
	UsersCollectionUsernameFieldname = "username"
	//  UsersCollectionPasswordFieldname = "password"
	//  UsersCollectionParentFieldname = "isParent"

	// ParentChildCollectionName - the name of the parent child collection
	ParentChildCollectionName = "parentChild"
	// ParentChildCollectionParentFieldname - the name fo the parent field
	ParentChildCollectionParentFieldname = "parentUserId"
	//  ParentChildCollectionChildFieldname = "childUserId"

	//MongoIDFieldname - the name of the main mongo db id field
	MongoIDFieldname = "_id"
)

type mongoUserCollectionDoc struct {
	ID       primitive.ObjectID `bson:"_id,omitempty"`
	Username string             `bson:"username"`
	Password string             `bson:"password"`
	IsParent bool               `bson:"isParent"`
}

type mongoParentChildCollectionDoc struct {
	ID           primitive.ObjectID `bson:"_id,omitempty"`
	ParentUserID string             `bson:"parentUserId"`
	ChildUserID  string             `bson:"childUserId"`
}

// MongoUserDataRepoImpl - data for MongoUserDataRepoImpl
type MongoUserDataRepoImpl struct {
	client   *mongo.Client
	database *mongo.Database
}

// RepositoryStartup - to initialize the repo
func (repo *MongoUserDataRepoImpl) RepositoryStartup() error {
	ctx, contextCancel := context.WithTimeout(context.Background(), 16*time.Second)
	defer contextCancel()

	usersCollection := repo.database.Collection(UsersCollectionName)
	if usersCollection != nil {
		userDropError := usersCollection.Drop(ctx)
		if userDropError != nil {
			return userDropError
		}
	}

	parentChildCollection := repo.database.Collection(ParentChildCollectionName)
	if parentChildCollection != nil {
		parentChildDropError := parentChildCollection.Drop(ctx)
		if parentChildDropError != nil {
			return parentChildDropError
		}
	}

	return nil
}

// GetUserByUsername - get a user by username
func (repo MongoUserDataRepoImpl) GetUserByUsername(username string) *apimodel.User {

	filter := bson.M{}
	filter[UsersCollectionUsernameFieldname] = username

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	usersCollection := repo.database.Collection(UsersCollectionName)
	result := usersCollection.FindOne(ctx, filter)

	if result == nil {
		log.Println("GetUserByUsername(", username, ") result was nil")
		return nil
	}
	if result.Err() != nil {
		log.Println("GetUserByUsername(", username, ") errored", result.Err())
		return nil
	}

	userDoc := mongoUserCollectionDoc{}
	result.Decode(&userDoc)

	user := apimodel.User{}

	user.UserID = userDoc.ID.Hex()
	user.Username = userDoc.Username
	user.Password = userDoc.Password
	user.IsParent = userDoc.IsParent

	log.Println("MongoUserDataRepoImpl.getUserByUsername(", username, ") returning ", user)
	return &user
}

// AddUser - add a user to the system
func (repo *MongoUserDataRepoImpl) AddUser(user *apimodel.User) *apimodel.User {
	newUser := mongoUserCollectionDoc{}
	newUser.Username = user.Username
	newUser.Password = user.Password
	newUser.IsParent = user.IsParent

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	usersCollection := repo.database.Collection(UsersCollectionName)
	usersCollection.InsertOne(ctx, newUser)

	return repo.GetUserByUsername(user.Username)
}

// RemoveUser - remove a user from the repo
func (repo MongoUserDataRepoImpl) RemoveUser(user *apimodel.User) {

	filter := bson.M{}
	filter[UsersCollectionUsernameFieldname] = user.Username

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	usersCollection := repo.database.Collection(UsersCollectionName)
	usersCollection.DeleteOne(ctx, filter)
}

// ReplaceUser - replace the user
func (repo MongoUserDataRepoImpl) ReplaceUser(userID string, user *apimodel.User) *apimodel.User {
	log.Println("MongoUserDataRepoImpl.ReplaceUser userID", userID, "as", user)
	userByUsername := repo.GetUserByUsername(user.Username)

	if userByUsername != nil {
		objID, _ := primitive.ObjectIDFromHex(userID)
		filter := bson.M{}
		filter[MongoIDFieldname] = objID

		updatedUserDoc := mongoUserCollectionDoc{}
		updatedUserDoc.Username = user.Username
		updatedUserDoc.Password = user.Password
		updatedUserDoc.IsParent = user.IsParent

		ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
		defer contextCancel()

		usersCollection := repo.database.Collection(UsersCollectionName)
		_, err := usersCollection.ReplaceOne(ctx, filter, updatedUserDoc)
		if err != nil {
			log.Println("MongoUserDataRepoImpl.ReplaceUser userID", userID, ", mongo replace errored", err)
		}

		return repo.getUserByID(userID)
	}

	log.Println("MongoUserDataRepoImpl.ReplaceUser userID", userID, ", userByName returned nil, so returning nil")
	return nil
}

// AddChildToParent - add a child to a parent
func (repo MongoUserDataRepoImpl) AddChildToParent(parentUserID string, childUserID string) {
	// TODO should probably worry about dups here...
	document := mongoParentChildCollectionDoc{}
	document.ParentUserID = parentUserID
	document.ChildUserID = childUserID

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	parentChildCollection := repo.database.Collection(ParentChildCollectionName)
	parentChildCollection.InsertOne(ctx, document)
}

// GetChildrenForParent - get all of the children for a parent
func (repo MongoUserDataRepoImpl) GetChildrenForParent(parentUserID string) []*apimodel.User {
	var children []*apimodel.User

	filter := bson.M{}
	filter[ParentChildCollectionParentFieldname] = parentUserID

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	parentChildCollection := repo.database.Collection(ParentChildCollectionName)
	cur, err := parentChildCollection.Find(ctx, filter)
	defer cur.Close(ctx)

	if err != nil {
		log.Println("GetChildrenForParent search for parentUserID", parentUserID, "errored", err)
	} else {
		for cur.Next(ctx) {
			var parentChildDoc mongoParentChildCollectionDoc
			err = cur.Decode(&parentChildDoc)
			if err == nil {
				childUser := repo.getUserByID(parentChildDoc.ChildUserID)
				children = append(children, childUser)
			} else {
				log.Println("GetChildrenForParent search for parentUserID", parentUserID, "errored decoding record")
			}
		}
	}

	return children
}

func (repo MongoUserDataRepoImpl) getUserByID(userID string) *apimodel.User {

	objID, _ := primitive.ObjectIDFromHex(userID)
	filter := bson.M{}
	filter[MongoIDFieldname] = objID

	ctx, contextCancel := context.WithTimeout(context.Background(), 4*time.Second)
	defer contextCancel()

	usersCollection := repo.database.Collection(UsersCollectionName)
	result := usersCollection.FindOne(ctx, filter)

	if result == nil {
		log.Println("getUserByID(", userID, ") returned no results")
		return nil
	}

	userDoc := mongoUserCollectionDoc{}
	err := result.Decode(&userDoc)
	if err != nil {
		log.Println("getUserByID(", userID, ") decoding errored", err)
		return nil
	}
	user := apimodel.User{}

	user.UserID = userDoc.ID.Hex()
	user.Username = userDoc.Username
	user.Password = userDoc.Password
	user.IsParent = userDoc.IsParent

	log.Println("MongoUserDataRepoImpl.getUserById(", userID, ") returning ", user)
	return &user
}

// NewMongoUserDataRepoImpl - create a new MongoUserDataRepoImpl
func NewMongoUserDataRepoImpl(connectionString string, databaseName string) *MongoUserDataRepoImpl {
	repo := MongoUserDataRepoImpl{}

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
