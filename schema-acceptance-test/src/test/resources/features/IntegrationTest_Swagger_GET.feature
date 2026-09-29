Feature: To verify Swagger UI and API docs endpoints

  @SchemaService @Swagger
  Scenario: Verify Swagger UI endpoint is accessible
    Given I send get request without a token to swagger ui endpoint
    Then service should respond back with 200 for swagger ui

  @SchemaService @Swagger
  Scenario: Verify Swagger API docs endpoint is accessible
    Given I send get request without a token to swagger api docs endpoint
    Then service should respond back with 200 for swagger api docs
