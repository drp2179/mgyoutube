package com.djpedesen.mgyoutube.api_java.webservices;

import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;
import com.google.gson.Gson;

@Path("/support")
public class SupportWebService {
	private static final Gson GSON = new Gson();

	private UserModule userModule;

	public SupportWebService() {
		this.userModule = ModuleRepoRegistry.getUserModule();
	}

	@GET
	@Path("/users/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserByUsername(@PathParam(value = "username") final String username) {
		System.out.println("getUserByUsername: username=" + username);

		// TODO: need to sanitize payload input before using
		final String santizedUsername = username;
		final User user = userModule.getUser(santizedUsername);
		System.out.println("getUserByUsername: user=" + user);

		if (user == null) {
			System.out.println("getUserByUsername: username=" + santizedUsername + " returning NOT_FOUND");
			return Response.status(Status.NOT_FOUND).build();
		}

		user.password = null;

		final String responseJson = GSON.toJson(user);

		System.out.println("getUserByUsername: username=" + santizedUsername + " returning OK");
		return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
	}

	@PUT
	@Path("/users/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUpdateUserByUsername(@PathParam(value = "username") final String username,
			final String userJson) throws URISyntaxException {
		System.out.println("createUpdateUserByUsername: username=" + username + " userJson=" + userJson);

		// TODO: need to sanitize payload input before using
		// final String sanitizedUsername = username;
		final String sanitizedUserJson = userJson;
		final User user = Helpers.marshalUserFromJson(sanitizedUserJson);
		System.out.println("createUpdateUserByUsername: user=" + user);

		final User createdUser = userModule.createUpdateUser(user);
		createdUser.password = null;
		final String createdUserJson = GSON.toJson(createdUser);

		// final URI location = new URI("/users/" + createdUser.userId);

		System.out.println(
				"createUpdateUserByUsername: userJson=" + sanitizedUserJson + " returning OK with " + createdUserJson);
		return Response.ok(createdUserJson, MediaType.APPLICATION_JSON).build();
	}

	@DELETE
	@Path("/users/{username}")
	public Response deleteUserByUsername(@PathParam(value = "username") final String username) {
		System.out.println("deleteUserByUsername: username=" + username);

		// TODO: need to sanitize payload input before using
		final String santizedUsername = username;
		final User oldUser = userModule.removeUser(santizedUsername);

		if (oldUser == null) {
			System.out.println("deleteUserByUsername: username=" + santizedUsername + " returning NOT_FOUND");
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok().build();
	}

}
