package com.djpedersen.mgyoutube.webservices;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.djpedersen.mgyoutube.services.YouTubeProperties;
import com.djpedersen.mgyoutube.services.YouTubeService;
import com.djpedersen.mgyoutube.services.YouTubeVideo;
import com.djpedersen.mgyoutube.services.YouTubeVideoConverter;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

@Path("/youtube")
public class YouTubeWebService {
	private static final Logger logger = Logger.getLogger(YouTubeWebService.class.getName());

	private final YouTubeService youTubeService;

	public YouTubeWebService() throws IOException {
		logger.info("YouTubeWebService()");

		final YouTubeProperties youTubeProperties = new YouTubeProperties();

		final HttpTransport httpTransport = new UrlFetchTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		this.youTubeService = new YouTubeService(youTubeProperties.apiKey, httpTransport, jsonFactory);
	}

	public YouTubeWebService(final YouTubeService youTubeService) {
		logger.info("YouTubeWebService(youTubeService)");
		this.youTubeService = youTubeService;
	}

	@GET
	@Path("/search")
	@Produces("application/json")
	public String search(@QueryParam("terms") final String uncleanSearchTerms) throws IOException {
		logger.info("search:" + uncleanSearchTerms);
		final List<YouTubeVideo> searchResults = youTubeService.search(uncleanSearchTerms);
		final YouTubeVideoConverter converter = new YouTubeVideoConverter();
		return converter.youTubeVideoListToJson(searchResults);
	}

}
