package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opengroup.osdu.schema.constants.SchemaConstants.INVALID_INPUT;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.service.Transaction;
import org.opengroup.osdu.core.osm.core.translate.TranslatorRuntimeException;
import org.opengroup.osdu.schema.configuration.PropertiesConfiguration;
import org.opengroup.osdu.schema.destination.provider.impl.OsmDestinationProvider;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.EntityType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class OsmEntityTypeStoreTest {

  @InjectMocks
  OsmEntityTypeStore osmEntityTypeStore;

  @Mock
  EntityType mockEntityType;

  @Mock
  DpsHeaders headers;

  @Mock
  OsmDestinationProvider destinationProvider;

  @Mock
  TenantInfo tenantInfo;

  @Mock
  Context context;

  @Mock
  JaxRsDpsLog log;

  @Mock
  PropertiesConfiguration configuration;

  @Mock
  Transaction transaction;

  private static final String COMMON_TENANT_ID = "common";
  private static final Destination DESTINATION = Destination.builder()
      .partitionId("partitionId")
      .namespace(new Namespace("namespace"))
      .kind(new Kind("testKind"))
      .build();

  @Before
  public void setUp() throws BadRequestException {
    when(configuration.getSharedTenantName()).thenReturn(COMMON_TENANT_ID);
    ReflectionTestUtils.setField(osmEntityTypeStore, "configuration", configuration);
    when(headers.getPartitionId()).thenReturn("partitionId");
    when(destinationProvider.getDestination(any(), any(), any())).thenReturn(DESTINATION);
    when(context.getOne(any())).thenReturn(null);
    when(context.createAndGet(any(), any())).thenReturn(mockEntityType);
    when(context.findOne(any())).thenReturn(Optional.ofNullable(mockEntityType));
    when(context.beginTransaction(any())).thenReturn(transaction);
  }

  @Test
  public void testGet() throws NotFoundException, ApplicationException {
    System.out.println("testGet");
    String entityId = "testEntityId";
    assertNotNull(osmEntityTypeStore.get(entityId));
  }

  @Test
  public void testGet_SystemSchemas() throws NotFoundException, ApplicationException {
    System.out.println("testGet");
    String entityId = "testEntityId";

    assertNotNull(osmEntityTypeStore.getSystemEntity(entityId));
  }

  @Test
  public void testGet_NotFoundException() {
    System.out.println("testGet_NotFoundException");
    String entityId = "";

    when(context.findOne(any())).thenReturn(Optional.empty());

    try {
      osmEntityTypeStore.get(entityId);
      fail("Should not succeed");
    } catch (NotFoundException e) {
      assertEquals("bad input parameter", e.getMessage());
    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testGet_NotFoundException_SystemSchemas() {
    System.out.println("testGet_NotFoundException");
    String entityId = "";

    when(context.findOne(any())).thenReturn(Optional.empty());

    try {
      osmEntityTypeStore.getSystemEntity(entityId);
      fail("Should not succeed");
    } catch (NotFoundException e) {
      assertEquals("bad input parameter", e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testCreate() throws NotFoundException, ApplicationException, BadRequestException {
    System.out.println("testCreate");
    when(mockEntityType.getEntityTypeId()).thenReturn("wellbore");
    assertNotNull(osmEntityTypeStore.create(mockEntityType));
  }

  @Test
  public void testCreate_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    System.out.println("testCreate");
    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    when(mockEntityType.getEntityTypeId()).thenReturn("wellbore");

    assertNotNull(osmEntityTypeStore.createSystemEntity(mockEntityType));
  }

  @Test
  public void testCreate_AlreadyExists_ReturnsExisting()
      throws ApplicationException, BadRequestException {
    when(context.getOne(any())).thenReturn(mockEntityType);

    EntityType result = osmEntityTypeStore.create(mockEntityType);

    assertNotNull(result);
    assertEquals(mockEntityType, result);
    verify(context, never()).createAndGet(any(), any());
  }

  @Test
  public void testCreateSystemEntity_AlreadyExists_ReturnsExisting()
      throws ApplicationException, BadRequestException {
    when(context.getOne(any())).thenReturn(mockEntityType);

    EntityType result = osmEntityTypeStore.createSystemEntity(mockEntityType);

    assertNotNull(result);
    assertEquals(mockEntityType, result);
    verify(context, never()).createAndGet(any(), any());
  }

  @Test
  public void testCreate_ApplicationException()
      throws NotFoundException, ApplicationException, BadRequestException {
    System.out.println("testCreate_ApplicationException");

    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);

    try {
      osmEntityTypeStore.create(mockEntityType);
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals(INVALID_INPUT, e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testCreate_ApplicationException_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    System.out.println("testCreate_ApplicationException");

    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);

    try {
      osmEntityTypeStore.createSystemEntity(mockEntityType);
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals(INVALID_INPUT, e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }
}
