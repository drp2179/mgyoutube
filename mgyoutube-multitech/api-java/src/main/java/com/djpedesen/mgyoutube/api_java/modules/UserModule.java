package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;

public interface UserModule {

	User authUser(UserCredential userCredential);

	User createUpdateUser(User user) throws Exception;

	User createUser(User user) throws Exception;

	User updateUser(String userId, User user) throws Exception;

	User getUser(String username) throws Exception;

	User removeUser(String username) throws Exception;

	User addUpdateChildToParent(String parentUsername, User childUser) throws Exception;

	List<User> getChildrenForParent(String parentUsername) throws Exception;

}
