package com.djpedersen.mgyoutube.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.djpedersen.mgyoutube.converters.SearchConverter;
import com.djpedersen.mgyoutube.converters.WatchedVideoConverter;
import com.djpedersen.mgyoutube.entities.UserSearchAuditRecord;
import com.djpedersen.mgyoutube.entities.WatchedVideoAuditRecord;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.SortDirection;

public class AuditsService {

	private static final Logger logger = Logger.getLogger(AuditsService.class.getName());

	private final DatastoreService datastoreService;

	public AuditsService(final DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
	}

	public long recordWatchedVideo(final String userId, final String videoId, final String thumbnailUrl,
			final String videoTitle) {

		if (userId == null) {
			throw new IllegalArgumentException("userId parameter cannot be null");
		}
		if (videoId == null) {
			throw new IllegalArgumentException("videoId parameter cannot be null");
		}

		final DateTime now = new DateTime();

		final WatchedVideoConverter converter = new WatchedVideoConverter();
		final Entity entity = converter.createWatchedVideoEntity(userId, videoId, thumbnailUrl, videoTitle, now);

		final Key key = datastoreService.put(entity);

		logger.info("saved audit record for '" + userId + "', '" + videoId + "','" + now + "' key:" + key.toString());

		return key.getId();
	}

	public List<WatchedVideoAuditRecord> getRecentlyWatchedVideosForUser(final String userId) {
		final List<WatchedVideoAuditRecord> watchedList = new ArrayList<WatchedVideoAuditRecord>();

		final Filter userIdFilter = new Query.FilterPredicate(WatchedVideoConverter.WVA_FIELD_NAME_USERID,
				Query.FilterOperator.EQUAL, userId);

		final Query q = new Query(WatchedVideoConverter.WATCHED_VIDEO_AUDIT_ENTITY_NAME).setFilter(userIdFilter);
		q.addSort(WatchedVideoConverter.WVA_FIELD_NAME_TIMESTAMP, SortDirection.DESCENDING);

		final PreparedQuery pq = datastoreService.prepare(q);
		final Iterable<Entity> iterable = pq.asIterable();
		final WatchedVideoConverter converter = new WatchedVideoConverter();

		for (Entity entity : iterable) {
			final WatchedVideoAuditRecord record = converter.entityToWatchedVideoAuditRecord(entity);
			watchedList.add(record);
		}
		return watchedList;
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
		final Key key = datastoreService.put(entity);
		logger.info("created audit record for '" + userId + "', '" + searchTerms + "','" + dt + "' key:"
				+ key.toString());
	}

	public List<UserSearchAuditRecord> getUserSearchAuditRecordsForUserId(final String userId) {
		final SearchConverter converter = new SearchConverter();
		final List<UserSearchAuditRecord> records = new ArrayList<UserSearchAuditRecord>();

		final Filter userIdFilter = new Query.FilterPredicate(SearchConverter.USA_FIELD_NAME_USERID,
				Query.FilterOperator.EQUAL, userId);

		final Query q = new Query(SearchConverter.USER_SEARCH_AUDIT_ENTITY_NAME).setFilter(userIdFilter);
		// final Query q = new
		// Query(SearchConverter.USER_SEARCH_AUDIT_ENTITY_NAME);

		// q.addSort(USA_FIELD_NAME_TIMESTAMP, SortDirection.DESCENDING);

		final PreparedQuery pq = datastoreService.prepare(q);
		// pq.countEntities();
		// final Iterable<Entity> iterator =
		// pq.asIterable(FetchOptions.Builder.withLimit(64));
		final Iterable<Entity> iterable = pq.asIterable();
		// Iterator<Entity> iterator = iterable.iterator();
		// boolean hasNext = iterator.hasNext();
		// List<Entity> asList = pq.asList(FetchOptions.Builder.withLimit(64));

		for (Entity entity : iterable) {

			// for (Entity entity : asList) {
			final UserSearchAuditRecord record = converter.entityToUserSearchAuditRecord(entity);
			records.add(record);
		}

		return records;
	}

}
