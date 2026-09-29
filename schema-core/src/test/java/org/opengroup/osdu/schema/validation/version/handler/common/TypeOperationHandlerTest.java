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
public class TypeOperationHandlerTest {
	
	@InjectMocks
	TypeOperationHandler typeOperationHandler;
	
	@Test
	public void testCompare_Allowed_TypeAdded() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/type_prop/allowed/base-schema.json"
				,"/schema_compare/type_prop/allowed/type-added.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		for(SchemaPatch patch : schemaPatchList) {
			typeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
			if(schemaBreakingChanges.size() > 0)
				Assertions.fail();
		}

	}
	
	@Test
	public void testCompare_BreakingChange_TypeDeleted() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/type_prop/base-schema.json"
																	,"/schema_compare/type_prop/breaking/type-removed.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			typeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		if(schemaBreakingChanges.size() ==0)
			Assertions.fail();
	}
	
	@Test
	public void testCompare_Breaking_TypeChanged() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/type_prop/base-schema.json"
																	,"/schema_compare/type_prop/breaking/type-changed.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		for(SchemaPatch patch : schemaPatchList) {
			typeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		if(schemaBreakingChanges.size() ==0)
			Assertions.fail();
	}
	
	@Test
	public void testCompare_PassdownToNextHandler() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/type_prop/base-schema.json"
				,"/schema_compare/type_prop/other/added-new-attr.json");
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());

		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		typeOperationHandler.setNextHandler(dummyHandler);

		for(SchemaPatch patch : schemaPatchList) {
			typeOperationHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
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
