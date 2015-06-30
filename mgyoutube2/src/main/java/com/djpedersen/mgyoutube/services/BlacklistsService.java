package com.djpedersen.mgyoutube.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class BlacklistsService {

	private static final Logger logger = Logger.getLogger(BlacklistsService.class.getName());

	private static final String PARENTS_BLACKLIST_WORDS_ENTITY_NAME = "ParentBlacklistedWords";
	private static final String PARENT_ACCOUNT_FIELD_NAME = "parentAccount";
	private static final String BLACKLISTED_WORD_FIELD_NAME = "blacklistedWord";
	private static final String TIMESTAMP_FIELD_NAME = "timestamp";

	private final DatastoreService datastoreService;

	private final MemcacheService cache;

	public BlacklistsService(final DatastoreService datastoreService) {
		this.datastoreService = datastoreService;
		cache = MemcacheServiceFactory.getMemcacheService();
	}

	public List<String> getBlacklistedWordsForParent(final String parentAccount) {
		final String cacheKey = getCacheKeyBlacklistedWords(parentAccount);

		@SuppressWarnings("unchecked")
		final List<String> cachedWords = (List<String>) cache.get(cacheKey);

		if (cachedWords != null) {
			return cachedWords;
		} else {
			final List<String> words = new ArrayList<String>();

			final Filter userIdFilter = new Query.FilterPredicate(PARENT_ACCOUNT_FIELD_NAME,
					Query.FilterOperator.EQUAL, parentAccount);

			final Query q = new Query(PARENTS_BLACKLIST_WORDS_ENTITY_NAME).setFilter(userIdFilter);

			final PreparedQuery pq = datastoreService.prepare(q);
			final Iterable<Entity> iterable = pq.asIterable();

			for (Entity entity : iterable) {

				final String record = (String) entity.getProperty(BLACKLISTED_WORD_FIELD_NAME);
				words.add(record);
			}

			cache.put(cacheKey, words);

			return words;
		}
	}

	public void blacklistWordForParent(final String parentAccount, final String wordToBlacklist) {
		logger.fine("blacklisting word '" + wordToBlacklist + "' for parent: " + parentAccount);

		if (parentAccount == null) {
			throw new IllegalArgumentException("parentAccount field cannot be null");
		}
		if (wordToBlacklist == null) {
			throw new IllegalArgumentException("wordToBlacklist field cannot be null");
		}

		final Entity entity = createParentBlacklistEntity(parentAccount, wordToBlacklist);
		final Key key = datastoreService.put(entity);
		logger.info("Blacklisted '" + wordToBlacklist + "' for '" + parentAccount + "', key:" + key.toString());

		invalidateBlacklistedWordsCacheForParent(parentAccount);
	}

	private String getCacheKeyBlacklistedWords(final String parentAccount) {
		return parentAccount + "-blacklistwords";
	}

	private void invalidateBlacklistedWordsCacheForParent(final String parentAccount) {
		final String cacheKey = this.getCacheKeyBlacklistedWords(parentAccount);
		cache.delete(cacheKey);
	}

	private Entity createParentBlacklistEntity(final String parentAccount, final String wordToBlacklist) {
		final DateTime now = new DateTime();
		final String keyValue = parentAccount + "-" + wordToBlacklist;
		final Entity entity = new Entity(PARENTS_BLACKLIST_WORDS_ENTITY_NAME, keyValue);

		entity.setProperty(PARENT_ACCOUNT_FIELD_NAME, parentAccount);
		entity.setProperty(BLACKLISTED_WORD_FIELD_NAME, wordToBlacklist);
		entity.setProperty(TIMESTAMP_FIELD_NAME, now.getMillis());

		return entity;
	}

	public void removeBlacklistWordForParent(final String parentAccount, final String wordToRemove) {
		final String keyValue = parentAccount + "-" + wordToRemove;
		final Key key = KeyFactory.createKey(PARENTS_BLACKLIST_WORDS_ENTITY_NAME, keyValue);
		datastoreService.delete(key);
		invalidateBlacklistedWordsCacheForParent(parentAccount);
	}

}
