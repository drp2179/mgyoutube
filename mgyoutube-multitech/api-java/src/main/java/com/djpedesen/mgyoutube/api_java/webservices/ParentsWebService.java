package com.djpedesen.mgyoutube.api_java.webservices;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.djpedesen.mgyoutube.api_java.modules.SearchesModule;
import com.djpedesen.mgyoutube.api_java.modules.UserModule;
import com.djpedesen.mgyoutube.api_java.modules.UserNotFoundException;
import com.google.gson.Gson;

@Path("/parents")
public class ParentsWebService {
	private static final Gson GSON = new Gson();

	private UserModule userModule;
	private SearchesModule searchesModule;

	public ParentsWebService() {
		this.userModule = ModuleRepoRegistry.getUserModule();
		this.searchesModule = ModuleRepoRegistry.getSearchesModule();
	}

	@POST
	@Path("/auth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authParentUser(final String userCredentialJson) {
		try {
			System.out.println("authParentUser: userCredentialJson=" + userCredentialJson);

			// TODO: need to sanitize payload input before using
			final String sanitizedUserCredentialJson = userCredentialJson;
			final UserCredential userCredential = Helpers.marshalUserCredentialFromJson(sanitizedUserCredentialJson);
			System.out.println("authParentUser: userCredential=" + userCredential);

			final User authedUser = userModule.authUser(userCredential);

			if (authedUser == null) {
				System.out.println("authParentUser: userCredentialJson=" + sanitizedUserCredentialJson
						+ " failed auth, returning UNAUTHORIZED");
				return Response.status(Status.UNAUTHORIZED).build();
			} else if (!authedUser.isParent) {
				System.out.println("authParentUser: userCredentialJson=" + sanitizedUserCredentialJson
						+ " is not a parent, returning UNAUTHORIZED");
				return Response.status(Status.UNAUTHORIZED).build();
			}

			authedUser.password = null;

			final String responseJson = GSON.toJson(authedUser);

			System.out.println("authParentUser: userCredentialJson=" + sanitizedUserCredentialJson + " returning OK");
			return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
		} catch (Throwable thrown) {
			System.out.println("authParentUser, unhandled exception:");
			thrown.printStackTrace(System.out);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/{parentUsername}/children")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChildrenForParent(@PathParam(value = "parentUsername") final String parentUsername) {
		try {
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

				System.out.println("getChildrenForParent: parentUsername=" + sanitizedParentUsername
						+ " returning OK for size " + children.size() + " and json=" + responseJson);
				return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
			} catch (UserNotFoundException unfe) {
				System.out
						.println("getChildrenForParent: unable to find user " + unfe.username + " returning NOT_FOUND");
				return Response.status(Status.NOT_FOUND).build();
			}
		} catch (Throwable thrown) {
			System.out.println("getChildrenForParent, unhandled exception:");
			thrown.printStackTrace(System.out);
			return Response.serverError().build();
		}
	}

	@PUT
	@Path("/{parentUsername}/children/{childUsername}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUpdateChildToParent(@PathParam(value = "parentUsername") final String parentUsername,
			@PathParam(value = "childUsername") final String childUsername, final String childUserJson) {
		try {
			System.out.println("addUpdateChildToParent: parentUsername=" + parentUsername + ", childUsername="
					+ childUsername + ", childUserJson=" + childUserJson);

			// TODO: need to sanitize payload input before using
			final String sanitizedParentUsername = parentUsername;
			final String sanitizedChildUsername = childUsername;
			final String sanitizedChildUserJson = childUserJson;

			final User childUser = Helpers.marshalUserFromJson(sanitizedChildUserJson);
			System.out.println("addUpdateChildToParent: childUser=" + childUser);

			try {
				final User addedUpdatedUser = userModule.addUpdateChildToParent(sanitizedParentUsername, childUser);

				addedUpdatedUser.password = null;

				final String responseJson = GSON.toJson(addedUpdatedUser);

				System.out.println("addUpdateChildToParent: parentUsername=" + sanitizedParentUsername
						+ ", childUsername=" + sanitizedChildUsername + " returning OK");
				return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
			} catch (UserNotFoundException unfe) {
				System.out.println(
						"addUpdateChildToParent: unable to find user " + unfe.username + " returning NOT_FOUND");
				return Response.status(Status.NOT_FOUND).build();
			}
		} catch (Throwable thrown) {
			System.out.println("addUpdateChildToParent, unhandled exception:");
			thrown.printStackTrace(System.out);
			return Response.serverError().build();
		}
	}

	@GET
	@Path("/{parentUsername}/searches")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSavedSearches(@PathParam(value = "parentUsername") final String parentUsername) {
		try {
			System.out.println("getSavedSearches: parentUsername=" + parentUsername);

			// TODO: need to sanitize payload input before using
			final String sanitizedParentUsername = parentUsername;

			try {
				final List<String> searches = searchesModule.getSavedSearchesForParent(sanitizedParentUsername);
				System.out.println("getSavedSearches: getSavedSearchesForParent=" + searches);

				final String responseJson = GSON.toJson(searches);

				System.out.println("getSavedSearches: parentUsername=" + sanitizedParentUsername
						+ " returning OK for size " + searches.size() + " and json=" + responseJson);
				return Response.ok(responseJson, MediaType.APPLICATION_JSON).build();
			} catch (UserNotFoundException unfe) {
				System.out.println("getSavedSearches: unable to find user " + unfe.username + " returning NOT_FOUND");
				return Response.status(Status.NOT_FOUND).build();
			}
		} catch (Throwable thrown) {
			System.out.println("getSavedSearches, unhandled exception:");
			thrown.printStackTrace(System.out);
			return Response.serverError().build();
		}
	}

	@PUT
	@Path("/{parentUsername}/searches/{searchphrase}")
	public Response saveSearch(@PathParam(value = "parentUsername") final String parentUsername,
			@PathParam(value = "searchphrase") final String searchPhrase) throws UnsupportedEncodingException {
		try {
			final String decodedSearchPhrase = URLDecoder.decode(searchPhrase, StandardCharsets.UTF_8.toString());
			System.out.println("saveSearch: parentUsername=" + parentUsername + ", searchPhrase=" + searchPhrase
					+ ", decodedSearchPhrase=" + decodedSearchPhrase);

			// TODO: need to sanitize payload input before using
			final String sanitizedParentUsername = parentUsername;
			final String sanitizedSearchPhrase = decodedSearchPhrase;

			try {
				searchesModule.addSearchToParent(sanitizedParentUsername, sanitizedSearchPhrase);

				System.out.println("saveSearch: parentUsername=" + sanitizedParentUsername + ", searchPhrase="
						+ sanitizedSearchPhrase + " returning CREATED");
				final URI location = new URI("/parents/" + sanitizedParentUsername + "/searches");
				return Response.created(location).build();
			} catch (UserNotFoundException unfe) {
				System.out.println("saveSearch: unable to find user " + unfe.username + " returning NOT_FOUND");
				return Response.status(Status.NOT_FOUND).build();
			} catch (URISyntaxException use) {
				System.out.println("saveSearch: problem creating location for response");
				use.printStackTrace(System.out);
				return Response.serverError().build();
			}
		} catch (Throwable thrown) {
			System.out.println("saveSearch, unhandled exception:");
			thrown.printStackTrace(System.out);
			return Response.serverError().build();
		}
	}

}
