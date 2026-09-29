package org.opengroup.osdu.schema.logging;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.IpAddressUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
@RequestScope
public class AuditLogger {

	private final JaxRsDpsLog logger;
	private final DpsHeaders headers;
	private final HttpServletRequest httpRequest;

	private AuditEvents events = null;

	private AuditEvents getAuditEvents() {
		if (this.events == null) {
			String userIpAddress = IpAddressUtil.getClientIpAddress(this.httpRequest);
            String userAgent = httpRequest.getHeader("user-agent");
			String userAuthorizedGroupName = headers.getUserAuthorizedGroupName();

            this.events = new AuditEvents(this.headers.getUserEmail(), userIpAddress, userAgent, userAuthorizedGroupName);
		}
		return this.events;
	}

	public void schemaRegisteredSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaRegistered(AuditStatus.SUCCESS, resources));
	}
	public void schemaRegisteredFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaRegistered(AuditStatus.FAILURE, resources));
	}
	public void systemSchemaRegisteredSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaRegistered(AuditStatus.SUCCESS, resources));
	}
	public void systemSchemaRegisteredFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaRegistered(AuditStatus.FAILURE, resources));
	}
	public void schemaRetrievedSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaRetrieved(AuditStatus.SUCCESS, resources));
	}
	public void schemaRetrievedFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaRetrieved(AuditStatus.FAILURE, resources));
	}
	public void searchSchemaSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSearchForSchema(AuditStatus.SUCCESS, resources));
	}
	public void searchSchemaFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSearchForSchema(AuditStatus.FAILURE, resources));
	}
	public void schemaUpdatedSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaUpdated(AuditStatus.SUCCESS, resources));
	}
	public void schemaUpdatedFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaUpdated(AuditStatus.FAILURE, resources));
	}
	public void systemSchemaUpdatedSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaUpdated(AuditStatus.SUCCESS, resources));
	}
	public void systemSchemaUpdatedFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaUpdated(AuditStatus.FAILURE, resources));
	}
	public void schemaNotificationSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaUpdated(AuditStatus.SUCCESS, resources));
	}
	public void schemaNotificationFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSchemaUpdated(AuditStatus.FAILURE, resources));
	}
	public void systemSchemaNotificationSuccess(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaUpdated(AuditStatus.SUCCESS, resources));
	}
	public void systemSchemaNotificationFailure(List<String> resources){
		this.writeLog(this.getAuditEvents().getSystemSchemaUpdated(AuditStatus.FAILURE, resources));
	}

	private void writeLog(AuditPayload log) {
		this.logger.audit(log);
	}
}