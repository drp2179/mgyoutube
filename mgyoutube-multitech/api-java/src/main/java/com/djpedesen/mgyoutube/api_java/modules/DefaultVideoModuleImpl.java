package com.djpedesen.mgyoutube.api_java.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.djpedesen.mgyoutube.api_java.apimodel.Video;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;

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
		// this.youTube = buildYouTube(applicationName, httpTransport, jsonFactory);
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

		System.out.println("searching for " + searchTerms);
		final SearchListResponse searchResponse = search.execute();
		final List<SearchResult> searchResultList = searchResponse.getItems();

		final List<Video> videosToReturn = new ArrayList<Video>();

		if (searchResultList != null) {
			for (SearchResult searchResult : searchResultList) {
				System.out.println("searchResult " + searchResult);
				// final ResourceId resourceId = searchResult.getId(); // id
				final SearchResultSnippet snippet = searchResult.getSnippet(); // snippet
				// final ThumbnailDetails thumbnails = snippet.getThumbnails(); // thumbnails

				final Video video = new Video();
				// video.setVideoId(resourceId.getVideoId());
				video.title = snippet.getTitle();
				video.description = snippet.getDescription();
				// video.setThumbnailUrl(thumbnails.getDefault().getUrl());
				// video.setChannelId(snippet.getChannelId());
				// video.setChannelTitle(snippet.getChannelTitle());
				// video.setPublishedAt(snippet.getPublishedAt());

				videosToReturn.add(video);
			}
		}

		return videosToReturn;
	}

}
