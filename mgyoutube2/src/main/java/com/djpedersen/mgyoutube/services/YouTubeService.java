package com.djpedersen.mgyoutube.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/*
 * @See https://github.com/youtube/api-samples
 */
public class YouTubeService {

	// must be between [0,50], should be 64...
	public static final long MAX_NUMBER_OF_VIDEOS_RETURNED = 50;

	private final String apiKey;

	private final YouTube youTube;

	public YouTubeService(final String apiKey, final HttpTransport httpTransport, final JsonFactory jsonFactory) {
		this.apiKey = apiKey;
		this.youTube = buildYouTube(httpTransport, jsonFactory);
	}

	public YouTubeService(final String apiKey, YouTube youTube) {
		this.apiKey = apiKey;
		this.youTube = youTube;
	}

	protected YouTube buildYouTube(HttpTransport httpTransport, JsonFactory jsonFactory) {
		return new YouTube.Builder(httpTransport, jsonFactory, new HttpRequestInitializer() {
			@Override
			public void initialize(HttpRequest request) throws IOException {
			}
		}).setApplicationName("mgyoutube").build();
	}

	public List<YouTubeVideo> search(final String searchTerms) throws IOException {

		final YouTube.Search.List search = youTube.search().list("id,snippet");

		search.setKey(apiKey);
		search.setQ(searchTerms);
		search.setMaxResults(MAX_NUMBER_OF_VIDEOS_RETURNED);

		// Restrict the search results to only include videos. See:
		// https://developers.google.com/youtube/v3/docs/search/list#type
		search.setType("video");

		// To increase efficiency, only retrieve the fields that the
		// application uses.
		// search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
		// @See https://developers.google.com/youtube/v3/getting-started#partial

		final SearchListResponse searchResponse = search.execute();
		final List<SearchResult> searchResultList = searchResponse.getItems();

		final List<YouTubeVideo> videosToReturn = new ArrayList<YouTubeVideo>();

		if (searchResultList != null) {
			final Iterator<SearchResult> searchResultIterator = searchResultList.iterator();
			while (searchResultIterator.hasNext()) {
				final SearchResult searchResult = searchResultIterator.next();
				final ResourceId resourceId = searchResult.getId();
				final SearchResultSnippet snippet = searchResult.getSnippet();
				final ThumbnailDetails thumbnails = snippet.getThumbnails();

				final YouTubeVideo video = new YouTubeVideo();
				video.setVideoId(resourceId.getVideoId());
				video.setTitle(snippet.getTitle());
				video.setDescription(snippet.getDescription());
				video.setThumbnailUrl(thumbnails.getDefault().getUrl());
				video.setChannelId(snippet.getChannelId());
				video.setChannelTitle(snippet.getChannelTitle());
				video.setPublishedAt(snippet.getPublishedAt());

				videosToReturn.add(video);
			}
		}

		return videosToReturn;
	}
}
