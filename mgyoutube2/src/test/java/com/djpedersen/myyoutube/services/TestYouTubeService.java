package com.djpedersen.myyoutube.services;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.entities.YouTubeVideo;
import com.djpedersen.mgyoutube.services.YouTubeProperties;
import com.djpedersen.mgyoutube.services.YouTubeService;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class TestYouTubeService {

	private YouTubeService service;

	@Before
	public void setup() throws IOException {
		final YouTubeProperties youTubeProperties = new YouTubeProperties();
		service = new YouTubeService(youTubeProperties.apiKey, new NetHttpTransport(), new JacksonFactory());
	}

	@Test
	public void testSearchReturnsNonNull() throws IOException {
		final String searchTerms = null;
		final List<YouTubeVideo> videos = service.search(searchTerms);

		assertNotNull(videos);
	}

}
