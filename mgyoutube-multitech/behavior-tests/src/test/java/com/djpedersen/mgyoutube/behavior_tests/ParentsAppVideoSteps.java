package com.djpedersen.mgyoutube.behavior_tests;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ParentsAppVideoSteps extends BaseSteps {

	@When("the parent user (.*) submits \"(.*)\" as search words")
	public void whenParentSubmitsVideoSearch(final String parentUsername, final String searchTerms) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final WebElement searchTermsField = getWebDriver().findElement(By.id("searchterms"));
		final WebElement searchTermsButton = getWebDriver().findElement(By.id("submitSearchTermsButton"));

		searchTermsField.sendKeys(searchTerms);
		searchTermsButton.click();
	}

	@Then("the parent user (.*) can see search results for \"(.*)\"")
	public void thenParentCanSeeVideSearchResults(final String parentUsername, final String searchTerms) {
		final WebElement searchResultsPanel = this.getWebDriver().findElement(By.id("searchresultspanel"));
		Assert.assertNotNull("no search result panel", searchResultsPanel);

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 5 /* timeout in seconds */);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-result-0")));

		final WebElement searchResult0 = this.getWebDriver().findElement(By.id("search-result-0"));
		Assert.assertNotNull("no search result #0", searchResult0);
		final WebElement searchResult1 = this.getWebDriver().findElement(By.id("search-result-1"));
		Assert.assertNotNull("no search result #1", searchResult1);
		final WebElement searchResult2 = this.getWebDriver().findElement(By.id("search-result-2"));
		Assert.assertNotNull("no search result #2", searchResult2);
	}
}
