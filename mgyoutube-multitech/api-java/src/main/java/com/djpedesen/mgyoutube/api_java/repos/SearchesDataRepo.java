package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

public interface SearchesDataRepo {

	List<String> getSearchesForParentUser(long userId);

	void addSearchToParentUser(long parentUserId, String searchPhrase);

}
