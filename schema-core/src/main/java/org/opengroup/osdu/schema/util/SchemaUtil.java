package org.opengroup.osdu.schema.util;


import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISchemaInfoStore;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaUtil {

	private final ISchemaInfoStore schemaInfoStore;

	private final JaxRsDpsLog log;

	private final DpsHeaders headers;

	@Value("${shared.tenant.name:common}")
	private String sharedTenant;

	public SchemaInfo[] findSchemaToCompare(SchemaInfo schemaInfo, Boolean isSystemSchema) throws ApplicationException {

		SchemaInfo[] matchingSchemaInfo = findClosestSchemas(schemaInfo, true, isSystemSchema);

		if(null == matchingSchemaInfo[0] && null == matchingSchemaInfo[1]) {
			log.info("Latest patch version is not found so trying to find the nearest matching major version");
			matchingSchemaInfo =  findClosestSchemas(schemaInfo, false, isSystemSchema);
		}

		return matchingSchemaInfo;
	}

	public SchemaInfo[] findClosestSchemas(SchemaInfo schemaInfo, boolean atPatchlevel, Boolean isSystemSchema) throws ApplicationException {
		SchemaIdentity schemaIdentity = schemaInfo.getSchemaIdentity();
		SchemaInfo[] schemaInfoArr = new SchemaInfo [2];
		QueryParams.QueryParamsBuilder queryParamBuilder = QueryParams.builder().authority(schemaIdentity.getAuthority())
				.source(schemaIdentity.getSource())
				.entityType(schemaIdentity.getEntityType())
				.schemaVersionMajor(schemaIdentity.getSchemaVersionMajor());

		if(atPatchlevel)
			queryParamBuilder.schemaVersionMinor(schemaIdentity.getSchemaVersionMinor());

		QueryParams queryParams = queryParamBuilder.build();
		List<SchemaInfo> schemaInfoList;

		if (isSystemSchema) {
			schemaInfoList = schemaInfoStore.getSystemSchemaInfoList(queryParams);
		} else {
			schemaInfoList = schemaInfoStore.getSchemaInfoList(queryParams, headers.getPartitionId());
		}

		if(null == schemaInfoList || schemaInfoList.isEmpty())
			return schemaInfoArr;

		SchemaComparatorByVersion schemaComparatorByVersion = new SchemaComparatorByVersion();
		Collections.sort(schemaInfoList, schemaComparatorByVersion);

		SchemaInfo smaller = null,bigger =null; 

		for(SchemaInfo element : schemaInfoList) {

			if(schemaComparatorByVersion.compare(element, schemaInfo) < 0) {
				smaller = element;
			}else if (schemaComparatorByVersion.compare(element, schemaInfo) > 0) {
				bigger = element;
				break;
			}
		}

		schemaInfoArr[0]=smaller;
		schemaInfoArr[1]=bigger;

		return schemaInfoArr;
	}

	public boolean isValidSchemaVersionChange(String oldSchema, String newSchema, SchemaValidationType type) {

		Pattern pattern = Pattern.compile(SchemaConstants.SCHEMA_KIND_REGEX);
		Matcher oldMatcher = pattern.matcher(oldSchema);
		Matcher newMatcher = pattern.matcher(newSchema);

		if(false == oldMatcher.find() || 7 != oldMatcher.groupCount()
				|| false == newMatcher.find() || 7 != newMatcher.groupCount())
			return oldSchema.equals(newSchema);

		if(!StringUtils.substringBeforeLast(oldSchema, ":").equals((StringUtils.substringBeforeLast(newSchema, ":"))))
			return false;

		if(SchemaValidationType.MINOR == type)
			return isValidVersionChange(oldMatcher.group(4), newMatcher.group(4), (oldMinor, newMinor) -> oldMinor > newMinor);

		else 
			return isValidVersionChange(oldMatcher.group(4), newMatcher.group(4), (oldMinor, newMinor) -> oldMinor != newMinor);
	}

	private boolean isValidVersionChange(String oldVersion, String newVersion, BiPredicate<Long, Long> minorVersionPredicate) {
		String [] oldVersionArr = oldVersion.split("\\.");
		String [] newVersionArr = newVersion.split("\\.");


		if(Long.valueOf(oldVersionArr[0]) != Long.valueOf(newVersionArr[0])
				|| minorVersionPredicate.test(Long.valueOf(oldVersionArr[1]), Long.valueOf(newVersionArr[1]))
				|| Long.valueOf(oldVersionArr[2]) > Long.valueOf(newVersionArr[2]))
			return false;

		return true;
	}
}
