package com.djpedersen.mgyoutube.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.djpedersen.mgyoutube.converters.SearchConverter;
import com.djpedersen.mgyoutube.entities.SavedSearch;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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

	public void deleteSavedSearch(long searchId) {

		final Key key = KeyFactory.createKey(SearchConverter.SAVED_SEARCH_ENTITY_NAME, searchId);
		datastore.delete(key);

	}
}
