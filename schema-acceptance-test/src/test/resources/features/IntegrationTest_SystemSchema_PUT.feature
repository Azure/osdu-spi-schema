Feature: To verify functionality of PUT schema Service

  ### Commons test steps are accomplished here
  Background: Common steps for all tests are executed
    Given I generate user token and set request headers for "TENANT1"
    Given I get latest schema with authority, source, entityType as "SchemaSanityTest", "testSource", "testEntity" respectively
    Given I generate user token and set request headers for system API
    Given I hit system schema PUT API with "/input_payloads/postInPrivateScope_positiveScenario.json" only if status is not development

  @SchemaService
  Scenario Outline: Verify that System schema PUT API works correctly without scope field
    Given I hit system schema PUT API with <InputPayload>
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    Given I set request headers for "TENANT1"
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples:
      | InputPayload                                                | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json"  | "200"                   | "200"                   | "/output_payloads/ResolvedSchema.json" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API works correctly and update schema properly
    Given I hit system schema PUT API with <InputPayload>
    And put schema service should respond back with <ReponseStatusCodeForPUT>
    When I hit system schema PUT API with <UpdatedInputPayload>
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    Given I set request headers for "TENANT1"
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples:
      | InputPayload                                                | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                         | UpdatedInputPayload                                      |
      | "/input_payloads/postInPrivateScope_positiveScenario.json"  | "200"                   | "200"                   | "/output_payloads/UpdatedResolvedSchema.json" | "/input_payloads/putUpdatedSchema_positiveScenario.json" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API is success if put request tries to create new record without development status
    Given I hit system schema PUT API with <InputPayload> and mark schema as <status> for next major version
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                                | ReponseStatusCode | ResponseMessage                                                    | status      |
      | "/input_payloads/postInPrivateScope_positiveScenario.json"  | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "OBSOLETE"  |
      | "/input_payloads/postInPrivateScope_positiveScenario.json"  | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "PUBLISHED" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API throws error if modification in schemaInfo is requested
    Given I hit system schema PUT API with <InputPayload> with different entityType
    Then put schema service should respond back with <ReponseStatusCodeForPUT>
    Given I set request headers for "TENANT1"
    And schema service should respond back with <ReponseStatusCodeForGET> and <ResponseMessageforGET>

    Examples:
      | InputPayload                                 | ReponseStatusCodeForPUT | ReponseStatusCodeForGET | ResponseMessageforGET                  |
      | "/input_payloads/PUT_ModifySchemaInfo.json"  | "201"                   | "200"                   | "/output_payloads/UpdatedSchema_EntityTypeUpdate.json" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API validates input payload
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                             | ReponseStatusCode | ResponseMessage                                       |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json"  | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API responds as bad request for wrong value of $ref attribute in schema input
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                                 | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/postSchema_InvalidRefSchemaObject.json"  | "400"             | "/output_payloads/PostSchema_InvalidRefSchemaObject.json" |
      | "/input_payloads/postSchema_RefNotResolvable.json"        | "400"             | "/output_payloads/PostSchema_RefNotResolvable.json"       |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API responds as bad request for wrong value of $ref attribute in schema input
    Given I hit system schema PUT API with <InputPayload> with next major version
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                                 | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/postSchema_InvalidRefSchemaObject.json"  | "400"             | "/output_payloads/PostSchema_InvalidRefSchemaObject.json" |
      | "/input_payloads/postSchema_RefNotResolvable.json"        | "400"             | "/output_payloads/PostSchema_RefNotResolvable.json"       |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API validates input payload for JSON correctness
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                         | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json" | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json"  |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API validates input payload for JSON correctness
    Given I hit system schema PUT API with <InputPayload> with next major version
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                         | ReponseStatusCode | ResponseMessage                                           |
      | "/input_payloads/inputPayloadWithIncorrectJSON.json" | "400"             | "/output_payloads/SchemaPost_IncorrectJsonError.json"  |

  #@SchemaService Commenting this tag for now since this scenario for "common" is failing
  Scenario Outline: Verify that System schema PUT API registers authority, source, entity and creates a private schema correctly with $ref attribute
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage> and scope whould be <responceScope>

    Examples:
      | InputPayload                                                         | ReponseStatusCode | ResponseMessage                                                    | responceScope |
      | "/input_payloads/postSchemaServiceWithRef_positiveScenario.json"  | "200"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "INTERNAL"    |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API registers authority, source, entity and creates a private schema correctly with $ref attribute
    Given I hit system schema PUT API with <InputPayload> with next major version
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage> and scope whould be <responceScope>

    Examples:
      | parameter   | value              | latestVersion | InputPayload                                                         | otherTenant | ReponseStatusCode | ResponseMessage                                                    | responceScope |
      | "authority" | "OSDUTest" | "true"        | "/input_payloads/postSchemaServiceWithRef_positiveScenario.json"  | "COMMON"    | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "INTERNAL"    |

  @SchemaService
  Scenario Outline: Verify that create Schema Service supersededBy functionality work correctly
    Given I hit system schema PUT API for supersededBy with <InputPayload>
    Then the put service for supersededBy should respond back with <ReponseStatusCode>

    Examples:
      | InputPayload                                               | ReponseStatusCode |
      | "/input_payloads/supercededInputPayload_positive.json"  | "201"             |

  @SchemaService
  Scenario Outline: Verify that update Schema Service supersededBy functionality work correctly
    Given I set request headers for "TENANT1"
    Given I get latest schema with authority, source, entityType as "SchemaSanityTest", "testSource", "testEntity" respectively
    Given I hit system schema PUT API with <InputPayload> for superceded input
    Then the put service for supersededBy should respond back with <ReponseStatusCode>

    Examples:
      | InputPayload                                             | ReponseStatusCode |
      | "/input_payloads/postSchemaService_EmptySchema.json"  | "200"             |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API throws correct error if input payload is not valid
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                                       | ReponseStatusCode | ResponseMessage                                            |
      | "/input_payloads/postSchema_withEntityAttributeInPayload.json"  | "400"             | "/output_payloads/PostSchema_EntityNotAllowedError.json"   |
      | "/input_payloads/postSchema_flattenedSchemaAsInput.json"        | "400"             | "/output_payloads/PostSchema_InvalidInputSchemaError.json" |

  @SchemaService
  Scenario Outline: Verify that System schema PUT API throws correct error if input payload is not valid
    Given I hit system schema PUT API with <InputPayload>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                                       | ReponseStatusCode | ResponseMessage                                            |
      | "/input_payloads/postSchema_withEntityAttributeInPayload.json"  | "400"             | "/output_payloads/PostSchema_EntityNotAllowedError.json"   |
      | "/input_payloads/postSchema_flattenedSchemaAsInput.json"        | "400"             | "/output_payloads/PostSchema_InvalidInputSchemaError.json" |
