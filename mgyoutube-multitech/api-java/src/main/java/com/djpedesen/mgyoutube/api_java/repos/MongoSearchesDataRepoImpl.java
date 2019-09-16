package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoSearchesDataRepoImpl implements SearchesDataRepo {

	private final MongoClient mongoClient;
	private final MongoDatabase database;

	public static final String SEARCHES_COLLECTION_NAME = "searches";
	public static final String SEARCHES_COLLECTION_PARENT_FIELDNAME = "parentUserId";
	public static final String SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME = "phrase";

	public static final String MONGO_ID_FIELDNAME = "_id";

	public MongoSearchesDataRepoImpl(final String connectionString, final String databaseName) {
		mongoClient = MongoClients.create(connectionString);
		database = mongoClient.getDatabase(databaseName);
	}

	@Override
	public void repositoryStartup() {
		final MongoCollection<Document> searchesCollection = database.getCollection(SEARCHES_COLLECTION_NAME);
		searchesCollection.drop();
	}

	@Override
	public List<String> getSearchesForParentUser(final String parentUserId) {
		final List<String> searches = new ArrayList<>();
		final Consumer<Document> consumer = new Consumer<Document>() {

			@Override
			public void accept(final Document doc) {
				final String phrase = doc.getString(SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME);
				searches.add(phrase);
			}
		};

		final MongoCollection<Document> searchesCollection = database.getCollection(SEARCHES_COLLECTION_NAME);
		searchesCollection.find(Filters.eq(SEARCHES_COLLECTION_PARENT_FIELDNAME, parentUserId)).forEach(consumer);

		return searches;
	}

	@Override
	public void addSearchToParentUser(final String parentUserId, final String searchPhrase) {
		final MongoCollection<Document> searchesCollection = database.getCollection(SEARCHES_COLLECTION_NAME);
		final Document document = new Document().append(SEARCHES_COLLECTION_PARENT_FIELDNAME, parentUserId)
				.append(SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME, searchPhrase);
		searchesCollection.insertOne(document);
	}

}
