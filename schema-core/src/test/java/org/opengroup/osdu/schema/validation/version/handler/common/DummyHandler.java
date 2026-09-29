package org.opengroup.osdu.schema.validation.version.handler.common;

import java.util.List;
import java.util.Set;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;

public class DummyHandler implements SchemaValidationHandler{

	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch,
			List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath)
			throws ApplicationException {
		
	}

	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}
	
}