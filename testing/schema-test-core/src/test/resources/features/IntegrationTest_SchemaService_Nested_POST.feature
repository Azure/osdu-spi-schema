Feature: To verify functionality of nested POST schema Service

  ### Common test steps are accomplished here
  Background: Common steps for all tests are executed
    Given I generate user token and set request headers for "TENANT1"
    Given I get latest schema with authority, source, entityType as "SchemaSanityTest", "testSource", "testEntity" respectively
    Given I hit schema service POST API with "/input_payloads/Base_Schema.json" and data-partition-id as "TENANT1" only if status is not development

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully even if ID, Title, x-osdu and few other optional attributes are added
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                          | InputPayloadWithChanges                                   | tenant    | ReponseStatusCode | ResponseMessage                                       | ResponseMessage1                                                   |
      | "/input_payloads/Nested_Base_Schema.json" | "/input_payloads/Nested_Base_Schema_ID.json"              | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Nested_Base_Schema.json" | "/input_payloads/Nested_Base_Schema_Title.json"           | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Nested_Base_Schema.json" | "/input_payloads/Nested_Base_Schema_MultipleChanges.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      | "/input_payloads/Nested_Base_Schema.json" | "/input_payloads/Nested_Base_Schema_x-osdu-added.json"    | "TENANT1" | "201"             | "/output_payloads/SchemaPost_SuccessfulCreation.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when minor and patch versions are increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version and patch version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute has false value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully when only minor version is increased and Additional properties attribute having true(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute having true(Earlier AP=False) value
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                                      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_WithFalseAP.json" | "/input_payloads/Nested_Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute having true(Earlier AP=False) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute having false(Earlier AP=NA) value
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                                    | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_WithoutAP.json" | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute having false(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully when only minor version is increased and Additional properties attribute having true(Earlier AP=NA) value
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully when only patch version is increased and Additional properties attribute having true(Earlier AP=NA) value
    When I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                     | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsT.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only minor version is increased and Additional properties attribute is absent(Earlier AP=false)
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                                            | InputPayloadWithChanges                             | tenant    | ReponseStatusCode1 | ReponseStatusCode | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_AdditionalPropIsF.json" | "/input_payloads/Nested_Base_Schema_WithoutAP.json" | "TENANT1" | "201"              | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only patch version is increased and Additional properties attribute is absent(Earlier AP=false)
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased patch version only
    Then user gets patch version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                             | tenant    | ReponseStatusCode | ResponseMessage                                              |
      | "/input_payloads/Nested_Base_Schema_WithoutAP.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_Patch_BreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request for adding/replacing any attribute which is not present in the immediate next schema record
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithAddedChanges> and data-partition-id as <tenant> with increased minor version with 2 count
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithReplacedChanges> and data-partition-id as <tenant> with less minor version by 1 count than earlier
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                              | InputPayloadWithAddedChanges                             | InputPayloadWithReplacedChanges                             | tenant    | ReponseStatusCode | ReponseStatusCode1 | ResponseMessage                                             | ResponseMessage1                                                   |
      | "/input_payloads/Nested_Base_Schema_New.json" | "/input_payloads/Nested_Base_Schema_AddedAttribute.json" | "/input_payloads/Nested_Base_Schema_ReplacedAttribute.json" | "TENANT1" | "400"             | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request for removing/replacing any attribute from previous schema record
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithRemovedChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                              | InputPayloadWithRemovedChanges                             | tenant    | ReponseStatusCode | ReponseStatusCode1 | ResponseMessage1                                                   | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_New.json" | "/input_payloads/Nested_Base_Schema_RemovedAttribute.json" | "TENANT1" | "400"             | "201"              | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request for removing/replacing any element of OneOf from previous schema record
    Given I hit schema service POST API with <InputPayloadWithRemovedChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithRemovedChanges                                             | tenant    | ReponseStatusCode | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_RemovedAttributeFromOneOfElement.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds as bad request when only minor version is increased with jumbled oneOf elements and position change of elements on index basis
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets minor version error response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | BaseInputPayload                                      | InputPayloadWithChanges                                           | tenant    | ReponseStatusCode | ResponseMessage1                                                   | ReponseStatusCode1 | ResponseMessage                                             |
      | "/input_payloads/Nested_Base_Schema_NestedoneOf.json" | "/input_payloads/Nested_Base_Schema_JumbledOneOfWithFalseAP.json" | "TENANT1" | "400"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              | "/output_payloads/SchemaPost_MinorBreakingChangeError.json" |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully when only minor version is increased with jumbled ref elements in oneOf
    Given I hit schema service POST API with <BaseInputPayload> and data-partition-id as <tenant> and update versions
    Then service should respond back with <ReponseStatusCode1> and <ResponseMessage1>
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode1> and <ResponseMessage1>

    Examples: 
      | BaseInputPayload                                                    | InputPayloadWithChanges                                           | tenant    |  | ResponseMessage1                                                   | ReponseStatusCode1 |
      | "/input_payloads/Nested_Base_Schema_NestedoneOf_RemoveElement.json" | "/input_payloads/Nested_Base_Schema_NestedoneOf_JumbeledRef.json" | "TENANT1" |  | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" | "201"              |

  @SchemaService
  Scenario Outline: Verify that nested Schema Service's POST API responds successfully when only minor version is increased and added one element in oneOf elements
    Given I hit schema service POST API with <InputPayloadWithChanges> and data-partition-id as <tenant> with increased minor version
    Then user gets response as <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | InputPayloadWithChanges                                                 | tenant    | ReponseStatusCode | ResponseMessage                                                    |
      | "/input_payloads/Nested_Base_Schema_NestedoneOf_AddedExtraElement.json" | "TENANT1" | "201"             | "/output_payloads/SchemaPost_PrivateScope_SuccessfulCreation.json" |
      