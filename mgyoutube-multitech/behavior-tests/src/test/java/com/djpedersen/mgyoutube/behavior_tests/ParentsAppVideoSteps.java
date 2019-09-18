package com.djpedersen.mgyoutube.behavior_tests;

import java.io.IOException;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;
import com.djpedersen.mgyoutube.behavior_tests.cucumber.ScenarioContext;
import com.djpedersen.mgyoutube.behavior_tests.selenium.ByIdStartsWith;
import com.djpedersen.mgyoutube.behavior_tests.selenium.ExtendedExpectedConditions;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ParentsAppVideoSteps extends BaseSteps {

	@Given("the parent user (.*) has searched for \"(.*)\"")
	public void givenParentSearched(final String parentUsername, final String searchTerms) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final WebElement searchTermsField = getWebDriver().findElement(By.id("searchterms"));
		final WebElement searchTermsButton = getWebDriver().findElement(By.id("submitSearchTermsButton"));

		searchTermsField.sendKeys(searchTerms);
		searchTermsButton.click();

		final WebElement searchResultsPanel = this.getWebDriver().findElement(By.id("searchresultspanel"));
		Assert.assertNotNull("no search result panel", searchResultsPanel);

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 4 /* timeout in seconds */);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-result-0")));

		final WebElement searchResult0 = this.getWebDriver().findElement(By.id("search-result-0"));
		Assert.assertNotNull("no search result #0", searchResult0);
	}

	@Given("the parent user (.*) has previously saved a search for \"(.*)\"")
	public void givenParentUserHasPreviouslySavedASearch(final String parentUsername, final String searchPhrase)
			throws IOException {
		final User parentUser = this.verifyParentUserIsInScenario(parentUsername);

		this.getApiSdk().saveSearchForParent(parentUser, searchPhrase);
	}

	@When("the parent user (.*) submits \"(.*)\" as search words")
	public void whenParentSubmitsVideoSearch(final String parentUsername, final String searchTerms) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final WebElement searchTermsField = getWebDriver().findElement(By.id("searchterms"));
		final WebElement searchTermsButton = getWebDriver().findElement(By.id("submitSearchTermsButton"));

		searchTermsField.sendKeys(searchTerms);
		searchTermsButton.click();
	}

	@When("the parent user (.*) clicks on search result (.*)")
	public void whenParentClicksOnSpecificSearchResult(final String parentUsername, final int searchResultIndex) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final String searchResultIdPrefix = "search-result-" + searchResultIndex;
		final String searchResultId = searchResultIdPrefix + "-watch";
		final String searchResultVideoIdPrefix = searchResultIdPrefix + "-video-";

		final WebElement searchResultVideoDiv = this.getWebDriver()
				.findElement(new ByIdStartsWith(searchResultVideoIdPrefix));

		final String searchResultVideoDivIdValue = searchResultVideoDiv.getAttribute("id");
		Assert.assertNotNull("searchResultVideoDivIdValue should not be empty", searchResultVideoDivIdValue);
		final String videoId = searchResultVideoDivIdValue.substring(searchResultVideoIdPrefix.length());
		System.out.println("videoId: " + videoId);

		final WebElement searchResultWatchLink = this.getWebDriver().findElement(By.id(searchResultId));

		ScenarioContext.put(ScenarioKeys.CLICKED_VIDEO_ID, videoId);
		searchResultWatchLink.click();
	}

	@When("the parent user (.*) saves the \"(.*)\" search")
	public void theParentUserDadASavesTheSearch(final String parentUsername, final String searchTerms) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement saveSearchButton = getWebDriver().findElement(By.id("submitSaveSearchButton"));
		saveSearchButton.click();
	}

	@When("the parent user (.*) deletes saved search \"(.*)\"")
	public void whenParentUserDeletesSavedSearch(final String parentUsername, final String searchPhrase) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement deleteSavedSearchButton = getWebDriver().findElement(By.id("deleteSavedSearchButton"));
		deleteSavedSearchButton.click();
	}

	@When("the parent user (.*) clicks on the \"(.*)\" saved search")
	public void whenParentUserClicksOnSavedSearch(final String parentUsername, final String searchPhrase) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement savedSearchLink = getWebDriver().findElement(By.linkText(searchPhrase));
		savedSearchLink.click();
	}

	@Then("the parent user (.*) can see the youtube player with video (.*)")
	public void thenParentCanSeeYouTubePlayerWithVideo(final String parentUsername, final int searchResultIndex) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final String videoId = ScenarioContext.get(ScenarioKeys.CLICKED_VIDEO_ID, String.class);

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 8 /* timeout in seconds */);
		wait.until(ExpectedConditions.attributeContains(By.id("youtubeplayer"), "src", videoId));
	}

	@Then("the parent user (.*) can see search results for \"(.*)\"")
	public void thenParentCanSeeVideSearchResults(final String parentUsername, final String searchTerms) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement searchResultsPanel = this.getWebDriver().findElement(By.id("searchresultspanel"));
		Assert.assertNotNull("no search result panel", searchResultsPanel);

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 4 /* timeout in seconds */);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-result-0")));

		final WebElement searchResult0 = this.getWebDriver().findElement(By.id("search-result-0"));
		Assert.assertNotNull("no search result #0", searchResult0);
		final WebElement searchResult1 = this.getWebDriver().findElement(By.id("search-result-1"));
		Assert.assertNotNull("no search result #1", searchResult1);
		final WebElement searchResult2 = this.getWebDriver().findElement(By.id("search-result-2"));
		Assert.assertNotNull("no search result #2", searchResult2);
	}

	@Then("the parent user (.*) can see \"(.*)\" in the saved search list")
	public void thenParentUserCanSeeSearchTheSavedSearchList(final String parentUsername, final String searchTerms) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 4 /* timeout in seconds */);
		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("savedsearchespanel"), searchTerms));
	}

	@Then("the parent user (.*) cannot see \"(.*)\" in the saved search list")
	public void thenParentUserCannotSeeSearchTheSavedSearchList(final String parentUsername, final String searchTerms) {
		this.verifyParentUserIsInScenario(parentUsername);

		final WebElement savedSearchesPanel = this.getWebDriver().findElement(By.id("savedsearchespanel"));

		final WebDriverWait wait = new WebDriverWait(this.getWebDriver(), 4 /* timeout in seconds */);
		wait.until(ExtendedExpectedConditions.textToBeMissingInElement(savedSearchesPanel, searchTerms));
	}

}
