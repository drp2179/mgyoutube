package com.djpedesen.mgyoutube.api_java.repos;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public interface UserDataRepo {

	User getUserByUsername(String username);

	User addUser(User user);

	void removeUser(User user);

	User replaceUser(long userId, User user);

	void addChildToParent(long parentUserId, long childUserId);

	List<User> getChildrenForParent(long userId);

}
