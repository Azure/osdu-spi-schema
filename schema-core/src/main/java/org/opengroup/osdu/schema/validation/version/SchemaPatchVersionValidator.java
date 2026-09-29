package org.opengroup.osdu.schema.validation.version;

import java.util.List;

import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.SchemaVersionException;
import org.opengroup.osdu.schema.validation.version.AbstractVersionValidator;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.springframework.stereotype.Component;

@Component
public class SchemaPatchVersionValidator extends AbstractVersionValidator{

	@Override
	public SchemaValidationType getType() {
		return SchemaValidationType.PATCH;
	}

	@Override
	public void handleBreakingChanges(List<SchemaBreakingChanges> schemaBreakingChanges) throws SchemaVersionException {

		if(schemaBreakingChanges.size() > 0)
			throw new SchemaVersionException(SchemaConstants.BREAKING_CHANGES_PATCH);

	}

}
