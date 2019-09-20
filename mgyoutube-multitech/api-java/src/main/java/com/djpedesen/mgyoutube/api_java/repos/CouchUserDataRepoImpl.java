package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.djpedesen.mgyoutube.api_java.apimodel.User;

public class CouchUserDataRepoImpl implements UserDataRepo {

	public static final String USERS_BUCKET_NAME = "users";
	public static final String USERS_FIND_BY_USERNAME = "SELECT META(users).id, users.* FROM users WHERE username = $1";
	public static final String USERS_USERNAME_INDEX_NAME = "users-username-idx";
	public static final String USERS_BUCKET_ID_FIELDNAME = "id";
	public static final String USERS_BUCKET_USERNAME_FIELDNAME = "username";
	public static final String USERS_BUCKET_PASSWORD_FIELDNAME = "password";
	public static final String USERS_BUCKET_PARENT_FIELDNAME = "isParent";

	public static final String PARENT_CHILD_BUCKET_NAME = "parentChild";
	public static final String PARENT_CHILD_FIND_BY_PARENT = "SELECT childUserId FROM parentChild WHERE parentUserId = $1";
	public static final String PARENT_CHILD_PARENT_INDEX_NAME = "parnetchild-parent-idx";
	public static final String PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId";
	public static final String PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId";

	private final CouchbaseCluster cluster;

	public CouchUserDataRepoImpl(final String connectionString, final String username, final String password) {
		cluster = CouchbaseCluster.create(connectionString);
		cluster.authenticate(username, password);
	}

	private Bucket getUsersBucket() {
		return cluster.openBucket(USERS_BUCKET_NAME);
	}

	private Bucket getParentChildBucket() {
		return cluster.openBucket(PARENT_CHILD_BUCKET_NAME);
	}

	@Override
	public void repositoryStartup() {
		{
			final BucketSettings usersBucketSettings = cluster.clusterManager().getBucket(USERS_BUCKET_NAME);
			if (usersBucketSettings == null) {
				final BucketSettings bucketSettings = new DefaultBucketSettings.Builder().type(BucketType.COUCHBASE)
						.name(USERS_BUCKET_NAME).password("").quota(120) // megabytes
						.replicas(1).indexReplicas(true).enableFlush(true).build();
				final BucketSettings insertedBucketSettings = cluster.clusterManager().insertBucket(bucketSettings);
				System.out.println("Couchdb " + USERS_BUCKET_NAME + " created: " + insertedBucketSettings);

				final Bucket usersBucket = getUsersBucket();
				final boolean createdN1qlPrimaryIndex = usersBucket.bucketManager().createN1qlPrimaryIndex(true, false);
				System.out
						.println("Couchdb " + USERS_BUCKET_NAME + " created primary index: " + createdN1qlPrimaryIndex);
				final boolean createdN1qlUsernameIndex = usersBucket.bucketManager()
						.createN1qlIndex(USERS_USERNAME_INDEX_NAME, true, true, USERS_BUCKET_USERNAME_FIELDNAME);
				System.out.println(
						"Couchdb " + USERS_BUCKET_NAME + " created username index: " + createdN1qlUsernameIndex);
			} else {
				final Bucket usersBucket = getUsersBucket();
				final Boolean flushed = usersBucket.bucketManager().flush();

				System.out.println("Couchdb " + USERS_BUCKET_NAME + " flushed: " + flushed);
			}
		}
		{
			final BucketSettings parentChildBucketSettings = cluster.clusterManager()
					.getBucket(PARENT_CHILD_BUCKET_NAME);
			if (parentChildBucketSettings == null) {
				final BucketSettings bucketSettings = new DefaultBucketSettings.Builder().type(BucketType.COUCHBASE)
						.name(PARENT_CHILD_BUCKET_NAME).password("").quota(120) // megabytes
						.replicas(1).indexReplicas(true).enableFlush(true).build();
				final BucketSettings insertedBucketSettings = cluster.clusterManager().insertBucket(bucketSettings);
				System.out.println("Couchdb " + PARENT_CHILD_BUCKET_NAME + " created: " + insertedBucketSettings);

				final Bucket parentChildBucket = getParentChildBucket();
				final boolean createdN1qlPrimaryIndex = parentChildBucket.bucketManager().createN1qlPrimaryIndex(true,
						false);
				System.out.println(
						"Couchdb " + PARENT_CHILD_BUCKET_NAME + " created primary index: " + createdN1qlPrimaryIndex);
				final boolean createdN1qlParentIndex = parentChildBucket.bucketManager().createN1qlIndex(
						PARENT_CHILD_PARENT_INDEX_NAME, true, true, PARENT_CHILD_COLLECTION_PARENT_FIELDNAME);
				System.out.println(
						"Couchdb " + PARENT_CHILD_BUCKET_NAME + " created parent index: " + createdN1qlParentIndex);
			} else {
				final Bucket parentChildBucket = getParentChildBucket();
				final Boolean flushed = parentChildBucket.bucketManager().flush();

				System.out.println("Couchdb " + PARENT_CHILD_BUCKET_NAME + " flushed: " + flushed);
			}
		}
	}

	@Override
	public User getUserByUsername(final String username) {
		final Bucket usersBucket = this.getUsersBucket();

		final N1qlParams queryConfig = N1qlParams.build();
		queryConfig.consistency(ScanConsistency.REQUEST_PLUS);

		final N1qlQueryResult result = usersBucket
				.query(N1qlQuery.parameterized(USERS_FIND_BY_USERNAME, JsonArray.from(username), queryConfig));

		User user = null;

		if (result.iterator().hasNext()) {
			final N1qlQueryRow row = result.iterator().next();
			user = this.createUserFromRow(row);
		}

		System.out.println("Couch.getUserByUsername(" + username + ") returning " + user);
		return user;
	}

	@Override
	public User addUser(final User user) {
		final JsonObject content = JsonObject.empty();
		content.put(USERS_BUCKET_USERNAME_FIELDNAME, user.username);
		content.put(USERS_BUCKET_PASSWORD_FIELDNAME, user.password);
		content.put(USERS_BUCKET_PARENT_FIELDNAME, user.isParent);

		final String id = UUID.randomUUID().toString();
		final JsonDocument document = JsonDocument.create(id, content);

		final Bucket usersBucket = getUsersBucket();
		final JsonDocument insertedDoc = usersBucket.insert(document);
		System.out.println("Couchdb.addUser " + user.username + ", docId: " + insertedDoc.id());

		return this.getUserByUsername(user.username);
	}

	@Override
	public void removeUser(final User user) {
		final Bucket usersBucket = this.getUsersBucket();
		final JsonDocument removed = usersBucket.remove(user.userId);
		System.out.println("Couchdb.removeUser(" + user.userId + ") removed " + removed);
	}

	@Override
	public User replaceUser(final String userId, final User user) {
		final User userByUsername = this.getUserByUsername(user.username);

		if (userByUsername != null) {
			final JsonObject content = JsonObject.empty();
			content.put(USERS_BUCKET_USERNAME_FIELDNAME, user.username);
			content.put(USERS_BUCKET_PASSWORD_FIELDNAME, user.password);
			content.put(USERS_BUCKET_PARENT_FIELDNAME, user.isParent);
			final JsonDocument document = JsonDocument.create(userId, content);

			final Bucket usersBucket = this.getUsersBucket();
			usersBucket.replace(document);

			System.out.println("Couchdb.removeUser(" + userId + ") removed.");
			return this.getUserById(userId);
		}

		System.out.println("Couchdb.removeUser(" + userId + ") not removed, null match to username");
		return null;
	}

	@Override
	public void addChildToParent(final String parentUserId, final String childUserId) {
		// TODO should probably worry about dups here...
		final JsonObject content = JsonObject.empty();
		content.put(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, parentUserId);
		content.put(PARENT_CHILD_COLLECTION_CHILD_FIELDNAME, childUserId);

		final String id = UUID.randomUUID().toString();
		final JsonDocument document = JsonDocument.create(id, content);

		final Bucket parentChildrenBucket = this.getParentChildBucket();
		final JsonDocument inserted = parentChildrenBucket.insert(document);

		System.out.println("Couchdb.addChildToParent parentUserId " + parentUserId + " childUserId " + childUserId
				+ ", docId: " + inserted.id());
	}

	@Override
	public List<User> getChildrenForParent(final String parentUserId) {
		final Bucket parentChildrenBucket = this.getParentChildBucket();

		final List<User> children = new ArrayList<>();

		final N1qlParams queryConfig = N1qlParams.build();
		queryConfig.consistency(ScanConsistency.REQUEST_PLUS);

		final N1qlQueryResult result = parentChildrenBucket
				.query(N1qlQuery.parameterized(PARENT_CHILD_FIND_BY_PARENT, JsonArray.from(parentUserId), queryConfig));

		for (N1qlQueryRow row : result) {
			final String childUserId = row.value().getString(PARENT_CHILD_COLLECTION_CHILD_FIELDNAME);
			final User user = this.getUserById(childUserId);
			children.add(user);
		}

		return children;

	}

	// probably will be public eventually
	private User getUserById(final String userId) {
		System.out.println("Couchdb.getUserById(" + userId + ")");
		User user = null;

		final Bucket usersBucket = getUsersBucket();
		final JsonDocument userDocument = usersBucket.get(userId);

		if (userDocument != null) {
			user = createUserFromDocument(userDocument);
		}

		System.out.println("Couchdb.getUserById(" + userId + ") returning user " + user);
		return user;
	}

	private User createUserFromRow(final N1qlQueryRow row) {
		final User user = new User();
		user.userId = row.value().getString(USERS_BUCKET_ID_FIELDNAME);
		user.username = row.value().getString(USERS_BUCKET_USERNAME_FIELDNAME);
		user.password = row.value().getString(USERS_BUCKET_PASSWORD_FIELDNAME);
		user.isParent = row.value().getBoolean(USERS_BUCKET_PARENT_FIELDNAME);
		return user;
	}

	private User createUserFromDocument(final JsonDocument doc) {
		final User user = new User();
		user.userId = doc.id();
		user.username = doc.content().getString(USERS_BUCKET_USERNAME_FIELDNAME);
		user.password = doc.content().getString(USERS_BUCKET_PASSWORD_FIELDNAME);
		user.isParent = doc.content().getBoolean(USERS_BUCKET_PARENT_FIELDNAME);
		return user;
	}

}
