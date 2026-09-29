package org.opengroup.osdu.schema.security;

import static org.opengroup.osdu.schema.constants.SchemaConstants.WORKFLOW_SYSTEM_ADMIN;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.UnauthorizedException;
import org.opengroup.osdu.schema.provider.interfaces.authorization.IAuthorizationServiceForServiceAdmin;
import org.opengroup.osdu.schema.provider.interfaces.authorization.SystemPartitionAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("authorizationFilterSA")
@RequestScope
public class AuthorizationFilterSA {
    public static final String SERVICE_ADMIN_USER = "ServiceAdminUser";
    @Autowired
    private DpsHeaders headers;

    @Autowired
    private IAuthorizationServiceForServiceAdmin authorizationServiceForServiceAdmin;

    @Autowired
    private SystemPartitionAuthService authorizationServiceForSystemPartition;

    public boolean hasPermissions()  {
        validateMandatoryHeaders();
        if (!StringUtils.isEmpty(this.headers.getPartitionId())) {
            throw new BadRequestException("data-partition-id header should not be passed");
        }

        if(authorizationServiceForServiceAdmin.isDomainAdminServiceAccount())
        {
            headers.put(DpsHeaders.USER_EMAIL, SERVICE_ADMIN_USER);
            headers.put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, WORKFLOW_SYSTEM_ADMIN);
            return true;
        }

        return authorizationServiceForSystemPartition.hasSystemLevelPermissions();
    }

    private void validateMandatoryHeaders() {
        if (StringUtils.isEmpty(this.headers.getAuthorization())) {
            throw new UnauthorizedException("Authorization header is mandatory");
        }
    }
}
