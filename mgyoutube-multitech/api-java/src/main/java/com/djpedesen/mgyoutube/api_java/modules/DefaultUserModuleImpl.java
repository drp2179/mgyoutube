package com.djpedesen.mgyoutube.api_java.modules;

import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.djpedesen.mgyoutube.api_java.repos.UserDataRepo;

public class DefaultUserModuleImpl implements UserModule {

	private UserDataRepo userDataRepo;

	public DefaultUserModuleImpl(final UserDataRepo userDataRepo) {
		this.setUserDataRepo(userDataRepo);
	}

	public void setUserDataRepo(final UserDataRepo userDataRepo) {
		this.userDataRepo = userDataRepo;
	}

	@Override
	public User authUser(final UserCredential userCredential) {

		User user = null;
		try {
			user = userDataRepo.getUserByUsername(userCredential.username);

			if (user != null) {

				if (user.password == null) {
					System.out.println("user.password is null");
					user = null;
				} else if (userCredential.password == null) {
					System.out.println("userCredential.password is null");
					user = null;
				} else if (!user.password.equals(userCredential.password)) {
					System.out.println("user.password(" + user.password + ") != userCredential.password("
							+ userCredential.password + ")");
					user = null;
				} else {
					System.out.println("password match!");
					// password match!
				}
			} else {
				System.out.println("getUserByUsername(" + userCredential.username + ") returned null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			user = null;
		}

		return user;
	}

	@Override
	public User createUpdateUser(final User user) throws Exception {
		final User existingUser = this.userDataRepo.getUserByUsername(user.username);
		if (existingUser != null) {
			if (user.userId == null) {
				user.userId = existingUser.userId;
			}
			System.out.println("createUpdateUser is updating existing user " + existingUser + " to be " + user);
			return this.updateUser(existingUser.userId, user);
		}
		System.out.println("createUpdateUser is creating new user " + user);
		return this.createUser(user);
	}

	@Override
	public User createUser(final User user) throws Exception {

		if (user.userId == null) {
			final User createdUser = this.userDataRepo.addUser(user);

			if (createdUser != null) {
				return createdUser;
			}
		}

		return null;
	}

	@Override
	public User updateUser(final String userId, final User user) throws Exception {
		if (userId == null) {
			throw new IllegalArgumentException("parameter 'userId' must not be null");
		}
		if (user == null) {
			throw new IllegalArgumentException("parameter 'user' must not be null");
		}
		if (!userId.equals(user.userId)) {
			if (user.userId != null) {
				throw new IllegalStateException("user.userId is not null AND userId (" + userId + ") and user.userId ("
						+ user.userId + ") paremters do not match");
			}
			user.userId = userId;
		}

		return this.userDataRepo.replaceUser(userId, user);
	}

	@Override
	public User getUser(final String username) {
		try {
			return this.userDataRepo.getUserByUsername(username);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public User removeUser(final String username) throws Exception {
		final User user = this.getUser(username);

		if (user != null) {
			this.userDataRepo.removeUser(user);
		}

		return user;
	}

	@Override
	public User addUpdateChildToParent(final String parentUsername, final User childUser) throws Exception {

		final User parentUser = this.getUser(parentUsername);
		if (parentUser == null) {
			throw new UserNotFoundException(parentUsername);
		}

		final User existingChildUser = this.getUser(childUser.username);
		if (existingChildUser == null) {
			System.out.println("creating child " + childUser);
			final User createdChildUser = this.createUser(childUser);
			userDataRepo.addChildToParent(parentUser.userId, createdChildUser.userId);
			return createdChildUser;
		} else {
			System.out.println("updating child " + childUser + " as " + existingChildUser.userId);
			userDataRepo.addChildToParent(parentUser.userId, existingChildUser.userId);
			return this.updateUser(existingChildUser.userId, childUser);
		}
	}

	@Override
	public List<User> getChildrenForParent(final String parentUsername) throws Exception {
		final User parentUser = this.getUser(parentUsername);
		if (parentUser == null) {
			throw new UserNotFoundException(parentUsername);
		}

		final List<User> children = userDataRepo.getChildrenForParent(parentUser.userId);

		return children;
	}

}
