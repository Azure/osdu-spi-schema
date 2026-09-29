Feature: To verify liveness check endpoint content

  @SchemaService
  Scenario: Verify liveness check endpoint content
    Given I send get request without a token to liveness check endpoint
    Then service should respond back with 200 in response
