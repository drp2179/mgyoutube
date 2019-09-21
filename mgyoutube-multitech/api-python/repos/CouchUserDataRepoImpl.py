from repos.UserDataRepo import UserDataRepo
from apimodel.User import User, cloneUser
from couchbase.cluster import Cluster
from couchbase.cluster import PasswordAuthenticator
from couchbase.n1ql import N1QLQuery, CONSISTENCY_REQUEST
from couchutils.couchutils import doesBucketExist, createBasicBucket
import uuid


class CouchUserDataRepoImpl(UserDataRepo):
    USERS_BUCKET_NAME = "users"
    USERS_FIND_BY_USERNAME = "SELECT META(users).id, users.* FROM users WHERE username = $1"
    USERS_USERNAME_INDEX_NAME = "users-username-idx"
    USERS_BUCKET_ID_FIELDNAME = "id"
    USERS_BUCKET_USERNAME_FIELDNAME = "username"
    USERS_BUCKET_PASSWORD_FIELDNAME = "password"
    USERS_BUCKET_PARENT_FIELDNAME = "isParent"

    PARENT_CHILD_BUCKET_NAME = "parentChild"
    PARENT_CHILD_FIND_BY_PARENT = "SELECT childUserId FROM parentChild WHERE parentUserId = $1"
    PARENT_CHILD_PARENT_INDEX_NAME = "parnetchild-parent-idx"
    PARENT_CHILD_BUCKET_PARENT_FIELDNAME = "parentUserId"
    PARENT_CHILD_BUCKET_CHILD_FIELDNAME = "childUserId"

    def __init__(self, connectionString: str, username: str,  password: str):
        self.cluster = Cluster(connectionString)
        authenticator = PasswordAuthenticator(username, password)
        self.cluster.authenticate(authenticator)

    def repositoryStartup(self) -> None:
        if (not doesBucketExist(CouchUserDataRepoImpl.USERS_BUCKET_NAME, self.cluster)):
            usersBucket = createBasicBucket(
                CouchUserDataRepoImpl.USERS_BUCKET_NAME, self.cluster)
            usersBucket.bucket_manager().create_n1ql_index(
                CouchUserDataRepoImpl.USERS_USERNAME_INDEX_NAME, defer=False, ignore_exists=True, fields=[CouchUserDataRepoImpl.USERS_BUCKET_USERNAME_FIELDNAME])
        else:
            usersBucket = self.cluster.open_bucket(
                CouchUserDataRepoImpl.USERS_BUCKET_NAME)
            usersBucket.flush()

        if (not doesBucketExist(CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_NAME, self.cluster)):
            parentChildBucket = createBasicBucket(
                CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_NAME, self.cluster)
            parentChildBucket.bucket_manager().create_n1ql_index(
                CouchUserDataRepoImpl.PARENT_CHILD_PARENT_INDEX_NAME, defer=False, ignore_exists=True, fields=[CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_PARENT_FIELDNAME])
        else:
            parentChildBucket = self.cluster.open_bucket(
                CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_NAME)
            parentChildBucket.flush()

    def getUserByUsername(self, username: str) -> User:
        query = N1QLQuery(
            CouchUserDataRepoImpl.USERS_FIND_BY_USERNAME, username)
        query.consistency = CONSISTENCY_REQUEST
        usersBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.USERS_BUCKET_NAME)
        row = usersBucket.n1ql_query(query).get_single_result()
        if (row is None):
            return None
        user = User()
        user.userId = row[CouchUserDataRepoImpl.USERS_BUCKET_ID_FIELDNAME]
        user.username = row[CouchUserDataRepoImpl.USERS_BUCKET_USERNAME_FIELDNAME]
        user.password = row[CouchUserDataRepoImpl.USERS_BUCKET_PASSWORD_FIELDNAME]
        user.isParent = row[CouchUserDataRepoImpl.USERS_BUCKET_PARENT_FIELDNAME]
        return user

    def addUser(self, user: User) -> User:
        id = str(uuid.uuid4())
        doc = dict()
        doc[CouchUserDataRepoImpl.USERS_BUCKET_USERNAME_FIELDNAME] = user.username
        doc[CouchUserDataRepoImpl.USERS_BUCKET_PASSWORD_FIELDNAME] = user.password
        doc[CouchUserDataRepoImpl.USERS_BUCKET_PARENT_FIELDNAME] = user.isParent

        usersBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.USERS_BUCKET_NAME)
        insertRv = usersBucket.insert(id, doc)
        if (not insertRv.success):
            print("CouchUserDataRepoImpl.addUser failed", insertRv.rc)
            return None
        return self.getUserById(id)

    def removeUser(self, user: User):
        usersBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.USERS_BUCKET_NAME)
        usersBucket.remove(user.userId)

    def replaceUser(self, userId: str, user: User) -> User:
        id = userId
        doc = dict()
        doc[CouchUserDataRepoImpl.USERS_BUCKET_USERNAME_FIELDNAME] = user.username
        doc[CouchUserDataRepoImpl.USERS_BUCKET_PASSWORD_FIELDNAME] = user.password
        doc[CouchUserDataRepoImpl.USERS_BUCKET_PARENT_FIELDNAME] = user.isParent

        usersBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.USERS_BUCKET_NAME)
        updateRv = usersBucket.replace(id, doc)
        if (not updateRv.success):
            print("CouchUserDataRepoImpl.addUser replaceUser", updateRv.rc)
            return None
        return self.getUserById(id)

    def addChildToParent(self, parentUserId: str, childUserId: int):
        id = str(uuid.uuid4())
        document = dict()
        document[CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_PARENT_FIELDNAME] = parentUserId
        document[CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_CHILD_FIELDNAME] = childUserId

        parentChildBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_NAME)

        insertRv = parentChildBucket.insert(id, document)
        if (not insertRv.success):
            print("addChildToParent failed", insertRv.rc)

    def getChildrenForParent(self, parentUserId: str) -> list:
        children = list()
        query = N1QLQuery(
            CouchUserDataRepoImpl.PARENT_CHILD_FIND_BY_PARENT, parentUserId)
        query.consistency = CONSISTENCY_REQUEST

        parentChildBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_NAME)
        for row in parentChildBucket.n1ql_query(query):
            childUserId = row[CouchUserDataRepoImpl.PARENT_CHILD_BUCKET_CHILD_FIELDNAME]
            childUser = self.getUserById(childUserId)
            if (childUser is not None):
                children.append(childUser)
            else:
                print("unknown child userid ", childUserId)

        return children

    def getUserById(self, userId: str) -> User:
        usersBucket = self.cluster.open_bucket(
            CouchUserDataRepoImpl.USERS_BUCKET_NAME)
        getResults = usersBucket.get(userId)
        if (not getResults.success):
            print("CouchUserDataRepoImpl.getUserById getResults", getResults.rc)
            return None
        user = User()
        user.userId = userId
        user.username = getResults.value[CouchUserDataRepoImpl.USERS_BUCKET_USERNAME_FIELDNAME]
        user.password = getResults.value[CouchUserDataRepoImpl.USERS_BUCKET_PASSWORD_FIELDNAME]
        user.isParent = getResults.value[CouchUserDataRepoImpl.USERS_BUCKET_PARENT_FIELDNAME]
        return user
