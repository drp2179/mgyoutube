package com.djpedersen.mgyoutube.behavior_tests;

import java.io.IOException;

import org.junit.Assert;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class BasicDataSteps extends BaseSteps {

	@Given("user (.*) currently does not exist")
	public void givenAUserDoesNotExist(final String username) throws IOException {

		final User user = getApiSdk().getUser(username);

		if (user != null) {
			getApiSdk().removeUser(username);
		}
	}

	@Then("the user (.*) should be exist")
	public void thenAUserExists(final String username) throws IOException {
		final User existingUser = getApiSdk().getUser(username);

		Assert.assertNotNull("user " + username + " should exist", existingUser);
	}

}
