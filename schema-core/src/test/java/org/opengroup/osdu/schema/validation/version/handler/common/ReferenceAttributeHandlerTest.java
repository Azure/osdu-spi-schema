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
import org.opengroup.osdu.schema.validation.version.handler.minor.ReferenceAttributeHandler;
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
public class ReferenceAttributeHandlerTest {
	
	@InjectMocks
	ReferenceAttributeHandler referenceAttributeHandler;
	
	@Mock
	SchemaUtil schemaUtil;
	
	@Test
	public void testCompare_ValidChange_Minor_IncrementMinorVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/minor/base-schema.json"
				,"/schema_compare/ref_prop/minor/allowed/increment-minor.json", SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.2.1", SchemaValidationType.MINOR)).thenReturn(true);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			assertThat(schemaBreakingChanges.size() > 0).isFalse();
		}

	}
	
	@Test
	public void testCompare_ValidChange_Minor_IncrementPatchVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/minor/base-schema.json"
				,"/schema_compare/ref_prop/minor/allowed/increment-patch.json", SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.1.2", SchemaValidationType.MINOR)).thenReturn(true);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			assertThat(schemaBreakingChanges.size() > 0).isFalse();
		}

	}
	
	@Test
	public void testCompare_BreakingChange_Minor_DecrementMinorVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/minor/base-schema.json"
				,"/schema_compare/ref_prop/minor/breaking/decrement-minor.json", SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.0.1", SchemaValidationType.MINOR)).thenReturn(false);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Minor_DecrementPatchVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/minor/base-schema.json"
				,"/schema_compare/ref_prop/minor/breaking/decrement-patch.json", SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.1.0", SchemaValidationType.MINOR)).thenReturn(false);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Minor_RefIsNotSchemaKind() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/breaking/ref-not-schemakind.json", SchemaValidationType.MINOR);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:Abstract.CommonResources:2.1.1", SchemaValidationType.MINOR)).thenReturn(false);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Minor_PassdownToNextHandler() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/minor/base-schema.json"
				,"/schema_compare/ref_prop/minor/other/adding-new-ref.json", SchemaValidationType.MINOR);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		referenceAttributeHandler.setNextHandler(dummyHandler);
		
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertThat(schemaBreakingChanges.size()).isSameAs(0);
		Mockito.verify(dummyHandler, Mockito.atLeastOnce()).compare(schemaHandlerVO, schemaPatchList.get(0), schemaBreakingChanges, processedArrayPath);
	}
	
	
	/***
	 * Patch conditions mentioned below
	 * 
	 */
	
	
	@Test
	public void testCompare_BreakingChange_Patch_IncrementMinorVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/breaking/increment-minor.json", SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.2.1", SchemaValidationType.PATCH)).thenReturn(false);;
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Patch_DecrementPatchVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/breaking/increment-minor.json", SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.1.0", SchemaValidationType.PATCH)).thenReturn(false);;
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Patch_DecrementMinorVersion() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/breaking/decrement-minor.json", SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:AbstractCommonResources:2.0.1", SchemaValidationType.PATCH)).thenReturn(false);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Patch_RefIsNotSchemaKind() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/breaking/ref-not-schemakind.json", SchemaValidationType.PATCH);
		Mockito.when(schemaUtil.isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1", "osdu:wks:Abstract.CommonResources:2.1.2", SchemaValidationType.PATCH)).thenReturn(false);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}

		assertThat(schemaBreakingChanges.size() == 0).isFalse();
	}
	
	@Test
	public void testCompare_BreakingChange_Patch_PassdownToNextHandler() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/ref_prop/patch/base-schema.json"
				,"/schema_compare/ref_prop/patch/other/adding-new-ref.json", SchemaValidationType.PATCH);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		referenceAttributeHandler.setNextHandler(dummyHandler);
		
		for(SchemaPatch patch : schemaPatchList) {
			referenceAttributeHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertThat(schemaBreakingChanges.size()).isSameAs(0);
		Mockito.verify(dummyHandler, Mockito.atLeastOnce()).compare(schemaHandlerVO, schemaPatchList.get(0), schemaBreakingChanges, processedArrayPath);
	}
	
	private SchemaHandlerVO getMockSchemaHandlerVO(String baseSchemaPath, String newSchemaPath, SchemaValidationType type) throws IOException {
		JsonNode baseSchema = TestUtility.getJsonNodeFromFile(baseSchemaPath);
		JsonNode newSchema = TestUtility.getJsonNodeFromFile(newSchemaPath);
		JsonNode baseDef = new ObjectMapper().createObjectNode();
		JsonNode newDef =  new ObjectMapper().createObjectNode();

		SchemaHandlerVO newSchemaDiff = new SchemaHandlerVO(baseSchema, newSchema, baseDef, newDef, type);

		return newSchemaDiff;
	}
}
