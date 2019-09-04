package com.djpedersen.mgyoutube.behavior_tests;

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

}
