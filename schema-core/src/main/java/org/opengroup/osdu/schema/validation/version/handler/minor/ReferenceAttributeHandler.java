package org.opengroup.osdu.schema.validation.version.handler.minor;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;


@Order(4)
@Component
public class ReferenceAttributeHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;
	
	@Autowired
	SchemaUtil schemaUtil;

	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {

		String attributeName = StringUtils.substringAfterLast(patch.getPath(), "/");

		if("$ref".equals(attributeName)) {
			if("replace".equals(patch.getOp())) {
				checkRefChangesAreAllowed(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
				return;
			}else if(SchemaValidationType.MINOR == schemaDiff.getValidationType() 
					&& "add".equals(patch.getOp())) {
				return;
			}
			
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "This operation with \"$ref\" is not permitted."));
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}

	}
	
	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}

	private void checkRefChangesAreAllowed(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) {

		JsonPointer pointer = JsonPointer.valueOf(patch.getPath());
		JsonNode oldRefValue = schemaDiff.getSourceSchema().at(pointer);
		JsonNode newRefValue = schemaDiff.getTargetSchema().at(pointer);
		String oldRefId = StringUtils.substringAfterLast(oldRefValue.asText(), "/");
		String newRefId = StringUtils.substringAfterLast(newRefValue.asText(), "/");

		if(schemaUtil.isValidSchemaVersionChange(oldRefId, newRefId, schemaDiff.getValidationType()) == false) {
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Invalid \"$ref\" version change."));
			return;
		}
		schemaDiff.getChangedRefIds().put(oldRefId, newRefId);

	}

}