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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class AccountsService {
	private static final String TIMESTAMP_FIELD_NAME = "timestamp";
	private static final String CHILD_ACCOUNT_FIELD_NAME = "childAccount";
	private static final String PARENT_ACCOUNT_FIELD_NAME = "parentAccount";
	private static final String PARENTS_KIDS_ENTITY_NAME = "ParentsKids";

	private static final Logger logger = Logger.getLogger(AccountsService.class.getName());

	private final DatastoreService datastore;
	private final MemcacheService cache;

	public AccountsService(final DatastoreService datastore) {
		this.datastore = datastore;
		cache = MemcacheServiceFactory.getMemcacheService();
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

		invalidateChildAccountCache(childAccount);
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
		// final Filter userIdFilter = new Query.FilterPredicate(PARENT_ACCOUNT_FIELD_NAME, Query.FilterOperator.EQUAL,
		// parentAccount);
		// final Filter childIdFilter = new Query.FilterPredicate(CHILD_ACCOUNT_FIELD_NAME, Query.FilterOperator.EQUAL,
		// childAccount);
		//
		// final Filter parentChildFilter = CompositeFilterOperator.and(userIdFilter, childIdFilter);
		// final Query q = new Query(PARENTS_KIDS_ENTITY_NAME).setFilter(parentChildFilter);
		//
		// final PreparedQuery pq = datastore.prepare(q);
		// final Iterable<Entity> iterable = pq.asIterable();
		//
		// return iterable.iterator().hasNext();
		final String foundParentAccountForChildAccount = getParentAccountForChildAccount(childAccount);
		return parentAccount.equalsIgnoreCase(foundParentAccountForChildAccount);
	}

	public String getParentAccountForChildAccount(final String childAccount) {
		final String cacheKey = this.getCacheKeyChildAccount(childAccount);
		final String parentCacheAccount = (String) cache.get(cacheKey);

		if (parentCacheAccount != null) {
			return parentCacheAccount;
		} else {

			final Filter childIdFilter = new Query.FilterPredicate(CHILD_ACCOUNT_FIELD_NAME,
					Query.FilterOperator.EQUAL, childAccount);

			final Query q = new Query(PARENTS_KIDS_ENTITY_NAME).setFilter(childIdFilter);

			final PreparedQuery pq = datastore.prepare(q);
			final Iterable<Entity> iterable = pq.asIterable();

			if (iterable.iterator().hasNext()) {
				final Entity parentEntity = iterable.iterator().next();
				final String parentAccount = (String) parentEntity.getProperty(PARENT_ACCOUNT_FIELD_NAME);
				cache.put(cacheKey, parentAccount);
				return parentAccount;
			} else {
				logger.warning("unable to find parent account for child:" + childAccount);
				return null;
			}
		}
	}

	private String getCacheKeyChildAccount(final String childAccount) {
		return childAccount + "-parentchildaccount";
	}

	private void invalidateChildAccountCache(final String childAccount) {
		final String cacheKey = this.getCacheKeyChildAccount(childAccount);
		cache.delete(cacheKey);
	}
}
