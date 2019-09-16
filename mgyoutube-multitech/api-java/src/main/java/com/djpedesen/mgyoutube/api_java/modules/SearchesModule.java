package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

public interface SearchesModule {

	List<String> getSavedSearchesForParent(String parentUsername) throws Exception;

	void addSearchToParent(String parentUsername, String searchPhrase) throws Exception;

}
