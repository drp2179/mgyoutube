package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.services.AccountsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class TestAccountsService {

	private final LocalServiceTestHelper gaeLocalServiceHelper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig());
	private AccountsService service;
	private DatastoreService datastoreService;

	@Before
	public void setup() {
		gaeLocalServiceHelper.setUp();

		datastoreService = DatastoreServiceFactory.getDatastoreService();
		service = new AccountsService(datastoreService);
	}

	@After
	public void tearDown() {
		gaeLocalServiceHelper.tearDown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullParentThrowsException() {
		final String parentAccount = null;
		final String childAccount = "";
		service.associateParentChildAccounts(parentAccount, childAccount);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullChildThrowsException() {
		final String parentAccount = "";
		final String childAccount = null;
		service.associateParentChildAccounts(parentAccount, childAccount);
	}

	@Test
	public void testAssociateParentAndChild() {
		final String parentAccount = "dan";
		final String childAccount = "erika";
		service.associateParentChildAccounts(parentAccount, childAccount);

		final List<String> childAccounts = service.getChildAccountsForParent(parentAccount);
		assertNotNull(childAccounts);
		assertEquals(1, childAccounts.size());
		assertEquals(childAccount, childAccounts.get(0));
	}
}
