import uuid
from repos.SearchesDataRepo import SearchesDataRepo
from pymongo import MongoClient


class MongoSearchesDataRepoImpl(SearchesDataRepo):
    SEARCHES_COLLECTION_NAME = "searches"
    SEARCHES_COLLECTION_PARENT_FIELDNAME = "parentUserId"
    SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME = "phrase"

    MONGO_ID_FIELDNAME = "_id"

    def __init__(self, connectionString: str, databaseName: str):
        self.client = MongoClient(connectionString)
        self.database = self.client[databaseName]

    def repositoryStartup(self):
        searchesCollection = self.database[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_NAME]
        searchesCollection.drop()

    def getSearchesForParentUser(self, parentUserId: str) -> list:
        searches = list()
        filter = dict()
        filter[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId

        searchesCollection = self.database[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_NAME]
        cursor = searchesCollection.find(filter)
        for doc in cursor:
            phrase = doc[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME]
            searches.append(phrase)
        return searches

    def addSearchToParentUser(self, parentUserId: str, searchPhrase: str) -> None:
        document = dict()
        document[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId
        document[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME] = searchPhrase

        searchesCollection = self.database[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_NAME]
        insertResult = searchesCollection.insert_one(document)

        print(
            "MongoSearchesDataRepoImpl.addSearchToParentUser insert results", insertResult)

        if (not insertResult.acknowledged):
            print("MongoSearchesDataRepoImpl.addSearchToParentUser failed", insertResult)
        else:
            print("MongoSearchesDataRepoImpl.addSearchToParentUser success, inserted id",
                  insertResult.inserted_id)

    def removeSearchFromParentUser(self, parentUserId: str, searchPhrase: str):
        filter = dict()
        filter[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId
        filter[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME] = searchPhrase

        searchesCollection = self.database[MongoSearchesDataRepoImpl.SEARCHES_COLLECTION_NAME]
        deleteResult = searchesCollection.delete_one(filter)
        if (not deleteResult.acknowledged):
            print(
                "MongoSearchesDataRepoImpl.removeSearchFromParentUser failed", deleteResult)
        else:
            print("MongoSearchesDataRepoImpl.removeSearchFromParentUser success, rews deleted",
                  deleteResult.deleted_count)
