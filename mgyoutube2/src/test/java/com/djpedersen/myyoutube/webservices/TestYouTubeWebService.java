package com.djpedersen.myyoutube.webservices;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.djpedersen.mgyoutube.services.YouTubeService;
import com.djpedersen.mgyoutube.services.YouTubeVideo;
import com.djpedersen.mgyoutube.webservices.YouTubeWebService;

public class TestYouTubeWebService {

	private YouTubeWebService webService;
	private YouTubeService mockYouTubeService;

	@Before
	public void setup() {
		mockYouTubeService = Mockito.mock(YouTubeService.class);
		webService = new YouTubeWebService(mockYouTubeService);
	}

	@Test
	public void testSearchReturnsNotNull() throws IOException {
		final List<YouTubeVideo> videoList = new ArrayList<YouTubeVideo>();
		Mockito.when(mockYouTubeService.search(Mockito.anyString())).thenReturn(videoList);

		final String uncleanSearchTerms = null;
		final String searchResults = webService.search(uncleanSearchTerms);

		assertNotNull(searchResults);
	}
}
