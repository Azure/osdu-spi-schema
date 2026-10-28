/* Licensed Materials - Property of IBM              */		
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.core.ibm.multitenancy.TenantFactory;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.impl.schemastore.IBMSchemaStore;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.provider.ibm.SchemaDoc;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.views.AllDocsRequest;
import com.cloudant.client.api.views.AllDocsRequestBuilder;
import com.cloudant.client.api.views.AllDocsResponse;
import com.cloudant.client.org.lightcouch.DocumentConflictException;


@RunWith(SpringJUnit4ClassRunner.class)
public class IbmSchemaInfoStoreTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	IbmSchemaInfoStore schemaInfoStore;

	@Mock
	JaxRsDpsLog logger;

	@Mock
	IBMCloudantClientFactory cloudantFactory;

	@Mock
	Database db;

	@Mock
	SchemaDoc schemaDoc;
	
	@Mock
	SchemaInfo schemaInfo;
	
	@Mock
	SchemaRequest schemaRequest;
	
    @Mock
    TenantInfo tenantInfo;

    @Mock
    TenantInfo tenantInfoCommon;

    @Mock
    TenantFactory tenantFactory;

	@Mock
	DpsHeaders headers;
	
	@Mock
	QueryResult<SchemaDoc> queryResults;
	
	@Mock
    QueryResult queryResult;
	
	@Mock
	IBMSchemaStore schemaStore;
   
	@Mock
	TenantInfo tenant;
	
	@Mock
	AllDocsRequestBuilder request;
	
	@Mock
	AllDocsRequestBuilder allDocsRequest;
	
	@Mock
	AllDocsRequest docsRequest;
	
	@Mock
	AllDocsResponse  response;

	
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private static final String dataPartitionId = "testPartitionId";
	private static final String COMMON_TENANT_ID = "common";
	
    @Before
    public void setUp() {
    	 ReflectionTestUtils.setField(schemaInfoStore, "sharedTenant", COMMON_TENANT_ID);
    }

    @Test
    public void testGetLatestMinorVersion_ReturnNull() throws NotFoundException, ApplicationException, MalformedURLException {
    	String query = "{\"selector\": {\"$and\": [{\"schemaIdentity.authority\": {\"$eq\": \"os\"}}, {\"schemaIdentity.entityType\": {\"$eq\": \"well\"}}, {\"schemaIdentity.schemaVersionMajor\": {\"$eq\": 1}}, {\"schemaIdentity.source\": {\"$eq\": \"wks\"}}]}}";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
    	
    	Mockito.when(db.query(query, SchemaDoc.class)).thenReturn(queryResults);
        assertEquals("", schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
    }

    @Test
    public void testGetSchemaInfo_NotEmpty() throws NotFoundException, ApplicationException {
        String schemaId = "schemaId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(db.contains(schemaId)).thenReturn(true);
        Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
        Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);

        SchemaInfo schemaInfo = schemaInfoStore.getSchemaInfo(schemaId);
        assertNotNull(schemaInfo);
    }

	@Test
	public void testGetSchemaInfo_NotEmpty_SystemSchemas() throws NotFoundException, ApplicationException {
		String schemaId = "schemaId";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(db.contains(schemaId)).thenReturn(true);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);

		SchemaInfo schemaInfo = schemaInfoStore.getSystemSchemaInfo(schemaId);
		assertNotNull(schemaInfo);
	}

	@Test
    public void testGetSchemaInfo_Empty() throws NotFoundException, ApplicationException {

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
        String schemaId = "schemaId";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(db.contains(schemaId)).thenReturn(false);
        schemaInfoStore.getSchemaInfo(schemaId);
    }

	@Test
	public void testGetSchemaInfo_Empty_SystemSchemas() throws NotFoundException, ApplicationException {

		expectedException.expect(NotFoundException.class);
		expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
		String schemaId = "schemaId";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(db.contains(schemaId)).thenReturn(false);
		schemaInfoStore.getSystemSchemaInfo(schemaId);
	}
    
    @Test
    public void testCreateSchemaInfo_Positive() throws ApplicationException, BadRequestException {
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
     	Mockito.when(db.contains(anyString())).thenReturn(false);
         Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
        assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published()));
    }

	@Test
	public void testCreateSchemaInfo_Positive_SystemSchemas() throws ApplicationException, BadRequestException {
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(db.contains(anyString())).thenReturn(false);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		assertNotNull(schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published()));
	}


  	@Test
    public void testIsUnique_True() throws ApplicationException, MalformedURLException {

        String schemaId = "schemaId";
        String tenantId = "tenant";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
        Mockito.when(db.contains(anyString())).thenReturn(false);
        assertTrue(schemaInfoStore.isUnique(schemaId, tenantId));
    }

	@Test
	public void testIsUnique_True_SystemSchemas() throws ApplicationException, MalformedURLException {
		String schemaId = "schemaId";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.contains(anyString())).thenReturn(false);
		assertTrue(schemaInfoStore.isUniqueSystemSchema(schemaId));
	}

    @Test
    public void testIsUnique_False() throws ApplicationException, MalformedURLException {
    	 String schemaId = "schemaId";
         String tenantId = "tenant";
         Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
         Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
         Mockito.when(db.contains(anyString())).thenReturn(true);
         assertFalse(schemaInfoStore.isUnique(schemaId, tenantId));
    }

    @Test
    public void testIsUnique_False_CommomTenant() throws ApplicationException, MalformedURLException {
        String schemaId = "schemaId";
        String tenantId = "common";
        Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        Mockito.when(tenantFactory.getTenantInfo("tenant")).thenReturn(tenantInfo);
        Mockito.when(tenantFactory.getTenantInfo("common")).thenReturn(tenantInfoCommon);
        Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
        Mockito.when(db.contains(anyString())).thenReturn(true);
        assertFalse(schemaInfoStore.isUnique(schemaId, tenantId));
    }

	@Test
	public void testIsUnique_False_CommomTenant_SystemSchemas() throws ApplicationException, MalformedURLException {
		String schemaId = "schemaId";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(tenantFactory.getTenantInfo("tenant")).thenReturn(tenantInfo);
		Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfoCommon);
		Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.contains(anyString())).thenReturn(true);
		assertFalse(schemaInfoStore.isUniqueSystemSchema(schemaId));
	}


	@Test
	public void testUpdateSchemaInfo() throws NotFoundException, ApplicationException, BadRequestException {
		String schemaId = "os:wks:well.1.1.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		assertNotNull(schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published()));
	}

	@Test
	public void testUpdateSchemaInfo_SystemSchemas() throws NotFoundException, ApplicationException, BadRequestException {
		String schemaId = "os:wks:well.1.1.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		assertNotNull(schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_Published()));
	}

	@Test
	public void testCreateSchemaInfo_WithSupersededBy()
			throws NotFoundException, ApplicationException, BadRequestException {
		String schemaId = "os:wks:well.1.1.1";
		String superSededId = "os:wks:well.1.2.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
		Mockito.when(db.contains(schemaId)).thenReturn(false);
		Mockito.when(db.contains(superSededId)).thenReturn(true);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_SuperSededBy()));
	}

	@Test
	public void testCreateSchemaInfo_WithSupersededBy_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {
		String schemaId = "os:wks:well.1.1.1";
		String superSededId = "os:wks:well.1.2.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
		Mockito.when(db.contains(schemaId)).thenReturn(false);
		Mockito.when(db.contains(superSededId)).thenReturn(true);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		assertNotNull(schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_SuperSededBy()));
	}

    @Test
    public void testUpdateSchemaInfo_SupersededByException()
            throws NotFoundException, ApplicationException, BadRequestException {
        try {
        	
        	String schemaId = "os:wks:well.1.1.1";
        	String superSededId = "os:wks:well.1.2.1";
    		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    		Mockito.when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
    		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
    		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
    		Mockito.when(db.contains(superSededId)).thenReturn(false);
    		
            schemaInfoStore.updateSchemaInfo(getMockSchemaObject_SuperSededBy());
            fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals("Invalid SuperSededBy id", e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

	@Test
	public void testUpdateSchemaInfo_SupersededByException_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {
		try {

			String schemaId = "os:wks:well.1.1.1";
			String superSededId = "os:wks:well.1.2.1";
			Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
			Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
			Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
			Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
			Mockito.when(db.contains(superSededId)).thenReturn(false);

			schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_SuperSededBy());
			fail("Should not succeed");
		} catch (BadRequestException e) {
			assertEquals("Invalid SuperSededBy id", e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

    @Test
    public void testUpdateInfo_SupersededByWithoutIdException()
            throws NotFoundException, ApplicationException, BadRequestException {
    	String schemaId = "os:wks:well.1.1.1";
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		
        SchemaRequest schemaRequest = getMockSchemaObject_SuperSededByWithoutId();
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage(SchemaConstants.INVALID_SUPERSEDEDBY_ID);
        schemaInfoStore.updateSchemaInfo(schemaRequest);
    }

	@Test
	public void testUpdateInfo_SupersededByWithoutIdException_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {
		String schemaId = "os:wks:well.1.1.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);

		SchemaRequest schemaRequest = getMockSchemaObject_SuperSededByWithoutId();
		expectedException.expect(BadRequestException.class);
		expectedException.expectMessage(SchemaConstants.INVALID_SUPERSEDEDBY_ID);
		schemaInfoStore.updateSystemSchemaInfo(schemaRequest);
	}

	@Test
	public void testCreateSchemaInfo_BadRequestException()
			throws NotFoundException, ApplicationException, BadRequestException {

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(db.contains(anyString())).thenReturn(true);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);

		try {
			schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published());
			fail("Should not succeed");
		} catch (BadRequestException e) {
			assertEquals("Schema os:wks:well.1.1.1 already exist. Can't create again.", e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

	@Test
	public void testCreateSchemaInfo_BadRequestException_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(db.contains(anyString())).thenReturn(true);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);

		try {
			schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published());
			fail("Should not succeed");
		} catch (BadRequestException e) {
			assertEquals("Schema os:wks:well.1.1.1 already exist. Can't create again.", e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

	@Test
	public void testCreateSchemaInfo_ApplicationException()
			throws NotFoundException, ApplicationException, BadRequestException {

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(db.contains(anyString())).thenReturn(false);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		Mockito.when(db.save(any(SchemaDoc.class))).thenThrow(DocumentConflictException.class);

		try {
			schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published());
			fail("Should not succeed");
		} catch (ApplicationException e) {
			assertEquals(SchemaConstants.SCHEMA_CREATION_FAILED_INVALID_OBJECT, e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

	@Test
	public void testCreateSchemaInfo_ApplicationException_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(db.contains(anyString())).thenReturn(false);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		Mockito.when(db.save(any(SchemaDoc.class))).thenThrow(DocumentConflictException.class);

		try {
			schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published());
			fail("Should not succeed");
		} catch (ApplicationException e) {
			assertEquals(SchemaConstants.SCHEMA_CREATION_FAILED_INVALID_OBJECT, e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

    @Test
    public void testUpdateSchemaInfo_ApplicationException()
            throws NotFoundException, ApplicationException, BadRequestException {

    	String schemaId = "os:wks:well.1.1.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		Mockito.when(db.update(any(SchemaDoc.class))).thenThrow(DocumentConflictException.class);
		
        try {
            schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published());
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals("Invalid object, updation failed", e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

	@Test
	public void testUpdateSchemaInfo_ApplicationException_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException {

		String schemaId = "os:wks:well.1.1.1";
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		Mockito.when(schemaDoc.getSchemaInfo()).thenReturn(schemaInfo);
		Mockito.when(db.update(any(SchemaDoc.class))).thenThrow(DocumentConflictException.class);

		try {
			schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_Published());
			fail("Should not succeed");
		} catch (ApplicationException e) {
			assertEquals("Invalid object, updation failed", e.getMessage());

		} catch (Exception e) {
			fail("Should not get different exception");
		}
	}

	@Test
	public void testGetLatestMinorVersion_Entity() throws NotFoundException, ApplicationException {
		List<SchemaDoc> schemaDocsList = new LinkedList<>();
		schemaDocsList.add(getMockSchemaDocObject());
		Mockito.when(db.query(Mockito.any(), Mockito.any())).thenReturn(queryResult);
		Mockito.when(queryResult.getDocs()).thenReturn(schemaDocsList);
		Mockito.when(schemaStore.getSchema(any(), anyString())).thenReturn("{}");
		assertEquals("{}", schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
	}

    @Test
    public void testGetSchemaInfoList_withoutqueryparam()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {
        List<SchemaDoc> schemaDocsList = new LinkedList<>();
        schemaDocsList.add(getMockSchemaDocObject());
    	Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        assertEquals(1,
                schemaInfoStore.getSchemaInfoList(QueryParams.builder().limit(100).offset(0).build(), "test").size());
    }

	@Test
	public void testGetSchemaInfoList_withoutqueryparam_SystemSchemas()
			throws NotFoundException, ApplicationException, BadRequestException, IOException {
		List<SchemaDoc> schemaDocsList = new LinkedList<>();
		schemaDocsList.add(getMockSchemaDocObject());
		Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
		assertEquals(1,
				schemaInfoStore.getSystemSchemaInfoList(QueryParams.builder().limit(100).offset(0).build()).size());
	}

    @Test
    public void testGetSchemaInfoList_withqueryparam()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {
    	List<SchemaDoc> schemaDocsList = new LinkedList<>();
    	schemaDocsList.add(getMockSchemaDocObject());
    	Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        assertEquals(0,
                schemaInfoStore.getSchemaInfoList(QueryParams.builder().authority("test").source("test").entityType("test")
                        .schemaVersionMajor(1l).schemaVersionMinor(1l).scope("test").status("test").latestVersion(false)
                        .limit(100).offset(0).build(), "test").size());
    }

    @Test
    public void testGetSchemaInfoList_latestVersionTrue_NoSchemaMatchScenario()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {
    	List<SchemaDoc> schemaDocsList = new LinkedList<>();
    	Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        assertEquals(0,
                schemaInfoStore
                        .getSchemaInfoList(QueryParams.builder().authority("test").source("test").entityType("test")
                                .scope("test").status("test").latestVersion(true).limit(100).offset(0).build(), "test")
                        .size());
    }

    @Test
    public void testGetSchemaInfoList_LatestVersionFunctionalityTest_SchemasWithDifferentMajorVersion()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {
    	SchemaIdentity schemaIdentity = new SchemaIdentity("authority","source","entityType",2L,11L,111L,"id");
		SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
		SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
		SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
		SchemaDoc latestSchemaDoc =  getMockSchemaDocObject();
		List<SchemaDoc> schemaDocsList = new LinkedList<>();
		schemaDocsList.add(schemaDoc);
		schemaDocsList.add(latestSchemaDoc);
		Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        assertEquals(0, schemaInfoStore.getSchemaInfoList(
                QueryParams.builder().scope("test").status("test").latestVersion(true).limit(100).offset(0).build(),
                "test").size());
    }

    @Test
    public void testGetSchemaInfoList_LatestVersionFunctionalityTest_SchemasWithDifferentMinorVersion()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {
	    SchemaIdentity schemaIdentity = new SchemaIdentity("authority","source","entityType",1L,22L,111L,"id");
		SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
		SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
		SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
		SchemaDoc latestSchemaDoc =  getMockSchemaDocObject();
		List<SchemaDoc> schemaDocsList = new LinkedList<>();
		schemaDocsList.add(schemaDoc);
		schemaDocsList.add(latestSchemaDoc);
		Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        assertEquals(0, schemaInfoStore.getSchemaInfoList(
                QueryParams.builder().scope("test").status("test").latestVersion(true).limit(100).offset(0).build(),
                "test").size());
    }

    @Test
    public void testGetSchemaInfoList_LatestVersionFunctionalityTest_SchemasWithDifferentSource()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {

    	SchemaIdentity schemaIdentity = new SchemaIdentity("authority","sourceChanged","entityType",1L,11L,111L,"id");
    	SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
    	SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
    	SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
    	SchemaDoc latestSchemaDoc =  getMockSchemaDocObject();
    	List<SchemaDoc> schemaDocsList = new LinkedList<>();
    	schemaDocsList.add(schemaDoc);
    	schemaDocsList.add(latestSchemaDoc);
    	Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
    	
    	assertEquals(0, schemaInfoStore.getSchemaInfoList(
            QueryParams.builder().scope("test").status("test").latestVersion(true).limit(100).offset(0).build(),
            "test").size());
    }

    @Test
    public void testGetSchemaInfoList_LatestVersionFunctionalityTest_SchemasWithDifferentAuthority()
            throws NotFoundException, ApplicationException, BadRequestException, IOException {

    	SchemaIdentity schemaIdentity = new SchemaIdentity("authorityChanged","source","entityType",1L,11L,111L,"id");
    	SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
    	SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
    	SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
    	SchemaDoc latestSchemaDoc =  getMockSchemaDocObject();
    	List<SchemaDoc> schemaDocsList = new LinkedList<>();
    	schemaDocsList.add(schemaDoc);
    	schemaDocsList.add(latestSchemaDoc);
    	Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
    	Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
        Mockito.when(tenant.getName()).thenReturn(dataPartitionId);
        
        assertEquals(0, schemaInfoStore.getSchemaInfoList(
                QueryParams.builder().scope("test").status("test").latestVersion(true).limit(100).offset(0).build(),
                "test").size());
    }

  @Test
  public void testGetSchemaInfoList_LatestVersionFunctionalityTest_SchemasWithDifferentEntity()
          throws NotFoundException, ApplicationException, BadRequestException, IOException {
	  SchemaIdentity schemaIdentity = new SchemaIdentity("authority","source","entityTypeChanged",1L,11L,111L,"id");
	  SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
	  SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
	  
	  
	  SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
	  SchemaDoc latestSchemaDoc =  getMockSchemaDocObject();
	  List<SchemaDoc> schemaDocsList = new LinkedList<>();
	  schemaDocsList.add(schemaDoc);
	  schemaDocsList.add(latestSchemaDoc);
	  Mockito.when(cloudantFactory.getDatabase(any(),anyString())).thenReturn(db);
		Mockito.when(db.getAllDocsRequestBuilder()).thenReturn(request);
		Mockito.when(request.includeDocs(true)).thenReturn(allDocsRequest);
		Mockito.when(allDocsRequest.build()).thenReturn(docsRequest);
		Mockito.when(docsRequest.getResponse()).thenReturn(response);
		Mockito.when(response.getDocsAs(SchemaDoc.class)).thenReturn(schemaDocsList);
      
      assertEquals(0, schemaInfoStore.getSchemaInfoList(
              QueryParams.builder().scope("test").status("test").latestVersion(true).limit(100).offset(0).build(),
              "test").size());
  }


    @Test
    public void testCleanSchema_Success_SystemSchemas() throws ApplicationException, MalformedURLException {
        String schemaId = "schemaId";
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(schemaId)).thenReturn(true);
        Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
        assertEquals(true, schemaInfoStore.cleanSystemSchema(schemaId));
    }

	@Test
	public void testCleanSchema_Success() throws ApplicationException, MalformedURLException {
		String schemaId = "schemaId";
		Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
		Mockito.when(db.contains(schemaId)).thenReturn(true);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		assertEquals(true, schemaInfoStore.cleanSchema(schemaId));
	}

    @Test
    public void testCleanSchema_Failure() throws ApplicationException, MalformedURLException {
        String schemaId = "schemaId";
        Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
        Mockito.when(db.contains(schemaId)).thenReturn(false);
        Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
        assertEquals(false, schemaInfoStore.cleanSchema(schemaId));
    }

	@Test
	public void testCleanSchema_Failure_SystemSchemas() throws ApplicationException, MalformedURLException {
		String schemaId = "schemaId";
		Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
		Mockito.when(db.contains(schemaId)).thenReturn(false);
		Mockito.when(db.find(SchemaDoc.class, schemaId)).thenReturn(schemaDoc);
		assertEquals(false, schemaInfoStore.cleanSystemSchema(schemaId));
	}

    private SchemaRequest getMockSchemaObject_Published() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(SchemaInfo.builder()
                        .schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                                .id("os:wks:well.1.1.1").build())
                        .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("ibm").build())
                .build();

    }

    private SchemaInfo getMockSchemaInfo() {
        return SchemaInfo.builder().schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L).id("os:wks:well.1.1.1").build())
                .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("ibm")

                .build();

    }

    private SchemaRequest getMockSchemaObject_SuperSededBy() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(SchemaInfo.builder()
                        .schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                                .id("os:wks:well.1.1.1").build())
                        .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("ibm")
                        .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(2L)
                                .id("os:wks:well.1.2.1").build())
                        .build())
                .build();

    }

    private SchemaRequest getMockSchemaObject_SuperSededByWithoutId() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(SchemaInfo.builder()
                        .schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                                .id("os:wks:well.1.1.1").build())
                        .scope(SchemaScope.SHARED).status(SchemaStatus.DEVELOPMENT).createdBy("ibm")
                        .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L).build())
                        .build())
                .build();

    }

    private SchemaDoc getMockSchemaDocObject() 
    {
		SchemaIdentity schemaIdentity = new SchemaIdentity("authority","source","entityType",1L,11L,111L,"id");
		SchemaInfo schemaInfo = new SchemaInfo(schemaIdentity,"createdBy", new Date(), SchemaStatus.DEVELOPMENT, SchemaScope.INTERNAL, schemaIdentity);
		SchemaRequest schemaRequest = new SchemaRequest(schemaInfo, new Object());
		SchemaDoc schemaDoc = new SchemaDoc(schemaRequest.getSchemaInfo());
		
	    return schemaDoc;
    }

}
