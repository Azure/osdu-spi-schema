package org.opengroup.osdu.schema.validation.version.handler.common;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.schema.constants.SchemaConstants;
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

@Order(7)
@Component
public class RemoveOperationHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;
	@Autowired
	private SchemaUtil schemaUtil;
	
	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {

		String attributeName = StringUtils.substringAfterLast(patch.getPath(), "/");

		if(SchemaConstants.OP_REMOVE.equals(patch.getOp())) {

			if(schemaDiff.getChangedRefIds().containsKey(attributeName)) {
				return;
			}else if(isPresentInSource(schemaDiff, patch)) {
				return;
			}
			schemaBreakingChanges.add(new SchemaBreakingChanges(patch, "Removing elements from a schema at Minor level not permitted."));
		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}

	}

	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.COMMON;
	}
	
	private boolean isPresentInSource(SchemaHandlerVO schemaDiff, SchemaPatch patch) {
		Pattern pattern = Pattern.compile(SchemaConstants.SCHEMA_KIND_REGEX);
		String path =  patch.getPath();
		String sourceField = StringUtils.substringAfterLast(path, "/");
		if(!pattern.matcher(sourceField).matches() || !isAtRoot(path))
			return false;

		// Determine where to look for the new version based on the path
		Iterator<String> fieldNameItr;
		if (isInDefinitions(path)) {
			// For paths like /definitions/osdu:wks:..., look inside the definitions object
			com.fasterxml.jackson.databind.JsonNode definitions = schemaDiff.getTargetSchema().get(SchemaConstants.DEFINITIONS);
			if (definitions == null) {
				return false;
			}
			fieldNameItr = definitions.fieldNames();
		} else {
			fieldNameItr = schemaDiff.getTargetSchema().fieldNames();
		}

		while(fieldNameItr.hasNext()) {
			String fieldName = fieldNameItr.next();
			if(pattern.matcher(fieldName).matches()) {
				if(schemaUtil.isValidSchemaVersionChange(sourceField, fieldName,schemaDiff.getValidationType())){
					return true;
				}
			}
		}

		return false;
	}

	private boolean isInDefinitions(String path) {
		String[] subString = path.split("\\/");
		return subString.length == 3 && SchemaConstants.DEFINITIONS.equals(subString[1]);
	}
	
	private boolean isAtRoot(String path) {
		String subString [] = path.split("\\/");
		// Accept root level paths (e.g., /osdu:wks:...:1.0.0) with length 2
		// Also accept definition paths (e.g., /definitions/osdu:wks:...:1.0.0) with length 3
		return subString.length == 2 ||
			   (subString.length == 3 && SchemaConstants.DEFINITIONS.equals(subString[1]));
	}
}