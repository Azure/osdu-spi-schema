package org.opengroup.osdu.schema.validation.version.handler.common;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;

@Order(2)
@Component
public class AdditionalPropertiesHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;
	
	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {
		
		String attributeName = StringUtils.substringAfterLast(patch.getPath(), "/");
		//Changing state of "additionalProperties" is only permitted when it was not present and added as true
		//or it is present as true but removed now
		if("additionalProperties".equals(attributeName)) {
			if(SchemaConstants.OP_ADD.equals(patch.getOp()) && "true".equals(patch.getValue().asText())) {
				return;
			}else if(SchemaConstants.OP_REMOVE.equals(patch.getOp())) {
				JsonPointer pointer = JsonPointer.valueOf(patch.getPath());
				JsonNode originalValue = schemaDiff.getSourceSchema().at(pointer);
				
				if("true".equals(originalValue.asText()))
					return;
			}

			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Changing state of \"additionalProperties\" is not permitted."));
			
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}
		
	}

	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}

}
