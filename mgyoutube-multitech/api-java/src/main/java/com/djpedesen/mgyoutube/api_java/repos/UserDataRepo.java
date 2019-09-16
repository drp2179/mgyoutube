package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public interface UserDataRepo {
	void repositoryStartup() throws Exception;

	User getUserByUsername(String username) throws Exception;

	User addUser(User user) throws Exception;

	void removeUser(User user) throws Exception;

	User replaceUser(String userId, User user) throws Exception;

	void addChildToParent(String parentUserId, String childUserId) throws Exception;

	List<User> getChildrenForParent(String parentUserId) throws Exception;

}
