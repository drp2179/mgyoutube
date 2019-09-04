package com.djpedesen.mgyoutube.api_java.webservices;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;
import com.djpedesen.mgyoutube.api_java.modules.UserNotFoundException;
import com.google.gson.Gson;

@Path("/parents")
public class ParentsWebService {
	private static final Gson GSON = new Gson();

	private UserModule userModule;

	public ParentsWebService() {
		this.userModule = ModuleRepoRegistry.getUserModule();
	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authParentUser(final String userCredentialJson) {
		System.out.println("authParentUser: userCredentialJson=" + userCredentialJson);

		// TODO: need to sanitize payload input before using
		final String sanitizedUserCredentialJson = userCredentialJson;
		final UserCredential userCredential = marshalUserCredentialFromJson(sanitizedUserCredentialJson);
		System.out.println("authParentUser: userCredential=" + userCredential);

		final User authedUser = userModule.authUser(userCredential);

		if (authedUser == null) {
			System.out.println("authParentUser: userCredentialJson=" + userCredentialJson
					+ " failed auth, returning UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).build();
		} else if (!authedUser.isParent) {
			System.out.println("authParentUser: userCredentialJson=" + userCredentialJson
					+ " is not a parent, returning UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		authedUser.password = null;

		final String responseJson = GSON.toJson(authedUser);

		System.out.println("authParentUser: userCredentialJson=" + userCredentialJson + " returning OK");
		return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/{parentUsername}/children")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildrenForParent(@PathParam(value = "parentUsername") final String parentUsername) {
		System.out.println("getChildrenForParent: parentUsername=" + parentUsername);

		// TODO: need to sanitize payload input before using
		final String sanitizedParentUsername = parentUsername;

		try {
			final List<User> children = userModule.getChildrenForParent(sanitizedParentUsername);
			System.out.println("getChildrenForParent: getChildrenForParent=" + children);

			children.forEach(u -> {
				u.password = null;
			});

			final String responseJson = GSON.toJson(children);

			System.out.println("getChildrenForParent: parentUsername=" + parentUsername + " returning OK for size "
					+ children.size() + " and json=" + responseJson);
			return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
		} catch (UserNotFoundException unfe) {
			System.out.println("getChildrenForParent: unable to find user " + unfe.username + " returning NOT_FOUND");
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@PUT
	@Path("/{parentUsername}/children/{childUsername}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUpdateChildToParent(@PathParam(value = "parentUsername") final String parentUsername,
			@PathParam(value = "childUsername") final String childUsername, final String childUserJson) {
		System.out.println("addUpdateChildToParent: parentUsername=" + parentUsername + ", childUsername="
				+ childUsername + ", childUserJson=" + childUserJson);

		// TODO: need to sanitize payload input before using
		final String sanitizedParentUsername = parentUsername;
		// final String sanitizedChildUsername = childUsername;
		final String sanitizedChildUserJson = childUserJson;

		final User childUser = Helpers.marshalUserFromJson(sanitizedChildUserJson);
		System.out.println("addUpdateChildToParent: childUser=" + childUser);

		try {
			final User addedUpdatedUser = userModule.addUpdateChildToParent(sanitizedParentUsername, childUser);

			// if (addedUpdatedUser == null) {
			// System.out.println("authParentUser: userCredentialJson=" + userCredentialJson
			// + " failed auth, returning UNAUTHORIZED");
			// return Response.status(Status.UNAUTHORIZED).build();
			// } else if (!authedUser.isParent) {
			// System.out.println("authParentUser: userCredentialJson=" + userCredentialJson
			// + " is not a parent, returning UNAUTHORIZED");
			// return Response.status(Status.UNAUTHORIZED).build();
			// }

			addedUpdatedUser.password = null;

			final String responseJson = GSON.toJson(addedUpdatedUser);

			System.out.println("addUpdateChildToParent: parentUsername=" + parentUsername + ", childUsername="
					+ childUsername + " returning OK");
			return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
		} catch (UserNotFoundException unfe) {
			System.out.println("addUpdateChildToParent: unable to find user " + unfe.username + " returning NOT_FOUND");
			return Response.status(Status.NOT_FOUND).build();
		}
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
