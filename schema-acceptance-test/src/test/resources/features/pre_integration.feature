@Startup
Feature: Pre-integration setup

  Scenario: Verify version info endpoint is accessible
    Given I send get request to version info endpoint
    Then service should respond back with version info in response
