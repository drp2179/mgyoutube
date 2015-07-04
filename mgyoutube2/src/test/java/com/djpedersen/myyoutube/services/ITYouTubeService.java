package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.entities.YouTubeVideo;
import com.djpedersen.mgyoutube.services.YouTubeProperties;
import com.djpedersen.mgyoutube.services.YouTubeService;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class ITYouTubeService {

	private YouTubeService service;

	@Before
	public void setup() throws IOException {
		final YouTubeProperties youTubeProperties = new YouTubeProperties();
		service = new YouTubeService(youTubeProperties.apiKey, new NetHttpTransport(), new JacksonFactory());
	}

	@Test
	public void testSearchReturnsSomeResults() throws IOException {
		final String searchTerms = "camel animal";
		final List<YouTubeVideo> videos = service.search(searchTerms);

		assertNotNull(videos);
		assertTrue(videos.size() > 0);
		assertTrue(videos.size() <= YouTubeService.MAX_NUMBER_OF_VIDEOS_RETURNED);

		final YouTubeVideo video = videos.get(0);
		assertNotNull(video.getChannelId());
		assertNotNull(video.getChannelTitle());
		assertNotNull(video.getDescription());
		assertNotNull(video.getPublishedAt());
		assertNotNull(video.getThumbnailUrl());
		assertNotNull(video.getTitle());
		assertNotNull(video.getVideoId());
	}
}
