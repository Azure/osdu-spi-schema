/* Licensed Materials - Property of IBM              */		
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.schema.impl.schemainfostore;

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
//import org.mockito.Mockito;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.schema.model.EntityType;
import org.opengroup.osdu.schema.provider.ibm.EntityTypeDoc;
import org.opengroup.osdu.schema.constants.SchemaConstants;
//import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
//import org.opengroup.osdu.core.ibm.multitenancy.TenantFactory;
//import org.opengroup.osdu.schema.constants.SchemaConstants;
//import org.opengroup.osdu.schema.credentials.DatastoreFactory;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.Authority;
import org.opengroup.osdu.schema.provider.ibm.AuthorityDoc;

import java.net.MalformedURLException;
//import org.opengroup.osdu.schema.model.EntityType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class IbmEntityTypeStoreTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
	IbmEntityTypeStore mockIbmEntityStore;
    
    @Mock
    IBMCloudantClientFactory cloudantFactory;
    
    @Mock
    Database db;

    @Mock
    EntityType mockEntityType;
    
    @Mock
    EntityTypeDoc mockEntityTypeDoc;

    @Mock
    DpsHeaders headers;

    @Mock
	protected JaxRsDpsLog logger;
    
    private static final String dataPartitionId = "testPartitionId";
    private static final String COMMON_TENANT_ID = "common";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(mockIbmEntityStore, "sharedTenant", COMMON_TENANT_ID);
    }
    
    @Test
    public void testGet() throws NotFoundException, ApplicationException, MalformedURLException {
        String entityId = "testEntityId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(entityId)).thenReturn(true);
        Mockito.when(db.find(EntityTypeDoc.class, entityId)).thenReturn(mockEntityTypeDoc);
        Mockito.when(mockEntityTypeDoc.getEntityType()).thenReturn(mockEntityType);
        assertNotNull(mockIbmEntityStore.get(entityId));
    }

    @Test
    public void testGet_NotFoundException() {
        String entityId = "testEntityId";
        try {
            Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
            Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        	Mockito.when(db.contains(entityId)).thenReturn(false);
        	mockIbmEntityStore.get(entityId);
            fail("Should not succeed");
        } catch (NotFoundException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreateEntityType() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
     	Mockito.when(mockEntityTypeDoc.getEntityType()).thenReturn(mockEntityType);
     	
        assertNotNull(mockIbmEntityStore.create(mockEntityType));
    }

    @Test
    public void testCreate_BadRequestException() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String entityId = "testEntityId";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	mockIbmEntityStore = Mockito.spy(mockIbmEntityStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(true);
     	Mockito.when(mockEntityType.getEntityTypeId()).thenReturn(entityId);

        try {
     			mockIbmEntityStore.create(mockEntityType);
		        fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals("EntityType already registered with Id: "+entityId, e.getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreate_ApplicationException() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String entityId = "testEntityId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        mockIbmEntityStore = Mockito.spy(mockIbmEntityStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
     	Mockito.when(db.save(any(EntityTypeDoc.class))).thenThrow(DocumentConflictException.class);
     	Mockito.when(mockEntityType.getEntityTypeId()).thenReturn(entityId);

        try {
        	mockIbmEntityStore.create(mockEntityType);
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

}
