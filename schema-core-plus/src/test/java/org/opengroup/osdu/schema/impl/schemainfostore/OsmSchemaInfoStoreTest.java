package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.translate.TranslatorException;
import org.opengroup.osdu.core.osm.core.translate.TranslatorRuntimeException;
import org.opengroup.osdu.schema.configuration.PropertiesConfiguration;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.destination.provider.impl.OsmDestinationProvider;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class OsmSchemaInfoStoreTest {

  private static final String COMMON_TENANT_ID = "common";
  private static final Destination DESTINATION = Destination.builder()
      .partitionId("partitionId")
      .namespace(new Namespace("namespace"))
      .kind(new Kind("testKind"))
      .build();

  @InjectMocks
  OsmSchemaInfoStore schemaInfoStore;

  @Mock
  DpsHeaders headers;

  @Mock
  TenantInfo tenantInfo;

  @Mock
  TenantInfo tenantInfoCommon;

  @Mock
  List<Object> queryResult;

  @Mock
  ITenantFactory tenantFactory;

  @Mock
  JaxRsDpsLog log;

  @Mock
  Context context;

  @Mock
  OsmDestinationProvider destinationProvider;

  @Mock
  PropertiesConfiguration configuration;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    when(configuration.getSharedTenantName()).thenReturn(COMMON_TENANT_ID);
    ReflectionTestUtils.setField(schemaInfoStore, "configuration", configuration);
    when(headers.getPartitionId()).thenReturn("tenant");
    when(tenantFactory.getTenantInfo("tenant")).thenReturn(tenantInfo);

    when(destinationProvider.getDestination(any(), any(), any())).thenReturn(DESTINATION);

    when(context.getOne(any())).thenReturn(null);
    when(context.findOne(any())).thenReturn(Optional.of(getMockSchemaObject_Published()));
    when(context.createAndGet(any(), any())).thenReturn(getMockSchemaObject_Published());
  }

  @After
  public void tearDown() {
    queryResult.clear();
  }

  @Test
  public void testGetLatestMinorVersion_ReturnNull()
      throws NotFoundException, ApplicationException {
    assertEquals("", schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
  }

  @Test
  public void testGetSchemaInfo_NotEmptyEntity() throws NotFoundException, ApplicationException {
    String schemaId = "schemaId";
    this.setUpMocks("test", schemaId);
    SchemaInfo schemaInfo = schemaInfoStore.getSchemaInfo(schemaId);
    assertEquals(SchemaStatus.PUBLISHED, schemaInfo.getStatus());
  }

  @Test
  public void testGetSchemaInfo_NotEmptyEntity_SystemSchemas()
      throws NotFoundException, ApplicationException {
    String schemaId = "schemaId";
    this.setUpMocks(COMMON_TENANT_ID, schemaId);
    SchemaInfo schemaInfo = schemaInfoStore.getSystemSchemaInfo(schemaId);
    assertEquals(SchemaStatus.PUBLISHED, schemaInfo.getStatus());
  }

  @Test
  public void testGetSchemaInfo_EmptyEntity() throws NotFoundException, ApplicationException {

    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);
    String schemaId = "schemaId";
    this.setUpMocks("test", schemaId);
    when(context.findOne(any())).thenReturn(Optional.empty());
    schemaInfoStore.getSchemaInfo(schemaId);
  }

  @Test
  public void testGetSchemaInfo_EmptyEntity_SystemSchemas()
      throws NotFoundException, ApplicationException {
    expectedException.expect(NotFoundException.class);
    expectedException.expectMessage(SchemaConstants.SCHEMA_NOT_PRESENT);

    String schemaId = "schemaId";
    this.setUpMocks(COMMON_TENANT_ID, schemaId);

    when(context.findOne(any())).thenReturn(Optional.empty());

    schemaInfoStore.getSystemSchemaInfo(schemaId);
  }

  @Test
  public void testCreateSchemaInfo_Positive() throws ApplicationException, BadRequestException {
    this.setUpMocks("test", "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_Published()));
  }

  @Test
  public void testCreateSchemaInfo_Positive_SystemSchemas()
      throws ApplicationException, BadRequestException {

    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    assertNotNull(schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published()));
  }

  @Test
  public void testIsUnique_True() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "tenant";
    this.setUpMocksForMultiTenant_PositiveScenario(tenantId, COMMON_TENANT_ID, schemaId);
    assertTrue(schemaInfoStore.isUnique(schemaId, tenantId));
  }

  @Test
  public void testIsUnique_True_SystemSchemas() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "common";
    this.setUpMocksForMultiTenant_PositiveScenario(tenantId, COMMON_TENANT_ID, schemaId);
    assertTrue(schemaInfoStore.isUniqueSystemSchema(schemaId));
  }

  @Test
  public void testIsUnique_False() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "tenant";
    this.setUpMocksForMultiTenant_NegativeScenario(tenantId, COMMON_TENANT_ID, schemaId);
    assertFalse(schemaInfoStore.isUnique(schemaId, tenantId));
  }

  @Test
  public void testIsUnique_False_CommonTenant() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "common";
    this.setUpMocksForMultiTenant_NegativeScenario(tenantId, COMMON_TENANT_ID, schemaId);
    assertFalse(schemaInfoStore.isUnique(schemaId, tenantId));
  }

  @Test
  public void testIsUnique_False_CommonTenant_SystemSchemas() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "common";
    this.setUpMocksForMultiTenant_NegativeScenario(tenantId, COMMON_TENANT_ID, schemaId);
    assertFalse(schemaInfoStore.isUniqueSystemSchema(schemaId));
  }

  @Test
  public void testUpdateSchemaInfo()
      throws NotFoundException, ApplicationException, BadRequestException {
    String tenantId = "test";
    this.setUpMocks(tenantId, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    when(context.upsertAndGet(any(), any())).thenReturn(getMockSchemaObject_Published());
    assertNotNull(schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published()));
  }

  @Test
  public void testUpdateSchemaInfo_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    when(context.upsertAndGet(any(), any())).thenReturn(getMockSchemaObject_Published());
    assertNotNull(schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_Published()));
  }

  @Test
  public void testCreateSchemaInfo_WithSupersededBy()
      throws NotFoundException, ApplicationException, BadRequestException {
    this.setUpMocks("test", "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    when(context.findOne(any())).thenReturn(Optional.of(getMockSchemaObject_SuperSededObject()));
    assertNotNull(schemaInfoStore.createSchemaInfo(getMockSchemaObject_SuperSededBy()));
  }

  @Test
  public void testCreateSchemaInfo_WithSupersededBy_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    when(context.findOne(any())).thenReturn(Optional.of(getMockSchemaObject_SuperSededObject()));
    assertNotNull(schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_SuperSededBy()));
  }

  @Test
  public void testUpdateSchemaInfo_SupersededByException()
      throws NotFoundException, ApplicationException, BadRequestException {
    when(context.findOne(any())).thenReturn(Optional.empty());
    try {
      this.setUpMocks("test", "os:wks:well.1.1.1");
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
    when(context.findOne(any())).thenReturn(Optional.empty());
    try {
      this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
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
    this.setUpMocks("test", "os:wks:well.1.1.1");
    SchemaRequest schemaRequest = getMockSchemaObject_SuperSededByWithoutId();
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(SchemaConstants.INVALID_SUPERSEDEDBY_ID);

    when(context.getOne(any())).thenReturn(null);

    schemaInfoStore.updateSchemaInfo(schemaRequest);
  }

  @Test
  public void testUpdateInfo_SupersededByWithoutIdException_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    SchemaRequest schemaRequest = getMockSchemaObject_SuperSededByWithoutId();
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage(SchemaConstants.INVALID_SUPERSEDEDBY_ID);
    schemaInfoStore.updateSystemSchemaInfo(schemaRequest);
  }

  @Test
  public void testCreateSchemaInfo_BadRequestException()
      throws NotFoundException, ApplicationException, BadRequestException {

    this.setUpMocks("test", "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.getOne(any())).thenReturn(getMockSchemaObject_Published());
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
  public void testCreateSchemaInfo_BadRequestException_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {

    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.getOne(any())).thenReturn(getMockSchemaObject_Published());
    try {
      schemaInfoStore.createSystemSchemaInfo(getMockSchemaObject_Published());
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
    this.setUpMocks("test", "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
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
    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
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
    this.setUpMocks("test", "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.upsertAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
    try {
      schemaInfoStore.updateSchemaInfo(getMockSchemaObject_Published());
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals("Invalid object, update failed", e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testUpdateSchemaInfo_ApplicationException_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    this.setUpMocks(COMMON_TENANT_ID, "os:wks:well.1.1.1");
    when(headers.getUserEmail()).thenReturn("dummy-user");
    when(context.upsertAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
    try {
      schemaInfoStore.updateSystemSchemaInfo(getMockSchemaObject_Published());
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals("Invalid object, update failed", e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testGetLatestMinorVersion_Entity() throws NotFoundException, ApplicationException {
    when(headers.getPartitionId()).thenReturn("test");
    when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
    when(headers.getUserEmail()).thenReturn("hmadhani@delfi.com");
    when(context.getResultsAsList(any())).thenReturn(
        Collections.singletonList(getMockSchemaObject_Published()));
    assertEquals("{}", schemaInfoStore.getLatestMinorVerSchema(getMockSchemaInfo()));
  }

  @Test
  public void testGetSchemaInfoList_withoutqueryparam()
      throws NotFoundException, ApplicationException, BadRequestException {

    when(headers.getPartitionId()).thenReturn("test");
    when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
    when(tenantInfo.getName()).thenReturn("test");
    when(context.getResultsAsList(any())).thenReturn(
        Collections.singletonList(getMockSchemaObject_Published()));
    assertEquals(1,
        schemaInfoStore.getSchemaInfoList(QueryParams.builder().limit(100).offset(0).build(),
            "test").size());
  }

  @Test
  public void testGetSchemaInfoList_withoutqueryparam_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {

    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
    when(tenantInfo.getName()).thenReturn(COMMON_TENANT_ID);
    when(context.getResultsAsList(any())).thenReturn(
        Collections.singletonList(getMockSchemaObject_Published()));
    assertEquals(1,
        schemaInfoStore.getSystemSchemaInfoList(QueryParams.builder().limit(100).offset(0).build())
            .size());
  }

  @Test
  public void testGetSchemaInfoList_withqueryparam()
      throws NotFoundException, ApplicationException, BadRequestException {

    when(headers.getPartitionId()).thenReturn("test");
    when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);
    when(tenantInfo.getName()).thenReturn("test");
    when(context.getResultsAsList(any())).thenReturn(
        Collections.singletonList(getMockSchemaObject_Published()));
    assertEquals(1,
        schemaInfoStore.getSchemaInfoList(QueryParams.builder().authority("test").source("test")
            .entityType("test").schemaVersionMajor(1l).schemaVersionMinor(1l).scope("test")
            .status("test")
            .latestVersion(false).limit(100).offset(0).build(), "test").size());
  }

  @Test
  public void testCleanSchema_Success() throws ApplicationException {
    String schemaId = "schemaId";
    String dataPartitionId = "tenant1";
    this.setUpMocks(dataPartitionId, schemaId);
    assertEquals(true, schemaInfoStore.cleanSchema(schemaId));
  }

  @Test
  public void testCleanSchema_Success_SystemSchemas() throws ApplicationException {
    String schemaId = "schemaId";
    this.setUpMocks(COMMON_TENANT_ID, schemaId);
    assertEquals(true, schemaInfoStore.cleanSchema(schemaId));
  }

  @Test
  public void testCleanSchema_Failure() throws ApplicationException, TranslatorException {
    String schemaId = "schemaId";
    String dataPartitionId = "tenant1";
    this.setUpMocks(dataPartitionId, schemaId);
    doThrow(TranslatorException.class).when(context).delete(any(), any(), any());
    assertEquals(false, schemaInfoStore.cleanSchema(schemaId));
  }

  @Test
  public void testCleanSchema_Failure_SystemSchemas()
      throws ApplicationException, TranslatorException {
    String schemaId = "schemaId";
    this.setUpMocks(COMMON_TENANT_ID, schemaId);
    doThrow(TranslatorException.class).when(context).delete(any(), any(), any());
    assertEquals(false, schemaInfoStore.cleanSchema(schemaId));
  }

  @Test
  public void testMisconfiguredTenantInfoShouldThrowException() throws ApplicationException {
    String schemaId = "schemaId";
    String tenantId = "common";
    this.setUpMocks(tenantId, schemaId);
    when(context.getResultsAsList(any())).thenThrow(TranslatorRuntimeException.class);
    expectedException.expect(AppException.class);
    expectedException.expectMessage(
        "Misconfigured tenant-info for common, not possible to check schema uniqueness");
    schemaInfoStore.isUnique(schemaId, tenantId);
  }

  private SchemaRequest getMockSchemaObject_Published() {
    return SchemaRequest.builder().schema("{}")
        .schemaInfo(SchemaInfo.builder()
            .schemaIdentity(
                SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                    .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                    .id("os:wks:well.1.1.1").build())
            .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("subham").build())
        .build();

  }

  private SchemaInfo getMockSchemaInfo() {
    return SchemaInfo.builder()
        .schemaIdentity(
            SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                .schemaVersionMajor(1L)
                .schemaVersionMinor(1L).schemaVersionPatch(1L).id("os:wks:well.1.1.1").build())
        .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("subham")

        .build();

  }

  private SchemaRequest getMockSchemaObject_SuperSededBy() {
    return SchemaRequest.builder().schema("{}")
        .schemaInfo(SchemaInfo.builder()
            .schemaIdentity(
                SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                    .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                    .id("os:wks:well.1.1.1").build())
            .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("subham")
            .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                .schemaVersionMajor(1L).schemaVersionMinor(2L).schemaVersionPatch(1L)
                .id("os:wks:well.1.2.1").build())
            .build())
        .build();

  }

  private SchemaRequest getMockSchemaObject_SuperSededObject() {
    return SchemaRequest.builder().schema("{}")
        .schemaInfo(SchemaInfo.builder()
            .schemaIdentity(
                SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                    .schemaVersionMajor(1L).schemaVersionMinor(2L).schemaVersionPatch(1L)
                    .id("os:wks:well.1.2.1").build())
            .scope(SchemaScope.SHARED).status(SchemaStatus.PUBLISHED).createdBy("subham")
            .build())
        .build();

  }

  private SchemaRequest getMockSchemaObject_SuperSededByWithoutId() {
    return SchemaRequest.builder().schema("{}")
        .schemaInfo(SchemaInfo.builder()
            .schemaIdentity(
                SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                    .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L)
                    .id("os:wks:well.1.1.1").build())
            .scope(SchemaScope.SHARED).status(SchemaStatus.DEVELOPMENT).createdBy("subham")
            .supersededBy(SchemaIdentity.builder().authority("os").source("wks").entityType("well")
                .schemaVersionMajor(1L).schemaVersionMinor(1L).schemaVersionPatch(1L).build())
            .build())
        .build();

  }

  private void setUpMocks(String dataPartitionId, String schemaId) {
    when(headers.getPartitionId()).thenReturn(dataPartitionId);
    when(tenantFactory.getTenantInfo(dataPartitionId)).thenReturn(tenantInfo);
  }

  private void setUpMocksForMultiTenant_PositiveScenario(String privateTenant, String commonTenant,
      String schemaId) {
    this.setUpMocksForMultipleTenants(privateTenant, commonTenant, schemaId);
    when(context.getResultsAsList(any())).thenReturn(queryResult);
    when(queryResult.isEmpty()).thenReturn(true);
  }

  private void setUpMocksForMultiTenant_NegativeScenario(String privateTenant, String commonTenant,
      String schemaId) {
    this.setUpMocksForMultipleTenants(privateTenant, commonTenant, schemaId);
    when(context.getResultsAsList(any())).thenReturn(queryResult);
    when(queryResult.size()).thenReturn(2);
  }

  private void setUpMocksForMultipleTenants(String privateTenant, String commonTenant,
      String schemaId) {
    when(configuration.getSharedTenantName()).thenReturn(commonTenant);
    when(tenantFactory.getTenantInfo(privateTenant)).thenReturn(tenantInfo);
    when(tenantFactory.getTenantInfo(commonTenant)).thenReturn(tenantInfoCommon);
    when(tenantInfo.getName()).thenReturn(privateTenant);
    when(tenantInfoCommon.getName()).thenReturn(commonTenant);
  }
}
