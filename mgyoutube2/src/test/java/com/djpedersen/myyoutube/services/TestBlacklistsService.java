package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.services.BlacklistsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestBlacklistsService {

	private final LocalServiceTestHelper gaeLocalServiceHelper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());
	private DatastoreService datastoreService;
	private BlacklistsService service;

	@Before
	public void setup() {
		gaeLocalServiceHelper.setUp();

		datastoreService = DatastoreServiceFactory.getDatastoreService();
		service = new BlacklistsService(datastoreService);

	}

	@After
	public void tearDown() {
		gaeLocalServiceHelper.tearDown();
	}

	@Test
	public void testSimpleBlacklist() {
		final String parentAccount = "dan@gmail.com";
		final String wordToBlacklist = "blacklistedword";
		service.blacklistWordForParent(parentAccount, wordToBlacklist);

		final List<String> blacklistedWordsForParent = service.getBlacklistedWordsForParent(parentAccount);
		assertNotNull(blacklistedWordsForParent);
		assertEquals(1, blacklistedWordsForParent.size());
		assertEquals(wordToBlacklist, blacklistedWordsForParent.get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlacklistRejectsNullParent() {
		final String parentAccount = null;
		final String wordToBlacklist = "blacklistedword";
		service.blacklistWordForParent(parentAccount, wordToBlacklist);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBlacklistRejectsNullWord() {
		final String parentAccount = "dan@gmail.com";
		final String wordToBlacklist = null;
		service.blacklistWordForParent(parentAccount, wordToBlacklist);
	}

	@Test
	public void testSimpleDelete() {
		final String parentAccount = "jenine@gmail.com";
		final String wordToBlacklist = "blacklistedword";

		service.blacklistWordForParent(parentAccount, wordToBlacklist);

		final List<String> blacklistedWordsForParentAdded = service.getBlacklistedWordsForParent(parentAccount);
		assertNotNull(blacklistedWordsForParentAdded);
		assertEquals(1, blacklistedWordsForParentAdded.size());

		service.removeBlacklistWordForParent(parentAccount, wordToBlacklist);

		final List<String> blacklistedWordsForParentRemoved = service.getBlacklistedWordsForParent(parentAccount);
		assertNotNull(blacklistedWordsForParentRemoved);
		assertEquals(0, blacklistedWordsForParentRemoved.size());
	}
}
