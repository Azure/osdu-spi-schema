package org.opengroup.osdu.schema.validation.version;

import java.util.ArrayList;
import java.util.List;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.SchemaVersionException;
import org.opengroup.osdu.schema.util.JSONUtil;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationManager;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractVersionValidator implements VersionValidator{

	@Autowired
	private JaxRsDpsLog log;

	@Autowired
	private JSONUtil jsonUtil;
	
	
	@Autowired
	private SchemaValidationManager validationManager;
	
	@Override
	public void validateVersionChange(String oldSchema, String newSchema) throws ApplicationException, SchemaVersionException {

		try {
			
			log.info("Schema validation started.");
			
			JsonNode cleanSource = jsonUtil.getCleanJSON(oldSchema);
			JsonNode cleanTarget = jsonUtil.getCleanJSON(newSchema);
			JsonNode sourceDefinitions = jsonUtil.removeElement(cleanSource, "definitions").orElse(new ObjectMapper().createObjectNode());
			JsonNode targetDefinitions =  jsonUtil.removeElement(cleanTarget, "definitions").orElse(new ObjectMapper().createObjectNode());

			List<SchemaBreakingChanges> schemaBreakingChanges =  new ArrayList<>();
			SchemaHandlerVO schemaDiff = new SchemaHandlerVO(cleanSource, cleanTarget, sourceDefinitions, targetDefinitions, getType());
			
			log.info("Comparing two schema bodies");
			validationManager.initiateValidationProcess(schemaDiff, schemaBreakingChanges);
			
			schemaDiff.setSourceSchema(sourceDefinitions);
			schemaDiff.setTargetSchema(targetDefinitions);
			
			log.info("Comparing two schema definitions section");
			validationManager.initiateValidationProcess(schemaDiff, schemaBreakingChanges);
			
			handleBreakingChanges(schemaBreakingChanges);
			
		} catch (JsonProcessingException e) {
			throw new ApplicationException("Failed to vaildate schemas.");
		}
		
	}

	public abstract void handleBreakingChanges(List<SchemaBreakingChanges> schemaBreakingChanges) throws SchemaVersionException;
}
