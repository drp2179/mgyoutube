package com.djpedersen.mgyoutube.api_tests;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestSupport {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/support";
	}

	@Test
	public void createNewChildUser() {
		final String username = "a-new-child-user";
		final String password = "another-password";
		final String userPayload = ("{'username':'" + username + "', 'password' : '" + password + "'}").replace('\'',
				'"');
		final String userUrl = "/users/" + username;

		final RequestSpecification putRequestSpec = RestAssured.given();
		putRequestSpec.body(userPayload);
		putRequestSpec.contentType(ContentType.JSON);

		final Response putResponse = putRequestSpec.put(userUrl);
		Assert.assertEquals("should have created user", HttpStatus.SC_OK, putResponse.getStatusCode());
		final String body = putResponse.body().asString();

		final User createdUser = (new Gson()).fromJson(body, User.class);

		Assert.assertTrue("created userid should be > 0: " + createdUser.userId, createdUser.userId > 0);
		Assert.assertEquals("username is wrong", username, createdUser.username);
		Assert.assertFalse("should not be a parent", createdUser.isParent);
		Assert.assertNull("password should be null", createdUser.password);
	}

	@Test
	public void changeChildIntoParent() {
		final String username = "another-user";
		final String password = "another-password";
		final String userPayload = ("{'username':'" + username + "', 'password' : '" + password + "'}").replace('\'',
				'"');
		final String userUrl = "/users/" + username;

		final RequestSpecification putRequestSpec = RestAssured.given();
		putRequestSpec.body(userPayload);
		putRequestSpec.contentType(ContentType.JSON);

		final Response putResponse = putRequestSpec.put(userUrl);
		Assert.assertEquals("should have created user", HttpStatus.SC_OK, putResponse.getStatusCode());
		final String body = putResponse.body().asString();
		final User createdUser = (new Gson()).fromJson(body, User.class);

		Assert.assertTrue("created userid should be > 0: " + createdUser.userId, createdUser.userId > 0);
		Assert.assertEquals("username is wrong", username, createdUser.username);
		Assert.assertFalse("should not be a parent", createdUser.isParent);
		Assert.assertNull("password should be null", createdUser.password);

		final String adjustedUserPayload = ("{'username':'" + username + "', 'password' : '" + password
				+ "', 'isParent':true }").replace('\'', '"');

		final RequestSpecification put2RequestSpec = RestAssured.given();
		put2RequestSpec.body(adjustedUserPayload);
		put2RequestSpec.contentType(ContentType.JSON);

		final Response put2Response = put2RequestSpec.put(userUrl);
		Assert.assertEquals("should have created user", HttpStatus.SC_OK, put2Response.getStatusCode());
		final String body2 = put2Response.body().asString();
		final User updatedUser = (new Gson()).fromJson(body2, User.class);

		Assert.assertTrue("created userid should be > 0: " + updatedUser.userId, updatedUser.userId > 0);
		Assert.assertEquals("username is wrong", username, updatedUser.username);
		Assert.assertTrue("should not be a parent", updatedUser.isParent);
		Assert.assertNull("password should be null", updatedUser.password);
	}

	@Test
	public void getNotExistingUserShouldFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.get("/users/doesnotexist");
		Assert.assertEquals("get of not existing user returned wrong code", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

	@Test
	public void getExistingUserShouldWork() {
		Helpers.ensureChildUserExists("childtodelete", "simplepassword");
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.get("/users/childtodelete");
		Assert.assertEquals("get of existing user returned wrong code", HttpStatus.SC_OK, response.getStatusCode());
		final String body = response.body().asString();
		Assert.assertTrue("response body should have the username: " + body, body.contains("childtodelete"));
		// final User user = new Gson().fromJson(body, User.class);
		// Assert.assertEquals("childtodelete", user.);
	}

	@Test
	public void deleteNotExistingUserShouldFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.delete("/users/doesnotexist");
		Assert.assertEquals("delete of not existing user returned wrong code", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

	@Test
	public void deleteExistingUserShouldWork() {
		Helpers.ensureChildUserExists("childtodelete", "simplepassword");
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.delete("/users/childtodelete");
		Assert.assertEquals("delete of existing user returned wrong code", HttpStatus.SC_OK, response.getStatusCode());
	}

}
