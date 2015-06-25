package com.djpedersen.mgyoutube.services;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;

public class SearchConverter {
	public static final String USER_SEARCH_AUDIT_ENTITY_NAME = "UserSearchAudit";
	public static final String USA_FIELD_NAME_USERID = "userId";
	public static final String USA_FIELD_NAME_SEARCH_TERMS = "searchTerms";
	public static final String USA_FIELD_NAME_TIMESTAMP = "timestamp";

	public static final String SAVED_SEARCH_ENTITY_NAME = "SavedSearches";
	public static final String SS_FIELD_NAME_USERID = "userId";
	public static final String SS_FIELD_NAME_SEARCH_TERMS = "searchTerms";
	public static final String SS_FIELD_NAME_THUMBNAIL_URL = "thumbnailUrl";
	public static final String SS_FIELD_NAME_TIMESTAMP = "timestamp";

	// public static final String SS_FIELD_NAME_DELETED = "deleted";
	// public static final String SS_FIELD_NAME_DELETED_TIMESTAMP =
	// "deletedTimestamp";
	private static final Gson gson = new Gson();

	public Entity createUserSearchAuditRecordEntity(final String userId, final String searchTerms, final DateTime dt) {
		// final String keyId = userId + "_" + dt;
		// final Entity entity = new Entity(USER_SEARCH_AUDIT_ENTITY_NAME,
		// keyId);
		// let GAE gen a key
		final Entity entity = new Entity(USER_SEARCH_AUDIT_ENTITY_NAME);

		entity.setProperty(USA_FIELD_NAME_USERID, userId);
		entity.setProperty(USA_FIELD_NAME_SEARCH_TERMS, searchTerms);
		entity.setProperty(USA_FIELD_NAME_TIMESTAMP, dt.getMillis());

		return entity;
	}

	public UserSearchAuditRecord entityToUserSearchAuditRecord(final Entity entity) {
		final UserSearchAuditRecord record = new UserSearchAuditRecord();

		record.setId(entity.getKey().getId());
		record.setUserId((String) entity.getProperty(USA_FIELD_NAME_USERID));
		record.setSearchTerms((String) entity.getProperty(USA_FIELD_NAME_SEARCH_TERMS));
		final Date dt = new Date((long) entity.getProperty(USA_FIELD_NAME_TIMESTAMP));
		record.setTimestamp(dt);

		return record;
	}

	public Entity createSavedSearchEntity(final String userId, final String searchTerms, final String thumbnailUrl,
			final DateTime dt) {
		final Entity entity = new Entity(SAVED_SEARCH_ENTITY_NAME);

		entity.setProperty(SS_FIELD_NAME_USERID, userId);
		entity.setProperty(SS_FIELD_NAME_SEARCH_TERMS, searchTerms);
		entity.setProperty(SS_FIELD_NAME_THUMBNAIL_URL, thumbnailUrl);
		entity.setProperty(SS_FIELD_NAME_TIMESTAMP, dt.getMillis());
		// entity.setProperty(SS_FIELD_NAME_DELETED, false);
		// entity.setProperty(SS_FIELD_NAME_DELETED_TIMESTAMP, null);

		return entity;
	}

	public SavedSearch entityToSavedSearch(final Entity entity) {
		final SavedSearch record = new SavedSearch();

		record.setId(entity.getKey().getId());
		record.setUserId((String) entity.getProperty(SS_FIELD_NAME_USERID));
		record.setSearchTerms((String) entity.getProperty(SS_FIELD_NAME_SEARCH_TERMS));
		record.setThumbnailUrl((String) entity.getProperty(SS_FIELD_NAME_THUMBNAIL_URL));
		final Date date = new Date((long) entity.getProperty(SS_FIELD_NAME_TIMESTAMP));
		record.setTimestamp(date);

		return record;
	}

	public String savedSearchToJson(final SavedSearch search) {
		final String json = gson.toJson(search);
		return json;
	}

	public String savedSearchListToJson(final List<SavedSearch> savedSearchesForUser) {
		final StringBuffer sb = new StringBuffer();

		sb.append("[");

		if (savedSearchesForUser != null) {
			boolean firstTimeThrough = true;
			for (SavedSearch search : savedSearchesForUser) {
				if (!firstTimeThrough) {
					sb.append(",");
				}

				final String json = savedSearchToJson(search);
				sb.append(json);
				firstTimeThrough = false;
			}
		}

		sb.append("]");

		return sb.toString();
	}
}
