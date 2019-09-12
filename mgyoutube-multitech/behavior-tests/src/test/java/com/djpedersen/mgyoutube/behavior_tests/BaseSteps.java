package com.djpedersen.mgyoutube.behavior_tests;

import java.io.IOException;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.ApiSdk;
import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;
import com.djpedersen.mgyoutube.behavior_tests.cucumber.ScenarioContext;

public class BaseSteps {

	public WebDriver getWebDriver() {
		// return (WebDriver) ScenarioContext.get(ScenarioKeys.WEB_DRIVER_KEY);
		return ScenarioContext.get(ScenarioKeys.WEB_DRIVER_KEY, WebDriver.class);
	}

	public void addAUserToScenario(final User user) {
		final String key = ScenarioKeys.USER_KEY_PREFIX + user.username;
		ScenarioContext.put(key, user);
	}

	public User getAUserFromScenario(final String username) {
		final String key = ScenarioKeys.USER_KEY_PREFIX + username;
		// return (User) ScenarioContext.get(key);
		return ScenarioContext.get(key, User.class);
	}

	public ApiSdk getApiSdk() {
		return ScenarioContext.get(ScenarioKeys.API_SDK_KEY, ApiSdk.class);
	}

	public User ensureUserExists(final User user) throws IOException {
		final User existingUser = getApiSdk().getUser(user.username);
		if (existingUser == null) {
			final User createdUser = getApiSdk().createUser(user);

			Assert.assertNotNull("created user " + user.username + " should not be null", createdUser);

			return createdUser;
		}

		return existingUser;
	}

	public User verifyParentUserIsInScenario(final String parentUsername) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);
		Assert.assertTrue("User " + parentUsername + " is not a parent user", parentUser.isParent);
		return parentUser;
	}

	public User verifyChildUserIsInScenario(final String childUsername) {
		final User childUser = getAUserFromScenario(childUsername);
		Assert.assertNotNull("unable to find user '" + childUsername + "' in the scenario context", childUsername);
		Assert.assertFalse("User " + childUsername + " is not a child user", childUser.isParent);
		return childUser;
	}

}
