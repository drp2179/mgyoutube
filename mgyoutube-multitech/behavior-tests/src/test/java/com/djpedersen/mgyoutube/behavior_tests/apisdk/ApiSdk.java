package com.djpedersen.mgyoutube.behavior_tests.apisdk;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;
import com.google.gson.Gson;

public class ApiSdk {

	private static final Gson GSON = new Gson();

	public User getUser(final String username) throws IOException {
		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			return this.getUser(username, httpclient);
		}
	}

	public void removeUser(final String username) throws IOException {
		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			this.removeUser(username, httpclient);
		}
	}

	protected User getUser(final String username, final CloseableHttpClient httpclient) throws IOException {

		final String uri = "http://localhost/api/support/users/" + username;
		final HttpGet httpGet = new HttpGet(uri);

		try (final CloseableHttpResponse response = httpclient.execute(httpGet);) {

			System.out.println("GET " + uri + " response:" + response.getStatusLine());

			if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
				final HttpEntity userEntity = response.getEntity();
				final String userJson = EntityUtils.toString(userEntity);
				System.out.println("getUser, userJson: " + userJson);
				return GSON.fromJson(userJson, User.class);
			}

			return null;
		}
	}

	protected void removeUser(final String username, final CloseableHttpClient httpclient) throws IOException {

		final String uri = "http://localhost/api/support/users/" + username;
		final HttpDelete httpDelete = new HttpDelete(uri);

		try (final CloseableHttpResponse response = httpclient.execute(httpDelete);) {

			System.out.println("DELETE " + uri + " response:" + response.getStatusLine());
		}
	}

	public User createUser(final User user) throws IOException {

		final String uri = "http://localhost/api/support/users/" + user.username;
		final String userJson = GSON.toJson(user);

		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final HttpPut httpPut = new HttpPut(uri);
			final HttpEntity userEntity = EntityBuilder.create().setText(userJson)
					.setContentType(ContentType.APPLICATION_JSON).build();
			httpPut.setEntity(userEntity);

			try (final CloseableHttpResponse response = httpclient.execute(httpPut);) {

				System.out.println("PUT " + uri + " response:" + response.getStatusLine());

				if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
					return this.getUser(user.username, httpclient);
				}

				return null;
			}
		}
	}

	public List<User> getChildrenForParent(final String parentUsername) throws ClientProtocolException, IOException {
		final String uri = "http://localhost/api/parents/" + parentUsername + "/children";

		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final HttpGet httpGet = new HttpGet(uri);

			try (final CloseableHttpResponse response = httpclient.execute(httpGet);) {

				System.out.println("GET " + uri + " response:" + response.getStatusLine());

				if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
					final HttpEntity bodyEntity = response.getEntity();
					final String bodyJson = EntityUtils.toString(bodyEntity);
					final User[] childrenArray = new Gson().fromJson(bodyJson, User[].class);

					final List<User> childrenList = Arrays.asList(childrenArray);
					return childrenList;
				}

				return null;
			}
		}
	}

	public User addChildToParent(final User parentUser, final User childUser) throws IOException {
		final String uri = "http://localhost/api/parents/" + parentUser.username + "/children/" + childUser.username;

		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final HttpPut httpPut = new HttpPut(uri);

			final String userJson = GSON.toJson(childUser);
			final HttpEntity userEntity = EntityBuilder.create().setText(userJson)
					.setContentType(ContentType.APPLICATION_JSON).build();
			httpPut.setEntity(userEntity);

			try (final CloseableHttpResponse response = httpclient.execute(httpPut);) {

				System.out.println("PUT " + uri + " response:" + response.getStatusLine());

				if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
					final HttpEntity bodyEntity = response.getEntity();
					final String bodyJson = EntityUtils.toString(bodyEntity);
					final User updatedChild = new Gson().fromJson(bodyJson, User.class);

					return updatedChild;
				}

				return null;
			}
		}
	}

	public boolean saveSearchForParent(final User parentUser, final String searchPhrase) throws IOException {

		final String encodedSearchPhrase = URLEncoder.encode(searchPhrase, StandardCharsets.UTF_8.toString());
		final String uri = "http://localhost/api/parents/" + parentUser.username + "/searches/" + encodedSearchPhrase;

		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final HttpPut httpPut = new HttpPut(uri);

			try (final CloseableHttpResponse response = httpclient.execute(httpPut);) {

				System.out.println("PUT " + uri + " response:" + response.getStatusLine());

				if (response.getStatusLine().getStatusCode() == HttpServletResponse.SC_CREATED) {
					return true;
				}

				return false;
			}
		}
	}

}
