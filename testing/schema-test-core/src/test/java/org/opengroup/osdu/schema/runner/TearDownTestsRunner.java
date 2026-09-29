package org.opengroup.osdu.schema.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features", glue = { "classpath:org.opengroup.osdu.schema.stepdefs" }, tags = "@TearDown", plugin = { "pretty", "junit:target/cucumber-reports/schema-service-test-report.xml" })
public class TearDownTestsRunner {

}
