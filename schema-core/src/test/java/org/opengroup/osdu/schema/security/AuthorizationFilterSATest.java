package org.opengroup.osdu.schema.security;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opengroup.osdu.schema.constants.SchemaConstants.WORKFLOW_SYSTEM_ADMIN;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.entitlements.EntitlementsService;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.provider.interfaces.authorization.IAuthorizationServiceForServiceAdmin;
import org.opengroup.osdu.schema.provider.interfaces.authorization.SystemPartitionAuthService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorizationFilterSATest {

    @Mock
    IEntitlementsFactory entitlementsFactory;

    @Mock
    DpsHeaders headers;

    @Mock
    EntitlementsService en;

    @Mock
    JaxRsDpsLog log;

    @Mock
    SystemPartitionAuthService authorizationServiceForSystemPartition;

    @Mock
    IAuthorizationServiceForServiceAdmin authorizationServiceForServiceAdmin;

    @InjectMocks
    AuthorizationFilterSA authorizationFilterSA;


    @Test
    public void testRuntimeException_when_DataPartitionIsPresentInSystemHeader()  {
        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(headers.getPartitionId()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authorizationFilterSA.hasPermissions());
        org.junit.jupiter.api.Assertions.assertEquals("data-partition-id header should not be passed", exception.getMessage());
    }


    @Test
    public void testSystemAuthentication_is_checked_when_NotServiceAccount()  {

        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(authorizationServiceForServiceAdmin.isDomainAdminServiceAccount()).thenReturn(false);

        boolean response = authorizationFilterSA.hasPermissions();

        Mockito.verify(authorizationServiceForSystemPartition, Mockito.times(1)).hasSystemLevelPermissions();
        assertFalse(response);
    }

    @Test
    public void testSystemAuthentication_is_not_checked_when_ServiceAccount()  {

        Mockito.when(headers.getAuthorization()).thenReturn("test");
        Mockito.when(entitlementsFactory.create(headers)).thenReturn(en);
        Mockito.when(authorizationServiceForServiceAdmin.isDomainAdminServiceAccount()).thenReturn(true);

        boolean response = authorizationFilterSA.hasPermissions();

        Mockito.verify(authorizationServiceForSystemPartition, Mockito.never()).hasSystemLevelPermissions();
        Mockito.verify(headers).put(DpsHeaders.USER_EMAIL, AuthorizationFilterSA.SERVICE_ADMIN_USER);
        Mockito.verify(headers).put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, WORKFLOW_SYSTEM_ADMIN);
        assertTrue(response);
    }
}
