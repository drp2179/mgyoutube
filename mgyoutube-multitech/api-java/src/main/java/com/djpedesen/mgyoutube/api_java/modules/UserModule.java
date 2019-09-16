package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;

public interface UserModule {

	User authUser(UserCredential userCredential);

	User createUpdateUser(User user);

	User createUser(User user);

	User updateUser(String userId, User user);

	User getUser(String username);

	User removeUser(String username);

	User addUpdateChildToParent(String parentUsername, User childUser) throws UserNotFoundException;

	List<User> getChildrenForParent(String parentUsername) throws UserNotFoundException;

}
