package org.opengroup.osdu.schema.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:features",
    glue = {
        "classpath:org.opengroup.osdu.schema.stepdefs"
    },
    tags = "@TearDown",
    plugin = {
        "pretty",
        "junit:target/cucumber-reports/schema-service-test-report.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    }
)
public final class TearDownTestsRunner {

}
