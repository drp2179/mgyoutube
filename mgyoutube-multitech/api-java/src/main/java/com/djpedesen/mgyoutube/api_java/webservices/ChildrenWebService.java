package com.djpedesen.mgyoutube.api_java.webservices;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.djpedesen.mgyoutube.api_java.apimodel.User;
import com.djpedesen.mgyoutube.api_java.apimodel.UserCredential;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;
import com.google.gson.Gson;

@Path("/children")
public class ChildrenWebService {
	private static final Gson GSON = new Gson();

	private UserModule userModule;

	public ChildrenWebService() {
		this.userModule = ModuleRepoRegistry.getUserModule();
	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authChildUser(final String userCredentialJson) {
		System.out.println("authChildUser: userCredentialJson=" + userCredentialJson);

		// TODO: need to sanitize payload input before using
		final String sanitizedUserCredentialJson = userCredentialJson;
		final UserCredential userCredential = Helpers.marshalUserCredentialFromJson(sanitizedUserCredentialJson);
		System.out.println("authChildUser: userCredential=" + userCredential);

		final User authedUser = userModule.authUser(userCredential);

		if (authedUser == null) {
			System.out.println(
					"authChildUser: userCredentialJson=" + userCredentialJson + " failed auth, returning UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).build();
		} else if (authedUser.isParent) {
			System.out.println(
					"authChildUser: userCredentialJson=" + userCredentialJson + " is a parent, returning UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).build();
		}

		authedUser.password = null;

		final String responseJson = GSON.toJson(authedUser);

		System.out.println("authUser: userCredentialJson=" + userCredentialJson + " returning OK");
		return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
	}
}
