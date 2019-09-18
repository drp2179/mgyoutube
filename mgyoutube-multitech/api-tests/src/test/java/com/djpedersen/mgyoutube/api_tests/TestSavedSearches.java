package com.djpedersen.mgyoutube.api_tests;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.djpedersen.mgyoutube.api_tests.apimodel.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestSavedSearches {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/parents";
	}

	@Test
	public void getEmptySearchTermsWorks() {
		final String parentUsername = "emptySearchParent";
		Helpers.ensureParentUserExists(parentUsername, "parentPassword");

		final RequestSpecification requestSpec = RestAssured.given();

		final String url = "/" + parentUsername + "/searches";

		final Response response = requestSpec.get(url);
		Assert.assertEquals("getting searches should return status code 200", HttpStatus.SC_OK,
				response.getStatusCode());

		final String bodyJson = response.getBody().asString();

		final Type listType = new TypeToken<ArrayList<String>>() {
		}.getType();
		final List<User> searches = new Gson().fromJson(bodyJson, listType);

		Assert.assertEquals("the number of searches is wrong", 0, searches.size());
	}

	@Test
	public void getSearchTermsForUnknownParentFails() {
		final String parentUsername = "unknownparent";

		final RequestSpecification requestSpec = RestAssured.given();

		final String url = "/" + parentUsername + "/searches";

		final Response response = requestSpec.get(url);
		Assert.assertEquals("getting searches should return status code 404", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

	@Test
	public void canSaveAMoreComplexSearchTerm() throws UnsupportedEncodingException {
		final String parentUsername = "saveComplexSearchParent";
		final String searchPhrase = "learn trombone";
		final String encodedSearchPhrase = URLEncoder.encode(searchPhrase, StandardCharsets.UTF_8.toString());

		Helpers.ensureParentUserExists(parentUsername, "parentPassword");

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + encodedSearchPhrase;

		final Response responsePut = requestSpecPut.put(putUrl);
		Assert.assertEquals("saving a search should return status code 201", HttpStatus.SC_CREATED,
				responsePut.getStatusCode());

		final RequestSpecification requestSpecGet = RestAssured.given();
		final String getUrl = "/" + parentUsername + "/searches";
		final Response responseGet = requestSpecGet.get(getUrl);
		Assert.assertEquals("getting the saved searches should return status code 201", HttpStatus.SC_CREATED,
				responsePut.getStatusCode());

		final String bodyJson = responseGet.getBody().asString();

		final Type listType = new TypeToken<ArrayList<String>>() {
		}.getType();
		final List<String> searches = new Gson().fromJson(bodyJson, listType);

		Assert.assertEquals("the number of searches is wrong", 1, searches.size());
		Assert.assertEquals("the returned search phrase is wrong", searchPhrase, searches.get(0));
	}

	@Test
	public void canSaveASimpleSearchTerm() {
		final String parentUsername = "saveSimpleSearchParent";
		final String searchPhrase = "Hawaii";
		Helpers.ensureParentUserExists(parentUsername, "parentPassword");

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + searchPhrase;

		final Response responsePut = requestSpecPut.put(putUrl);
		Assert.assertEquals("saving a search should return status code 201", HttpStatus.SC_CREATED,
				responsePut.getStatusCode());

		final RequestSpecification requestSpecGet = RestAssured.given();
		final String getUrl = "/" + parentUsername + "/searches";
		final Response responseGet = requestSpecGet.get(getUrl);
		Assert.assertEquals("getting the saved searches should return status code 201", HttpStatus.SC_CREATED,
				responsePut.getStatusCode());

		final String bodyJson = responseGet.getBody().asString();

		final Type listType = new TypeToken<ArrayList<String>>() {
		}.getType();
		final List<User> searches = new Gson().fromJson(bodyJson, listType);

		Assert.assertEquals("the number of searches is wrong", 1, searches.size());
		Assert.assertEquals("the returned search phrase is wrong", searchPhrase, searches.get(0));
	}

	@Test
	public void saveSearchTermForBadParentFails() {
		final String parentUsername = "unknownparent";
		final String searchPhrase = "learn trombone";

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + searchPhrase;

		final Response responsePut = requestSpecPut.put(putUrl);
		Assert.assertEquals("saving a search for unknown parent should return status code 404", HttpStatus.SC_NOT_FOUND,
				responsePut.getStatusCode());
	}

	@Test
	public void deletingNonExistingSearchReturns204() {
		final String parentUsername = "knownparent-deletenonexistingsearch";
		final String searchPhrase = "non existing search phrase";

		Helpers.ensureParentUserExists(parentUsername, "parentuserpassword");

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + searchPhrase;

		final Response responsePut = requestSpecPut.delete(putUrl);
		Assert.assertEquals("deleting a search for known parent but non exsting search should return status code 204",
				HttpStatus.SC_NO_CONTENT, responsePut.getStatusCode());
	}

	@Test
	public void deletingSearchFromNonParentSearchReturns404() {
		final String parentUsername = "unknownparent-deletesearch";
		final String searchPhrase = "learn trombone";

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + searchPhrase;

		final Response responsePut = requestSpecPut.delete(putUrl);
		Assert.assertEquals("deleting a search for unknown parent should return status code 404",
				HttpStatus.SC_NOT_FOUND, responsePut.getStatusCode());
	}

	@Test
	public void deletingExistingSearchReturns200() {
		final String parentUsername = "knownparent-deletesearch";
		final String searchPhrase = "known search phrase";

		Helpers.ensureParentUserExists(parentUsername, "parentuserpassword");
		Helpers.ensureSearchExistsForParent(parentUsername, searchPhrase);
		final List<String> initialSearches = Helpers.getSearchesForParent(parentUsername);
		Assert.assertEquals("initial number of searches is wrong", 1, initialSearches.size());

		final RequestSpecification requestSpecPut = RestAssured.given();

		final String putUrl = "/" + parentUsername + "/searches/" + searchPhrase;

		final Response responsePut = requestSpecPut.delete(putUrl);
		Assert.assertEquals("deleting a search for known parent and known search should return status code 204",
				HttpStatus.SC_NO_CONTENT, responsePut.getStatusCode());

		final List<String> finalSearches = Helpers.getSearchesForParent(parentUsername);
		Assert.assertEquals("final number of searches is wrong", 0, finalSearches.size());
	}
}
