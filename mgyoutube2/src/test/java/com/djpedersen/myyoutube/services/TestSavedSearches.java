package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.services.SavedSearch;
import com.djpedersen.mgyoutube.services.SearchTermsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestSavedSearches {

	private final LocalServiceTestHelper gaeLocalServiceHelper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());

	private SearchTermsService service;

	private DatastoreService datastoreService;

	@Before
	public void setup() {
		gaeLocalServiceHelper.setUp();

		datastoreService = DatastoreServiceFactory.getDatastoreService();

		service = new SearchTermsService(datastoreService);
	}

	@After
	public void tearDown() {
		gaeLocalServiceHelper.tearDown();
	}

	@Test
	public void testBasicSaveSearch() throws InterruptedException {
		final String userId = "1111";
		final String searchTerms = "owls";
		final String thumbnailUrl = "www.rit.edu";

		final long key = service.saveSearch(userId, searchTerms, thumbnailUrl);
		assertTrue(key > 0);

		final List<SavedSearch> savedSearches = service.getSavedSearchesForUser(userId);

		assertNotNull(savedSearches);
		assertEquals(1, savedSearches.size());

		final SavedSearch savedSearch = savedSearches.get(0);
		assertEquals(searchTerms, savedSearch.getSearchTerms());
		assertEquals(thumbnailUrl, savedSearch.getThumbnailUrl());
		assertEquals(userId, savedSearch.getUserId());
		final Date timestamp = savedSearch.getTimestamp();
		final DateTime timestampDt = new DateTime(timestamp.getTime());
		assertTrue(timestampDt.isBefore(new DateTime()));
	}
}
