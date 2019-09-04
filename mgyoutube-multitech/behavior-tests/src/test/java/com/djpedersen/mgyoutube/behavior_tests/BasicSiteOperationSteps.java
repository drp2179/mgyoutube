package com.djpedersen.mgyoutube.behavior_tests;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class BasicSiteOperationSteps extends BaseSteps {

	@Given("a user none with no password")
	public void givenAnNonAuthenticatedUser() {
		final User user = new User("none", "");
		addAUserToScenario(user);
	}

	@When("the user none accesses the home page")
	public void whenUserAccessesHomePage() {
		getWebDriver().get("http://localhost/");
	}

	@When("the user none accesses the chilrens page")
	public void whenUserAccessesChildrensPage() {
		getWebDriver().get("http://localhost/children/");
	}

	@When("the user (.*) accesses the parents page")
	public void whenUserAccessesParentsPage(final String username) {
		getWebDriver().get("http://localhost/parents/");
	}

	@Then("the user none can see a link to the childrens page")
	public void thenUserCanSeeChildrensPageLink() {
		final WebElement element = getWebDriver().findElement(By.id("children"));
		Assert.assertEquals("childrens page id is not a link", "a", element.getTagName());
	}

	@Then("the user none can see the childrens page")
	public void thenUserCanSeeChildrensPage() {
		final WebElement title = getWebDriver().findElement(By.tagName("h1"));
		Assert.assertEquals("childrens page title is wrong", "MG YouTube - Children", title.getText());
	}

	@Then("the user (.*) can see the parents page")
	public void thenUserCanSeeParentsPage(final String username) {
		final WebElement title = getWebDriver().findElement(By.tagName("h1"));
		Assert.assertEquals("parents page title is wrong", "MG YouTube - Parents", title.getText());
	}

	@Then("the user none can see a link to the parents page")
	public void thenUserCanSeeParentsPageLink() {
		final WebElement element = getWebDriver().findElement(By.id("parents"));
		Assert.assertEquals("parents page id is not a link", "a", element.getTagName());
	}

	@Then("the user (.*) should be able to login")
	public void thenUserShouldNeedToLogin(final String username) {
		final WebElement element = getWebDriver().findElement(By.id("auth"));
		Assert.assertEquals("auth element doesn't indicate a need to login", "Login", element.getText());
	}

	@Then("the user (.*) should be able to logout")
	public void thenUserShouldNotNeedToLogin(final String username) {
		final WebElement element = getWebDriver().findElement(By.id("auth"));
		Assert.assertEquals("auth element doesn't indicate a need to logout", "Logout", element.getText());
	}

}
