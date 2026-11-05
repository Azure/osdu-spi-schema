package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.service.Transaction;
import org.opengroup.osdu.core.osm.core.translate.TranslatorRuntimeException;
import org.opengroup.osdu.schema.configuration.PropertiesConfiguration;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.destination.provider.impl.OsmDestinationProvider;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.Authority;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class OsmAuthorityStoreTest {

  private static final String COMMON_TENANT_ID = "common";
  private static final String AUTHORITY_ID = "testAuthorityId";
  private static final Destination DESTINATION = Destination.builder()
      .partitionId("partitionId")
      .namespace(new Namespace("namespace"))
      .kind(new Kind("testKind"))
      .build();

  @InjectMocks
  OsmAuthorityStore mockOsmAuthorityStore;

  @Mock
  Authority mockAuthority;

  @Mock
  DpsHeaders headers;

  @Mock
  ITenantFactory tenantFactory;

  @Mock
  TenantInfo tenantInfo;

  @Mock
  Context context;

  @Mock
  OsmDestinationProvider destinationProvider;

  @Mock
  JaxRsDpsLog log;

  @Mock
  PropertiesConfiguration configuration;

  @Mock
  Transaction transaction;


  @Before
  public void setUp() {
    when(configuration.getSharedTenantName()).thenReturn(COMMON_TENANT_ID);
    ReflectionTestUtils.setField(mockOsmAuthorityStore, "configuration", configuration);
    when(headers.getPartitionId()).thenReturn("test");
    when(tenantFactory.getTenantInfo("test")).thenReturn(tenantInfo);

    when(mockAuthority.getAuthorityId()).thenReturn("os");

    when(destinationProvider.getDestination(any(), any(), any())).thenReturn(DESTINATION);

    when(context.getOne(any())).thenReturn(null);
    when(context.findOne(any())).thenReturn(Optional.of(mockAuthority));
    when(context.createAndGet(any(), any())).thenReturn(mockAuthority);
    when(context.beginTransaction(any())).thenReturn(transaction);
  }

  @Test
  public void testGetAuthority() throws NotFoundException, ApplicationException {
    assertNotNull(mockOsmAuthorityStore.get(AUTHORITY_ID));
  }

  @Test
  public void testGetAuthority_SystemSchemas() throws NotFoundException, ApplicationException {
    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
    assertNotNull(mockOsmAuthorityStore.getSystemAuthority(AUTHORITY_ID));
  }

  @Test
  public void testGetAuthority_NotFoundException() {
    when(context.findOne(any())).thenReturn(Optional.empty());
    try {
      mockOsmAuthorityStore.get(Strings.EMPTY);
      fail("Should not succeed");
    } catch (NotFoundException e) {
      assertEquals("bad input parameter", e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testGetAuthority_NotFoundException_SystemSchemas() {
    when(context.findOne(any())).thenReturn(Optional.empty());
    try {
      when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
      when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
      mockOsmAuthorityStore.get(Strings.EMPTY);
      fail("Should not succeed");
    } catch (NotFoundException e) {
      assertEquals("bad input parameter", e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testCreateAuthority()
      throws NotFoundException, ApplicationException, BadRequestException {

    assertNotNull(mockOsmAuthorityStore.create(mockAuthority));
  }

  @Test
  public void testCreateAuthority_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);

    when(mockAuthority.getAuthorityId()).thenReturn("os");
    assertNotNull(mockOsmAuthorityStore.createSystemAuthority(mockAuthority));
  }

  @Test
  public void testCreateAuthority_AlreadyExists_ReturnsExisting()
      throws ApplicationException, BadRequestException {
    when(context.getOne(any())).thenReturn(mockAuthority);

    Authority result = mockOsmAuthorityStore.create(mockAuthority);

    assertNotNull(result);
    assertEquals(mockAuthority, result);
    verify(context, never()).createAndGet(any(), any());
  }

  @Test
  public void testCreateSystemAuthority_AlreadyExists_ReturnsExisting()
      throws ApplicationException, BadRequestException {
    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
    when(context.getOne(any())).thenReturn(mockAuthority);

    Authority result = mockOsmAuthorityStore.createSystemAuthority(mockAuthority);

    assertNotNull(result);
    assertEquals(mockAuthority, result);
    verify(context, never()).createAndGet(any(), any());
  }

  @Test
  public void testCreateAuthority_ApplicationException()
      throws NotFoundException, ApplicationException, BadRequestException {
    mockOsmAuthorityStore = Mockito.spy(mockOsmAuthorityStore);
    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
    try {
      mockOsmAuthorityStore.create(mockAuthority);
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

  @Test
  public void testCreateAuthority_ApplicationException_SystemSchemas()
      throws NotFoundException, ApplicationException, BadRequestException {
    when(headers.getPartitionId()).thenReturn(COMMON_TENANT_ID);
    mockOsmAuthorityStore = Mockito.spy(mockOsmAuthorityStore);
    when(tenantFactory.getTenantInfo(COMMON_TENANT_ID)).thenReturn(tenantInfo);
    when(context.createAndGet(any(), any())).thenThrow(TranslatorRuntimeException.class);
    try {
      mockOsmAuthorityStore.createSystemAuthority(mockAuthority);
      fail("Should not succeed");
    } catch (ApplicationException e) {
      assertEquals(SchemaConstants.INVALID_INPUT, e.getMessage());

    } catch (Exception e) {
      fail("Should not get different exception");
    }
  }

}
