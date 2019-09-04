package com.djpedesen.mgyoutube.api_java.modules;

public class UserNotFoundException extends Exception {

	private static final long serialVersionUID = 7319712820184105763L;

	public String username = null;
	public long userId = Long.MIN_VALUE;

	public UserNotFoundException() {
	}

	public UserNotFoundException(final String username) {
		this.username = username;
	}

	public UserNotFoundException(final long userId) {
		this.userId = userId;
	}

}
