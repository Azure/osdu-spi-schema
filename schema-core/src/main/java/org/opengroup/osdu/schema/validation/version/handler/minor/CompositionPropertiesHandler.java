package org.opengroup.osdu.schema.validation.version.handler.minor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.schema.constants.SchemaConstants.CompositionTags;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationManager;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Order(3)
@Component
public class CompositionPropertiesHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;

	private SchemaValidationManager validationManager;

	private SchemaUtil schemaUtil;

	public CompositionPropertiesHandler(@Lazy SchemaValidationManager validationManager, SchemaUtil schemaUtil) {
		this.validationManager = validationManager;
		this.schemaUtil = schemaUtil;
	}
	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {

		//Check if is composition property
		if(patch.getPath().contains("/allOf") || patch.getPath().contains("/oneOf") || patch.getPath().contains("/anyOf")) {
			String [] attributes = patch.getPath().split("/");

			Optional<String> xxxOfAttrOpt = Arrays.stream(attributes)
					.filter(attr -> (CompositionTags.ALL_OF.getValue().equals(attr) 
										|| CompositionTags.ONE_OF.getValue().equals(attr) 
										|| CompositionTags.ANY_OF.getValue().equals(attr)))
					.reduce((first, second) -> second);
			
			int lastIdx = patch.getPath().lastIndexOf(xxxOfAttrOpt.get());
			String rootPath = patch.getPath().substring(0,	lastIdx+5);
			
			if(patch.getOp().equals("remove") && patch.getPath().equals(rootPath)) {
				schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Removing composition attribute is not allowed"));
				return;
			}

			//Array is already validated
			if (processedArrayPath.contains(rootPath)) 
				return;

			validateCompositionAttributesForMinorVersion(schemaDiff, patch, schemaBreakingChanges, processedArrayPath, rootPath);
			processedArrayPath.add(rootPath);
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}

	}
	
	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}

	private  void validateCompositionAttributesForMinorVersion(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath, String rootPath) throws ApplicationException {
		
		JsonNode oldJsonArray = schemaDiff.getSourceSchema().at(rootPath);
		JsonNode newJsonArray  = schemaDiff.getTargetSchema().at(rootPath);

		if(schemaDiff.getValidationType() == SchemaValidationType.MINOR && oldJsonArray.size() > newJsonArray.size()) {
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Removing element fromt the array is not allowed"));
			return;
		}
		
		else if(schemaDiff.getValidationType() == SchemaValidationType.PATCH && oldJsonArray.size() != newJsonArray.size()) {
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Changing the array is not allowed"));
			return;
		}

		for(int i=0;i<oldJsonArray.size();i++) {
			String oldTitle=null;
			String oldRef=null;
			JsonNode oldValue = oldJsonArray.get(i);

			//Check Ref and Title attribute is present inside the array
			if(oldValue.has("title")) {
				oldTitle = oldValue.get("title").asText();
			}
			if(oldValue.has("$ref")) {
				oldRef = oldValue.get("$ref").asText();
			}

			//If both the elements are missing then compare the value at respective index
			if(null == oldTitle && null == oldRef) {

				SchemaHandlerVO newSchemaDiff = new SchemaHandlerVO(oldValue, newJsonArray.get(i), schemaDiff.getSourceDefinition(), schemaDiff.getTargetDefinition(), schemaDiff.getValidationType());
				newSchemaDiff.setChangedRefIds(schemaDiff.getChangedRefIds());
				newSchemaDiff.setProcessedArrayPath(schemaDiff.getProcessedArrayPath());
				
				validationManager.initiateValidationProcess(newSchemaDiff, schemaBreakingChanges);
			}else {
				Iterator<JsonNode> newItr = newJsonArray.iterator();
				int count=0;
				String newTitle=null;
				String newRef=null;
				while(newItr.hasNext()) {
					JsonNode newValue = newItr.next();
					if(null != oldTitle && newValue.has("title")) {
						newTitle = newValue.get("title").asText();
						if(oldTitle.equals(newTitle)) {
							
							SchemaHandlerVO newSchemaDiff = new SchemaHandlerVO(oldValue, newValue, schemaDiff.getSourceDefinition(), schemaDiff.getTargetDefinition(), schemaDiff.getValidationType());
							newSchemaDiff.setChangedRefIds(schemaDiff.getChangedRefIds());
							newSchemaDiff.setProcessedArrayPath(schemaDiff.getProcessedArrayPath());
							validationManager.initiateValidationProcess(newSchemaDiff, schemaBreakingChanges);
							break;
						}

					}else if(null != oldRef && newValue.has("$ref")) {
						newRef = newValue.get("$ref").asText();
						String oldRefId = StringUtils.substringAfterLast(oldRef, "/");
						String newRefId = StringUtils.substringAfterLast(newRef, "/");
						if(schemaUtil.isValidSchemaVersionChange(oldRefId, newRefId, schemaDiff.getValidationType()) && schemaDiff.getTargetDefinition().has(newRefId)) {
							schemaDiff.getChangedRefIds().put(oldRefId, newRefId);
							break;
						}
					}

					//increment the count
					count++;

				}

				//Element is not found in the entire Array
				if(count == newJsonArray.size()) {
					schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Element not found in the entire schema"));
				}
			}
		}

	}
}
