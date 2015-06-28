package com.djpedersen.mgyoutube.webservices;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.djpedersen.mgyoutube.services.AccountsService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Path("/accounts")
public class AccountsWebService {
	private static final Logger logger = Logger.getLogger(AccountsWebService.class.getName());

	private final UserService userService;
	private final AccountsService accountsService;

	public AccountsWebService() {
		final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

		this.userService = UserServiceFactory.getUserService();
		this.accountsService = new AccountsService(datastoreService);
	}

	public AccountsWebService(final UserService userService, final AccountsService accountsService) {
		this.userService = userService;
		this.accountsService = accountsService;
	}

	private void ensureAuthenticated(final User user) {
		if (user == null) {
			throw new WebApplicationException("user must be authenticated", Response.Status.UNAUTHORIZED);
		} else {
			logger.info("User:" + user);
		}
	}

	@PUT
	@Path("/associated/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void associateChildAccount(@FormParam("childAccount") final String uncleanChildAccount) {

		final User user = userService.getCurrentUser();
		ensureAuthenticated(user);

		// TODO: clean inputs
		final String childAccount = uncleanChildAccount;
		final String parentAccount = user.getEmail();

		accountsService.associateParentChildAccounts(parentAccount, childAccount);
	}

	@GET
	@Path("/associated/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAssociatedChildAccounts() {

		final User user = userService.getCurrentUser();
		ensureAuthenticated(user);

		final String parentAccount = user.getEmail();

		final List<String> childAccountsForParent = accountsService.getChildAccountsForParent(parentAccount);
		final StringBuffer sb = new StringBuffer();
		boolean firstTime = true;
		sb.append("[");
		for (String childAccount : childAccountsForParent) {
			if (!firstTime) {
				sb.append(",");
			}
			sb.append("\"").append(childAccount).append("\"");
			firstTime = false;
		}
		sb.append("]");

		logger.info("returning:" + sb.toString());

		return sb.toString();
	}
}
