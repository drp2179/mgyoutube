package com.djpedersen.mgyoutube.api_tests;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestChildrenAuthentication {

	@Before
	public void setup() {
		RestAssured.port = 80;
		RestAssured.basePath = "/api/children";
	}

	@Test
	public void unknownUserFail() {
		final RequestSpecification requestSpec = RestAssured.given();
		String loginPayload = "{'username':'no one', 'password' : 'should fail'}".replace('\'', '"');
		requestSpec.body(loginPayload);
		requestSpec.contentType(ContentType.JSON);
		final Response response = requestSpec.post("/auth");
		Assert.assertEquals("Unknown user auth should fail", HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
	}

}
