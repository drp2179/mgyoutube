package com.djpedesen.mgyoutube.api_java.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NoHttpResponseException;

import com.djpedesen.mgyoutube.api_java.apimodel.Video;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.ThumbnailDetails;

public class DefaultVideoModuleImpl implements VideoModule {
	// must be between [0,50], should be 64...
	public static final long MAX_NUMBER_OF_VIDEOS_RETURNED = 50;
	private final String apiKey;
	private final String applicationName;
	private final YouTube youTube;
	private final HttpTransport httpTransport;
	private final JsonFactory jsonFactory;

	public DefaultVideoModuleImpl(final String apiKey, final String applicationName, final HttpTransport httpTransport,
			final JsonFactory jsonFactory) {
		this.apiKey = apiKey;
		this.applicationName = applicationName;
		youTube = null;
		this.httpTransport = httpTransport;
		this.jsonFactory = jsonFactory;
	}

	public DefaultVideoModuleImpl(final String apiKey, final String applicationName, final YouTube youTube) {
		this.apiKey = apiKey;
		this.applicationName = applicationName;
		this.youTube = youTube;
		this.httpTransport = null;
		this.jsonFactory = null;
	}

	protected YouTube getYouTube() {
		if (this.youTube != null) {
			return this.youTube;
		}

		return new YouTube.Builder(this.httpTransport, this.jsonFactory, new HttpRequestInitializer() {
			@Override
			public void initialize(final HttpRequest request) throws IOException {
			}
		}).setApplicationName(this.applicationName).build();
	}

	@Override
	public List<Video> search(final String searchTerms) throws IOException {
		final YouTube.Search.List search = this.getYouTube().search().list("id,snippet");

		search.setKey(this.apiKey);
		search.setQ(searchTerms);
		search.setMaxResults(MAX_NUMBER_OF_VIDEOS_RETURNED);
		search.setSafeSearch("strict");

		// Restrict the search results to only include videos. See:
		// https://developers.google.com/youtube/v3/docs/search/list#type
		search.setType("video");

		// To increase efficiency, only retrieve the fields that the
		// application uses.
		// search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
		// @See https://developers.google.com/youtube/v3/getting-started#partial

		System.out.println("searching for '" + searchTerms + "'");
		final SearchListResponse searchResponse = getSearchResponse(search);
		final List<SearchResult> searchResultList = searchResponse.getItems();
		final List<Video> videosToReturn = new ArrayList<Video>();

		if (searchResultList != null) {
			for (SearchResult searchResult : searchResultList) {
				// System.out.println("searchResult " + searchResult);
				final ResourceId resourceId = searchResult.getId(); // id
				final SearchResultSnippet snippet = searchResult.getSnippet(); // snippet
				final ThumbnailDetails thumbnails = snippet.getThumbnails(); // thumbnails

				final Video video = new Video();
				video.videoId = resourceId.getVideoId();
				video.title = snippet.getTitle();
				video.description = snippet.getDescription();
				video.thumbnailUrl = thumbnails.getDefault().getUrl();
				video.thumbnailHeight = thumbnails.getDefault().getHeight();
				video.thumbnailWidth = thumbnails.getDefault().getWidth();
				video.channelId = snippet.getChannelId();
				video.channelTitle = snippet.getChannelTitle();
				if (snippet.getPublishedAt() != null) {
					video.publishedAt = snippet.getPublishedAt().toString();
				}

				videosToReturn.add(video);
			}
		}

		return videosToReturn;
	}

	private SearchListResponse getSearchResponse(com.google.api.services.youtube.YouTube.Search.List search)
			throws IOException {
		SearchListResponse searchResponse = null;
		int attempts = 0;
		do {
			attempts++;
			try {
				searchResponse = search.execute();
			} catch (NoHttpResponseException nhre) {
				System.out.println(nhre.getMessage());
				if (attempts < 3) {
					System.out.println("" + attempts + " < 3 so trying again");
				} else {
					System.out.println(">= 3 attempts so re-throwing the exception");
					throw nhre;
				}
			}
		} while (searchResponse == null);

		return searchResponse;
	}

}
