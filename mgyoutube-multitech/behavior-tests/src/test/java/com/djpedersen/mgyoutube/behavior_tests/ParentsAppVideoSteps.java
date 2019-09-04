package com.djpedersen.mgyoutube.behavior_tests;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.djpedersen.mgyoutube.behavior_tests.apisdk.model.User;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ParentsAppVideoSteps extends BaseSteps {

	@When("the parent user (.*) submits \"(.*)\" as search words")
	public void whenParentSubmitsVideoSearch(final String parentUsername, final String searchTerms) {
		final User parentUser = getAUserFromScenario(parentUsername);
		Assert.assertNotNull("unable to find user '" + parentUsername + "' in the scenario context", parentUser);

		final WebElement searchTermsField = getWebDriver().findElement(By.id("searchTerms"));
		final WebElement searchTermsButton = getWebDriver().findElement(By.id("submitSearchTermsButton"));

		searchTermsField.sendKeys(searchTerms);
		searchTermsButton.click();
	}

	@Then("the parent user (.*) can see search results for \"(.*)\"")
	public void thenParentCanSeeVideSearchResults(final String parentUsername, final String searchTerms) {
		final WebElement searchResultsPanel = this.getWebDriver().findElement(By.id("searchresultspanel"));

		Assert.fail();
		// final boolean foundText = (new WebDriverWait(this.getWebDriver(), 4))
		// .until(ExpectedConditions.textToBePresentInElement(searchResultsPanel,
		// childUsername));
		//
		// Assert.assertTrue("cannot find child " + childUsername + " in child panel
		// text: " + childrenPanel.getText(),
		// foundText);
	}
}
