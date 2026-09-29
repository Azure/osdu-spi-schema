Feature: To verify functionality of PUT schema Service

  ### Commons test steps are accomplished here
  Background: Common steps for all tests are executed
    Given I generate user token and set request headers for "TENANT1"
    Given I get latest schema with authority, source, entityType as "SchemaSanityTest", "testSource", "testEntity" respectively
    Given I hit schema service POST API with "/input_payloads/postInPrivateScope_positiveScenario.json" and data-partition-id as "TENANT1" only if status is not development

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API works correctly without scope field
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                               | tenant    | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "200"                   | "200"                   | "/output_payloads/ResolvedSchema.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API works correctly and update schema properly
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    And put schema service should respond back with <ReponseStatusCodeForPUT>
    When I hit schema service PUT API with <UpdatedInputPayload>, data-partition-id as <tenant>
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                               | tenant    | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                         | UpdatedInputPayload                                      |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "200"                   | "200"                   | "/output_payloads/UpdatedResolvedSchema.json" | "/input_payloads/putUpdatedSchema_positiveScenario.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API throws error if update is requested for schema which is not in development status.
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> and mark schema as <status>
    When I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                               | tenant    | ReponseStatusCode | ResponseMessage                                                       | status      |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPut_InvalidStatusObsoletePublishedError.json" | "OBSOLETE"  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPut_InvalidStatusObsoletePublishedError.json" | "PUBLISHED" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API works like POST request when parameter from id is changed
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with next major version
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                               | tenant    | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "201"                   | "200"                   | "/output_payloads/ResolvedSchema.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API throws error if put request tries to create new record without development status
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> and mark schema as <status> for next major version
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                               | tenant    | ReponseStatusCode | ResponseMessage                                        | status      |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPut_InvalidStatusMessage.json" | "OBSOLETE"  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPut_InvalidStatusMessage.json" | "PUBLISHED" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API throws error if modification in schemaInfo is requested
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with different entityType
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                | tenant    | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                  |
      | "/input_payloads/PUT_ModifySchemaInfo.json" | "TENANT1" | "201"                   | "200"                   | "/output_payloads/ResolvedSchema.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API validates input payload
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                         | tenant    | ReponseStatusCode | ResponseMessage                                       |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API creates a empty private schema correctly.
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                         | tenant    | ReponseStatusCode | ResponseMessage                                                    | ReponseStatusCodeForGET | ResponseMessageforGET                        |
      | "/input_payloads/postSchemaService_EmptySchema.json" | "TENANT1" | "200"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "200"                   | "/output_payloads/ResolvedSchema_Empty.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API creates a empty private schema correctly.
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with next major version
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage>
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples: 
      | InputPayload                                         | tenant    | ReponseStatusCode | ResponseMessage                                                    | ReponseStatusCodeForGET | ResponseMessageforGET                        |
      | "/input_payloads/postSchemaService_EmptySchema.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "200"                   | "/output_payloads/ResolvedSchema_Empty.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API responds as bad request for wrong value of $ref attribute in schema input
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                             | tenant    | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/postSchema_InvalidRefSchemaObject.json" | "TENANT1" | "400"             | "/output_payloads/PostSchema_InvalidRefSchemaObject.json" |
      | "/input_payloads/postSchema_RefNotResolvable.json"       | "TENANT1" | "400"             | "/output_payloads/PostSchema_RefNotResolvable.json"       |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API responds as bad request for wrong value of $ref attribute in schema input
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with next major version
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                             | tenant    | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/postSchema_InvalidRefSchemaObject.json" | "TENANT1" | "400"             | "/output_payloads/PostSchema_InvalidRefSchemaObject.json" |
      | "/input_payloads/postSchema_RefNotResolvable.json"       | "TENANT1" | "400"             | "/output_payloads/PostSchema_RefNotResolvable.json"       |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API validates input payload for JSON correctness
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                         | ReponseStatusCode | ResponseMessage                                       | tenant    |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json" | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json" | "TENANT1" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API validates input payload for JSON correctness
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with next major version
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                         | ReponseStatusCode | ResponseMessage                                       | tenant    |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json" | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json" | "TENANT1" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API registers authority, source, entity and creates a private schema correctly with $ref attribute
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage> and scope whould be <responceScope>

    Examples: 
      | InputPayload                                                     | tenant    | ReponseStatusCode | ResponseMessage                                                    | responceScope |
      | "/input_payloads/postSchemaServiceWithRef_positiveScenario.json" | "TENANT1" | "200"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "INTERNAL"    |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API registers authority, source, entity and creates a private schema correctly with $ref attribute
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> with next major version
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage> and scope whould be <responceScope>

    Examples: 
      | parameter   | value              | latestVersion | InputPayload                                                     | tenant    | otherTenant | ReponseStatusCode | ResponseMessage                                                    | responceScope |
      | "authority" | "SchemaSanityTest" | "true"        | "/input_payloads/postSchemaServiceWithRef_positiveScenario.json" | "TENANT1" | "COMMON"    | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "INTERNAL"    |

  @SchemaService
  Scenario Outline: Verify that create Schema Service supersededBy functionality work correctly
    Given I hit schema service PUT API for supersededBy with <InputPayload> and data-partition-id as <tenant>
    Then the put service for supersededBy should respond back with <ReponseStatusCode>

    Examples: 
      | InputPayload                                           | tenant    | ReponseStatusCode |
      | "/input_payloads/supercededInputPayload_positive.json" | "TENANT1" | "201"             |

  @SchemaService
  Scenario Outline: Verify that update Schema Service supersededBy functionality work correctly
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant> for superceded input
    Then the put service for supersededBy should respond back with <ReponseStatusCode>

    Examples: 
      | InputPayload                                         | tenant    | ReponseStatusCode |
      | "/input_payloads/postSchemaService_EmptySchema.json" | "TENANT1" | "200"             |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API throws correct error if input payload is not valid
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                                   | tenant    | ReponseStatusCode | ResponseMessage                                            |
      | "/input_payloads/postSchema_withEntityAttributeInPayload.json" | "TENANT1" | "400"             | "/output_payloads/PostSchema_EntityNotAllowedError.json"   |
      | "/input_payloads/postSchema_flattenedSchemaAsInput.json"       | "TENANT1" | "400"             | "/output_payloads/PostSchema_InvalidInputSchemaError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's PUT API throws correct error if input payload is not valid
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayload                                                   | tenant    | ReponseStatusCode | ResponseMessage                                            |
      | "/input_payloads/postSchema_withEntityAttributeInPayload.json" | "TENANT1" | "400"             | "/output_payloads/PostSchema_EntityNotAllowedError.json"   |
      | "/input_payloads/postSchema_flattenedSchemaAsInput.json"       | "TENANT1" | "400"             | "/output_payloads/PostSchema_InvalidInputSchemaError.json" |

  @SchemaService
  Scenario Outline: Verify whether schema can not be registered with already existing major, but increased minor version
    Given I hit schema service PUT API with <InputPayload>, data-partition-id as <tenant>
    Given I hit schema service PUT API with <EmptyInputPayload>, data-partition-id as <tenant> with increased minor version only
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | EmptyInputPayload                                    | InputPayload                                           | ReponseStatusCode | tenant    | ResponseMessage                                             |
      | "/input_payloads/postSchemaService_EmptySchema.json" | "/input_payloads/inputPayloadWithExistingVersion.json" | "400"             | "TENANT1" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |
