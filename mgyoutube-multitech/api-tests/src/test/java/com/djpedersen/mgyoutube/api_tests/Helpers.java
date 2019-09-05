package com.djpedersen.mgyoutube.api_tests;

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Assert;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Helpers {

	public static List<User> getChildrenForParent(final String parentUsername) {
		final String savedBasePath = RestAssured.basePath;
		try {
			RestAssured.basePath = "/api/parents";

			final RequestSpecification requestSpec = RestAssured.given();
			final String url = "/" + parentUsername + "/children";

			final Response response = requestSpec.get(url);
			Assert.assertEquals("getting children should return status code 200", HttpStatus.SC_OK,
					response.getStatusCode());

			final String bodyJson = response.getBody().asString();

			System.out.println("getChildrenForParent(" + parentUsername + ") returned json: \"" + bodyJson + "\"");

			// final Type listType = new TypeToken<ArrayList<User>>() {
			// }.getType();
			// final List<User> children = new Gson().fromJson(bodyJson, listType);

			final User[] childrenArray = new Gson().fromJson(bodyJson, User[].class);

			System.out.println("getChildrenForParent(" + parentUsername + ") returned childrenArray "
					+ childrenArray.length + ", childrenArray " + childrenArray);

			final List<User> childrenList = Arrays.asList(childrenArray);

			System.out.println("getChildrenForParent(" + parentUsername + ") returned childrenList "
					+ childrenList.size() + ", childrenList " + childrenList);

			return childrenList;

		} finally {
			RestAssured.basePath = savedBasePath;
		}
	}

	public static void ensureChildUserExists(final String childUsername, final String childPassword) {
		final String savedBasePath = RestAssured.basePath;

		try {
			RestAssured.basePath = "/api/support";

			final String getUserUrl = "/user/" + childUsername;
			final RequestSpecification getRequestSpec = RestAssured.given();

			final Response getResponse = getRequestSpec.get(getUserUrl);
			if (getResponse.getStatusCode() != HttpStatus.SC_OK) {
				final String userPayload = ("{'username':'" + childUsername + "', 'password' : '" + childPassword
						+ "'}").replace('\'', '"');

				final RequestSpecification postRequestSpec = RestAssured.given();
				postRequestSpec.body(userPayload);
				postRequestSpec.contentType(ContentType.JSON);

				final Response postResponse = postRequestSpec.post("/user");
				Assert.assertEquals("should have created child user", HttpStatus.SC_CREATED,
						postResponse.getStatusCode());
			}
		} finally {
			RestAssured.basePath = savedBasePath;
		}
	}

	public static void ensureParentUserExists(final String parentUsername, final String parentPassword) {
		final String savedBasePath = RestAssured.basePath;

		try {
			RestAssured.basePath = "/api/support";

			final String getUserUrl = "/user/" + parentUsername;
			final RequestSpecification getRequestSpec = RestAssured.given();

			final Response getResponse = getRequestSpec.get(getUserUrl);
			if (getResponse.getStatusCode() != HttpStatus.SC_OK) {
				final String userPayload = ("{'username':'" + parentUsername + "', 'password' : '" + parentPassword
						+ "', 'isParent': true}").replace('\'', '"');

				final RequestSpecification postRequestSpec = RestAssured.given();
				postRequestSpec.body(userPayload);
				postRequestSpec.contentType(ContentType.JSON);

				final Response postResponse = postRequestSpec.post("/user");
				Assert.assertEquals("should have created parent user", HttpStatus.SC_CREATED,
						postResponse.getStatusCode());
			}
		} finally {
			RestAssured.basePath = savedBasePath;
		}
	}

}
