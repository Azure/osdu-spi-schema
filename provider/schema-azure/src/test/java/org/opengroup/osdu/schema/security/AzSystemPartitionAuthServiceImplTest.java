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

package org.opengroup.osdu.schema.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AzSystemPartitionAuthServiceImplTest {
    @Spy
    private DpsHeaders headers;

    @Mock
    private IAuthorizationService entitlementsService;

    @InjectMocks
    AzSystemPartitionAuthServiceImpl sut;

    @Test
    public void shouldCallEntitlementsWithConfiguredPartition_when_hasPermissionsIsCalled() {
        ReflectionTestUtils.setField(sut, "authorizationPartition", "opendes");
        AuthorizationResponse authorizationResponse = mock(AuthorizationResponse.class);
        when(authorizationResponse.getUser()).thenReturn("user");
        when(entitlementsService.authorizeAny(headers, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS)).thenReturn(authorizationResponse);

        boolean hasPermissions = sut.hasSystemLevelPermissions();

        assertEquals("opendes", headers.getPartitionId());
        assertEquals("user", headers.getUserEmail());
        assertTrue(hasPermissions);
    }

    @Test
    public void shouldCallEntitlementsWithSystemPartition_when_hasPermissionsIsCalled() {
        ReflectionTestUtils.setField(sut, "authorizationPartition", SchemaConstants.SYSTEM_PARTITION_NAME);
        AuthorizationResponse authorizationResponse = mock(AuthorizationResponse.class);
        when(authorizationResponse.getUser()).thenReturn("user");
        when(entitlementsService.authorizeAny(headers, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS)).thenReturn(authorizationResponse);

        boolean hasPermissions = sut.hasSystemLevelPermissions();

        assertEquals("system", headers.getPartitionId());
        assertEquals("user", headers.getUserEmail());
        assertEquals(SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS, headers.getUserAuthorizedGroupName());
        assertTrue(hasPermissions);
    }

    @Test
    public void shouldCallEntitlementsWithSystemPartition_when_partitionIsBlank() {
        ReflectionTestUtils.setField(sut, "authorizationPartition", " ");
        AuthorizationResponse authorizationResponse = mock(AuthorizationResponse.class);
        when(authorizationResponse.getUser()).thenReturn("user");
        when(entitlementsService.authorizeAny(headers, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS)).thenReturn(authorizationResponse);

        sut.hasSystemLevelPermissions();

        assertEquals("system", headers.getPartitionId());
    }

    @Test
    public void shouldDefaultToSystemPartition_when_propertyIsUnset() {
        contextRunner().run(context -> assertEquals("system", partitionUsedBy(context)));
    }

    @Test
    public void shouldUseConfiguredPartition_when_propertyIsSet() {
        contextRunner()
                .withPropertyValues("azure.system.authorizationPartition=opendes")
                .run(context -> assertEquals("opendes", partitionUsedBy(context)));
    }

    private ApplicationContextRunner contextRunner() {
        AuthorizationResponse authorizationResponse = mock(AuthorizationResponse.class);
        when(authorizationResponse.getUser()).thenReturn("user");
        when(entitlementsService.authorizeAny(any(DpsHeaders.class), eq(SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS)))
                .thenReturn(authorizationResponse);
        return new ApplicationContextRunner()
                .withBean(DpsHeaders.class)
                .withBean(IAuthorizationService.class, () -> entitlementsService)
                .withBean(AzSystemPartitionAuthServiceImpl.class);
    }

    private static String partitionUsedBy(AssertableApplicationContext context) {
        context.getBean(AzSystemPartitionAuthServiceImpl.class).hasSystemLevelPermissions();
        return context.getBean(DpsHeaders.class).getPartitionId();
    }
}