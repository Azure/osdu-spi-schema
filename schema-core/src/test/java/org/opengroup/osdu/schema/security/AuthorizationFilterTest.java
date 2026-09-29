package org.opengroup.osdu.schema.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.entitlements.EntitlementsService;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.UnauthorizedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationFilterTest {

    @Mock
    IEntitlementsFactory entitlementsFactory;

    @Mock
    DpsHeaders headers;

    @Mock
    EntitlementsService en;

    @Mock
    Groups groups;

    @Mock
    JaxRsDpsLog log;

    @InjectMocks
    AuthorizationFilter authorizationFilter;

    @Test
    public void testHasRole_UnAuthorozed() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenReturn(groups);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals(SchemaConstants.UNAUTHORIZED_EXCEPTION, exception.getMessage());
    }

    @Test
    public void testHasRole_EntitlementException_BadRequest() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenThrow(
                new EntitlementsException("Invalid token", new HttpResponse(null, null, null, 400, null, null, 0L)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals(SchemaConstants.BAD_INPUT, exception.getMessage());
    }

    @Test
    public void testHasRole_EntitlementException_UnAuthenticated() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenThrow(
                new EntitlementsException("Invalid token", new HttpResponse(null, null, null, 403, null, null, 0L)));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals(SchemaConstants.UNAUTHORIZED_EXCEPTION, exception.getMessage());
    }

    @Test
    public void testHasRole_EntitlementException_UnAuthorized() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenThrow(
                new EntitlementsException("Invalid token", new HttpResponse(null, null, null, 401, null, null, 0L)));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals(SchemaConstants.UNAUTHORIZED_EXCEPTION, exception.getMessage());
    }

    @Test
    public void testHasRole_EntitlementException_Runtime() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenThrow(
                new EntitlementsException("Invalid token", new HttpResponse(null, null, null, 500, null, null, 0L)));

        assertThrows(RuntimeException.class, () -> authorizationFilter.hasRole("service.schema.admins"));
    }

    @Test
    public void testHasRole_Authorized() throws BadRequestException, EntitlementsException {

        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(en.getGroups()).thenReturn(getMockGrooups());

        assertEquals(true, authorizationFilter.hasRole("service.schema.admins"));

        Mockito.verify(headers).put(DpsHeaders.USER_EMAIL, "test@slb.com");
        Mockito.verify(headers).put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, "service.schema.admins");
    }

    @Test
    public void testHasRole_NoAuthorization_header() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getPartitionId()).thenReturn("test");

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals("Authorization header is mandatory", exception.getMessage());
    }

    @Test
    public void testHasRole_NoPartition_header() throws BadRequestException, EntitlementsException {
        Mockito.when(headers.getAuthorization()).thenReturn("test");

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> authorizationFilter.hasRole("service.schema.admins"));
        assertEquals("data-partition-id header is mandatory", exception.getMessage());
    }

    private Groups getMockGrooups() {

        Groups groups = new Groups();
        List<GroupInfo> groupInfoList = new ArrayList<GroupInfo>();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("service.schema.admins");
        groupInfo.setEmail("service.schema.admins");

        groupInfoList.add(groupInfo);
        groups.setGroups(groupInfoList);
        groups.setDesId("test@slb.com");

        return groups;

    }

}
