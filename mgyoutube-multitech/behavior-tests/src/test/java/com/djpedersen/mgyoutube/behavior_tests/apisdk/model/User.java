package com.djpedersen.mgyoutube.behavior_tests.apisdk.model;

public class User {

	public String userId;
	public String username;
	public String password;
	public boolean isParent;

	public User(final String username, final String password) {
		this(username, password, false);
	}

	public User(final String username, final String password, final boolean isParent) {
		this.username = username;
		this.password = password;
		this.isParent = isParent;
	}

	public static User createChildUser(final String username, final String password) {
		return new User(username, password, false);
	}

	public static User createParentUser(final String username, final String password) {
		return new User(username, password, true);
	}

}
