package com.djpedesen.mgyoutube.api_java.apimodel;

import com.google.gson.Gson;

public class UserCredential {
	public String username;
	public String password;

	public UserCredential() {

	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
