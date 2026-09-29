package org.opengroup.osdu.schema.validation.version.handler.patch;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.constants.SchemaConstants.SkipTags;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.handler.SchemaValidationHandler;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Order(1)
@Component
public class SkipAttributeHandler implements SchemaValidationHandler{

	private SchemaValidationHandler nextHandler;
	
	private EnumSet<SkipTags> skipTags = EnumSet.allOf(SkipTags.class);
	
	@Override
	public void setNextHandler(SchemaValidationHandler nextHandler) {
		this.nextHandler = nextHandler;
	}

	@Override
	public void compare(SchemaHandlerVO schemaDiff, SchemaPatch patch, List<SchemaBreakingChanges> schemaBreakingChanges, Set<String> processedArrayPath) throws ApplicationException {
		
		String attributeName = StringUtils.substringAfterLast(patch.getPath(), "/");
		
		if(schemaDiff.getValidationType() == getValidationType() && skipTags.stream().anyMatch(tag -> tag.getValue().equals(attributeName))
				|| attributeName.startsWith(SchemaConstants.TRANSFORMATION_TAG)) {
			return;

		}else if(null != nextHandler){
			this.nextHandler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
		}
		
	}
	
	@Override
	public SchemaValidationType getValidationType() {
		return SchemaValidationType.PATCH;
	}

}