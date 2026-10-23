/* Licensed Materials - Property of IBM              */		
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;

import java.net.MalformedURLException;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.Authority;
import org.opengroup.osdu.schema.provider.ibm.AuthorityDoc;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class IbmAuthorityStoreTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    IbmAuthorityStore mockIbmAuthorityStore;

    @Mock
	protected JaxRsDpsLog logger;
	
    @Mock
    IBMCloudantClientFactory cloudantFactory;
    
    @Mock
    Database db;

    @Mock
    Authority mockAuthority;
    
    @Mock
    AuthorityDoc mockAuthorityDoc;

    @Mock
    DpsHeaders headers;

    private static final String dataPartitionId = "testPartitionId";
    private static final String COMMON_TENANT_ID = "common";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(mockIbmAuthorityStore, "sharedTenant", COMMON_TENANT_ID);
    }
    
    @Test
    public void testGetAuthority() throws NotFoundException, ApplicationException, MalformedURLException {
        String authorityId = "testAuthorityId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
    	Mockito.when(db.contains(authorityId)).thenReturn(true);
    	Mockito.when(db.find(AuthorityDoc.class, authorityId)).thenReturn(mockAuthorityDoc);
    	Mockito.when(mockAuthorityDoc.getAuthority()).thenReturn(mockAuthority);
        assertNotNull(mockIbmAuthorityStore.get(authorityId));
    }

    @Test
    public void testGetAuthority_NotFoundException() {
    	String authorityId = "testAuthorityId";
        try {
        	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        	Mockito.when(db.contains(authorityId)).thenReturn(false);
            
            mockIbmAuthorityStore.get(authorityId);
            fail("Should not succeed");
        } catch (NotFoundException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreateAuthority() throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
     	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
     	Mockito.when(mockAuthorityDoc.getAuthority()).thenReturn(mockAuthority);
     	 
        assertNotNull(mockIbmAuthorityStore.create(mockAuthority));
    }

    @Test
    public void testCreateAuthority_BadRequestException()
            throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String authorityId = "testAuthorityId";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	mockIbmAuthorityStore = Mockito.spy(mockIbmAuthorityStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
     	Mockito.when(db.contains(anyString())).thenReturn(true);

        Mockito.when(mockAuthority.getAuthorityId()).thenReturn(authorityId);
        try {
        	mockIbmAuthorityStore.create(mockAuthority);
            fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals("Authority already registered with Id: "+authorityId, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreateAuthority_ApplicationException()
            throws NotFoundException, ApplicationException, BadRequestException, MalformedURLException {
    	String authorityId = "testAuthorityId";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	mockIbmAuthorityStore = Mockito.spy(mockIbmAuthorityStore);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
    	Mockito.when(db.contains(anyString())).thenReturn(false);
    	Mockito.when(db.save(any(AuthorityDoc.class))).thenThrow(DocumentConflictException.class);
    	Mockito.when(mockAuthority.getAuthorityId()).thenReturn(authorityId);
        try {
        	mockIbmAuthorityStore.create(mockAuthority);
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

}
