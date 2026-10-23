/* Licensed Materials - Property of IBM              */		
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import java.net.MalformedURLException;
import org.opengroup.osdu.schema.model.Source;
import org.opengroup.osdu.schema.provider.ibm.SourceDoc;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class IbmSourceStoreTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    IbmSourceStore sourceStore;
    
    @Mock
    IBMCloudantClientFactory cloudantFactory;

    @Mock
    Database db;

    @Mock
    Source mockSource;

    @Mock
    SourceDoc mockSourceDoc;


    @Mock
    DpsHeaders headers;

    @Mock
	protected JaxRsDpsLog logger;

	private static final String dataPartitionId = "testPartitionId";
    private static final String COMMON_TENANT_ID = "common";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(sourceStore, "sharedTenant", COMMON_TENANT_ID);
    }
	
    @Test
    public void testGet() throws NotFoundException, ApplicationException, MalformedURLException {
        String sourceId = "testSourceId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(sourceId)).thenReturn(true);
        Mockito.when(db.find(SourceDoc.class, sourceId)).thenReturn(mockSourceDoc);
        Mockito.when(mockSourceDoc.getSource()).thenReturn(mockSource);
        assertNotNull(sourceStore.get(sourceId));
    }

    @Test
    public void testGet_SystemSchemas() throws NotFoundException, ApplicationException, MalformedURLException {
        String sourceId = "testSourceId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(sourceId)).thenReturn(true);
        Mockito.when(db.find(SourceDoc.class, sourceId)).thenReturn(mockSourceDoc);
        Mockito.when(mockSourceDoc.getSource()).thenReturn(mockSource);
        assertNotNull(sourceStore.getSystemSource(sourceId));
    }

    @Test
    public void testGet_NotFoundException() {
        String sourceId = "testSourceId";
        try {
            Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
            Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        	Mockito.when(db.contains(sourceId)).thenReturn(false);
            sourceStore.get(sourceId);
            fail("Should not succeed");
        } catch (NotFoundException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testGet_NotFoundException_SystemSchemas() {
        String sourceId = "testSourceId";
        try {
            Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
            Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
            Mockito.when(db.contains(sourceId)).thenReturn(false);
            sourceStore.getSystemSource(sourceId);
            fail("Should not succeed");
        } catch (NotFoundException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreateSource() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
     	Mockito.when(mockSourceDoc.getSource()).thenReturn(mockSource);
        assertNotNull(sourceStore.create(mockSource));
    }

    @Test
    public void testCreateSource_SystemSchemas() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(anyString())).thenReturn(false);
        Mockito.when(mockSourceDoc.getSource()).thenReturn(mockSource);
        assertNotNull(sourceStore.createSystemSource(mockSource));
    }

    @Test
    public void testCreate_BadRequestException() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String sourceId = "testSourceId";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	sourceStore = Mockito.spy(sourceStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(true);
     	Mockito.when(mockSource.getSourceId()).thenReturn(sourceId);

        try {
        	sourceStore.create(mockSource);
		        fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals("Source already registered with Id: "+sourceId, e.getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreate_BadRequestException_SystemSchemas() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
        String sourceId = "testSourceId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
        sourceStore = Mockito.spy(sourceStore);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(anyString())).thenReturn(true);
        Mockito.when(mockSource.getSourceId()).thenReturn(sourceId);

        try {
            sourceStore.createSystemSource(mockSource);
            fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals("Source already registered with Id: "+sourceId, e.getMessage());
        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreate_ApplicationException() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String sourceId = "testSourceId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        sourceStore = Mockito.spy(sourceStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
     	Mockito.when(db.save(any(SourceDoc.class))).thenThrow(DocumentConflictException.class);
     	Mockito.when(mockSource.getSourceId()).thenReturn(sourceId);

        try {
        	sourceStore.create(mockSource);
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreate_ApplicationException_SystemSchemas() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
        String sourceId = "testSourceId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
        sourceStore = Mockito.spy(sourceStore);
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(anyString())).thenReturn(false);
        Mockito.when(db.save(any(SourceDoc.class))).thenThrow(DocumentConflictException.class);
        Mockito.when(mockSource.getSourceId()).thenReturn(sourceId);

        try {
            sourceStore.createSystemSource(mockSource);
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }
}
