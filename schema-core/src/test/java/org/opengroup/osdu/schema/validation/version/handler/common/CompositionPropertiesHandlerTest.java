package org.opengroup.osdu.schema.validation.version.handler.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISchemaInfoStore;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.util.TestUtility;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationManager;
import org.opengroup.osdu.schema.validation.version.handler.minor.CompositionPropertiesHandler;
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
public class CompositionPropertiesHandlerTest {
	
	@InjectMocks
	CompositionPropertiesHandler compositionPropertiesHandler;
	
	@Mock
	private SchemaUtil schemaUtil;
	
	@Mock
	private ISchemaInfoStore schemaInfoStore;

	@Mock
	private JaxRsDpsLog log;

	@Mock
	private DpsHeaders headers;
	
	@Spy
	SchemaValidationManager schemaValidationManager;
	
	
	@Test
	public void testCompare_ValidChange_NewAttributeAddedInTheMiddleWithTitle() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/allowed/base-schema-with-title.json"
				,"/schema_compare/composition_prop/allowed/adding-new-attr-inthe-middle.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.1",SchemaValidationType.MINOR);
		
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		Mockito.verify(schemaValidationManager, Mockito.atMost(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_ValidChange_JumblingPositionWithTitle() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/allowed/base-schema-with-title.json"
				,"/schema_compare/composition_prop/allowed/jumbling-with-title.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.1",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		assertTrue(schemaHandlerVO.getChangedRefIds().get("osdu:wks:AbstractCommonResources:2.1.1").equals("osdu:wks:AbstractCommonResources:2.1.1"));
		Mockito.verify(schemaValidationManager, Mockito.atMost(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_ValidChange_NewAttributeAddedAtEnd() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/allowed/adding-new-attr-end.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.1",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		assertTrue(schemaHandlerVO.getChangedRefIds().get("osdu:wks:AbstractCommonResources:2.1.1").equals("osdu:wks:AbstractCommonResources:2.1.1"));
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_ValidChange_Minor_MinorVersionIncr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/allowed/minor-ref-minorversion-incr.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.2.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.2.1",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		assertTrue(schemaHandlerVO.getChangedRefIds().get("osdu:wks:AbstractCommonResources:2.1.1").equals("osdu:wks:AbstractCommonResources:2.2.1"));
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_ValidChange_Minor_PatchVersionIncr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/allowed/minor-ref-patchversion-incr.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.2", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.2",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		assertTrue(schemaHandlerVO.getChangedRefIds().get("osdu:wks:AbstractCommonResources:2.1.1").equals("osdu:wks:AbstractCommonResources:2.1.2"));
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_ValidChange_Patch_PatchVersionIncr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/allowed/minor-ref-patchversion-incr.json", SchemaValidationType.PATCH);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.2", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.2",SchemaValidationType.PATCH);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		assertTrue(schemaHandlerVO.getChangedRefIds().get("osdu:wks:AbstractCommonResources:2.1.1").equals("osdu:wks:AbstractCommonResources:2.1.2"));
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	/**
	 * 
	 * Breaking changes
	 * 
	 */
	
	@Test
	public void testCompare_Breaking_NewAttributeAddedInTheMiddleWithoutTitle() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/breaking/adding-new-attr-middle.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				schemaBreakingChanges.add(new SchemaBreakingChanges(schemaPatchList.get(0), "breaking changes found"));
				return null;
			}}).when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.1",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() > 0);
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	@Test
	public void testCompare_Breaking_ContentChangedWithoutTitle() throws IOException, ApplicationException {

		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/breaking/adding-new-attr-middle.json", SchemaValidationType.MINOR);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(true).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.1",SchemaValidationType.MINOR);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() == 0);
		Mockito.verify(schemaValidationManager, Mockito.atLeast(2)).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());

	}
	
	
	@Test
	public void testCompare_Breaking_Patch_MinorIncr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/allowed/minor-ref-minorversion-incr.json", SchemaValidationType.PATCH);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.2.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(false).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.2.1",SchemaValidationType.PATCH);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() > 0);

	}
	
	@Test
	public void testCompare_Breaking_Patch_MinorDecr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/breaking/minor-ref-minorversion-decr.json", SchemaValidationType.PATCH);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.0.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(false).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.0.1",SchemaValidationType.PATCH);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() > 0);
	}
	
	@Test
	public void testCompare_Breaking_Patch_PatchDecr() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/breaking/minor-ref-patchversion-decr.json", SchemaValidationType.PATCH);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.0", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(false).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:2.1.0",SchemaValidationType.PATCH);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() > 0);
	}
	
	@Test
	public void testCompare_Breakinge_MajorChnaged() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/breaking/ref-majorversion-change.json", SchemaValidationType.PATCH);
		
		JsonNode sourceDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:2.1.1", "");
		JsonNode targetDef = new ObjectMapper().createObjectNode().put("osdu:wks:AbstractCommonResources:3.1.1", "");
		
		schemaHandlerVO.setTargetDefinition(targetDef);
		schemaHandlerVO.setSourceDefinition(sourceDef);
		
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());
		
		Mockito.doNothing().when(schemaValidationManager).initiateValidationProcess(Mockito.any(SchemaHandlerVO.class), ArgumentMatchers.anyList());
		Mockito.doReturn(false).when(schemaUtil).isValidSchemaVersionChange("osdu:wks:AbstractCommonResources:2.1.1","osdu:wks:AbstractCommonResources:3.2.1",SchemaValidationType.PATCH);
		
		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
		}
		
		assertTrue(schemaBreakingChanges.size() > 0);
	}
	
	@Test
	public void testCompare_PassdownToNextHandler() throws IOException, ApplicationException {
		List<SchemaBreakingChanges> schemaBreakingChanges = new ArrayList<>();
		Set<String> processedArrayPath = new HashSet<>();
		SchemaHandlerVO schemaHandlerVO = getMockSchemaHandlerVO("/schema_compare/composition_prop/base-schema.json"
				,"/schema_compare/composition_prop/other/add-new-attr.json", SchemaValidationType.PATCH);
		List<SchemaPatch> schemaPatchList = TestUtility.findSchemaPatch(schemaHandlerVO.getSourceSchema(), schemaHandlerVO.getTargetSchema());

		SchemaValidationHandler dummyHandler = Mockito.spy(new DummyHandler());
		compositionPropertiesHandler.setNextHandler(dummyHandler);

		for(SchemaPatch patch : schemaPatchList) {
			compositionPropertiesHandler.compare(schemaHandlerVO, patch, schemaBreakingChanges, processedArrayPath);
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
