package org.opengroup.osdu.schema.impl.schemastore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.ObmDriverRuntimeException;
import org.opengroup.osdu.core.obm.core.model.ObmBlob;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.schema.configuration.PartitionPropertyNames;
import org.opengroup.osdu.schema.configuration.PropertiesConfiguration;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.destination.provider.impl.ObmDestinationProvider;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ObmSchemaStoreTest {

    private static final ObmDestination DESTINATION = ObmDestination.builder().partitionId("partitionId").build();
    @InjectMocks
    ObmSchemaStore schemaStore;

    @Mock
    private Driver driver;
    @Mock
    private ObmBlob blob;
    @Mock
    private ObmDestinationProvider destinationProvider;
    @Mock
    DpsHeaders headers;
    @Mock
    ITenantFactory tenantFactory;
    @Mock
    TenantInfo TenantInfo;
    @Mock
    JaxRsDpsLog log;
    @Mock
    PropertiesConfiguration configuration;
    @Mock
    PartitionPropertyNames partitionPropertyNames;
    @Mock
    PartitionPropertyResolver partitionPropertyResolver;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String BUCKET = "test-schema";
    private static final String dataPartitionId = "dataPartitionId";
    private static final String FILE_PATH = "/test-folder/test-file";
    private static final String CONTENT = "Hello World";
    private static final String COMMON_TENANT_ID = "common";
    private static final String COMMON_TENANT_BUCKET = "common-schema";

    @Before
    public void setUp() {
        when(configuration.getSharedTenantName()).thenReturn(COMMON_TENANT_ID);
    	 ReflectionTestUtils.setField(schemaStore, "configuration", configuration);

         when(destinationProvider.getDestination(any())).thenReturn(DESTINATION);
         when(driver.createAndGetBlob(any(), any(), any())).thenReturn(blob);
    }

    @Test
    public void testCreateSchema() throws ApplicationException {

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        String filepath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(blob.getName()).thenReturn(BUCKET + filepath);
        when(driver.createAndGetBlob(any(), any(), any())). thenReturn(blob);

        String blobName = schemaStore.createSchema(FILE_PATH, CONTENT);

        Assert.assertEquals((BUCKET + filepath), blobName);
    }

    @Test
    public void testCreateSchema_SystemSchemas() throws ApplicationException {

        when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
        String filepath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn(COMMON_TENANT_ID);
        when(blob.getName()).thenReturn(COMMON_TENANT_BUCKET + filepath);
        when(driver.createAndGetBlob(any(), any(), any())).thenReturn(blob);

        String blobName = schemaStore.createSystemSchema(FILE_PATH, CONTENT);

        Assert.assertEquals((COMMON_TENANT_BUCKET + filepath), blobName);
    }

    @Test
    public void testCreateSchema_Failure() throws ApplicationException {
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        String filepath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(driver.createAndGetBlob(any(), any(), any())).thenThrow(ObmDriverRuntimeException.class);

        schemaStore.createSchema(FILE_PATH, CONTENT);
    }

    @Test
    public void testCreateSchema_Failure_SystemSchemas() throws ApplicationException {
        expectedException.expect(ApplicationException.class);
        expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

        when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
        String filepath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn(COMMON_TENANT_ID);
        when(driver.createAndGetBlob(any(), any(), any())).thenThrow(ObmDriverRuntimeException.class);

        schemaStore.createSystemSchema(FILE_PATH, CONTENT);
    }

    @Test
    public void testGetSchema() throws ApplicationException, NotFoundException {
        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(driver.getBlobContent(any(), any(), any())).thenReturn(CONTENT.getBytes());
        Assert.assertEquals(CONTENT, schemaStore.getSchema(dataPartitionId, FILE_PATH));
    }

    @Test
    public void testGetSchema_SystemSchemas() throws ApplicationException, NotFoundException {
        when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn(COMMON_TENANT_ID);
        when(driver.getBlobContent(any(), any(), any())).thenReturn(CONTENT.getBytes());
        Assert.assertEquals(CONTENT, schemaStore.getSystemSchema(FILE_PATH));
    }

    @Test
    public void testGetSchema_NotFound() throws ApplicationException, NotFoundException {

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
        String filePath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(driver.getBlobContent(BUCKET, filePath, DESTINATION)).thenReturn(null);
        schemaStore.getSchema(dataPartitionId, FILE_PATH);
    }

    @Test
    public void testGetSchema_NotFound_StorageException() throws ApplicationException, NotFoundException {

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
        String filePath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        ObmDriverRuntimeException obmDriverRuntimeException = new ObmDriverRuntimeException(404, new RuntimeException());

        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(driver.getBlobContent(BUCKET, filePath, DESTINATION)).thenThrow(obmDriverRuntimeException);
        schemaStore.getSchema(dataPartitionId, FILE_PATH);
    }

    @Test
    public void testGetSchema_NotFound_SystemSchemas() throws ApplicationException, NotFoundException {
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);

        when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn(COMMON_TENANT_ID);
        when(driver.getBlobContent(any(), any(), any())).thenReturn(null);
        schemaStore.getSystemSchema(FILE_PATH);
    }

    @Test
    public void testDeleteSchema() throws ApplicationException {

        when(headers.getPartitionId()).thenReturn(dataPartitionId);
        String filepath = FILE_PATH + SchemaConstants.JSON_EXTENSION;
        when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn("test");
        when(driver.deleteBlob(any(), any(), any())).thenReturn(true);

        Boolean result = schemaStore.cleanSchemaProject(FILE_PATH);

        Assert.assertEquals(true, result);
    }

    @Test
    public void testDeleteSchema_SystemSchemas() throws ApplicationException {

        when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
        when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(TenantInfo);
        when(TenantInfo.getProjectId()).thenReturn(COMMON_TENANT_ID);
        when(driver.deleteBlob(any(), any(), any())).thenReturn(true);

        Boolean result = schemaStore.cleanSystemSchemaProject(FILE_PATH);

        Assert.assertEquals(true, result);
    }

}
