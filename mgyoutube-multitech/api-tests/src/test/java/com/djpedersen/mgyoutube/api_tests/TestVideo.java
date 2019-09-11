package com.djpedersen.mgyoutube.api_tests;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.api_tests.apimodel.Video;
import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestVideo {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/videos";
	}

	@Test
	public void missingSearchQueryStringFailsWith400() {
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.get();
		Assert.assertEquals("missing search query param should fail", HttpStatus.SC_BAD_REQUEST,
				response.getStatusCode());
	}

	@Test
	public void emptySearchQueryStringFailsWith400() {
		final RequestSpecification requestSpec = RestAssured.given();
		requestSpec.queryParam("search", "");
		final Response response = requestSpec.get();
		Assert.assertEquals("empty search query param should fail", HttpStatus.SC_BAD_REQUEST,
				response.getStatusCode());
	}

	@Test
	public void basicSearchQueryStringWorks() {
		final RequestSpecification requestSpec = RestAssured.given();
		requestSpec.queryParam("search", "genetic algorithms");
		final Response response = requestSpec.get();
		Assert.assertEquals("'genetic algorithms' should be ok", HttpStatus.SC_OK, response.getStatusCode());

		final String bodyJson = response.getBody().asString();

		// System.out.println("basicSearchQueryStringWorks returned json: \"" + bodyJson
		// + "\"");

		final Video[] videos = new Gson().fromJson(bodyJson, Video[].class);

		Assert.assertTrue("video length is wrong: " + videos.length, videos.length > 0);

		final Video video0 = videos[0];
		Assert.assertNotNull("video0 channelId should not be null", video0.channelId);
		Assert.assertNotNull("video0 channelTitle should not be null", video0.channelTitle);
		Assert.assertNotNull("video0 description should not be null", video0.description);
		Assert.assertNotNull("video0 publishedAt should not be null", video0.publishedAt);
		Assert.assertNotNull("video0 thumbnailHeight should not be null", video0.thumbnailHeight);
		Assert.assertNotNull("video0 thumbnailWidth should not be null", video0.thumbnailWidth);
		Assert.assertNotNull("video0 title should not be null", video0.title);
		Assert.assertNotNull("video0 videoId should not be null", video0.videoId);
	}

}
