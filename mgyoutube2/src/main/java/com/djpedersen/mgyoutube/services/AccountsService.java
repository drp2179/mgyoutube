package com.djpedersen.mgyoutube.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;

public class AccountsService {
	private static final String TIMESTAMP_FIELD_NAME = "timestamp";
	private static final String CHILD_ACCOUNT_FIELD_NAME = "childAccount";
	private static final String PARENT_ACCOUNT_FIELD_NAME = "parentAccount";
	private static final String PARENTS_KIDS_ENTITY_NAME = "ParentsKids";

	private static final Logger logger = Logger.getLogger(AccountsService.class.getName());

	private final DatastoreService datastore;

	public AccountsService(final DatastoreService datastore) {
		this.datastore = datastore;
	}

	public void associateParentChildAccounts(final String parentAccount, final String childAccount) {
		logger.fine("associating parent " + parentAccount + " with " + childAccount);

		if (parentAccount == null) {
			throw new IllegalArgumentException("parentAccount field cannot be null");
		}
		if (childAccount == null) {
			throw new IllegalArgumentException("childAccount field cannot be null");
		}

		final Entity entity = createParentChildEntity(parentAccount, childAccount);
		final Key key = datastore.put(entity);
		logger.info("Associated '" + parentAccount + "' with '" + childAccount + "', key:" + key.toString());
	}

	private Entity createParentChildEntity(final String parentAccount, final String childAccount) {
		final DateTime now = new DateTime();
		final Entity entity = new Entity(PARENTS_KIDS_ENTITY_NAME);

		entity.setProperty(PARENT_ACCOUNT_FIELD_NAME, parentAccount);
		entity.setProperty(CHILD_ACCOUNT_FIELD_NAME, childAccount);
		entity.setProperty(TIMESTAMP_FIELD_NAME, now.getMillis());

		return entity;
	}

	public List<String> getChildAccountsForParent(final String parentAccount) {
		final List<String> accounts = new ArrayList<String>();

		final Filter userIdFilter = new Query.FilterPredicate(PARENT_ACCOUNT_FIELD_NAME, Query.FilterOperator.EQUAL,
				parentAccount);

		final Query q = new Query(PARENTS_KIDS_ENTITY_NAME).setFilter(userIdFilter);

		final PreparedQuery pq = datastore.prepare(q);
		final Iterable<Entity> iterable = pq.asIterable();

		for (Entity entity : iterable) {

			final String record = (String) entity.getProperty(CHILD_ACCOUNT_FIELD_NAME);
			accounts.add(record);
		}

		return accounts;
	}

	public boolean isChildAssociatedWithParent(final String childAccount, final String parentAccount) {
		final Filter userIdFilter = new Query.FilterPredicate(PARENT_ACCOUNT_FIELD_NAME, Query.FilterOperator.EQUAL,
				parentAccount);
		final Filter childIdFilter = new Query.FilterPredicate(CHILD_ACCOUNT_FIELD_NAME, Query.FilterOperator.EQUAL,
				childAccount);

		final Filter parentChildFilter = CompositeFilterOperator.and(userIdFilter, childIdFilter);
		final Query q = new Query(PARENTS_KIDS_ENTITY_NAME).setFilter(parentChildFilter);

		final PreparedQuery pq = datastore.prepare(q);
		final Iterable<Entity> iterable = pq.asIterable();

		return iterable.iterator().hasNext();
	}
}
