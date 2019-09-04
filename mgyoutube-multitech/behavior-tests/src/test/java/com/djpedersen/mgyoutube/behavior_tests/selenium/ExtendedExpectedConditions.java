package com.djpedersen.mgyoutube.behavior_tests.selenium;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ExtendedExpectedConditions {

	public static ExpectedCondition<Boolean> textToBeMissingInElement(final WebElement element, final String text) {

		return new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					String elementText = element.getText();
					return !elementText.contains(text);
				} catch (StaleElementReferenceException e) {
					return null;
				}
			}

			@Override
			public String toString() {
				return String.format("text ('%s') to be missing in element %s", text, element);
			}
		};
	}

}
