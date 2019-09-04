package com.djpedersen.mgyoutube.api_tests;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestParentsAuthentication {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/parents";
	}

	@Test
	public void authWithUnknownUserFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		final String loginPayload = "{'username':'no one', 'password' : 'should fail'}".replace('\'', '"');
		requestSpec.body(loginPayload);
		requestSpec.contentType(ContentType.JSON);
		final Response response = requestSpec.post("/auth");
		Assert.assertEquals("Unknown user should return status code 401", HttpStatus.SC_UNAUTHORIZED,
				response.getStatusCode());
	}

	@Test
	public void childCredsFail() {
		Helpers.ensureChildUserExists("child", "childpass");

		final RequestSpecification requestSpec = RestAssured.given();
		final String loginPayload = "{'username':'child', 'password' : 'childpass'}".replace('\'', '"');
		requestSpec.body(loginPayload);
		requestSpec.contentType(ContentType.JSON);
		final Response response = requestSpec.post("/auth");
		Assert.assertEquals("Child auth of parents should fail", HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	public void parentCredsSucceed() {
		Helpers.ensureParentUserExists("dad", "dadpassword");

		final RequestSpecification requestSpec = RestAssured.given();
		final String loginPayload = "{'username':'dad', 'password' : 'dadpassword'}".replace('\'', '"');
		requestSpec.body(loginPayload);
		requestSpec.contentType(ContentType.JSON);
		final Response response = requestSpec.post("/auth");
		Assert.assertEquals("Parent auth of parents should succeed", HttpStatus.SC_OK, response.getStatusCode());
	}

}
