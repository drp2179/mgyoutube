package com.djpedesen.mgyoutube.api_java.apimodel;

import com.google.gson.Gson;

public class User {
	public String userId;
	public String username;
	public String password;
	public boolean isParent;

	public User() {

	}

	public User(User user) {
		this.userId = user.userId;
		this.username = user.username;
		this.password = user.password;
		this.isParent = user.isParent;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
