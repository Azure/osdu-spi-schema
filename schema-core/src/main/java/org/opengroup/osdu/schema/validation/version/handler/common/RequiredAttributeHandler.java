package org.opengroup.osdu.schema.validation.version.handler.common;

import java.util.List;
import java.util.Set;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(5)
@Component
public class RequiredAttributeHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;
	
	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {
		
		//Changing list of "required" is not permitted
		if(patch.getPath().contains("/required")) {
			int lastIdx = patch.getPath().indexOf("required");
			String rootPath = patch.getPath().substring(0,	lastIdx+8);

			//Array is already validated
			if (processedArrayPath.contains(rootPath)) 
				return;

			validateRequiredAttribute(schemaDiff, patch, schemaBreakingChanges);
			processedArrayPath.add(rootPath);
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}
		
	}
	
	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}
	
	private  void validateRequiredAttribute(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges) {
		if(false == schemaDiff.getSourceSchema().equals(schemaDiff.getTargetSchema())) {
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Changing list of \"required\" is not permitted."));
		}

	}

}