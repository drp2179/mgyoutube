package com.djpedersen.mgyoutube.behavior_tests;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/", plugin = { "pretty" })
public class RunCucumberTest {
}
