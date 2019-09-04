package com.djpedersen.mgyoutube.api_tests;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestSupport {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/support";
	}

	@Test
	public void getNotExistingUserShouldFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.get("/user/doesnotexist");
		Assert.assertEquals("get of not existing user returned wrong code", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

	@Test
	public void getExistingUserShouldWork() {
		Helpers.ensureChildUserExists("childtodelete", "simplepassword");
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.get("/user/childtodelete");
		Assert.assertEquals("get of existing user returned wrong code", HttpStatus.SC_OK, response.getStatusCode());
		final String body = response.body().asString();
		Assert.assertTrue("response body should have the username: " + body, body.contains("childtodelete"));
		// final User user = new Gson().fromJson(body, User.class);
		// Assert.assertEquals("childtodelete", user.);
	}

	@Test
	public void deleteNotExistingUserShouldFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.delete("/user/doesnotexist");
		Assert.assertEquals("delete of not existing user returned wrong code", HttpStatus.SC_NOT_FOUND,
				response.getStatusCode());
	}

	@Test
	public void deleteExistingUserShouldWork() {
		Helpers.ensureChildUserExists("childtodelete", "simplepassword");
		final RequestSpecification requestSpec = RestAssured.given();
		final Response response = requestSpec.delete("/user/childtodelete");
		Assert.assertEquals("delete of existing user returned wrong code", HttpStatus.SC_OK, response.getStatusCode());
	}

}
