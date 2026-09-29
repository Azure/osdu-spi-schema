Feature: To verify version info endpoint content

  @SchemaService
  Scenario: Verify version info endpoint content
    Given I send get request without a token to version info endpoint
    Then service should respond back with version info in response

  @SchemaService
  Scenario: Verify version info endpoint content for request with trailing slash
    Given I send get request to version info with Trailing Slash
    Then service should respond back with trailing slash
