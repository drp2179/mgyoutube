from repos.UserDataRepo import UserDataRepo
from apimodel.User import User, cloneUser
from pymongo import MongoClient
from bson.objectid import ObjectId
import uuid


class MongoUserDataRepoImpl(UserDataRepo):
    USERS_COLLECTION_NAME = "users"
    USERS_COLLECTION_USERNAME_FIELDNAME = "username"
    USERS_COLLECTION_PASSWORD_FIELDNAME = "password"
    USERS_COLLECTION_PARENT_FIELDNAME = "isParent"

    PARENT_CHILD_COLLECTION_NAME = "parentChild"
    PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId"
    PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId"

    MONGO_ID_FIELDNAME = "_id"

    def __init__(self, connectionString: str, databaseName: str):
        self.client = MongoClient(connectionString)
        self.database = self.client[databaseName]

    def repositoryStartup(self) -> None:
        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        usersCollection.drop()
        parentChildCollection = self.database[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_NAME]
        parentChildCollection.drop()

    def getUserByUsername(self, username: str) -> User:
        user: User = None

        filter = dict()
        filter[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME] = username

        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        doc = usersCollection.find_one(filter)
        if (doc is not None):
            user = User()
            user.userId = str(doc[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME])
            user.username = doc[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME]
            user.password = doc[MongoUserDataRepoImpl.USERS_COLLECTION_PASSWORD_FIELDNAME]
            user.isParent = doc[MongoUserDataRepoImpl.USERS_COLLECTION_PARENT_FIELDNAME]
        return user

    def addUser(self, user: User) -> User:
        id = ObjectId()
        userId = str(id)

        doc = dict()
        doc[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME] = id
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME] = user.username
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent

        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        insertResult = usersCollection.insert_one(doc)
        if (not insertResult.acknowledged):
            print("MongoUserDataRepoImpl.addUser failed")
            return None
        return self.getUserById(userId)

    def removeUser(self, user: User):
        filter = dict()
        filter[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME] = user.username

        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        deleteResult = usersCollection.delete_one(filter)
        if (not deleteResult.acknowledged):
            print("MongoUserDataRepoImpl.removeUser failed", user)

    def replaceUser(self, userId: str, user: User) -> User:
        filter = dict()
        filter[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME] = ObjectId(userId)

        doc = dict()
        doc[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME] = ObjectId(userId)
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME] = user.username
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_PASSWORD_FIELDNAME] = user.password
        doc[MongoUserDataRepoImpl.USERS_COLLECTION_PARENT_FIELDNAME] = user.isParent

        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        updateResult = usersCollection.replace_one(filter, doc)
        if (not updateResult.acknowledged):
            print("CouchUserDataRepoImpl.replaceUser failed")
            return None
        return self.getUserById(userId)

    def addChildToParent(self, parentUserId: str, childUserId: int):
        document = dict()
        document[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME] = ObjectId()
        document[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId
        document[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_CHILD_FIELDNAME] = childUserId

        parentChildCollection = self.database[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_NAME]
        insertRv = parentChildCollection.insert_one(document)
        if (not insertRv.acknowledged):
            print("addChildToParent failed")

    def getChildrenForParent(self, parentUserId: str) -> list:
        children = list()

        filter = dict()
        filter[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_PARENT_FIELDNAME] = parentUserId

        parentChildCollection = self.database[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_NAME]
        cursor = parentChildCollection.find(filter)
        for doc in cursor:
            childUserId = doc[MongoUserDataRepoImpl.PARENT_CHILD_COLLECTION_CHILD_FIELDNAME]
            childUser = self.getUserById(childUserId)
            if (childUser is not None):
                children.append(childUser)
            else:
                print("unknown child userid ", childUserId)

        return children

    def getUserById(self, userId: str) -> User:
        filter = dict()
        filter[MongoUserDataRepoImpl.MONGO_ID_FIELDNAME] = ObjectId(userId)
        usersCollection = self.database[MongoUserDataRepoImpl.USERS_COLLECTION_NAME]
        doc = usersCollection.find_one(filter)
        user: User = None
        if (doc is not None):
            user = User()
            user.userId = userId
            user.username = doc[MongoUserDataRepoImpl.USERS_COLLECTION_USERNAME_FIELDNAME]
            user.password = doc[MongoUserDataRepoImpl.USERS_COLLECTION_PASSWORD_FIELDNAME]
            user.isParent = doc[MongoUserDataRepoImpl.USERS_COLLECTION_PARENT_FIELDNAME]
        return user
