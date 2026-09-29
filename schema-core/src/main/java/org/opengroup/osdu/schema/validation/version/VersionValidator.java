package org.opengroup.osdu.schema.validation.version;

import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.SchemaVersionException;

public interface VersionValidator {
	
	public SchemaValidationType getType();
	
	public void validateVersionChange(String oldSchema, String newSchema) throws SchemaVersionException, ApplicationException;
	
	
}
