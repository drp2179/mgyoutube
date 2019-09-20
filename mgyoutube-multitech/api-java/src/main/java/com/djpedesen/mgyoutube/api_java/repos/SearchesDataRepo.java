package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

public interface SearchesDataRepo {

	void repositoryStartup() throws Exception;

	List<String> getSearchesForParentUser(String parentUserId) throws Exception;

	void addSearchToParentUser(String parentUserId, String searchPhrase) throws Exception;

	void removeSearchFromParentUser(String parentUserId, String searchPhrase) throws Exception;

}
