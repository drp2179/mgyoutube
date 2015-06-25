package com.djpedersen.mgyoutube.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.SortDirection;

public class SearchTermsService {

	private static final Logger logger = Logger.getLogger(SearchTermsService.class.getName());

	private final DatastoreService datastore;

	public SearchTermsService(final DatastoreService datastore) {
		this.datastore = datastore;
	}

	public void recordUserSearchAudit(final String userId, final String searchTerms, final DateTime dt) {
		if (userId == null) {
			throw new IllegalStateException("userId parameter cannot be null");
		}
		if (searchTerms == null || "".equals(searchTerms.trim())) {
			throw new IllegalStateException("searchTerms parameter cannot be null or empty");
		}
		if (dt == null) {
			throw new IllegalStateException("datetime parameter cannot be null");
		}

		final SearchConverter converter = new SearchConverter();
		final Entity entity = converter.createUserSearchAuditRecordEntity(userId, searchTerms, dt);
		final Key key = datastore.put(entity);
		logger.info("created audit record for '" + userId + "', '" + searchTerms + "','" + dt + "' key:"
				+ key.toString());
	}

	public List<UserSearchAuditRecord> getUserSearchAuditRecordsForUserId(final String userId) {
		final SearchConverter converter = new SearchConverter();
		final List<UserSearchAuditRecord> records = new ArrayList<UserSearchAuditRecord>();

		// final Filter userIdFilter = new
		// Query.FilterPredicate(USA_FIELD_NAME_USERID,
		// Query.FilterOperator.EQUAL, userId);

		// final Query q = new
		// Query(USER_SEARCH_AUDIT_ENTITY_NAME).setFilter(userIdFilter);
		final Query q = new Query(SearchConverter.USER_SEARCH_AUDIT_ENTITY_NAME);

		// q.addSort(USA_FIELD_NAME_TIMESTAMP, SortDirection.DESCENDING);

		final PreparedQuery pq = datastore.prepare(q);
		// pq.countEntities();
		// final Iterable<Entity> iterator =
		// pq.asIterable(FetchOptions.Builder.withLimit(64));
		// final Iterable<Entity> iterable = pq.asIterable();
		// Iterator<Entity> iterator = iterable.iterator();
		// boolean hasNext = iterator.hasNext();
		List<Entity> asList = pq.asList(FetchOptions.Builder.withLimit(64));
		int size = asList.size();

		// for (Entity entity : iterable) {

		for (Entity entity : asList) {
			final UserSearchAuditRecord record = converter.entityToUserSearchAuditRecord(entity);
			records.add(record);
		}

		return records;
	}

	public long saveSearch(final String userId, final String searchTerms, final String thumbnailUrl) {
		final DateTime now = new DateTime();
		final SearchConverter converter = new SearchConverter();
		final Entity entity = converter.createSavedSearchEntity(userId, searchTerms, thumbnailUrl, now);
		final Key key = datastore.put(entity);
		logger.info("created saved search for '" + userId + "', '" + searchTerms + "','" + thumbnailUrl + "','" + now
				+ "' key:" + key.toString());
		return key.getId();
	}

	public List<SavedSearch> getSavedSearchesForUser(final String userId) {
		final SearchConverter converter = new SearchConverter();
		final List<SavedSearch> searches = new ArrayList<SavedSearch>();

		final Filter userIdFilter = new Query.FilterPredicate(SearchConverter.SS_FIELD_NAME_USERID,
				Query.FilterOperator.EQUAL, userId);

		final Query q = new Query(SearchConverter.SAVED_SEARCH_ENTITY_NAME).setFilter(userIdFilter);

		q.addSort(SearchConverter.SS_FIELD_NAME_TIMESTAMP, SortDirection.DESCENDING);

		final PreparedQuery pq = datastore.prepare(q);
		final Iterable<Entity> iterable = pq.asIterable();

		for (Entity entity : iterable) {

			final SavedSearch record = converter.entityToSavedSearch(entity);
			searches.add(record);
		}

		return searches;
	}
}
