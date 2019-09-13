package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

public interface SearchesModule {

	List<String> getSavedSearchesForParent(String parentUsername) throws UserNotFoundException;

	void addSearchToParent(String parentUsername, String searchPhrase) throws UserNotFoundException;

}
