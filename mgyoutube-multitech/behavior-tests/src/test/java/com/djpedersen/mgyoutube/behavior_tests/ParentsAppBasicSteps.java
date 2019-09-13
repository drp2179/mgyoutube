package com.djpedersen.mgyoutube.behavior_tests;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;
import com.djpedersen.mgyoutube.behavior_tests.selenium.ExtendedExpectedConditions;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ParentsAppBasicSteps extends BaseSteps {

	@Given("a parent user (.*) with password (.*)$")
	public void givenAParentUserAndPassword(final String username, final String password) throws IOException {
		final User user = User.createParentUser(username, password);
		final User parentUser = ensureUserExists(user);
		parentUser.password = password;
		addAUserToScenario(user);
	}

	@Given("a child user (.*) with password (.*) and (.*) as a parent")
	public void givenChildUserToParent(final String childUsername, final String childPassword,
			final String parentUsername) throws IOException {
		final User parentUser = this.getAUserFromScenario(parentUsername);
		Assert.assertNotNull("parent user " + parentUsername + " does not seem to exist", parentUser);

		final List<User> existingChildren = getApiSdk().getChildrenForParent(parentUsername);
		final List<User> matchingChildren = existingChildren.stream().filter(u -> u.username.equals(childUsername))
				.collect(Collectors.toList());

		if (matchingChildren.size() <= 0) {
			final User initialChildUser = new User(childUsername, childPassword);
			final User middleChildUser = ensureUserExists(initialChildUser);

			final User finalChildUser = this.getApiSdk().addChildToParent(parentUser, middleChildUser);
			finalChildUser.password = childPassword;
			addAUserToScenario(finalChildUser);
		} else if (this.getAUserFromScenario(childUsername) == null) {
			final User childUser = matchingChildren.get(0);
			childUser.password = childPassword;
			this.addAUserToScenario(childUser);
		}
	}

	// @Given("a user (.*) with password (.*)")
	// public void givenAUserAndPassword(final String username, final String
	// password) throws IOException {
	//
	// final User user = new User(username, password);
	// final User finalUser = ensureUserExists(user);
	// finalUser.password = password;
	//
	// addAUserToScenario(finalUser);
	// }

	@Given("the parent user (.*) has logged into the parents page")
	public void givenUserLogsIntoParentsApp(final String username) {
		final User user = getAUserFromScenario(username);
		Assert.assertNotNull("unable to find user '" + username + "' in the scenario context", user);

		getWebDriver().get("http://localhost/parents/");

		final WebElement usernameField = getWebDriver().findElement(By.name("username"));
		final WebElement passwordField = getWebDriver().findElement(By.name("password"));
		final WebElement loginButton = getWebDriver().findElement(By.id("loginButton"));

		usernameField.sendKeys(user.username);
		passwordField.sendKeys(user.password);
		loginButton.click();

		if (!wasLoginSuccess(getWebDriver())) {
			System.out.println("parent app login for " + username + " failed");
			getWebDriver().switchTo().alert().dismiss();
			// should probably save this info in the scenario context
		}
	}

	// @When("the user (.*) logs into the parents app")
	// public void whenUserLogsIntoParentsApp(final String username) {
	// final User user = getAUserFromScenario(username);
	// Assert.assertNotNull("unable to find user '" + username + "' in the scenario
	// context", user);
	//
	// final WebElement usernameField =
	// getWebDriver().findElement(By.name("username"));
	// final WebElement passwordField =
	// getWebDriver().findElement(By.name("password"));
	// final WebElement loginButton =
	// getWebDriver().findElement(By.id("loginButton"));
	//
	// usernameField.sendKeys(user.username);
	// passwordField.sendKeys(user.password);
	// loginButton.click();
	//
	// if (!wasLoginSuccess(getWebDriver())) {
	// System.out.println("parent app login for " + username + " failed");
	// getWebDriver().switchTo().alert().dismiss();
	// // should probably save this info in the scenario context
	// }
	// }
	//
	@When("the parent user (.*) logs into the parents app")
	public void whenParentUserLogsIntoParentsApp(final String parentUsername) {
		final User parentUser = this.verifyParentUserIsInScenario(parentUsername);

		final WebElement usernameField = getWebDriver().findElement(By.name("username"));
		final WebElement passwordField = getWebDriver().findElement(By.name("password"));
		final WebElement loginButton = getWebDriver().findElement(By.id("loginButton"));

		usernameField.sendKeys(parentUser.username);
		passwordField.sendKeys(parentUser.password);
		loginButton.click();

		if (!wasLoginSuccess(getWebDriver())) {
			System.out.println("parent app login for " + parentUsername + " failed");
			getWebDriver().switchTo().alert().dismiss();
			// should probably save this info in the scenario context
		}
	}

	@When("the child user (.*) logs into the parents app")
	public void whenChildUserLogsIntoParentsApp(final String childUsername) {
		final User childUser = this.verifyChildUserIsInScenario(childUsername);

		final WebElement usernameField = getWebDriver().findElement(By.name("username"));
		final WebElement passwordField = getWebDriver().findElement(By.name("password"));
		final WebElement loginButton = getWebDriver().findElement(By.id("loginButton"));

		usernameField.sendKeys(childUser.username);
		passwordField.sendKeys(childUser.password);
		loginButton.click();

		if (!wasLoginSuccess(getWebDriver())) {
			System.out.println("parent app login for " + childUsername + " failed");
			getWebDriver().switchTo().alert().dismiss();
			// should probably save this info in the scenario context
		}
	}

	@When("the parent user (.*) adds child user (.*) with password (.*)")
	public void whenParentUserAddsChild(final String parentUsername, final String childUsername,
			final String childPassword) throws IOException {

		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement newChildUserField = this.getWebDriver().findElement(By.id("newchildnamefield"));
		newChildUserField.sendKeys(childUsername);

		final WebElement newChildPasswordField = this.getWebDriver().findElement(By.id("newchildpasswordfield"));
		newChildPasswordField.sendKeys(childPassword);

		final WebElement addChildButton = this.getWebDriver().findElement(By.id("addNewChildButton"));
		addChildButton.click();
	}

	@When("the parent user (.*) removes the child user (.*)")
	public void whenParentRemovesChild(final String parentUsername, final String childUsername) {
		// final User parentUser = this.getAUserFromScenario(parentUsername);
		// Assert.assertNotNull("parent user " + parentUsername + " does not exist",
		// parentUser);
		//
		// final WebElement newChildUserField =
		// this.getWebDriver().findElement(By.id("newchildnamefield"));
		// newChildUserField.sendKeys(childUsername);
		//
		// final WebElement newChildPasswordField =
		// this.getWebDriver().findElement(By.id("newchildpasswordfield"));
		// newChildPasswordField.sendKeys(childPassword);
		//
		// final WebElement addChildButton =
		// this.getWebDriver().findElement(By.id("addNewChildButton"));
		// addChildButton.click();
		Assert.fail();
	}

	@Then("the parent user (.*) can see (.*) listed in their children section")
	public void thenUserCanSeeChildInChildSection(final String parentUsername, final String childUsername) {
		final WebElement childrenPanel = (new WebDriverWait(this.getWebDriver(), 4))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("childrenpanel")));

		// final WebElement childrenPanel =
		// this.getWebDriver().findElement(By.id("childrenpanel"));

		final boolean foundText = (new WebDriverWait(this.getWebDriver(), 4))
				.until(ExpectedConditions.textToBePresentInElement(childrenPanel, childUsername));

		Assert.assertTrue("cannot find child " + childUsername + " in child panel text: " + childrenPanel.getText(),
				foundText);
	}

	@Then("the parent user (.*) cannot see (.*) listed in their children section")
	public void thenUserCannotSeeChildInChildSection(final String parentUsername, final String childUsername) {
		final WebElement childrenPanel = this.getWebDriver().findElement(By.id("childrenpanel"));

		final boolean foundText = (new WebDriverWait(this.getWebDriver(), 4))
				.until(ExtendedExpectedConditions.textToBeMissingInElement(childrenPanel, childUsername));

		Assert.assertFalse("Can still find child " + childUsername + " in child panel text: " + childrenPanel.getText(),
				foundText);
	}

	private boolean wasLoginSuccess(final WebDriver driver) {
		final WebDriverWait wait = new WebDriverWait(driver, 2 /* timeout in seconds */);
		try {
			final Alert alertPresent = wait.until(ExpectedConditions.alertIsPresent());
			// System.out.println("wasLoginSuccess: alertPresent: " + alertPresent);
			return alertPresent == null;
		} catch (TimeoutException toe) {
			// System.out.println("wasLoginSuccess: timedout true");
			return true;
		}
	}

}
