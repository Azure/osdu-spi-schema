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
import org.mockito.Mockito;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
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
public class AdditionalPropertiesHandlerTest {
	
	@InjectMocks
	AdditionalPropertiesHandler additionalPropertiesHandler;
	
	@Test
	public void testCompare_ValidChange_AddPropWithTrue() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-without-addprop.json"
																	,"/schema_compare/additional_prop/allowed/new-schema-adding-addpropT.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() >0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_ValidChange_RemovePropWithOldTrue() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-with-addpropT.json"
																	,"/schema_compare/additional_prop/base-schema-without-addprop.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() >0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_BreakingChange_ChangeFromTrueToFalse() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-with-addpropT.json"
																	,"/schema_compare/additional_prop/base-schema-with-addpropF.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() == 0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_BreakingChange_ChangeFromFalseToTrue() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-with-addpropF.json"
																	,"/schema_compare/additional_prop/base-schema-with-addpropT.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() ==0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_BreakingChange_AddPropWithFalse() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-without-addprop.json"
																		,"/schema_compare/additional_prop/breaking/new-schema-adding-addpropF.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() == 0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_BreakingChange_RemovePropWithOldFalse() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-with-addpropF.json"
																	,"/schema_compare/additional_prop/base-schema-without-addprop.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() == 0)
				Assertions.fail();
		}
		
	}
	
	@Test
	public void testCompare_PassdownToNextHandler() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/additional_prop/base-schema-without-addprop.json"
																	,"/schema_compare/additional_prop/other/irrelevant-change.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		additionalPropertiesHandler.setNextHandler(dummyHandler);
		
		for(SchemaPatch patch : schemaPatchList) {
			additionalPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertThat(schemaBreakingChanges.size()).isSameAs(0);
		Mockito.verify(dummyHandler, Mockito.atLeastOnce()).compare(schemaHandlerVO, schemaPatchList.get(0), schemaBreakingChanges, processedArrayPath);
		
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
