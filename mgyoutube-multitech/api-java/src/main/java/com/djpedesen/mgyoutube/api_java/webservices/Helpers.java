package com.djpedesen.mgyoutube.api_java.webservices;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.google.gson.Gson;

public class Helpers {
	private static final Gson GSON = new Gson();

	public static User marshalUserFromJson(final String userJson) {

		final User user = GSON.fromJson(userJson, User.class);

		// TODO: do additional content validation

		return user;
	}

}
