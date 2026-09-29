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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.azure.publisherFacade.MessagePublisher;
import org.opengroup.osdu.azure.publisherFacade.PublisherInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.schema.azure.di.EventGridConfig;
import org.opengroup.osdu.schema.azure.di.PubSubConfig;
import org.opengroup.osdu.schema.azure.di.SystemResourceConfig;
import org.opengroup.osdu.schema.azure.impl.messagebus.model.SchemaPubSubInfo;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.logging.AuditLogger;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

@ExtendWith(MockitoExtension.class)
public class MessageBusImplTest {

    private static final String DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID = "data-partition-account-id";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String PARTITION_ID = "partition-id";
    private static final String OTHER_TENANT = "other-tenant-id";
    private static final String systemCosmosDBName = "osdu-system-db";
    private static final String sharedTenantId = "common";

    @Mock
    private EventGridConfig eventGridConfig;
    @Mock
    private DpsHeaders dpsHeaders;
    @Mock
    private JaxRsDpsLog logger;
    @Mock
   	private AuditLogger auditLogger;
    @Mock
    private ITenantFactory tenantFactory;
    @Mock
    SystemResourceConfig systemResourceConfig;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
	private PubSubConfig pubSubConfig;
    
    @InjectMocks
    private MessageBusImpl messageBusImpl;
    
    @Before
    public void init() throws ServiceBusException, InterruptedException {
        initMocks(this);
        doReturn(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID).when(dpsHeaders).getPartitionIdWithFallbackToAccountId();
        doReturn(PARTITION_ID).when(dpsHeaders).getPartitionId();
        doReturn(CORRELATION_ID).when(dpsHeaders).getCorrelationId();
        Mockito.when(systemResourceConfig.getCosmosDatabase()).thenReturn(systemCosmosDBName);
        Mockito.when(systemResourceConfig.getSharedTenant()).thenReturn(sharedTenantId);
    }

    @Test
    public void should_publishToEventGrid_WhenFlagIsFalse() {
    	//The schema-notification is turned off
    	when(this.eventGridConfig.isEventGridEnabled()).thenReturn(false);
    	when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(false);
        //Call publish Message
        messageBusImpl.publishMessage("dummy", "dummy");
        //Assert that eventGridTopicStore is not called even once
        verify(this.messagePublisher, times(0)).publishMessage(any(), any(), any());
    }
    @Test
    public void shouldPublishGSMMessage_ValidInput_LogsWarning() {
        //given
        when(this.eventGridConfig.isEventGridEnabled()).thenReturn(true);
        when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(false);
        //The schema-notification is turned off
        when(this.eventGridConfig.getCustomTopicName()).thenReturn("dummy-topic");

        doThrow(new RuntimeException("Some error occurred")).when(messagePublisher)
                .publishMessage(any(DpsHeaders.class), any(PublisherInfo.class), any());

        //when
        messageBusImpl.publishMessage("dummy", "dummy");

        //then
        verify(messagePublisher, times(1))
                .publishMessage(any(DpsHeaders.class), any(PublisherInfo.class), any());

        verify(logger,times(1)).error("Some error occurred");

    }

    @Test
    public void shouldPublishGSMMessage_ValidInput_PublicSchema_LogsWarning() {
        //given
        when(this.eventGridConfig.isEventGridEnabled()).thenReturn(true);
        when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(false);
        //The schema-notification is turned off
        when(this.eventGridConfig.getCustomTopicName()).thenReturn("dummy-topic");

        doThrow(new RuntimeException("Some error occurred")).when(messagePublisher)
                .publishMessage(any(DpsHeaders.class), any(PublisherInfo.class), any());

        //Call publish Message
        messageBusImpl.publishMessageForSystemSchema("dummy", "dummy");
        messageBusImpl.publishMessage("dummy", "dummy");

        //Assert that eventGridTopicStore is not called even once
        verify(this.messagePublisher, times(1)).publishMessage(any(DpsHeaders.class), any(PublisherInfo.class), any());

        verify(logger,times(1)).error("Some error occurred");
    }

    @Test
    public void should_publishToEventGrid_WhenFlagIsFalse_PublicSchemas() {
        Mockito.when(dpsHeaders.getPartitionId()).thenReturn(sharedTenantId);
        //The schema-notification is turned off
        when(this.eventGridConfig.isEventGridEnabled()).thenReturn(false);
        when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(false);
        //Call publish Message
        messageBusImpl.publishMessageForSystemSchema("dummy", "dummy");
        messageBusImpl.publishMessage("dummy", "dummy");
        //Assert that eventGridTopicStore is not called even once
        verify(this.messagePublisher, times(0)).publishMessage(any(), any(), any());
    }
    
    @Test
    public void should_publishToEventGridOnly_WhenFlagIsTrue() {
    	
    	//The schema-notification is turned on
    	when(this.eventGridConfig.isEventGridEnabled()).thenReturn(true);
    	when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(false);
    	//The schema-notification is turned off
    	when(this.eventGridConfig.getCustomTopicName()).thenReturn("dummy-topic");
    	
    	ArgumentCaptor<PublisherInfo> captorArg = ArgumentCaptor.forClass(PublisherInfo.class);
    	
    	
    	SchemaPubSubInfo[] schemaPubSubMsgs = new SchemaPubSubInfo [1];
		schemaPubSubMsgs[0]=new SchemaPubSubInfo("dummy","schema_create");
    	
		//The schema-notification is turned off
    	doNothing().when(this.messagePublisher).publishMessage(any(), any(), any());
		
		//Call publish Message
    	messageBusImpl.publishMessage("dummy", "schema_create");
        
    	//Assert that eventGridTopicStore is called once
        verify(this.messagePublisher, times(1)).publishMessage(any(), captorArg.capture(), any());
        PublisherInfo publisherInfoCaptured = captorArg.getValue();
        assertNotNull(publisherInfoCaptured);
        
        SchemaPubSubInfo schemaPubSubInfoActual = ((SchemaPubSubInfo[])publisherInfoCaptured.getBatch())[0];
        assertEquals("dummy", schemaPubSubInfoActual.getKind());
        assertEquals("schema_create", schemaPubSubInfoActual.getOp());
        
    }
    
    @Test
    public void should_publishToServiceBusOnly_WhenFlagIsTrue() {
    	
    	//The schema-notification is turned on
    	when(this.eventGridConfig.isEventGridEnabled()).thenReturn(false);
    	when(this.pubSubConfig.isServiceBusEnabled()).thenReturn(true);
    	//The schema-notification is turned off
    	when(this.eventGridConfig.getCustomTopicName()).thenReturn("dummy-topic");
    	
    	ArgumentCaptor<PublisherInfo> captorArg = ArgumentCaptor.forClass(PublisherInfo.class);
    	
    	
    	SchemaPubSubInfo[] schemaPubSubMsgs = new SchemaPubSubInfo [1];
		schemaPubSubMsgs[0]=new SchemaPubSubInfo("dummy","schema_create");
    	
		//The schema-notification is turned off
    	doNothing().when(this.messagePublisher).publishMessage(any(), any(), any());
		
		//Call publish Message
    	messageBusImpl.publishMessage("dummy", "schema_create");
        
    	//Assert that eventGridTopicStore is called once
        verify(this.messagePublisher, times(1)).publishMessage(any(), captorArg.capture(), any());
        PublisherInfo publisherInfoCaptured = captorArg.getValue();
        assertNotNull(publisherInfoCaptured);
        
        SchemaPubSubInfo schemaPubSubInfoActual = ((SchemaPubSubInfo[])publisherInfoCaptured.getBatch())[0];
        assertEquals("dummy", schemaPubSubInfoActual.getKind());
        assertEquals("schema_create", schemaPubSubInfoActual.getOp());
        
    }

}
