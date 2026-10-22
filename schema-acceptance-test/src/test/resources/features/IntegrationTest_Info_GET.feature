Feature: To verify version info endpoint content

  @SchemaService
  Scenario: Verify version info endpoint content
    Given I send get request to version info endpoint
    Then service should respond back with version info in response

  @SchemaService
  Scenario: Verify version info endpoint reports the expected revision
    Given I send get request to version info endpoint
    Then service should respond back with expected revision
