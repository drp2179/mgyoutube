package com.djpedesen.mgyoutube.api_java.repos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djpedesen.mgyoutube.api_java.apimodel.User;

public class SimplisticUserDataRepoImpl implements UserDataRepo {

	private final Map<String, User> usernameMap = new HashMap<>();
	private final Map<Long, User> userIdMap = new HashMap<>();
	private final Map<Long, List<Long>> parentChildrenMap = new HashMap<>();
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
	public synchronized User addUser(final User user) {

		// cloning so that we can mutate userId without affecting the input object
		final User addedUser = new User(user);
		addedUser.userId = nextUserId++;

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
	public User replaceUser(final long userId, final User user) {

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
	public void addChildToParent(final long parentUserId, final long childUserId) {
		if (!this.parentChildrenMap.containsKey(parentUserId)) {
			this.parentChildrenMap.put(parentUserId, new ArrayList<>());
		}

		final List<Long> childrenUserIds = this.parentChildrenMap.get(parentUserId);

		if (!childrenUserIds.contains(childUserId)) {
			childrenUserIds.add(childUserId);
		}
	}

	@Override
	public List<User> getChildrenForParent(final long parentUserId) {
		final List<User> children = new ArrayList<>();

		if (this.parentChildrenMap.containsKey(parentUserId)) {
			final List<Long> childrenUserIds = this.parentChildrenMap.get(parentUserId);

			for (Long childUserId : childrenUserIds) {
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
	private User getUserById(final long userId) {
		return this.userIdMap.get(userId);
	}

}
