package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

public interface SearchesDataRepo {

	void repositoryStartup();

	List<String> getSearchesForParentUser(String userId);

	void addSearchToParentUser(String parentUserId, String searchPhrase);

}
