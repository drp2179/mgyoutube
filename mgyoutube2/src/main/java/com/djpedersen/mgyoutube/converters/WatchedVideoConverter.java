package com.djpedersen.mgyoutube.converters;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.djpedersen.mgyoutube.entities.WatchedVideoAuditRecord;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;

public class WatchedVideoConverter {
	public static final String WATCHED_VIDEO_AUDIT_ENTITY_NAME = "WatchedVideoAudits";
	public static final String WVA_FIELD_NAME_USERID = "userId";
	public static final String WVA_FIELD_NAME_VIDEOID = "videoId";
	public static final String WVA_FIELD_NAME_THUMBNAIL_URL = "thumbnailUrl";
	public static final String WVA_FIELD_NAME_VIDEO_TITLE = "videoTitle";
	public static final String WVA_FIELD_NAME_TIMESTAMP = "timestamp";

	private static final Gson gson = new Gson();

	public Entity createWatchedVideoEntity(final String userId, final String videoId, final String thumbnailUrl,
			final String videoTitle, final DateTime dt) {
		// final String keyId = userId + "_" + dt;
		// final Entity entity = new Entity(WATCHED_VIDEO_AUDIT_ENTITY_NAME,
		// keyId);
		// let GAE gen a key
		final Entity entity = new Entity(WATCHED_VIDEO_AUDIT_ENTITY_NAME);

		entity.setProperty(WVA_FIELD_NAME_USERID, userId);
		entity.setProperty(WVA_FIELD_NAME_VIDEOID, videoId);
		entity.setProperty(WVA_FIELD_NAME_THUMBNAIL_URL, thumbnailUrl);
		entity.setProperty(WVA_FIELD_NAME_VIDEO_TITLE, videoTitle);
		entity.setProperty(WVA_FIELD_NAME_TIMESTAMP, dt.getMillis());

		return entity;
	}

	public WatchedVideoAuditRecord entityToWatchedVideoAuditRecord(final Entity entity) {
		final WatchedVideoAuditRecord record = new WatchedVideoAuditRecord();

		record.setId(entity.getKey().getId());
		record.setUserId((String) entity.getProperty(WVA_FIELD_NAME_USERID));
		record.setVideoId((String) entity.getProperty(WVA_FIELD_NAME_VIDEOID));
		record.setThumbnailUrl((String) entity.getProperty(WVA_FIELD_NAME_THUMBNAIL_URL));
		record.setVideoTitle((String) entity.getProperty(WVA_FIELD_NAME_VIDEO_TITLE));
		final Date dt = new Date((long) entity.getProperty(WVA_FIELD_NAME_TIMESTAMP));
		record.setTimestamp(dt);

		return record;
	}

	//
	// public Entity createSavedSearchEntity(final String userId, final String searchTerms, final String thumbnailUrl,
	// final DateTime dt) {
	// final Entity entity = new Entity(SAVED_SEARCH_ENTITY_NAME);
	//
	// entity.setProperty(SS_FIELD_NAME_USERID, userId);
	// entity.setProperty(SS_FIELD_NAME_SEARCH_TERMS, searchTerms);
	// entity.setProperty(SS_FIELD_NAME_THUMBNAIL_URL, thumbnailUrl);
	// entity.setProperty(SS_FIELD_NAME_TIMESTAMP, dt.getMillis());
	// // entity.setProperty(SS_FIELD_NAME_DELETED, false);
	// // entity.setProperty(SS_FIELD_NAME_DELETED_TIMESTAMP, null);
	//
	// return entity;
	// }
	//
	// public SavedSearch entityToSavedSearch(final Entity entity) {
	// final SavedSearch record = new SavedSearch();
	//
	// record.setId(entity.getKey().getId());
	// record.setUserId((String) entity.getProperty(SS_FIELD_NAME_USERID));
	// record.setSearchTerms((String) entity.getProperty(SS_FIELD_NAME_SEARCH_TERMS));
	// record.setThumbnailUrl((String) entity.getProperty(SS_FIELD_NAME_THUMBNAIL_URL));
	// final Date date = new Date((long) entity.getProperty(SS_FIELD_NAME_TIMESTAMP));
	// record.setTimestamp(date);
	//
	// return record;
	// }
	//
	public String watchedVideoToJson(final WatchedVideoAuditRecord record) {
		final String json = gson.toJson(record);
		return json;
	}

	public String watchedVideosListToJson(final List<WatchedVideoAuditRecord> auditRecords) {
		final StringBuffer sb = new StringBuffer();

		sb.append("[");

		if (auditRecords != null) {
			boolean firstTimeThrough = true;
			for (WatchedVideoAuditRecord record : auditRecords) {
				if (!firstTimeThrough) {
					sb.append(",");
				}

				final String json = watchedVideoToJson(record);
				sb.append(json);
				firstTimeThrough = false;
			}
		}

		sb.append("]");

		return sb.toString();
	}
}
