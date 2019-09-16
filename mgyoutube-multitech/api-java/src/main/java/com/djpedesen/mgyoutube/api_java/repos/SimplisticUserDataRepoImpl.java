package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public class SimplisticUserDataRepoImpl implements UserDataRepo {

	private final Map<String, User> usernameMap = new HashMap<>();
	private final Map<String, User> userIdMap = new HashMap<>();
	private final Map<String, List<String>> parentChildrenMap = new HashMap<>();
	private long nextUserId = 1;

	@Override
	public User getUserByUsername(final String username) {
		final User user = this.usernameMap.get(username);
		if (user != null) {
			// cloning so modds after return do not affect maps
			return new User(user);
		}
		return user;
	}

	@Override
	public void repositoryStartup() {
		// nothing to do here
	}

	@Override
	public synchronized User addUser(final User user) {

		// cloning so that we can mutate userId without affecting the input object
		final User addedUser = new User(user);
		addedUser.userId = Long.toString(nextUserId++);

		this.userIdMap.put(addedUser.userId, addedUser);
		this.usernameMap.put(addedUser.username, addedUser);

		// cloning so that mods after return do not mutate maps
		return new User(addedUser);
	}

	@Override
	public void removeUser(final User user) {
		this.userIdMap.remove(user.userId);
		this.usernameMap.remove(user.username);
	}

	@Override
	public User replaceUser(final String userId, final User user) {

		final User existingUser = this.userIdMap.get(userId);

		if (existingUser != null) {
			// cloning so that we can mutate userId without affecting the input object
			final User replacingUser = new User(user);
			replacingUser.userId = userId;

			this.userIdMap.put(replacingUser.userId, replacingUser);
			this.usernameMap.put(replacingUser.username, replacingUser);

			// cloning so that mods after return do not mutate maps
			return new User(replacingUser);
		}

		return null;
	}

	@Override
	public void addChildToParent(final String parentUserId, final String childUserId) {
		if (!this.parentChildrenMap.containsKey(parentUserId)) {
			this.parentChildrenMap.put(parentUserId, new ArrayList<>());
		}

		final List<String> childrenUserIds = this.parentChildrenMap.get(parentUserId);

		if (!childrenUserIds.contains(childUserId)) {
			childrenUserIds.add(childUserId);
		}
	}

	@Override
	public List<User> getChildrenForParent(final String parentUserId) {
		final List<User> children = new ArrayList<>();

		if (this.parentChildrenMap.containsKey(parentUserId)) {
			final List<String> childrenUserIds = this.parentChildrenMap.get(parentUserId);

			for (String childUserId : childrenUserIds) {
				final User user = this.getUserById(childUserId);
				if (user != null) {
					children.add(user);
				} else {
					System.out.println("unknown child userid " + childUserId);
				}
			}
		}

		return children;
	}

	// probably will be public eventually
	private User getUserById(final String userId) {
		return this.userIdMap.get(userId);
	}
}
