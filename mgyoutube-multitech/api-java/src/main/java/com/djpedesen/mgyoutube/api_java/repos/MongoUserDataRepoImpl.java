package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class MongoUserDataRepoImpl implements UserDataRepo {

	private final MongoClient mongoClient;
	private final MongoDatabase database;

	public static final String USERS_COLLECTION_NAME = "users";
	public static final String USERS_COLLECTION_USERNAME_FIELDNAME = "username";
	public static final String USERS_COLLECTION_PASSWORD_FIELDNAME = "password";
	public static final String USERS_COLLECTION_PARENT_FIELDNAME = "isParent";

	public static final String PARENT_CHILD_COLLECTION_NAME = "parentChild";
	public static final String PARENT_CHILD_COLLECTION_PARENT_FIELDNAME = "parentUserId";
	public static final String PARENT_CHILD_COLLECTION_CHILD_FIELDNAME = "childUserId";

	public static final String MONGO_ID_FIELDNAME = "_id";

	public MongoUserDataRepoImpl(final String connectionString, final String databaseName) {
		mongoClient = MongoClients.create(connectionString);
		database = mongoClient.getDatabase(databaseName);
	}

	@Override
	public void repositoryStartup() {
		final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
		usersCollection.drop();
		final MongoCollection<Document> parentChildCollection = database.getCollection(PARENT_CHILD_COLLECTION_NAME);
		parentChildCollection.drop();
	}

	@Override
	public User getUserByUsername(final String username) {
		final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
		final Document userDoc = usersCollection.find(Filters.eq(USERS_COLLECTION_USERNAME_FIELDNAME, username))
				.first();

		if (userDoc == null) {
			return null;
		}
		final User user = new User();

		final ObjectId objectId = userDoc.getObjectId(MONGO_ID_FIELDNAME);
		user.userId = objectId.toHexString();
		user.username = userDoc.getString(USERS_COLLECTION_USERNAME_FIELDNAME);
		user.password = userDoc.getString(USERS_COLLECTION_PASSWORD_FIELDNAME);
		user.isParent = userDoc.getBoolean(USERS_COLLECTION_PARENT_FIELDNAME, false);

		System.out.println("MongoUserDataRepoImpl.getUserByUsername(" + username + ") returning " + user);
		return user;
	}

	@Override
	public User addUser(final User user) {
		final Document newUser = new Document().append(USERS_COLLECTION_USERNAME_FIELDNAME, user.username)
				.append(USERS_COLLECTION_PASSWORD_FIELDNAME, user.password)
				.append(USERS_COLLECTION_PARENT_FIELDNAME, user.isParent);
		final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
		usersCollection.insertOne(newUser);

		return this.getUserByUsername(user.username);
	}

	@Override
	public void removeUser(final User user) {
		final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
		usersCollection.deleteOne(Filters.eq(USERS_COLLECTION_USERNAME_FIELDNAME, user.username));
	}

	@Override
	public User replaceUser(final String userId, final User user) {

		final User userByUsername = this.getUserByUsername(user.username);

		if (userByUsername != null) {
			final Document updatedUserDoc = new Document();
			// updatedUserDoc.append(MONGO_ID_FIELDNAME, user.userId);
			updatedUserDoc.append(USERS_COLLECTION_USERNAME_FIELDNAME, user.username);
			updatedUserDoc.append(USERS_COLLECTION_PASSWORD_FIELDNAME, user.password);
			updatedUserDoc.append(USERS_COLLECTION_PARENT_FIELDNAME, user.isParent);

			final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
			usersCollection.replaceOne(Filters.eq(MONGO_ID_FIELDNAME, new ObjectId(userByUsername.userId)),
					updatedUserDoc);

			return this.getUserById(user.userId);
		}

		return null;
	}

	@Override
	public void addChildToParent(final String parentUserId, final String childUserId) {
		// TODO should probably worry about dups here...
		final MongoCollection<Document> parentChildCollection = database.getCollection(PARENT_CHILD_COLLECTION_NAME);
		final Document document = new Document().append(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, parentUserId)
				.append(PARENT_CHILD_COLLECTION_CHILD_FIELDNAME, childUserId);
		parentChildCollection.insertOne(document);
	}

	@Override
	public List<User> getChildrenForParent(final String parentUserId) {
		final List<User> children = new ArrayList<>();
		final MongoUserDataRepoImpl udr = this;
		final Consumer<Document> consumer = new Consumer<Document>() {

			@Override
			public void accept(final Document doc) {
				final String childUserId = doc.getString(PARENT_CHILD_COLLECTION_CHILD_FIELDNAME);

				final User user = udr.getUserById(childUserId);
				if (user != null) {
					children.add(user);
				} else {
					System.out.println("unknown child userid " + childUserId);
				}
			}
		};

		final MongoCollection<Document> parentChildCollection = database.getCollection(PARENT_CHILD_COLLECTION_NAME);
		parentChildCollection.find(Filters.eq(PARENT_CHILD_COLLECTION_PARENT_FIELDNAME, parentUserId))
				.forEach(consumer);

		return children;
	}

	// probably will be public eventually
	private User getUserById(final String userId) {
		final MongoCollection<Document> usersCollection = database.getCollection(USERS_COLLECTION_NAME);
		final Document userDoc = usersCollection.find(Filters.eq(MONGO_ID_FIELDNAME, new ObjectId(userId))).first();

		if (userDoc == null) {
			return null;
		}
		final User user = new User();

		final ObjectId objectId = userDoc.getObjectId(MONGO_ID_FIELDNAME);
		user.userId = objectId.toString();
		user.username = userDoc.getString(USERS_COLLECTION_USERNAME_FIELDNAME);
		user.password = userDoc.getString(USERS_COLLECTION_PASSWORD_FIELDNAME);
		user.isParent = userDoc.getBoolean(USERS_COLLECTION_PARENT_FIELDNAME, false);
		System.out.println("MongoUserDataRepoImpl.getUserById(" + userId + ") returning " + user);
		return user;
	}

}
