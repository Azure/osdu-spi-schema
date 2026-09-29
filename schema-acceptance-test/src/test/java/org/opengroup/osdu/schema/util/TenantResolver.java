package org.opengroup.osdu.schema.util;

import org.opengroup.osdu.schema.constants.TestConstants;

public class TenantResolver {

    public static String resolveTenant(String tenant) {
        return switch (tenant) {
            case "TENANT1" -> TestConstants.dataPartitionId();
            case "COMMON"  -> TestConstants.sharedTenant();
            default        -> tenant;
        };
    }
}
