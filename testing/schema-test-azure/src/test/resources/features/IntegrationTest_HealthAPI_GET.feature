Feature: To verify liveness check endpoint content

  @SchemaServiceAzure
  Scenario: Verify liveness check endpoint content
    Given I send get request to liveness check endpoint
    Then service should respond back with 200 in response
