package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimplisticSearchesDataRepoImpl implements SearchesDataRepo {

	private final Map<String, Set<String>> parentSearches = new HashMap<>();

	@Override
	public void repositoryStartup() {
		// nothing to do
	}

	@Override
	public List<String> getSearchesForParentUser(final String parentUserId) {
		if (parentSearches.containsKey(parentUserId)) {
			return new ArrayList<>(parentSearches.get(parentUserId));
		}
		return new ArrayList<>();
	}

	@Override
	public void addSearchToParentUser(final String parentUserId, final String searchPhrase) {
		if (!parentSearches.containsKey(parentUserId)) {
			parentSearches.put(parentUserId, new HashSet<>());
		}

		final Set<String> phrases = parentSearches.get(parentUserId);
		phrases.add(searchPhrase);
	}

	@Override
	public void removeSearchFromParentUser(final String parentUserId, final String searchPhrase) throws Exception {
		if (parentSearches.containsKey(parentUserId)) {
			final Set<String> phrases = parentSearches.get(parentUserId);
			phrases.remove(searchPhrase);
		}
	}
}
