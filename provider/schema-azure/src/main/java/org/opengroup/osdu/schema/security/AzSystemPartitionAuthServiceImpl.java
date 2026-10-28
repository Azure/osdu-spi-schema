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

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.provider.interfaces.authorization.SystemPartitionAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Primary
public class AzSystemPartitionAuthServiceImpl implements SystemPartitionAuthService {
    @Autowired
    private DpsHeaders headers;

    @Autowired
    private IAuthorizationService entitlementsService;

    @Value("${azure.system.authorizationPartition:" + SchemaConstants.SYSTEM_PARTITION_NAME + "}")
    private String authorizationPartition;

    @Override
    public boolean hasSystemLevelPermissions() {
        String partition = StringUtils.hasText(authorizationPartition)
                ? authorizationPartition
                : SchemaConstants.SYSTEM_PARTITION_NAME;
        headers.put(SchemaConstants.DATA_PARTITION_ID, partition);
        String user = entitlementsService
                .authorizeAny(headers, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS)
                .getUser();

        headers.put(DpsHeaders.USER_EMAIL, user);
        headers.put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, SchemaConstants.ENTITLEMENT_SERVICE_SYSTEM_SCHEMA_EDITORS);
        return true;
    }
}
