package com.djpedersen.myyoutube.webservices;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.djpedersen.mgyoutube.services.SearchTermsService;
import com.djpedersen.mgyoutube.services.YouTubeService;
import com.djpedersen.mgyoutube.services.YouTubeVideo;
import com.djpedersen.mgyoutube.webservices.YouTubeWebService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;

public class TestYouTubeWebService {

	private YouTubeWebService webService;
	private YouTubeService mockYouTubeService;
	private SearchTermsService mockSearchTermsService;

	private final LocalServiceTestHelper gaeLocalServiceHelper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig());

	@Before
	public void setup() {
		mockYouTubeService = Mockito.mock(YouTubeService.class);
		mockSearchTermsService = Mockito.mock(SearchTermsService.class);
		webService = new YouTubeWebService(mockYouTubeService, mockSearchTermsService);

		gaeLocalServiceHelper.setUp();
	}

	@After
	public void teardown() {
		gaeLocalServiceHelper.tearDown();
	}

	@Test
	public void testSearchReturnsNotNull() throws IOException {
		final List<YouTubeVideo> videoList = new ArrayList<YouTubeVideo>();
		Mockito.when(mockYouTubeService.search(Mockito.anyString())).thenReturn(videoList);

		final String uncleanSearchTerms = null;
		final String searchResults = webService.search(uncleanSearchTerms);

		assertNotNull(searchResults);
	}

	@Test
	public void testSearchIsSavedIfUserLoggedIn() throws IOException {
		final List<YouTubeVideo> videoList = new ArrayList<YouTubeVideo>();
		final String searchTerms = "testing 123";

		Mockito.when(mockYouTubeService.search(searchTerms)).thenReturn(videoList);
		gaeLocalServiceHelper.setEnvIsLoggedIn(true);
		gaeLocalServiceHelper.setEnvEmail("drp@rochester.rr.com");
		gaeLocalServiceHelper.setEnvAuthDomain("rochester.rr.com");

		final String searchResults = webService.search(searchTerms);

		assertNotNull(searchResults);
		Mockito.verify(mockSearchTermsService).recordUserSearchAudit(Mockito.anyString(), Mockito.eq(searchTerms),
				Mockito.any(DateTime.class));
	}

	@Test
	public void testSearchIsNotSavedIfUserNotLoggedIn() throws IOException {
		final List<YouTubeVideo> videoList = new ArrayList<YouTubeVideo>();
		final String searchTerms = "testing 123";

		Mockito.when(mockYouTubeService.search(searchTerms)).thenReturn(videoList);
		gaeLocalServiceHelper.setEnvIsLoggedIn(false);

		final String searchResults = webService.search(searchTerms);

		assertNotNull(searchResults);
		Mockito.verify(mockSearchTermsService, Mockito.never()).recordUserSearchAudit(Mockito.anyString(),
				Mockito.eq(searchTerms), Mockito.any(DateTime.class));
	}
}
