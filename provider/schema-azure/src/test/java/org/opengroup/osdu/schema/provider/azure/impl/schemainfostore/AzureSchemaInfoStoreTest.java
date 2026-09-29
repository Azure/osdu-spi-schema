// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.schema.provider.azure.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.opengroup.osdu.schema.constants.SchemaConstants.INTERNAL_SERVER_ERROR;
import static org.opengroup.osdu.schema.constants.SchemaConstants.INVALID_SUPERSEDEDBY_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.schema.azure.definitions.FlattenedSchemaInfo;
import org.opengroup.osdu.schema.azure.definitions.SchemaInfoDoc;
import org.opengroup.osdu.schema.azure.di.SystemResourceConfig;
import org.opengroup.osdu.schema.azure.impl.schemainfostore.AzureSchemaInfoStore;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

public class AzureSchemaInfoStoreTest {
    @Mock
    private CosmosStore cosmosStore;

    @Mock
    private ITenantFactory tenantFactory;

    @InjectMocks
    private AzureSchemaInfoStore schemaInfoStore;

    @Mock
    JaxRsDpsLog logger;

    @Mock
    SchemaInfoDoc schemaInfoDoc;

    @Mock
    SchemaInfo schemaInfo;

    @Mock
    SchemaRequest schemaRequest;

    @Mock
    DpsHeaders headers;

    @Mock
    FlattenedSchemaInfo flattenedSchemaInfo;

    @Mock
    SystemResourceConfig systemResourceConfig;

    private static final String dataPartitionId = "testPartitionId";
    private static final String partitionKey = "os:wks:well:1";
    private static final String CONTENT = "Hello World";
    private static final String schemaId = "os:wks:well:1.1.1";
    private static final String supersedingSchemaId = "os:wks:well:1.2.1";
    private static final String commonTenantId = "common";
    private static final String systemCosmosDBName = "osdu-system-db";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
        initMocks(this);
        ReflectionTestUtils.setField(schemaInfoStore, "sharedTenant", commonTenantId);
        doReturn(dataPartitionId).when(headers).getPartitionId();
        Mockito.when(systemResourceConfig.getCosmosDatabase()).thenReturn(systemCosmosDBName);
        Mockito.when(systemResourceConfig.getSharedTenant()).thenReturn(commonTenantId);
    }

    @Test
    public void testGetLatestMinorVersion_ReturnNull() throws NotFoundException, ApplicationException, IOException {

        List<SchemaInfoDoc> cosmosItem = new ArrayList<>();
        doReturn(cosmosItem)
                .when(cosmosStore)
                .queryItems(
                        eq(dataPartitionId),
                        anyString(),
                        anyString(),
                        any(),
                        any(),
                        any());
        assertEquals("", schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
    }

    @Test
    public void testGetLatestMinorVersion_Entity() throws NotFoundException, ApplicationException {
        List<SchemaInfoDoc> schemaInfoDocsList = new LinkedList<>();
        schemaInfoDocsList.add(getMockSchemaInfoDoc());
        doReturn(schemaInfoDocsList)
                .when(cosmosStore)
                .queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());

        assertEquals(CONTENT, schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
    }

    @Test
    public void testGetSchemaInfo_NotEmpty() throws NotFoundException, ApplicationException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());

        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();
        SchemaInfo schemaInfo = schemaInfoStore.getSchemaInfo(schemaId);
        assertNotNull(schemaInfo);
    }

    @Test
    public void testGetSchemaInfo_NotEmpty_PublicSchemas() throws NotFoundException, ApplicationException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(systemCosmosDBName),
                        any(),
                        eq(schemaId),
                        eq(partitionKey),
                        any());

        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        SchemaInfo schemaInfo1 = schemaInfoStore.getSystemSchemaInfo(schemaId);
        assertNotNull(schemaInfo1);

        verify(this.cosmosStore, times(1)).findItem(any(), any(), eq("os:wks:well:1.1.1"), eq("os:wks:well:1"), eq(SchemaInfoDoc.class));
        verify(this.cosmosStore, times(0)).findItem(anyString(), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testGetSchemaInfo_Empty() throws NotFoundException, ApplicationException {
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);

        Optional<SchemaInfoDoc> cosmosItem = Optional.empty();
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        schemaInfoStore.getSchemaInfo(schemaId);
    }

    @Test
    public void testCreateSchemaInfo_Positive() throws ApplicationException, BadRequestException {
        // the schema is not present in schemaInfoStore
        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(dataPartitionId),
                        any());
        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published()));
    }

    @Test
    public void testCreateSchemaInfo_Positive_PublicSchemas() throws ApplicationException, BadRequestException {
        // the schema is not present in schemaInfoStore
        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(systemCosmosDBName),
                        any(),
                        eq(schemaId),
                        anyString(),
                        any());
        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        assertNotNull(schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published()));

        verify(this.cosmosStore, times(1)).createItem(any(), any(), eq("os:wks:well:1"), any());
        verify(this.cosmosStore, times(0)).createItem(anyString(), anyString(), anyString(), anyString(), any());
        verify(this.cosmosStore, times(0)).findItem(any(), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testCreateSchemaInfo_WithSupersededBy()
            throws NotFoundException, ApplicationException, BadRequestException {
        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);

        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + supersedingSchemaId),
                        eq(partitionKey),
                        any());
        doReturn(getFlattenedSchemaInfo_SupersededBy()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_SupersededBy()));
    }

    @Test
    public void testCreateSchemaInfo_BadRequestException()
            throws NotFoundException, ApplicationException, BadRequestException {

        AppException exception = getMockAppException(409);
        doThrow(exception).when(cosmosStore).createItem(eq(dataPartitionId), any(), any(), any(), any());

        try {
            schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published());
            fail("Should not succeed");
        } catch (BadRequestException e) {
            assertEquals(SchemaConstants.SCHEMA_ID_EXISTS, e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testCreateSchemaInfo_ApplicationException()
            throws NotFoundException, ApplicationException, BadRequestException {

        AppException exception = getMockAppException(500);
        doThrow(exception).when(cosmosStore).createItem(eq(dataPartitionId), any(), any(), any(), any());

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
    public void testIsUnique_ApplicationException() throws ApplicationException {
        TenantInfo tenant1 = new TenantInfo();
        tenant1.setName(commonTenantId);
        tenant1.setDataPartitionId(commonTenantId);
        TenantInfo tenant2 = new TenantInfo();
        tenant2.setName(dataPartitionId);
        tenant2.setDataPartitionId(dataPartitionId);
        Collection<TenantInfo> tenants = Lists.newArrayList(tenant1, tenant2);
        when(this.tenantFactory.listTenantInfo()).thenReturn(tenants);
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);

        // An error is encountered while checking uniqueness in one tenant.
        doThrow(AppException.class)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertTrue(schemaInfoStore.isUnique(schemaId, commonTenantId));
    }

    @Test
    public void testIsUnique_True() throws ApplicationException {

        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertTrue(schemaInfoStore.isUnique(schemaId, dataPartitionId));
    }

    @Test
    public void testIsUnique_False() throws ApplicationException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertFalse(schemaInfoStore.isUnique(schemaId, dataPartitionId));
    }

    @Test
    public void testIsUnique_False_PublicSchemas() throws ApplicationException {
        Mockito.when(headers.getPartitionId()).thenReturn(commonTenantId);
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(systemCosmosDBName),
                        any(),
                        eq(schemaId),
                        eq(partitionKey),
                        any());

        assertFalse(schemaInfoStore.isUniqueSystemSchema(schemaId));

        // This is temporary and will be removed once schema-core starts consuming *system* methods
        assertFalse(schemaInfoStore.isUnique(schemaId, commonTenantId));

        verify(this.cosmosStore, times(2)).findItem(any(), any(), anyString(), anyString(), any());
        verify(this.cosmosStore, times(0)).findItem(anyString(), anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testIsUnique_False_CommomTenant() throws ApplicationException {
        TenantInfo tenant1 = new TenantInfo();
        tenant1.setName(commonTenantId);
        tenant1.setDataPartitionId(commonTenantId);
        TenantInfo tenant2 = new TenantInfo();
        tenant2.setName(dataPartitionId);
        tenant2.setDataPartitionId(dataPartitionId);
        Collection<TenantInfo> tenants = Lists.newArrayList(tenant1, tenant2);
        when(this.tenantFactory.listTenantInfo()).thenReturn(tenants);
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertFalse(schemaInfoStore.isUniqueSystemSchema(schemaId));
    }

    @Test
    public void testUpdateSchemaInfo() throws NotFoundException, ApplicationException, BadRequestException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + supersedingSchemaId),
                        eq(partitionKey),
                        any());

        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();
        assertNotNull(schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published()));
    }

    @Test
    public void testUpdateSchemaInfo_PublicSchemas() throws NotFoundException, ApplicationException, BadRequestException {
        Mockito.when(headers.getPartitionId()).thenReturn(commonTenantId);
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(systemCosmosDBName),
                        any(),
                        eq(supersedingSchemaId),
                        eq(partitionKey),
                        any());
        doNothing().when(cosmosStore)
                .upsertItem(
                        eq(systemCosmosDBName),
                        any(),
                        any(),
                        any());

        doReturn(getFlattenedSchemaInfo()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        assertNotNull(schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_Published()));

        verify(this.cosmosStore, times(1)).upsertItem(any(), any(), any(), any());
        verify(this.cosmosStore, times(0)).upsertItem(any(), any(), any(), any(), any());
        verify(this.cosmosStore, times(0)).findItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testUpdateSchemaInfo_SupersededBy()
            throws NotFoundException, ApplicationException, BadRequestException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(getMockSchemaInfoDoc());
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + supersedingSchemaId),
                        eq(partitionKey),
                        any());
        doReturn(getFlattenedSchemaInfo_SupersededBy()).when(schemaInfoDoc).getFlattenedSchemaInfo();
        assertNotNull(schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published()));
    }

    @Test
    public void testUpdateSchemaInfo_SupersededByException()
            throws NotFoundException, ApplicationException, BadRequestException {
        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + supersedingSchemaId),
                        eq(partitionKey),
                        any());
        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        doReturn(getFlattenedSchemaInfo_SupersededBy()).when(schemaInfoDoc).getFlattenedSchemaInfo();
        try {
            schemaInfoStore.updateSchemaInfo(getMockSchemaObject_SupersededBy());
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

        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        doReturn(getFlattenedSchemaInfo_SupersededBy()).when(schemaInfoDoc).getFlattenedSchemaInfo();

        SchemaRequest schemaRequest = getMockSchemaObject_SuperSededByWithoutId();
        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage(INVALID_SUPERSEDEDBY_ID);
        schemaInfoStore.updateSchemaInfo(schemaRequest);
    }

    @Test
    public void testUpdateSchemaInfo_ApplicationException()
            throws NotFoundException, ApplicationException, BadRequestException {
        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        doThrow(AppException.class).when(cosmosStore).upsertItem(eq(dataPartitionId), any(), any(), any(), any());

        try {
            schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published());
            fail("Should not succeed");
        } catch (ApplicationException e) {
            assertEquals("Schema creation failed due to invalid object", e.getMessage());

        } catch (Exception e) {
            fail("Should not get different exception");
        }
    }

    @Test
    public void testGetSchemaInfoList_withoutqueryparam()
            throws NotFoundException, ApplicationException, BadRequestException {
        List<SchemaInfoDoc> schemaInfoDocsList = new LinkedList<>();
        schemaInfoDocsList.add(getMockSchemaInfoDoc());
        doReturn(schemaInfoDocsList).when(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());

        assertEquals(1,
                schemaInfoStore.getSchemaInfoList(QueryParams.builder().limit(100).offset(0).build(), dataPartitionId).size());
    }
    
    @Test
    public void testGetSchemaInfoList_Withoutqueryparam_FailedWhenSearchedIntoSharedPartition()
            throws NotFoundException, ApplicationException, BadRequestException {
        List<SchemaInfoDoc> schemaInfoDocsList = new LinkedList<>();
        schemaInfoDocsList.add(getMockSchemaInfoDocWithSupersededBy());
        doReturn(schemaInfoDocsList).when(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());
        doThrow(new NoSuchElementException()).when(cosmosStore).findItem(eq("common"), any(), any(), any(), any(), any());
        doReturn(Optional.of(getMockSchemaInfoDoc())).when(cosmosStore).findItem(eq(dataPartitionId), any(), any(), any(), any(), any());
        schemaInfoStore.getSchemaInfoList(QueryParams.builder().limit(100).offset(0).build(), dataPartitionId);
        expectedException.none();
    }

    @Test
    public void testGetSchemaInfoList_withqueryparam()
            throws NotFoundException, ApplicationException, BadRequestException {
        List<SchemaInfoDoc> schemaInfoDocsList = new LinkedList<>();
        schemaInfoDocsList.add(getMockSchemaInfoDoc());
        doReturn(schemaInfoDocsList).when(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());
        assertEquals(1,
                schemaInfoStore.getSchemaInfoList(QueryParams.builder().authority("test").source("test").entityType("test")
                        .schemaVersionMajor(1l).schemaVersionMinor(1l).scope("test").status("test").latestVersion(false)
                        .limit(100).offset(0).build(), dataPartitionId).size());
    }

    @Test
    public void testGetSchemaInfoList_withqueryparam_PublicSchemas()
            throws NotFoundException, ApplicationException, BadRequestException {
        List<SchemaInfoDoc> schemaInfoDocsList = new LinkedList<>();
        schemaInfoDocsList.add(getMockSchemaInfoDoc());
        doReturn(schemaInfoDocsList).when(cosmosStore).queryItems(eq(systemCosmosDBName), any(), any(), any(), any());

        assertEquals(1,
                schemaInfoStore.getSystemSchemaInfoList(QueryParams.builder().authority("test").source("test").entityType("test")
                        .schemaVersionMajor(1l).schemaVersionMinor(1l).scope("test").status("test").latestVersion(false)
                        .limit(100).offset(0).build()).size());

        verify(this.cosmosStore, times(1)).queryItems(eq(systemCosmosDBName), any(),any(), any(), eq(SchemaInfoDoc.class));
        verify(this.cosmosStore, times(0)).queryItems(any(), any(), any(),any(), any(), eq(SchemaInfoDoc.class));
    }

    @Test
    public void testGetSchemaInfoList_latestVersionTrue_NoSchemaMatchScenario()
            throws NotFoundException, ApplicationException, BadRequestException {
        List<SchemaInfoDoc> cosmosItem = new ArrayList<>();
        doReturn(cosmosItem).when(cosmosStore).queryItems(eq(dataPartitionId), any(), any(), any(), any(), any());
        assertEquals(0,
                schemaInfoStore
                        .getSchemaInfoList(QueryParams.builder().authority("test").source("test").entityType("test")
                                .scope("test").status("test").latestVersion(true).limit(100).offset(0).build(), "test")
                        .size());
    }

    @Test
    public void testCleanSchema_Success() throws ApplicationException {
        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertEquals(true, schemaInfoStore.cleanSchema(schemaId));
    }

    @Test
    public void testCleanSchema_Success_PublicSchemas() throws ApplicationException {
        doReturn(Optional.of(schemaInfoDoc))
                .when(cosmosStore)
                .findItem(
                        eq(systemCosmosDBName),
                        any(),
                        eq(schemaId),
                        eq(partitionKey),
                        any());

        assertEquals(true, schemaInfoStore.cleanSystemSchema(schemaId));

        verify(cosmosStore, times(1)).deleteItem(any(), any(), any(), any());
        verify(cosmosStore, times(0)).deleteItem(any(), any(), any(), any(), any());
        verify(cosmosStore, times(0)).findItem(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testCleanSchema_Failure() throws ApplicationException {
        doReturn(Optional.empty())
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());
        assertEquals(false, schemaInfoStore.cleanSchema(schemaId));
    }

    @Test
    public void getSchemaInfo_throwsAppException_whenSupersededBySchemaIsMissing() throws ApplicationException, NotFoundException {
        Optional<SchemaInfoDoc> cosmosItem = Optional.of(schemaInfoDoc);
        doReturn(cosmosItem)
                .when(cosmosStore)
                .findItem(
                        eq(dataPartitionId),
                        any(),
                        any(),
                        eq(dataPartitionId + ":" + schemaId),
                        eq(partitionKey),
                        any());

        doReturn(getFlattenedSchemaInfo_SupersededBy()).when(schemaInfoDoc).getFlattenedSchemaInfo();
        ApplicationException exception = assertThrows(ApplicationException.class, ()-> schemaInfoStore.getSchemaInfo(schemaId));
        assertEquals(exception.getStatus().value(), 500);
        assertEquals(exception.getMessage(), INVALID_SUPERSEDEDBY_ID);
    }

    private SchemaInfo getMockSchemaInfo() {
        return SchemaInfo.builder().schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L).id(schemaId).build())
                .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("azure")
                .build();
    }

    private SchemaInfoDoc getMockSchemaInfoDoc() {
        String id = headers.getPartitionId() + ":" + schemaId;

        return new SchemaInfoDoc(id, headers.getPartitionId(), getFlattenedSchemaInfo());
    }
    
    private SchemaInfoDoc getMockSchemaInfoDocWithSupersededBy() {
        String id = headers.getPartitionId() + ":" + schemaId;

        return new SchemaInfoDoc(id, headers.getPartitionId(), getFlattenedSchemaInfo_SupersededBy());
    }

    private FlattenedSchemaInfo getFlattenedSchemaInfo() {
        return FlattenedSchemaInfo.builder()
                .id(schemaId)
                .supersededBy("")
                .dateCreated(new Date())
                .createdBy("azure")
                .authority("os")
                .source("wks")
                .entityType("well")
                .majorVersion(1L)
                .minorVersion(1L)
                .patchVersion(1L)
                .scope(SchemaScope.SHARED.toString())
                .status(SchemaStatus.PUBLISHED.toString())
                .schema(CONTENT)
                .build();
    }

    private FlattenedSchemaInfo getFlattenedSchemaInfo_SupersededBy() {
        return FlattenedSchemaInfo.builder()
                .id(schemaId)
                .supersededBy(supersedingSchemaId)
                .dateCreated(new Date())
                .createdBy("azure")
                .authority("os")
                .source("wks")
                .entityType("well")
                .majorVersion(1L)
                .minorVersion(1L)
                .patchVersion(1L)
                .scope(SchemaScope.SHARED.toString())
                .status(SchemaStatus.PUBLISHED.toString())
                .build();
    }

    private SchemaRequest getMockSchemaObject_Published() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(getMockSchemaInfo())
                .build();
    }

    private SchemaRequest getMockSchemaObject_SupersededBy() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(SchemaInfo.builder()
                        .schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                                .id(schemaId).build())
                        .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("azure")
                        .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(2L)
                                .id(supersedingSchemaId).build())
                        .build())
                .build();

    }

    private SchemaRequest getMockSchemaObject_SuperSededByWithoutId() {
        return SchemaRequest.builder().schema("{}")
                .schemaInfo(SchemaInfo.builder()
                        .schemaIdentity(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                                .id(schemaId).build())
                        .scope(SchemaScope.SHARED).status(SchemaStatus.DEVELOPMENT).createdBy("ibm")
                        .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L).build())
                        .build())
                .build();

    }

    private AppException getMockAppException(int errorCode) {
        AppException mockException = mock(AppException.class);
        AppError mockError = mock(AppError.class);
        lenient().when(mockException.getError()).thenReturn(mockError);
        lenient().when(mockError.getCode()).thenReturn(errorCode);
        return mockException;
    }
}
