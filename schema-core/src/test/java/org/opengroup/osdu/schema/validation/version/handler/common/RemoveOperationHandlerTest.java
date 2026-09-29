package org.opengroup.osdu.schema.validation.version.handler.common;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
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
public class RemoveOperationHandlerTest {

	@InjectMocks
	RemoveOperationHandler removeOperationHandler;
	
	@Mock
	SchemaUtil schemaUtil;

	@Test
	public void testCompare_ValidChange_RemovePropWithOldTrue() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed/base-schema.json"
				,"/schema_compare/remove_operation/allowed/new-schema.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		schemaHandlerVO.getChangedRefIds().put("osdu:wks:AbstractCommonResources:1.0.0", "osdu:wks:AbstractCommonResources:1.1.0");
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_ValidChange_RemovePropInDefinition() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed/base-schema.json"
				,"/schema_compare/remove_operation/allowed/new-schema.json");
		schemaHandlerVO.setValidationType(SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:1.0.0", "osdu:wks:AbstractCommonResources:1.1.0", SchemaValidationType.MINOR)).thenReturn(true);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_ValidChange_RemovePropInDefinition_WithPatchUpgrage() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed-patch/base-schema.json"
				,"/schema_compare/remove_operation/allowed-patch/new-schema.json");
		schemaHandlerVO.setValidationType(SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:1.1.0", "osdu:wks:AbstractCommonResources:1.1.1", SchemaValidationType.MINOR)).thenReturn(true);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_ValidChange_RemovePropInDefinition_Patch() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed-patch/base-schema.json"
				,"/schema_compare/remove_operation/allowed-patch/new-schema.json");
		schemaHandlerVO.setValidationType(SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:1.1.0", "osdu:wks:AbstractCommonResources:1.1.1", SchemaValidationType.PATCH)).thenReturn(true);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_BreakingChange_RemovePropInDefinition() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed/new-schema.json",
						"/schema_compare/remove_operation/allowed/base-schema.json"
				);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		schemaHandlerVO.setValidationType(SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:1.1.0", "osdu:wks:AbstractCommonResources:1.0.0", SchemaValidationType.MINOR)).thenReturn(false);
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.assertTrue(true);
			else
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_BreakingChange_RemovePropInDefinition_Patch() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/allowed-patch/new-schema.json",
						"/schema_compare/remove_operation/allowed-patch/base-schema.json"
				);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		schemaHandlerVO.setValidationType(SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:1.1.1", "osdu:wks:AbstractCommonResources:1.1.0", SchemaValidationType.PATCH)).thenReturn(false);
		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.assertTrue(true);
			else
				Assertions.fail();
		}

	}

	@Test
	public void testCompare_BreakingChange_ChangeFromTrueToFalse() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/base-schema.json"
				,"/schema_compare/remove_operation/breaking/attribute-removed.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());

		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() == 0)
				Assertions.fail();
		}
	}

	@Test
	public void testCompare_PassdownToNextHandler() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/remove_operation/base-schema.json"
				,"/schema_compare/remove_operation/other/added-new-attr.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());

		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		removeOperationHandler.setNextHandler(dummyHandler);

		for(SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size()).isSameAs(0);
		Mockito.verify(dummyHandler, Mockito.atLeastOnce()).compare(schemaHandlerVO, schemaPatchList.get(0), schemaBreakingChanges, processedArrayPath);
	}

	/**
	 * Regression test for schema version validation bug.
	 *
	 * This test verifies that removing a definition (e.g., AbstractCommonResources:1.0.0)
	 * is correctly recognized as valid when a newer version (e.g., 1.0.1) exists in the target.
	 *
	 * The bug was that JSON Patch paths start with "/" per RFC 6901 (JSON Pointer),
	 * but the regex match was incorrectly checking the path WITH the "/" prefix
	 * instead of the extracted sourceField WITHOUT the "/" prefix.
	 *
	 * Without the fix: pattern.matcher("/osdu:wks:...").matches() returns false
	 * With the fix: pattern.matcher("osdu:wks:...").matches() returns true
	 */
	@Test
	public void testCompare_ValidChange_RemoveDefinitionWithPatchVersionUpgrade_RegressionTest() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();

		// Load schemas that simulate the exact bug scenario:
		// Source has AbstractCommonResources:1.0.0, Target has AbstractCommonResources:1.0.1
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO(
				"/schema_compare/definition_version_upgrade/source-schema.json",
				"/schema_compare/definition_version_upgrade/target-schema.json");
		schemaHandlerVO.setValidationType(SchemaValidationType.MINOR);

		// Mock: upgrading from 1.0.0 to 1.0.1 is a valid MINOR change
		Mockito.when(schemaUtil.isValidSchemaVersionChange(
				"test:wks:TestDefinition:1.0.0",
				"test:wks:TestDefinition:1.0.1",
				SchemaValidationType.MINOR)).thenReturn(true);

		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(
				schemaHandlerVO.getSourceSchema(),
				schemaHandlerVO.getTargetSchema());

		// Verify we have the expected patches (remove old version, add new version)
		Assertions.assertTrue(schemaPatchList.size() >= 1, "Expected patches for definition version change");

		for (SchemaPatch patch : schemaPatchList) {
			removeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
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

		SchemaHandlerVO newSchemaDiff = new SchemaHandlerVO(baseSchema, newSchema, baseDef, newDef, SchemaValidationType.MINOR);

		return newSchemaDiff;
	}
}
