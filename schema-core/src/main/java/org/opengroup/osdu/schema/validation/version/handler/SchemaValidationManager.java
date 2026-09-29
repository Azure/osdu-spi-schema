package org.opengroup.osdu.schema.validation.version.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.util.JSONUtil;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaHandlerVO;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatchRefComp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class SchemaValidationManager {
	
	@Autowired
	private JaxRsDpsLog log;
	
	@Autowired
	private SchemaValidationChainOfHandlers handlers;
	
	@Autowired
	private JSONUtil jsonUtil;

	public void initiateValidationProcess(SchemaHandlerVO schemaDiff, List<SchemaBreakingChanges> schemaBreakingChanges) throws ApplicationException {

		SchemaValidationHandler handler = handlers.getFirstHandler();

		List<SchemaPatch> schemaPatchList = null;
		try {
			
			log.info("Finding difference between two schemas");
			schemaPatchList = jsonUtil.findJSONDiff(schemaDiff.getSourceSchema(), schemaDiff.getTargetSchema());
			//Sort the patch such that $ref are processed first
			if(schemaPatchList != null && schemaPatchList.size()>0) {
				schemaPatchList = schemaPatchList.stream().sorted(new SchemaPatchRefComp()).collect(Collectors.toList());
			}
			log.info("Total differences found :"+schemaPatchList.size());
			Set<String> processedArrayPath = new HashSet<>();

			for(SchemaPatch patch : schemaPatchList) {
				handler.compare(schemaDiff, patch, schemaBreakingChanges, processedArrayPath);
				//TODO Can controlled through some flag if list of all the breaking changes are required
				if(schemaBreakingChanges.size() >0)
					return;
			}
		} catch ( JsonProcessingException e) {
			throw new ApplicationException("Failed to process schema validation.");
		}

	}

}
