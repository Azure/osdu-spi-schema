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


@Order(6)
@Component
public class TypeOperationHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;

	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {

		String attributeName = StringUtils.substringAfterLast(patch.getPath(), "/");

		if("type".endsWith(attributeName)) {
			//Remove operation is not permitted
			if(SchemaConstants.OP_REMOVE.equals(patch.getOp()) || SchemaConstants.OP_REPLACE.equals(patch.getOp())) {
				schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Remove operation is not permitted."));
			}
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}

	}
	
	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}

}