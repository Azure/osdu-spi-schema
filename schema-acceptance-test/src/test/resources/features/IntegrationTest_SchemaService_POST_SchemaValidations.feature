Feature: To verify schema validation functionality of POST schema Service

  ### Common test steps are accomplished here
  Background: Common steps for all tests are executed
    Given I generate user token and set request headers for "TENANT1"
    Given I get latest schema with authority, source, entityType as "SchemaSanityTest", "testSource", "testEntity" respectively
    Given I hit schema service POST API with "/input_payloads/Base_Schema.json" and data-partition-id as "TENANT1" only if status is not development

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully even if ID, Title, x-osdu and few other optional attributes are added
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                   | InputPayloadWithChanges                            | tenant    | ReponseStatusCode | ResponseMessage                                       | ResponseMessage1                                                   |
      | "/input_payloads/Base_Schema.json" | "/input_payloads/Base_Schema_ID.json"              | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Base_Schema.json" | "/input_payloads/Base_Schema_Title.json"           | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Base_Schema.json" | "/input_payloads/Base_Schema_MultipleChanges.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Base_Schema.json" | "/input_payloads/Base_Schema_x-osdu-added.json"    | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when minor and patch versions are increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version and patch version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when only minor version is increased and Additional properties attribute having true(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute having true(Earlier AP=False) value
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                               | InputPayloadWithChanges                              | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_WithFalseAP.json" | "/input_payloads/Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute having true(Earlier AP=False) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute having false(Earlier AP=NA) value
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                             | InputPayloadWithChanges                              | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_WithoutAP.json" | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute having false(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when only minor version is increased and Additional properties attribute having true(Earlier AP=NA) value
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when only patch version is increased and Additional properties attribute having true(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                              | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute is absent(Earlier AP=false)
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                                     | InputPayloadWithChanges                      | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_AdditionalPropIsF.json" | "/input_payloads/Base_Schema_WithoutAP.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute is absent(Earlier AP=false)
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                      | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Base_Schema_WithoutAP.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request for adding/replacing any attribute which is not present in the immediate next schema record
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithAddedChanges> and data-partition-id as <tenant> with increased minor version with 2 count
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithReplacedChanges> and data-partition-id as <tenant> with less minor version by 1 count than earlier
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                       | InputPayloadWithAddedChanges                      | InputPayloadWithReplacedChanges                      | tenant    | ReponseStatusCode | ReponseStatusCode1 | ResponseMessage                                             | ResponseMessage1                                                   |
      | "/input_payloads/Base_Schema_New.json" | "/input_payloads/Base_Schema_AddedAttribute.json" | "/input_payloads/Base_Schema_ReplacedAttribute.json" | "TENANT1" | "400"             | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request for adding/replacing any attribute which is not present in the immediate next schema record
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithAddedChanges> and data-partition-id as <tenant> with increased minor version with 1 count
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithPatchChanges> and data-partition-id as <tenant> with less minor version by 1 count than earlier and increase patch by 1
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>

    Examples: 
      | BaseInputPayload                             | InputPayloadWithAddedChanges                             | InputPayloadWithPatchChanges                    | tenant    | ReponseStatusCode1 | ResponseMessage1                                                   |
      | "/input_payloads/Base_Schema_WithoutAP.json" | "/input_payloads/Base_Schema_MinorLevelChangeApIsT.json" | "/input_payloads/Base_Schema_PatchChanges.json" | "TENANT1" | "201"              | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request for removing/replacing any attribute from previous schema record
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithRemovedChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                       | InputPayloadWithRemovedChanges                      | tenant    | ReponseStatusCode | ReponseStatusCode1 | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_New.json" | "/input_payloads/Base_Schema_RemovedAttribute.json" | "TENANT1" | "400"             | "201"              | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request for removing/replacing any element of OneOf from previous schema record
    Given I hit schema service POST API with <InputPayloadWithRemovedChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithRemovedChanges                                      | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_RemovedAttributeFromOneOfElement.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when only minor version is increased with jumbled oneOf elements and position change of elements on index basis
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                               | InputPayloadWithChanges                                    | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/Base_Schema_NestedoneOf.json" | "/input_payloads/Base_Schema_JumbledOneOfWithFalseAP.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when only minor version is increased and added one element in oneOf elements
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Base_Schema_NestedoneOf_AddedExtraElement.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when only minor version is increased with jumbled ref elements in oneOf
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>

    Examples: 
      | BaseInputPayload                                             | InputPayloadWithChanges                                    | tenant    | ResponseMessage1                                                   | ReponseStatusCode1 |
      | "/input_payloads/Base_Schema_NestedoneOf_RemoveElement.json" | "/input_payloads/Base_Schema_NestedoneOf_JumbeledRef.json" | "TENANT1" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when type of any element is changed
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                   | InputPayloadWithChanges                        | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/Base_Schema.json" | "/input_payloads/Base_Schema_TypeChanged.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  #------------------------------------------------------Ref scenarios
  ##Positive scenarios
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is changed at minor level and main schema is compared for minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_MinorChangeToInternalSchema.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is changed at patch level and main schema is compared for minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_PatchChangeToInternalSchema.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is changed at patch level and main schema is compared for patch change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_PatchChangeToInternalSchema.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is changed at patch, minor level and main schema is compared for patch and minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithMinorPatchChanges> and data-partition-id as <tenant> with increased minor version and patch version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>
    Given I hit schema service POST API with <InputPayloadWithMinorChanges> and data-partition-id as <tenant> with less minor version by 1 count than earlier
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithMinorPatchChanges                                      | InputPayloadWithMinorChanges                           | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_MinorPatchChangesToInternalSchema.json" | "/input_payloads/RefBaseSchema_MinorLevelChanges.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  #-------------------------- When internal schema has name as string
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is string and changed at minor level and main schema is compared for minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                         | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_String.json" | "/input_payloads/RefBaseSchema_MinorChangeToInternalSchema_String.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is string and changed at patch level and main schema is compared for minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                         | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_String.json" | "/input_payloads/RefBaseSchema_PatchChangeToInternalSchema_String.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is string and changed at patch level and main schema is compared for patch change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                         | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_String.json" | "/input_payloads/RefBaseSchema_PatchChangeToInternalSchema_String.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when schema of $ref value is having format other than kind format and is considered as string
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                                    | InputPayloadWithChanges                                           | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_StringHavingColon.json" | "/input_payloads/RefBaseSchema_StringHavingColon_MinorLevel.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when schema of $ref value is having string or kind format and is changed to kind or string respectively
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                                    | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_StringHavingColon.json" | "/input_payloads/RefBaseSchema_StringHavingColon_PatchIncremented.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |
      | "/input_payloads/RefBaseSchema_StringHavingColon.json" | "/input_payloads/RefBaseSchema_StringHavingColon_RefAsKind.json"        | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |
      | "/input_payloads/RefBaseSchema_String.json"            | "/input_payloads/RefBaseSchema_StringHavingColon_NoChange.json"         | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |
      | "/input_payloads/RefBaseSchema_String.json"            | "/input_payloads/RefBaseSchema_StringHavingColon_Patch.json"            | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  #------------------------------Negative scenarios
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when schema of $ref value is changed at major level and main schema is compared for minor change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                           | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_Negative.json" | "/input_payloads/RefBaseSchema_MajorChangeToInternalSchema.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when schema of $ref value is changed at major level and main schema is compared for patch change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                           | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_Negative.json" | "/input_payloads/RefBaseSchema_MajorChangeToInternalSchema.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when schema of $ref value is changed at minor level and main schema is compared for patch change
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                          | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_MinorChangeToInternalSchema.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when Authority is changed in ref schema value and minor level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                               | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_AuthorityChange_RefChangeAtMinor.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when Source is changed in ref schema value and minor level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                            | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_SourceChange_RefChangeAtMinor.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when entityType is changed in ref schema value and minor level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                                | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_EntityTypeChange_RefChangeAtMinor.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when Authority is changed in ref schema value and patch level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                               | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_AuthorityChange_RefChangeAtPatch.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when Source is changed in ref schema value and patch level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                            | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_SourceChange_RefChangeAtPatch.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when entityType is changed in ref schema value and patch level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                                | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_EntityTypeChange_RefChangeAtPatch.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  #-------------------------- Schemas should be posted in increment order for patch versions
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when main schema is incremented at patch level and $ref version is incremented at patch version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_PatchVersionIncremented_PatchLevel.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at patch level and $ref version is incremented at minor version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MinorVersionIncremented_PatchLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at patch level and $ref version is decremented at minor version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MinorVersionDecremented_PatchLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at patch level and $ref version is also decremented at patch version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_PatchVersionDecremented_PatchLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at patch level and $ref version is incremented at major version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MajorVersionIncremented_PatchLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at patch level and entity name is changed in $ref
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                           | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_EntityNameChanged_PatchLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

	@SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when entityType is changed in ref schema value and patch level incremented
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                  | InputPayloadWithChanges                                                | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                              |
      | "/input_payloads/RefBaseSchema.json" | "/input_payloads/RefBaseSchema_EntityTypeChange_RefChangeAtPatch.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |
	
  #-------------------------- Schemas should be posted in increment order for minor versions
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when main schema is incremented at minor level and $ref version is incremented at patch version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_PatchVersionIncremented_MinorLevel.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at minor level and $ref version is incremented at minor version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MinorVersionIncremented_MinorLevel.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |
	
	@SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds successfully when main schema is incremented at minor level and a new attribute is added to it
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                       |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_AddedNewAttribute_PatchLevel.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_SuccessfulCreation.json" |
	
  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at minor level and $ref version is decremented at minor version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MinorVersionDecremented_MinorLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at minor level and $ref version is also decremented at patch version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_PatchVersionDecremented_MinorLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at minor level and $ref version is incremented at major version level
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_MajorVersionIncremented_MinorLevel.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request when main schema is incremented at minor level and entity name is changed in $ref
    Given I hit schema service POST API with <RefBaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | RefBaseInputPayload                      | InputPayloadWithChanges                                             | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/RefBaseSchema_New.json" | "/input_payloads/RefBaseSchema_EntityNameChanged_MinorVersion.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's POST API responds as bad request for empty value of authority, source and entity
    When I hit schema service POST API with <InputPayload> and data-partition-id as <tenant>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples:
      | InputPayload                                          | ReponseStatusCode | ResponseMessage                               | tenant    |
      | "/input_payloads/inputPayloadWithEmptyAuthority.json" | "400"             | "/output_payloads/EmptyAuthorityPayload.json" | "TENANT1" |
      | "/input_payloads/inputPayloadWithEmptyEntity.json"    | "400"             | "/output_payloads/EmptyEntityPayload.json"    | "TENANT1" |
      | "/input_payloads/inputPayloadWithEmptySource.json"    | "400"             | "/output_payloads/EmptySourcePayload.json"    | "TENANT1" |

