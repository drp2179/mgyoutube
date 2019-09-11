package com.djpedersen.mgyoutube.behavior_tests.selenium;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ByIdStartsWith extends By implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1278305398817586597L;
	private final String idPrefix;

	public ByIdStartsWith(final String idPrefix) {
		if (idPrefix == null) {
			throw new IllegalArgumentException("Cannot find elements when the idPrefix is null.");
		}

		this.idPrefix = idPrefix;
	}

	@Override
	public List<WebElement> findElements(final SearchContext context) {
		final List<WebElement> allElementsWithIds = context.findElements(By.xpath(".//*[@id]"));
		final List<WebElement> elementsWithPrefixedIds = allElementsWithIds.stream().filter(e -> {
			final String idValue = e.getAttribute("id");
			return (idValue == null) ? false : idValue.startsWith(this.idPrefix);
		}).collect(Collectors.toList());
		return elementsWithPrefixedIds;
	}

}
