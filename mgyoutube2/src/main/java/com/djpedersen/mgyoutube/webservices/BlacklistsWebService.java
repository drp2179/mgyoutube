package com.djpedersen.mgyoutube.webservices;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.djpedersen.mgyoutube.services.AccountsService;
import com.djpedersen.mgyoutube.services.BlacklistsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Path("/blacklists")
public class BlacklistsWebService {
	private static final Logger logger = Logger.getLogger(BlacklistsWebService.class.getName());

	private final UserService userService;

	private final BlacklistsService blacklistsService;

	// private final AccountsService accountsService;

	public BlacklistsWebService() {
		final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

		this.userService = UserServiceFactory.getUserService();
		// this.accountsService = new AccountsService(datastoreService);
		this.blacklistsService = new BlacklistsService(datastoreService);
	}

	public BlacklistsWebService(final UserService userService, final AccountsService accountsService,
			final BlacklistsService blacklistsService) {
		this.userService = userService;
		// this.accountsService = accountsService;
		this.blacklistsService = blacklistsService;
	}

	private void ensureAuthenticated(final User user) {
		if (user == null) {
			throw new WebApplicationException("user must be authenticated", Response.Status.UNAUTHORIZED);
		} else {
			logger.info("User:" + user);
		}
	}

	@GET
	@Path("/words/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getBlacklistedWords() {

		final User user = userService.getCurrentUser();
		ensureAuthenticated(user);

		final String parentAccount = user.getEmail();

		final List<String> blacklistedWords = blacklistsService.getBlacklistedWordsForParent(parentAccount);
		final StringBuffer sb = new StringBuffer();
		boolean firstTime = true;
		sb.append("[");
		for (String blacklistedWord : blacklistedWords) {
			if (!firstTime) {
				sb.append(",");
			}
			sb.append("\"").append(blacklistedWord).append("\"");
			firstTime = false;
		}
		sb.append("]");

		logger.info("returning:" + sb.toString());

		return sb.toString();
	}

	@PUT
	@Path("/words/{wordToBlacklist}")
	public void addBlacklistedWord(@PathParam("wordToBlacklist") final String uncleanWordToBlacklist) {

		final User user = userService.getCurrentUser();
		ensureAuthenticated(user);

		// TODO: clean inputs
		final String wordToBlacklist = uncleanWordToBlacklist;
		final String parentAccount = user.getEmail();

		blacklistsService.blacklistWordForParent(parentAccount, wordToBlacklist);
	}

	@DELETE
	@Path("/words/{wordToBlacklist}")
	public void removeBlacklistedWord(@PathParam("wordToBlacklist") final String uncleanWordToRemove) {

		final User user = userService.getCurrentUser();
		ensureAuthenticated(user);

		// TODO: clean inputs
		final String wordToRemove = uncleanWordToRemove;
		final String parentAccount = user.getEmail();

		blacklistsService.removeBlacklistWordForParent(parentAccount, wordToRemove);
	}
}
