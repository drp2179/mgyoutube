package com.djpedersen.mgyoutube.behavior_tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.ApiSdk;
import com.djpedersen.mgyoutube.behavior_tests.cucumber.ScenarioContext;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class ScenarioHooks {

	@Before
	public void beforeScenario() {
		ScenarioContext.clear();

		final String driverLocation = System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY);
		if (driverLocation == null) {
			final String localChromeDriverLocation = "C:\\devtools\\chromedriver_win32-76\\chromedriver.exe";
			System.out.println("setting the Chrome driver location: " + localChromeDriverLocation);
			System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, localChromeDriverLocation);
		} else {
			System.out.println("Chrome driver location: " + driverLocation);
		}

		final ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.setHeadless(true);

		final WebDriver driver = new ChromeDriver(chromeOptions);
		ScenarioContext.put(ScenarioKeys.WEB_DRIVER_KEY, driver);

		final ApiSdk apiSdk = new ApiSdk();
		ScenarioContext.put(ScenarioKeys.API_SDK_KEY, apiSdk);
	}

	@After
	public void afterScenario() {
		final WebDriver driver = ScenarioContext.get(ScenarioKeys.WEB_DRIVER_KEY, WebDriver.class);
		if (driver != null) {
			driver.quit();
		}
	}
}
