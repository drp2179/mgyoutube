package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.services.SearchTermsService;
import com.djpedersen.mgyoutube.services.UserSearchAuditRecord;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestUserSearchAuditSearchTerms {

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

	@Test(expected = IllegalStateException.class)
	public void testRecordFailsIfNoUserId() {
		final String userId = null;
		final String searchTerms = "xyz";
		final DateTime dt = new DateTime();
		service.recordUserSearchAudit(userId, searchTerms, dt);
	}

	@Test(expected = IllegalStateException.class)
	public void testRecordFailsIfNoDateTime() {
		final String userId = "1111";
		final String searchTerms = "xyz";
		final DateTime dt = null;
		service.recordUserSearchAudit(userId, searchTerms, dt);
	}

	@Test(expected = IllegalStateException.class)
	public void testRecordFailsIfNoSearchTerms() {
		final String userId = "1111";
		final String searchTerms = null;
		final DateTime dt = new DateTime();
		service.recordUserSearchAudit(userId, searchTerms, dt);
	}

	@Test(expected = IllegalStateException.class)
	public void testRecordFailsIfEmptySearchTerms() {
		final String userId = "1111";
		final String searchTerms = "                    ";
		final DateTime dt = new DateTime();
		service.recordUserSearchAudit(userId, searchTerms, dt);
	}

	@Test
	public void testBasicRecordSucceeds() throws InterruptedException {
		final String userId = "1111";
		final String searchTerms = "owls";
		final DateTime dt = new DateTime();

		// final List<UserSearchAuditRecord> list0 =
		// service.getUserSearchAuditRecordsForUserId(userId);
		// assertEquals(0, list0.size());

		service.recordUserSearchAudit(userId, searchTerms, dt);

		Thread.sleep(500);

		final List<UserSearchAuditRecord> list1 = service.getUserSearchAuditRecordsForUserId(userId);

		assertEquals(1, list1.size());
	}
}
