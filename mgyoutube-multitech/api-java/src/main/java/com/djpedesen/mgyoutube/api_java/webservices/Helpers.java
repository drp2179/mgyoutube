package com.djpedesen.mgyoutube.api_java.webservices;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.google.gson.Gson;

public class Helpers {
	private static final Gson GSON = new Gson();

	public static User marshalUserFromJson(final String userJson) {

		final User user = GSON.fromJson(userJson, User.class);

		// TODO: do additional content validation

		return user;
	}

	public static UserCredential marshalUserCredentialFromJson(final String userCredentialJson) {

		final UserCredential userCredential = GSON.fromJson(userCredentialJson, UserCredential.class);

		// System.out.println("userCredential json: " + userCredentialJson + " object: "
		// + userCredential);
		//
		// post object create validation
		//
		if (userCredential.password == null || userCredential.username == null) {
			System.out.println("userCredential username or password is null, returning null");
			return null;
		}

		return userCredential;
	}

}
