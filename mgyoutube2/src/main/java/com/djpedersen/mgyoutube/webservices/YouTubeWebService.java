package com.djpedersen.mgyoutube.webservices;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import com.djpedersen.mgyoutube.services.SavedSearch;
import com.djpedersen.mgyoutube.services.SearchConverter;
import com.djpedersen.mgyoutube.services.SearchTermsService;
import com.djpedersen.mgyoutube.services.YouTubeProperties;
import com.djpedersen.mgyoutube.services.YouTubeService;
import com.djpedersen.mgyoutube.services.YouTubeVideo;
import com.djpedersen.mgyoutube.services.YouTubeVideoConverter;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@Path("/youtube")
public class YouTubeWebService {
	private static final Logger logger = Logger.getLogger(YouTubeWebService.class.getName());

	private final YouTubeService youTubeService;

	private final SearchTermsService searchTermsService;

	private final UserService userService;

	public YouTubeWebService() throws IOException {
		logger.info("YouTubeWebService()");

		final YouTubeProperties youTubeProperties = new YouTubeProperties();
		final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

		final HttpTransport httpTransport = new UrlFetchTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		this.youTubeService = new YouTubeService(youTubeProperties.apiKey, httpTransport, jsonFactory);
		this.searchTermsService = new SearchTermsService(datastoreService);
		this.userService = UserServiceFactory.getUserService();
	}

	public YouTubeWebService(final YouTubeService youTubeService, final SearchTermsService searchTermService) {
		logger.info("YouTubeWebService(youTubeService)");
		this.youTubeService = youTubeService;
		this.searchTermsService = searchTermService;
		this.userService = UserServiceFactory.getUserService();
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public String search(@QueryParam("terms") final String uncleanSearchTerms) throws IOException {

		logger.info("search:" + uncleanSearchTerms);
		final List<YouTubeVideo> searchResults = youTubeService.search(uncleanSearchTerms);
		final YouTubeVideoConverter converter = new YouTubeVideoConverter();
		final String youTubeVideoListToJson = converter.youTubeVideoListToJson(searchResults);

		final User user = userService.getCurrentUser();

		// create search audit record
		if (user != null) {
			final DateTime now = new DateTime();
			searchTermsService.recordUserSearchAudit(user.getUserId(), uncleanSearchTerms, now);
		}

		return youTubeVideoListToJson;
	}

	@POST
	@Path("/search/save")
	// @Consumes(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	// @Produces(MediaType.APPLICATION_JSON)
	public void saveSearchForUser(@FormParam("searchTerms") final String uncleanSearchTerms,
			@FormParam("thumbnailUrl") final String uncleanThumbnailUrl) throws IOException {

		logger.info("saveSearchForUser:" + uncleanSearchTerms + "," + uncleanThumbnailUrl);

		final User user = userService.getCurrentUser();
		if (user == null) {
			throw new WebApplicationException("user must be authenticated", Response.Status.UNAUTHORIZED);
		}

		searchTermsService.saveSearch(user.getUserId(), uncleanSearchTerms, uncleanThumbnailUrl);

		// return youTubeVideoListToJson;
	}

	@GET
	@Path("/savedsearches")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSavedSearchesForUser() throws IOException {

		logger.info("getSavedSearchesForUser");
		final User user = userService.getCurrentUser();
		if (user == null) {
			throw new WebApplicationException("user must be authenticated", Response.Status.UNAUTHORIZED);
		} else {
			logger.info("User:" + user);
		}

		final List<SavedSearch> savedSearchesForUser = searchTermsService.getSavedSearchesForUser(user.getUserId());

		final SearchConverter converter = new SearchConverter();
		final String savedSearchListToJson = converter.savedSearchListToJson(savedSearchesForUser);
		logger.info(savedSearchListToJson);

		return savedSearchListToJson;
	}
}
