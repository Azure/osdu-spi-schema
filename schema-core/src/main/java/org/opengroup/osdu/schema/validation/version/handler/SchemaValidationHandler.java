package org.opengroup.osdu.schema.validation.version.handler;

import java.util.List;
import java.util.Set;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;

public interface SchemaValidationHandler {

	public void setNextHandler(SchemaValidationHandler nextHandler);
	
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException;
	
	public SchemaValidationType getValidationType();
	
}
