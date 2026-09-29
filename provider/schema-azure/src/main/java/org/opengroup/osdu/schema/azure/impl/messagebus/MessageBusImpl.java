/*
 * Copyright 2021 Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opengroup.osdu.schema.azure.impl.messagebus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opengroup.osdu.azure.publisherFacade.MessagePublisher;
import org.opengroup.osdu.azure.publisherFacade.PublisherInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.schema.azure.di.EventGridConfig;
import org.opengroup.osdu.schema.azure.di.PubSubConfig;
import org.opengroup.osdu.schema.azure.impl.messagebus.model.SchemaPubSubInfo;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.logging.AuditLogger;
import org.opengroup.osdu.schema.provider.interfaces.messagebus.IMessageBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageBusImpl implements IMessageBus {

	@Autowired
	private JaxRsDpsLog logger;
	@Autowired
	private EventGridConfig eventGridConfig;
	@Autowired
	private AuditLogger auditLogger;
	@Autowired
	DpsHeaders headers;
	@Autowired
	private ITenantFactory tenantFactory;
	@Autowired
	private PubSubConfig pubSubConfig;
	@Autowired
	private MessagePublisher messagePublisher;

	private final static String EVENT_DATA_VERSION = "1.0";

	@Override
	public void publishMessage(String schemaId, String eventType) {

		if (eventGridConfig.isEventGridEnabled() || pubSubConfig.isServiceBusEnabled()) {
			logger.info("Generating event of type {}",eventType);
			try {
				publishSchemaEventToAzure(schemaId, eventType, headers);
				auditLogger.schemaNotificationSuccess(Collections.singletonList(schemaId));
			}catch (AppException ex) {

				//We do not want to fail schema creation if notification delivery has failed, hence just logging the exception
				auditLogger.schemaNotificationFailure(Collections.singletonList(schemaId));
				logger.warning(SchemaConstants.SCHEMA_NOTIFICATION_FAILED, ex);
			} catch(Exception e){

				logger.error(e.getMessage());
			}

		}else {
			logger.info(SchemaConstants.SCHEMA_NOTIFICATION_IS_DISABLED);
		}
	}

	/**
	 * Method to publish schema create notification for system schemas.
	 * @param schemaId
	 * @param eventType
	 */
	@Override
	public void publishMessageForSystemSchema(String schemaId, String eventType) {
		if (eventGridConfig.isEventGridEnabled() || pubSubConfig.isServiceBusEnabled()) {
			logger.info("Generating event of type {}",eventType);
			try {
				// Publish the event for all the tenants.
				List<String> privateTenantList = tenantFactory.listTenantInfo().stream().map(TenantInfo::getName)
						.collect(Collectors.toList());
				for (String tenant : privateTenantList) {
					HashMap<String, String> headersMap = new HashMap<>();
					headersMap.put(DpsHeaders.ACCOUNT_ID, tenant);
					headersMap.put(DpsHeaders.DATA_PARTITION_ID, tenant);
					headersMap.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
					DpsHeaders headers = DpsHeaders.createFromMap(headersMap);
					publishSchemaEventToAzure(schemaId, eventType, headers);

				}

				auditLogger.systemSchemaNotificationSuccess(Collections.singletonList(schemaId));
			}catch (AppException ex) {

				//We do not want to fail schema creation if notification delivery has failed, hence just logging the exception
				auditLogger.systemSchemaNotificationFailure(Collections.singletonList(schemaId));
				logger.warning(SchemaConstants.SCHEMA_NOTIFICATION_FAILED,ex);
			}catch (Exception e){

				logger.error(e.getMessage());
			}

		}else {
			logger.info(SchemaConstants.SCHEMA_NOTIFICATION_IS_DISABLED);
		}
	}

	private void publishSchemaEventToAzure(String schemaId, String eventType, DpsHeaders headers) {

		SchemaPubSubInfo[] schemaPubSubMsgs = new SchemaPubSubInfo [1];
		schemaPubSubMsgs[0]=new SchemaPubSubInfo(schemaId,eventType);


		//EventGridEvent supports array of messages to be triggered in a batch but at present we do not support
		//schema creation in bulk so generating one event at a time.
		PublisherInfo publisherInfo = PublisherInfo.builder()
				.batch(schemaPubSubMsgs)
				.eventGridTopicName(eventGridConfig.getCustomTopicName())
				.eventGridEventSubject(SchemaConstants.EVENT_SUBJECT)
				.eventGridEventType(eventType)
				.eventGridEventDataVersion(EVENT_DATA_VERSION)
				.serviceBusTopicName(pubSubConfig.getServiceBusTopic())
				.build();
		logger.info("Schema event created.");
		messagePublisher.publishMessage(headers, publisherInfo, Optional.empty());
		logger.info("Schema event triggered successfully");

	}

}
