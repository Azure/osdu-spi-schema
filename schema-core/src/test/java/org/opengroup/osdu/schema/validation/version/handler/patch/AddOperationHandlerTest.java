package org.opengroup.osdu.schema.validation.version.handler.patch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.util.TestUtility;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddOperationHandlerTest {
	@InjectMocks
	AddOperationHandler addOperationHandler;

	@Mock
	SchemaUtil schemaUtil;

	@Test
	public void testCompare_AddNewAttr_NotAllowed() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/add_operation/base-schema.json"
				,"/schema_compare/add_operation/addattr-breaking-patch.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			addOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() == 0)
				Assertions.fail();
		}

	}

	@Test
	public void testCompare_AddNewAttr_AllowedForAVersionUpgrade() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/add_operation/base-schema-2.json"
				,"/schema_compare/add_operation/addattr-version-upgrade-patch.json");
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.0.0", "osdu:wks:AbstractCommonResources:2.0.1", SchemaValidationType.PATCH)).thenReturn(true);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			addOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		Assertions.assertTrue(schemaBreakingChanges.size() == 0);

	}

	@Test
	public void testCompare_NoChange() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/add_operation/base-schema.json"
				,"/schema_compare/add_operation/base-schema.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			addOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}

	@Test
	public void testCompare_AddAttr_Minor() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/add_operation/base-schema.json"
				,"/schema_compare/add_operation/base-schema.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());

		schemaHandlerVO.setValidationType(SchemaValidationType.MINOR);
		for(SchemaPatch patch : schemaPatchList) {
			addOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}

	/**
	 * Regression test for schema version validation bug.
	 *
	 * This test verifies that adding a definition (e.g., AbstractCommonResources:1.0.1)
	 * is correctly recognized as valid when an older version (e.g., 1.0.0) exists in the source.
	 *
	 * The bug was that JSON Patch paths start with "/" per RFC 6901 (JSON Pointer),
	 * but the regex match was incorrectly checking the path WITH the "/" prefix
	 * instead of the extracted sourceField WITHOUT the "/" prefix.
	 *
	 * Without the fix: pattern.matcher("/osdu:wks:...").matches() returns false
	 * With the fix: pattern.matcher("osdu:wks:...").matches() returns true
	 */
	@Test
	public void testCompare_ValidChange_AddDefinitionWithPatchVersionUpgrade_RegressionTest() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();

		// Load schemas that simulate the exact bug scenario:
		// Source has AbstractCommonResources:1.0.0, Target has AbstractCommonResources:1.0.1
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO(
				"/schema_compare/definition_version_upgrade/source-schema.json",
				"/schema_compare/definition_version_upgrade/target-schema.json");

		// Mock: upgrading from 1.0.0 to 1.0.1 is a valid PATCH change
		Mockito.when(schemaUtil.isValidSchemaVersionChange(
				"test:wks:TestDefinition:1.0.0",
				"test:wks:TestDefinition:1.0.1",
				SchemaValidationType.PATCH)).thenReturn(true);

		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(
				schemaHandlerVO.getSourceSchema(),
				schemaHandlerVO.getTargetSchema());

		// Verify we have the expected patches (remove old version, add new version)
		Assertions.assertTrue(schemaPatchList.size() >= 1, "Expected patches for definition version change");

		for (SchemaPatch patch : schemaPatchList) {
			addOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		// With the fix, this should pass (no breaking changes for valid version upgrade)
		// Without the fix, this would fail because the regex wouldn't match the "/" prefixed path
		Assertions.assertTrue(schemaBreakingChanges.isEmpty(),
				"Valid definition version upgrade should not be flagged as breaking change");
	}

	private SchemaHandlerVO getMockSchemaHandlerVO(String baseSchemaPath, String newSchemaPath) throws IOException {
		JsonNode baseSchema = TestUtility.getJsonNodeFromFile(baseSchemaPath);
		JsonNode newSchema = TestUtility.getJsonNodeFromFile(newSchemaPath);
		JsonNode baseDef = new ObjectMapper().createObjectNode();
		JsonNode newDef =  new ObjectMapper().createObjectNode();

		SchemaHandlerVO newSchemaDiff = new SchemaHandlerVO(baseSchema, newSchema, baseDef, newDef, SchemaValidationType.PATCH);

		return newSchemaDiff;
	}
}
