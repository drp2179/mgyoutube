package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

public interface SearchesDataRepo {

	void repositoryStartup() throws Exception;

	List<String> getSearchesForParentUser(String userId) throws Exception;

	void addSearchToParentUser(String parentUserId, String searchPhrase) throws Exception;

}
