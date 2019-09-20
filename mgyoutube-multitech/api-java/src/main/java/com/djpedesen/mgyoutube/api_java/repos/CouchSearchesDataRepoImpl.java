package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
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

public class CouchSearchesDataRepoImpl implements SearchesDataRepo {

	public static final String SEARCHES_BUCKET_NAME = "searches";
	public static final String SEARCHES_PARENT_INDEX_NAME = "searches-parentuserid-idx";
	public static final String SEARCHES_BUCKET_PARENT_FIELDNAME = "parentUserId";
	public static final String SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME = "phrase";

	public static final String SEARCHES_FIND_PHRASE_BY_PARENT = "SELECT phrase FROM `searches` WHERE parentUserId = $1";
	public static final String SEARCHES_DELETE_PHRASE_AND_PARENT = "DELETE FROM `searches` WHERE parentUserId = $1 AND phrase = $2";

	private Cluster cluster;

	public CouchSearchesDataRepoImpl(final String connectionString, final String username, final String password) {
		cluster = CouchbaseCluster.create(connectionString);
		cluster.authenticate(username, password);
	}

	private Bucket getSearchesBucket() {
		return cluster.openBucket(SEARCHES_BUCKET_NAME);
	}

	@Override
	public void repositoryStartup() {
		final BucketSettings searchesBucketSettings = cluster.clusterManager().getBucket(SEARCHES_BUCKET_NAME);
		if (searchesBucketSettings == null) {
			final BucketSettings bucketSettings = new DefaultBucketSettings.Builder().type(BucketType.COUCHBASE)
					.name(SEARCHES_BUCKET_NAME).password("").quota(120) // megabytes
					.replicas(1).indexReplicas(true).enableFlush(true).build();
			final BucketSettings insertedBucketSettings = cluster.clusterManager().insertBucket(bucketSettings);
			System.out.println("Couchdb " + SEARCHES_BUCKET_NAME + " created: " + insertedBucketSettings);
			final Bucket searchesBucket = getSearchesBucket();
			final boolean createdN1qlPrimaryIndex = searchesBucket.bucketManager().createN1qlPrimaryIndex(true, true);
			System.out
					.println("Couchdb " + SEARCHES_BUCKET_NAME + " created primary index: " + createdN1qlPrimaryIndex);
			final boolean createdN1qlParentUserIdIndex = searchesBucket.bucketManager()
					.createN1qlIndex(SEARCHES_PARENT_INDEX_NAME, true, true, SEARCHES_BUCKET_PARENT_FIELDNAME);
			System.out.println(
					"Couchdb " + SEARCHES_BUCKET_NAME + " created parentid index: " + createdN1qlParentUserIdIndex);
		} else {
			final Bucket searchesBucket = getSearchesBucket();
			final Boolean flushed = searchesBucket.bucketManager().flush();

			System.out.println("Couchdb " + SEARCHES_BUCKET_NAME + " flushed: " + flushed);
		}
	}

	@Override
	public List<String> getSearchesForParentUser(final String parentUserId) {
		System.out.println("Couchdb searching " + SEARCHES_BUCKET_NAME + " for parentUserId " + parentUserId);

		final List<String> searches = new ArrayList<>();

		final Bucket searchesBucket = getSearchesBucket();

		final N1qlParams queryConfig = N1qlParams.build();
		queryConfig.consistency(ScanConsistency.REQUEST_PLUS);
		final N1qlQueryResult result = searchesBucket.query(
				N1qlQuery.parameterized(SEARCHES_FIND_PHRASE_BY_PARENT, JsonArray.from(parentUserId), queryConfig));

		for (N1qlQueryRow row : result) {
			final String phrase = row.value().getString(SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME);
			searches.add(phrase);
		}

		System.out.println("Couchdb searching " + SEARCHES_BUCKET_NAME + " for parentUserId " + parentUserId
				+ " returning " + searches.size() + " phrases");
		return searches;
	}

	@Override
	public void addSearchToParentUser(final String parentUserId, final String searchPhrase) {
		final JsonObject content = JsonObject.empty();
		content.put(SEARCHES_BUCKET_PARENT_FIELDNAME, parentUserId);
		content.put(SEARCHES_BUCKET_SEARCH_PHRASE_FIELDNAME, searchPhrase);

		final String id = UUID.randomUUID().toString();
		final JsonDocument document = JsonDocument.create(id, content);

		final Bucket searchesBucket = getSearchesBucket();
		final JsonDocument insertedDoc = searchesBucket.insert(document);
		System.out.println("Couchdb inserted parentUserId " + parentUserId + " and searchPhrase " + searchPhrase
				+ ", docId: " + insertedDoc.id());
	}

	@Override
	public void removeSearchFromParentUser(final String parentUserId, final String searchPhrase) throws Exception {
		final Bucket searchesBucket = getSearchesBucket();
		final N1qlQueryResult result = searchesBucket.query(
				N1qlQuery.parameterized(SEARCHES_DELETE_PHRASE_AND_PARENT, JsonArray.from(parentUserId, searchPhrase)));
		System.out.println("Couchdb deleted parentUserId " + parentUserId + " and searchPhrase " + searchPhrase + ": "
				+ result.status());
	}

}
