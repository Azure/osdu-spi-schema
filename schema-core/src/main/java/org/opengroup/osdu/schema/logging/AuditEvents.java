package org.opengroup.osdu.schema.logging;

import static java.lang.String.format;

import com.google.common.base.Strings;
import java.util.List;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload.AuditPayloadBuilder;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {

	public static final String SCHEMA_REGISTERED_ID = "SC001";
	public static final String SCHEMA_REGISTERED_MESSAGE = "Schema registered ";

	public static final String SCHEMA_RETRIEVED_ID = "SC002";
	public static final String SCHEMA_RETRIEVED_MESSAGE = "Schema retrieved";

	public static final String SEARCH_FOR_SCHEMA_ID = "SC003";
	public static final String SEARCH_FOR_SCHEMA_MESSAGE = "Search for schema";

	public static final String SCHEMA_UPDATED_ID = "SC004";
	public static final String SCHEMA_UPDATED_MESSAGE = "Schema update";

	private static final String UNKNOWN = "unknown";
  	private static final String UNKNOWN_IP = "0.0.0.0";

	private final String user;
	private final String userIpAddress;
  	private final String userAgent;
  	private final String userAuthorizedGroupName;

	public AuditEvents(String user, String userIpAddress, String userAgent, String userAuthorizedGroupName) {
		this.user = Strings.isNullOrEmpty(user) ? UNKNOWN : user;
    	this.userIpAddress = Strings.isNullOrEmpty(userIpAddress) ? UNKNOWN_IP : userIpAddress;
    	this.userAgent = Strings.isNullOrEmpty(userAgent) ? UNKNOWN : userAgent;
    	this.userAuthorizedGroupName = Strings.isNullOrEmpty(userAuthorizedGroupName) ? UNKNOWN : userAuthorizedGroupName;
	}

	/**
	* Creates an AuditPayload builder pre-populated with common audit fields.
	*/
	private AuditPayloadBuilder createAuditPayloadBuilder(List<String> requiredGroupsForAction, AuditStatus status, String actionId) {
		return AuditPayload.builder()
			.status(status)
			.user(this.user)
			.actionId(actionId)
			.requiredGroupsForAction(requiredGroupsForAction)
			.userIpAddress(this.userIpAddress)
			.userAgent(this.userAgent)
			.userAuthorizedGroupName(this.userAuthorizedGroupName);
	}

	// Schema operations

	public AuditPayload getSchemaRegistered(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SCHEMA_REGISTERED.getRequiredGroups(), status, SCHEMA_REGISTERED_ID)
				.action(AuditAction.CREATE)
				.message(getStatusMessage(status, SCHEMA_REGISTERED_MESSAGE))
				.resources(resources)
				.build();
	}

	public AuditPayload getSchemaRetrieved(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SCHEMA_RETRIEVED.getRequiredGroups(), status, SCHEMA_RETRIEVED_ID)
				.action(AuditAction.READ)
				.message(getStatusMessage(status, SCHEMA_RETRIEVED_MESSAGE))
				.resources(resources)
				.build();
	}

	public AuditPayload getSearchForSchema(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SEARCH_FOR_SCHEMA.getRequiredGroups(), status, SEARCH_FOR_SCHEMA_ID)
				.action(AuditAction.READ)
				.message(getStatusMessage(status, SEARCH_FOR_SCHEMA_MESSAGE))
				.resources(resources)
				.build();
	}

	public AuditPayload getSchemaUpdated(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SCHEMA_UPDATED.getRequiredGroups(), status, SCHEMA_UPDATED_ID)
				.action(AuditAction.UPDATE)
				.message(getStatusMessage(status, SCHEMA_UPDATED_MESSAGE))
				.resources(resources)
				.build();
	}

	// System schema operations

	public AuditPayload getSystemSchemaRegistered(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SYSTEM_SCHEMA_REGISTERED.getRequiredGroups(), status, SCHEMA_REGISTERED_ID)
				.action(AuditAction.CREATE)
				.message(getStatusMessage(status, SCHEMA_REGISTERED_MESSAGE))
				.resources(resources)
				.build();
	}

	public AuditPayload getSystemSchemaUpdated(AuditStatus status, List<String> resources){
		return createAuditPayloadBuilder(AuditOperation.SYSTEM_SCHEMA_UPDATED.getRequiredGroups(), status, SCHEMA_UPDATED_ID)
				.action(AuditAction.UPDATE)
				.message(getStatusMessage(status, SCHEMA_UPDATED_MESSAGE))
				.resources(resources)
				.build();
	}

	private String getStatusMessage(AuditStatus status, String message) {
		return format("%s %s", message, status.name().toLowerCase());
	}
}
