Feature: To verify functionality of GET schema Service

  #Commons test steps are accomplished here
  Background: Common steps for all tests are executed
    Given I generate user token and set request headers for "TENANT1"
    Given I hit schema service GET List API with "authority" , "SchemaSanityTest" , "true"
    Given I hit schema service POST API with "/input_payloads/postInPrivateScope_positiveScenario.json" and data-partition-id as "TENANT1" only if status is not development

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET API throws correct exception when requested schemaId does not exist or is Malformed
    Given I hit schema service GET API with <SchemaId>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | SchemaId                                    | ReponseStatusCode | ResponseMessage                                 |
      | "NonExisting_slb:techlog:wellbore.5.1024.0" | "404"             | "/output_payloads/SchemaGet_NotFoundError.json" |
      | "testsource:common:testentity.1.0"          | "404"             | "/output_payloads/SchemaGet_NotFoundError.json" |
      | "common..testsource:testentity.1.0"         | "404"             | "/output_payloads/SchemaGet_NotFoundError.json" |
      | "common..testsource..testentity"            | "404"             | "/output_payloads/SchemaGet_NotFoundError.json" |
      | "common.testsource..testentity1.0"          | "404"             | "/output_payloads/SchemaGet_NotFoundError.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET list API throws correct exception if mandatory headers are blank in request header
    #Given I hit schema service GET List API with <parameter> and <value> having blank <header>
    Given I hit schema service GET API with blank <header>
    Then service should respond back with status code <ResponseStatusCode> or <AlternateStatusCode>

    Examples: 
      | ResponseStatusCode | ResponseMessage                                          | header              | AlternateStatusCode |
      | "401"              | "/output_payloads/SchemaGet_MissingAuthorization.json"   | "authorization"     | "403"               |
      | "401"              | "/output_payloads/SchemaGet_MissingDataPartitionId.json" | "data-partition-id" | "401"               |

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET list API returns list of schema all scopes
    Given I hit schema service GET List API with <parameter> and <value>
    Then service should respond back with schemaInfo list matching <parameter> and <value>

    Examples: 
      | parameter            | value              |
      | "authority"          | "SchemaSanityTest" |
      | "source"             | "testSource"       |
      | "entityType"         | "testEntity"       |
      | "schemaVersionMajor" | "majorVersion"     |
      | "schemaVersionMinor" | "minorVersion"     |
      | "status"             | "PUBLISHED"        |

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET list API returns correct list of schemas when queried with multiple parameters
    Given I hit schema service GET List API with query parameters having values <authority>, <source>, <entityType>, <status>, <scope>, <schemaVersionMajor>, <schemaVersionMinor>, <schemaVersionPatch>, <count>
    Then service should respond back with <responseCode> and schemaInfo list values matching to input

    Examples: 
      | authority          | source       | entityType   | status      | scope | schemaVersionMajor | schemaVersionMinor | schemaVersionPatch | count | responseCode |
      | "SchemaSanityTest" | "NA"         | "NA"         | "NA"        | "NA"  | "majorVersion"     | "minorVersion"     | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "testSource" | "NA"         | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "testSource" | "testEntity" | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "testSource" | "testEntity" | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "testSource" | "testEntity" | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "NA"         | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "NA"         | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "testEntity" | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "testSource" | "testEntity" | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "NA"         | "NA"         | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "NA"         | "NA"         | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "SchemaSanityTest" | "NA"         | "testEntity" | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "testSource" | "NA"         | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "testSource" | "testEntity" | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "testSource" | "NA"         | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "testSource" | "NA"         | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "testEntity" | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "testEntity" | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "testEntity" | "NA"        | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "NA"         | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |
      | "NA"               | "NA"         | "NA"         | "PUBLISHED" | "NA"  | "NA"               | "NA"               | "NA"               | "NA"  | "200"        |

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET list API handles patch filter error combinations elegantly with correct error messages
    Given I hit schema service GET List API with filters of <authority>, <schemaVersionMajor>, <schemaVersionMinor>, <schemaVersionPatch> and getLatest flag is <latestFlag>
    Then service should respond back with error <ReponseStatusCode> and <ResponseMessage>

    Examples: 
      | authority          | schemaVersionMajor | schemaVersionMinor | schemaVersionPatch | latestFlag | ReponseStatusCode | ResponseMessage                                                          |
      | "SchemaSanityTest" | "NA"               | "latestVersion"    | "NA"               | "True"     | "400"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |
      | "SchemaSanityTest" | "NA"               | "NA"               | "latestVersion"    | "True"     | "400"             | "/output_payloads/GetSchema_incorrectPatchFilter_PatchwithoutMinor.json" |
      | "SchemaSanityTest" | "NA"               | "latestVersion"    | "latestVersion"    | "True"     | "400"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |
      | "SchemaSanityTest" | "latestVersion"    | "NA"               | "latestVersion"    | "True"     | "400"             | "/output_payloads/GetSchema_incorrectPatchFilter_PatchwithoutMinor.json" |

  @SchemaService
  Scenario Outline: Verify that Schema Service's GET list API handles patch filter combinations elegantly with correct success messages
    Given I hit schema service GET List API with filters of <authority>, <schemaVersionMajor>, <schemaVersionMinor>, <schemaVersionPatch> and getLatest flag is <latestFlag>
    Then service should respond back with <ReponseStatusCode> and <ResponseMessage> schema with correct major, minor and patch version.

    Examples: 
      | authority          | schemaVersionMajor | schemaVersionMinor | schemaVersionPatch | latestFlag | ReponseStatusCode | ResponseMessage                                                          |
      | "SchemaSanityTest" | "latestVersion"    | "latestVersion"    | "latestVersion"    | "False"    | "200"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |
      | "SchemaSanityTest" | "latestVersion"    | "NA"               | "latestVersion"    | "False"    | "200"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |
      | "SchemaSanityTest" | "latestVersion"    | "latestVersion"    | "NA"               | "True"     | "200"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |
      | "SchemaSanityTest" | "latestVersion"    | "latestVersion"    | "latestVersion"    | "True"     | "200"             | "/output_payloads/GetSchema_incorrectPatchFilter_MinorWithoutMajor.json" |

  @SchemaService1
  Scenario Outline: Verify that Schema Service's GET list API handles offset filter combinations elegantly with correct success messages
    Given I hit schema GET List API with <parameter> and <schemaOffset1>
    Then service should respond back with status code <ReponseStatusCode> and note down id of <schemaOffset2>
    When I hit schema service GET List API with <parameter> and <schemaOffset2>
    Then service should respond back with status code <ReponseStatusCode> and note down id of <schemaOffset1> and compare with earlier id

    Examples: 
      | parameter | schemaOffset1 | schemaOffset2 | ReponseStatusCode |
      | "offset"  | "0"           | "1"           | "200"             |
