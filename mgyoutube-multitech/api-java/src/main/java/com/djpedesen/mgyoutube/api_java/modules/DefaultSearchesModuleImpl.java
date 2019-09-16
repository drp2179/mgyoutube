package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.repos.SearchesDataRepo;
import com.djpedesen.mgyoutube.api_java.repos.UserDataRepo;

public class DefaultSearchesModuleImpl implements SearchesModule {

	private UserDataRepo userDataRepo;
	private SearchesDataRepo searchesDataRepo;

	public DefaultSearchesModuleImpl(final UserDataRepo userDataRepo, final SearchesDataRepo searchesDataRepo) {
		this.setUserDataRepo(userDataRepo);
		this.setSearchesDataRepo(searchesDataRepo);
	}

	public void setUserDataRepo(final UserDataRepo userDataRepo) {
		this.userDataRepo = userDataRepo;
	}

	public void setSearchesDataRepo(final SearchesDataRepo searchesDataRepo) {
		this.searchesDataRepo = searchesDataRepo;
	}

	@Override
	public List<String> getSavedSearchesForParent(final String parentUsername) throws Exception {

		final User parentUser = this.userDataRepo.getUserByUsername(parentUsername);

		if (parentUser == null) {
			throw new UserNotFoundException(parentUsername);
		}

		final List<String> searches = this.searchesDataRepo.getSearchesForParentUser(parentUser.userId);

		System.out.println(
				"getSavedSearchesForParent for " + parentUsername + " returning searches.size " + searches.size());

		return searches;
	}

	@Override
	public void addSearchToParent(final String parentUsername, final String searchPhrase) throws Exception {
		final User parentUser = this.userDataRepo.getUserByUsername(parentUsername);

		if (parentUser == null) {
			throw new UserNotFoundException(parentUsername);
		}

		this.searchesDataRepo.addSearchToParentUser(parentUser.userId, searchPhrase);
	}

}
