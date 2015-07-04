package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.entities.WatchedVideoAuditRecord;
import com.djpedersen.mgyoutube.services.AuditsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestVideoWatchedAudits {

	private final LocalServiceTestHelper gaeLocalServiceHelper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());

	private AuditsService service;

	private DatastoreService datastoreService;

	@Before
	public void setup() {
		gaeLocalServiceHelper.setUp();

		datastoreService = DatastoreServiceFactory.getDatastoreService();

		service = new AuditsService(datastoreService);
	}

	@After
	public void tearDown() {
		gaeLocalServiceHelper.tearDown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDisallowNullUserId() {
		final String userId = null;
		final String videoId = "1234abc";
		final String thumbnailUrl = "thumbnail";
		final String title = "title";

		service.recordWatchedVideo(userId, videoId, thumbnailUrl, title);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDisallowNullVideoId() {
		final String userId = "dan@gmail.com";
		final String videoId = null;
		final String thumbnailUrl = "thumbnail";
		final String title = "title";

		service.recordWatchedVideo(userId, videoId, thumbnailUrl, title);
	}

	@Test
	public void testSimpleRecordWatchedVideo() {
		final String userId = "dan@gmail.com";
		final String videoId = "1234abc";
		final String thumbnailUrl = "thumbnail";
		final String title = "title";

		service.recordWatchedVideo(userId, videoId, thumbnailUrl, title);
		final List<WatchedVideoAuditRecord> watchedVideos = service.getRecentlyWatchedVideosForUser(userId);

		assertNotNull(watchedVideos);
		assertEquals(1, watchedVideos.size());
		final WatchedVideoAuditRecord record = watchedVideos.get(0);

		assertEquals(userId, record.getUserId());
		assertEquals(videoId, record.getVideoId());
		assertEquals(thumbnailUrl, record.getThumbnailUrl());
		assertEquals(title, record.getVideoTitle());
		assertNotNull(record.getTimestamp());
	}

	@Test
	public void testMultipleRecordWatchedVideos() {
		final String userId = "dan@gmail.com";
		final String videoId = "1234abc";
		final String thumbnailUrl = "thumbnail";
		final String title = "title";

		service.recordWatchedVideo(userId, videoId, thumbnailUrl, title);
		final List<WatchedVideoAuditRecord> watchedVideos = service.getRecentlyWatchedVideosForUser(userId);

		assertNotNull(watchedVideos);
		assertEquals(1, watchedVideos.size());
		final WatchedVideoAuditRecord record = watchedVideos.get(0);

		assertEquals(userId, record.getUserId());
		assertEquals(videoId, record.getVideoId());
		assertNotNull(record.getTimestamp());
	}
}
