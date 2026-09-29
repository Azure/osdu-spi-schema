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

package org.opengroup.osdu.schema.provider.azure.impl.schemastore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.azure.di.AzureBootstrapConfig;
import org.opengroup.osdu.schema.azure.di.CosmosContainerConfig;
import org.opengroup.osdu.schema.azure.di.SystemResourceConfig;
import org.opengroup.osdu.schema.azure.impl.schemastore.AzureSchemaStore;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AzureSchemaStoreTest {
    @InjectMocks
    AzureSchemaStore schemaStore;

    @Mock
    BlobStore blobStore;

    @Mock
    CosmosContainerConfig config;

    @Mock
    DpsHeaders headers;

    @Mock
    JaxRsDpsLog log;

    @Mock
    SystemResourceConfig systemResourceConfig;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String dataPartitionId = "dataPartitionId";
    private static final String sharedTenantId = "common";
    private static final String FILE_PATH = "/test-folder/test-file";
    private static final String CONTENT = "Hello World";
    private static final String containerName = "opendes";
    private static final String systemContainerName = "systemContainer";
    private static final String filePath = dataPartitionId + ":" + FILE_PATH + SchemaConstants.JSON_EXTENSION;
    private static final String filePathPublic = FILE_PATH + SchemaConstants.JSON_EXTENSION;

    @Before
    public void init(){
        initMocks(this);
        doReturn(dataPartitionId).when(headers).getPartitionId();
        when(config.containerName()).thenReturn(containerName);
        Mockito.when(systemResourceConfig.getStorageContainerName()).thenReturn(systemContainerName);
        Mockito.when(systemResourceConfig.getSharedTenant()).thenReturn(sharedTenantId);
    }

    @Test
    public void testGetSchema() throws ApplicationException, NotFoundException {
        doReturn(CONTENT).when(blobStore).readFromStorageContainer(dataPartitionId, filePath, containerName);
        Assert.assertEquals(CONTENT, schemaStore.getSchema(dataPartitionId, FILE_PATH));
    }

    @Test
    public void testGetSchema_PublicSchemas() throws ApplicationException, NotFoundException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        doReturn(CONTENT).when(blobStore).readFromStorageContainer(filePathPublic, systemContainerName);

        Assert.assertEquals(CONTENT, schemaStore.getSystemSchema(FILE_PATH));

    }

    @Test
    public void testGetSchema_NotFound() throws ApplicationException, NotFoundException {
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
        doReturn(null).when(blobStore).readFromStorageContainer(dataPartitionId, filePath, containerName);
        schemaStore.getSchema(dataPartitionId, FILE_PATH);
    }

    @Test
    public void testGetSchema_NotFound_PublicSchemas() throws ApplicationException, NotFoundException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
        doReturn(null).when(blobStore).readFromStorageContainer(filePathPublic, systemContainerName);

        schemaStore.getSystemSchema(FILE_PATH);

        // This is temporary and will be removed once schema-core starts consuming *system* methods
        schemaStore.getSchema(sharedTenantId, FILE_PATH);
    }

    @Test
    public void testGetSchema_Failure() throws ApplicationException, NotFoundException {
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);

        doThrow(AppException.class).when(blobStore).readFromStorageContainer(dataPartitionId, filePath, containerName);
        schemaStore.getSchema(dataPartitionId, FILE_PATH);
    }

    @Test
    public void testGetSchema_Failure_PublicSchemas() throws ApplicationException, NotFoundException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);

        doThrow(AppException.class).when(blobStore).readFromStorageContainer(filePathPublic, systemContainerName);

        schemaStore.getSystemSchema(FILE_PATH);

        // This is temporary and will be removed once schema-core starts consuming *system* methods
        schemaStore.getSchema(sharedTenantId, FILE_PATH);
    }

    @Test
    public void testDeleteSchema() throws ApplicationException {
        doReturn(true).when(blobStore).deleteFromStorageContainer(dataPartitionId, filePath, containerName);

        Boolean result = schemaStore.cleanSchemaProject(FILE_PATH);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testDeleteSchema_PublicSchemas() throws ApplicationException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        doReturn(true).when(blobStore).deleteFromStorageContainer(filePathPublic, systemContainerName);

        Assert.assertEquals(true, schemaStore.cleanSystemSchemaProject(FILE_PATH));
    }

    @Test
    public void testDeleteSchema_Failure() throws ApplicationException {
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        doThrow(AppException.class).when(blobStore).deleteFromStorageContainer(dataPartitionId, filePath, containerName);
        schemaStore.cleanSchemaProject(FILE_PATH);
    }

    @Test
    public void testDeleteSchema_Failure_PublicSchemas() throws ApplicationException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        doThrow(AppException.class).when(blobStore).deleteFromStorageContainer(filePathPublic, systemContainerName);

        schemaStore.cleanSystemSchemaProject(FILE_PATH);

        // This is temporary and will be removed once schema-core starts consuming *system* methods
        schemaStore.cleanSchemaProject(FILE_PATH);
    }

    @Test
    public void testCreateSchema() throws ApplicationException {

        doNothing().when(blobStore).writeToStorageContainer(dataPartitionId, filePath, CONTENT, containerName);
        Assert.assertEquals(filePath, schemaStore.createSchema(FILE_PATH, CONTENT));
    }

    @Test
    public void testCreateSchema_PublicSchemas() throws ApplicationException {

        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        doNothing().when(blobStore).writeToStorageContainer(filePathPublic, CONTENT, systemContainerName);

        Assert.assertEquals(filePathPublic, schemaStore.createSystemSchema(FILE_PATH, CONTENT));

    }

    @Test
    public void testCreateSchema_Failure() throws ApplicationException {
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        doThrow(AppException.class).when(blobStore).writeToStorageContainer(dataPartitionId, filePath, CONTENT, containerName);
        schemaStore.createSchema(FILE_PATH, CONTENT);
    }

    @Test
    public void testCreateSchema_Failure_PublicSchemas() throws ApplicationException {
        Mockito.when(headers.getPartitionId()).thenReturn(sharedTenantId);
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        doThrow(AppException.class).when(blobStore).writeToStorageContainer(filePathPublic, CONTENT, systemContainerName);

        schemaStore.createSystemSchema(FILE_PATH, CONTENT);

        // This is temporary and will be removed once schema-core starts consuming *system* methods
        schemaStore.createSchema(FILE_PATH, CONTENT);
    }
}
