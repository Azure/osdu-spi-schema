/* Licensed Materials - Property of IBM              */		
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.schema.impl.schemastore;


import static org.mockito.ArgumentMatchers.*;

import org.junit.Assert;
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
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ibm.cloud.objectstorage.AmazonServiceException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.MalformedURLException;
import java.net.URL;

@RunWith(SpringJUnit4ClassRunner.class)
public class IBMSchemaStoreTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	IBMSchemaStore schemaStore;

	@Mock
	DpsHeaders headers;
	
	@Mock
	TenantInfo tenant;

	@Mock
	CloudObjectStorageFactory cosFactory;

	@Mock
	AmazonS3 s3Client;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final String BUCKET = "schema";
	private static final String dataPartitionId = "testPartitionId";
	private static final String SCHEMA_ID = "test-schema-id";
	private static final String BUCKET_NAME_PREFIX = "sandy-local-dev";

	private static final String CONTENT = "Hello World";
	private static final String COMMON_TENANT_ID = "common";

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(schemaStore, "sharedTenant", COMMON_TENANT_ID);
	}

	@Test
	public void testCreateSchema() throws ApplicationException, MalformedURLException {
		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, dataPartitionId, BUCKET);
		URL url = new URL("http://schema/" + SCHEMA_ID);

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(dataPartitionId, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.getUrl(bucketName, SCHEMA_ID)).thenReturn(url);

		String objectURL = schemaStore.createSchema(SCHEMA_ID, CONTENT);
		Assert.assertEquals(("http://schema/" + SCHEMA_ID), objectURL);
	}

	@Test
	public void testCreateSchema_SystemSchemas() throws ApplicationException, MalformedURLException {
		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, COMMON_TENANT_ID, BUCKET);
		URL url = new URL("http://schema/" + SCHEMA_ID);

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(COMMON_TENANT_ID, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.getUrl(bucketName, SCHEMA_ID)).thenReturn(url);

		String objectURL = schemaStore.createSystemSchema(SCHEMA_ID, CONTENT);
		Assert.assertEquals(("http://schema/" + SCHEMA_ID), objectURL);
	}

	@Test
	public void testCreateSchema_Failure() throws ApplicationException, MalformedURLException {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, dataPartitionId, BUCKET);
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(dataPartitionId, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.putObject(any())).thenThrow(AmazonServiceException.class);
		schemaStore.createSchema(SCHEMA_ID, CONTENT);
	}

	@Test
	public void testCreateSchema_Failure_SystemSchemas() throws ApplicationException, MalformedURLException {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage(SchemaConstants.INTERNAL_SERVER_ERROR);

		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, COMMON_TENANT_ID, BUCKET);
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(COMMON_TENANT_ID, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.putObject(any())).thenThrow(AmazonServiceException.class);
		schemaStore.createSystemSchema(SCHEMA_ID, CONTENT);
	}

	@Test
	public void testGetSchema() throws ApplicationException, NotFoundException {
		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, dataPartitionId, BUCKET);

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(dataPartitionId, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.getObjectAsString(bucketName, SCHEMA_ID)).thenReturn(CONTENT);
		Mockito.when(tenant.getName()).thenReturn(dataPartitionId);

		Assert.assertEquals(CONTENT, schemaStore.getSchema(dataPartitionId, SCHEMA_ID));
	}

	@Test
	public void testGetSchema_SystemSchemas() throws ApplicationException, NotFoundException {
		String bucketName = String.format("%s-dataecosystem-%s-%s", BUCKET_NAME_PREFIX, COMMON_TENANT_ID, BUCKET);

		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(cosFactory.getClient()).thenReturn(s3Client);
		Mockito.when(cosFactory.getBucketName(COMMON_TENANT_ID, BUCKET)).thenReturn(bucketName);
		Mockito.when(s3Client.getObjectAsString(bucketName, SCHEMA_ID)).thenReturn(CONTENT);
		Mockito.when(tenant.getName()).thenReturn(COMMON_TENANT_ID);

		Assert.assertEquals(CONTENT, schemaStore.getSystemSchema(SCHEMA_ID));
	}

	@Test
	public void testGetSchema_NotFound() throws ApplicationException, NotFoundException {
		expectedException.expect(NotFoundException.class);
		expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
		Mockito.when(s3Client.getObjectAsString(dataPartitionId, SCHEMA_ID)).thenReturn(null);
		schemaStore.getSchema(dataPartitionId, SCHEMA_ID);
	}

	@Test
	public void testGetSchema_NotFound_SystemSchemas() throws ApplicationException, NotFoundException {
		expectedException.expect(NotFoundException.class);
		expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
		Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(COMMON_TENANT_ID);
		Mockito.when(s3Client.getObjectAsString(COMMON_TENANT_ID, SCHEMA_ID)).thenReturn(null);
		schemaStore.getSystemSchema(SCHEMA_ID);
	}
}
