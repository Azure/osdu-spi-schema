Feature: To verify version info endpoint content

  @SchemaServiceAzure
  Scenario: Verify version info endpoint content
    Given I send get request to version info endpoint
    Then service should respond back with version info in response
