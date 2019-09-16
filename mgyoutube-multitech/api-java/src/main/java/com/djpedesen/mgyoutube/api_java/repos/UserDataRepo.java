package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public interface UserDataRepo {
	void repositoryStartup();

	User getUserByUsername(String username);

	User addUser(User user);

	void removeUser(User user);

	User replaceUser(String userId, User user);

	void addChildToParent(String parentUserId, String childUserId);

	List<User> getChildrenForParent(String userId);

}
