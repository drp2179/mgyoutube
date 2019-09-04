package com.djpedersen.mgyoutube.api_tests;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestParentsChildServices {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/parents";
	}

	@Test
	public void noChildrenForParentReturnsEmptyList() {
		final String parentUsername = "parentEmpty";

		Helpers.ensureParentUserExists(parentUsername, "parentPassword");

		final RequestSpecification requestSpec = RestAssured.given();

		final String url = "/" + parentUsername + "/children";

		final Response response = requestSpec.get(url);
		Assert.assertEquals("getting children should return status code 200", HttpStatus.SC_OK,
				response.getStatusCode());

		final String bodyJson = response.getBody().asString();

		final Type listType = new TypeToken<ArrayList<User>>() {
		}.getType();
		final List<User> children = new Gson().fromJson(bodyJson, listType);

		Assert.assertEquals("the number of children is wrong", 0, children.size());
	}

	@Test
	public void addNewChildToParentMainLine() {
		final String parentUsername = "parentB";

		Helpers.ensureParentUserExists(parentUsername, "parentBPassword");

		final List<User> emptyChildrenList = Helpers.getChildrenForParent(parentUsername);
		Assert.assertEquals("there shouldn't be any children for " + parentUsername + " yet", 0,
				emptyChildrenList.size());

		final String childUsername = "childB";
		final String payload = ("{'username':'" + childUsername + "', 'password' : 'childBPassword'}").replace('\'',
				'"');

		final RequestSpecification requestSpec = RestAssured.given();
		requestSpec.body(payload);
		requestSpec.contentType(ContentType.JSON);

		final String url = "/" + parentUsername + "/children/" + childUsername;

		final Response response = requestSpec.put(url);
		Assert.assertEquals("creating child should return status code 200", HttpStatus.SC_OK, response.getStatusCode());

		final String bodyJson = response.getBody().asString();
		final User createdChildUser = new Gson().fromJson(bodyJson, User.class);

		Assert.assertEquals("created child username is wrong", childUsername, createdChildUser.username);
		Assert.assertFalse("created child should not be a parent", createdChildUser.isParent);

		final List<User> childrenList = Helpers.getChildrenForParent(parentUsername);
		Assert.assertEquals("num children is wrong", 1, childrenList.size());
		final User childUser = childrenList.get(0);
		Assert.assertEquals("child username is wrong", childUsername, childUser.username);
		Assert.assertTrue("child userid is wrong: " + childUser.userId, childUser.userId > 0);
		Assert.assertFalse("child should not be a parent", childUser.isParent);
	}

	@Test
	public void replacingChildToParent() {
		final String parentUsername = "parentA";

		Helpers.ensureParentUserExists(parentUsername, "parentAPassword");

		// create
		final String child1Username = "child1";
		final String createPayload = ("{'username':'" + child1Username + "', 'password' : 'child1Password'}")
				.replace('\'', '"');

		final RequestSpecification createRequestSpec = RestAssured.given();
		createRequestSpec.body(createPayload);
		createRequestSpec.contentType(ContentType.JSON);

		final String url = "/" + parentUsername + "/children/" + child1Username;

		final Response createResponse = createRequestSpec.put(url);
		Assert.assertEquals("creating child should return status code 200", HttpStatus.SC_OK,
				createResponse.getStatusCode());

		// update
		final String child1aPassword = "child1aPassword";
		final String updatePayload = ("{'username':'" + child1Username + "', 'password' : '" + child1aPassword + "'}")
				.replace('\'', '"');

		final RequestSpecification updateRequestSpec = RestAssured.given();
		updateRequestSpec.body(updatePayload);
		updateRequestSpec.contentType(ContentType.JSON);

		final Response updateResponse = updateRequestSpec.put(url);
		Assert.assertEquals("udpate child should return status code 200", HttpStatus.SC_OK,
				updateResponse.getStatusCode());

		final String bodyJson = updateResponse.getBody().asString();
		@SuppressWarnings("rawtypes")
		final Map user = new Gson().fromJson(bodyJson, Map.class);

		Assert.assertTrue("missing the username field", user.containsKey("username"));
		Assert.assertEquals("username is wrong", child1Username, user.get("username"));
		Assert.assertTrue("missing the isParent field", user.containsKey("isParent"));
		Assert.assertFalse("isParent is wrong", ((Boolean) user.get("isParent")).booleanValue());
	}

	@Test
	public void addNewChildToNonExistingParentFails() {
		final String parentUsername = "parent-should-not-exist";

		final String childUsername = "child1";
		final String payload = ("{'username':'" + childUsername + "', 'password' : 'child1Password'}").replace('\'',
				'"');

		final RequestSpecification requestSpec = RestAssured.given();
		requestSpec.body(payload);
		requestSpec.contentType(ContentType.JSON);

		final String url = "/" + parentUsername + "/children/" + childUsername;

		final Response response = requestSpec.put(url);
		Assert.assertEquals("creating child for missing parent should return status code 404", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

}
