import uuid
from repos.SearchesDataRepo import SearchesDataRepo

from couchbase.cluster import Cluster
from couchbase.cluster import PasswordAuthenticator
from couchbase.n1ql import N1QLQuery, CONSISTENCY_REQUEST


class CouchSearchesDataRepoImpl(SearchesDataRepo):
    SEARCHES_BUCKET_NAME = "searches"
    SEARCHES_PARENT_INDEX_NAME = "searches-parentuserid-idx"
    SEARCHES_BUCKET_PARENT_FIELDNAME = "parentUserId"
    SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME = "phrase"

    SEARCHES_FIND_PHRASE_BY_PARENT = "SELECT phrase FROM `searches` WHERE parentUserId = $1"
    SEARCHES_DELETE_PHRASE_AND_PARENT = "DELETE FROM `searches` WHERE parentUserId = $1 AND phrase = $2"

    def __init__(self, connectionString: str, username: str, password: str):
        self.cluster = Cluster(connectionString)
        authenticator = PasswordAuthenticator(username, password)
        self.cluster.authenticate(authenticator)

    def repositoryStartup(self):
        searchesBucket = self.cluster.open_bucket(
            CouchSearchesDataRepoImpl.SEARCHES_BUCKET_NAME)
        searchesBucket.flush()

    def getSearchesForParentUser(self, parentUserId: str) -> list:
        searches = list()
        query = N1QLQuery(
            CouchSearchesDataRepoImpl.SEARCHES_FIND_PHRASE_BY_PARENT, parentUserId)
        query.consistency = CONSISTENCY_REQUEST
        searchesBucket = self.cluster.open_bucket(
            CouchSearchesDataRepoImpl.SEARCHES_BUCKET_NAME)
        for row in searchesBucket.n1ql_query(query):
            phrase: str = row[CouchSearchesDataRepoImpl.SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME]
            searches.append(phrase)
        return searches

    def addSearchToParentUser(self, parentUserId: str, searchPhrase: str) -> None:
        id = str(uuid.uuid4())
        doc = dict()
        doc[CouchSearchesDataRepoImpl.SEARCHES_BUCKET_PARENT_FIELDNAME] = parentUserId
        doc[CouchSearchesDataRepoImpl.SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME] = searchPhrase

        searchesBucket = self.cluster.open_bucket(
            CouchSearchesDataRepoImpl.SEARCHES_BUCKET_NAME)
        insertRv = searchesBucket.insert(id, doc)
        if (not insertRv.success):
            print("CouchSearchesDataRepoImpl.addSearchToParentUser failed", insertRv.rc)

    def removeSearchFromParentUser(self, parentUserId: str, searchPhrase: str):
        query = N1QLQuery(CouchSearchesDataRepoImpl.SEARCHES_DELETE_PHRASE_AND_PARENT,
                          parentUserId,  searchPhrase)
        searchesBucket = self.cluster.open_bucket(
            CouchSearchesDataRepoImpl.SEARCHES_BUCKET_NAME)
        searchesBucket.n1ql_query(query).execute()
