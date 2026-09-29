package org.opengroup.osdu.schema.security;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component("authorizationFilter")
@RequestScope
public class AuthorizationFilter {

    @Autowired
    IEntitlementsFactory entitlementsClientFactory;

    @Autowired
    private DpsHeaders headers;

    @Autowired
    JaxRsDpsLog log;

    public boolean hasRole(String... requiredRoles) throws BadRequestException {
        validateMandatoryHeaders();
        IEntitlementsService service = this.entitlementsClientFactory.create(this.headers);

        try {
            Groups user = service.getGroups();
            String userAuthorizedGroupName = user.getMatchingGroupName(requiredRoles);

            if (userAuthorizedGroupName != null) {
                this.headers.put(DpsHeaders.USER_EMAIL, user.getDesId());
                this.headers.put(DpsHeaders.USER_AUTHORIZED_GROUP_NAME, userAuthorizedGroupName);
                log.info("User authenticated");
                return true;
            } else {
                throw new UnauthorizedException(SchemaConstants.UNAUTHORIZED_EXCEPTION);
            }
        } catch (EntitlementsException e) {
            if (Arrays.asList(400).contains(e.getHttpResponse().getResponseCode())) {
                throw new BadRequestException(SchemaConstants.BAD_INPUT);
            } else if (Arrays.asList(401, 403).contains(e.getHttpResponse().getResponseCode())) {
                throw new UnauthorizedException(SchemaConstants.UNAUTHORIZED_EXCEPTION);
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    private void validateMandatoryHeaders() {

        if (StringUtils.isEmpty(this.headers.getAuthorization())) {
            throw new UnauthorizedException("Authorization header is mandatory");
        }

        if (StringUtils.isEmpty(this.headers.getPartitionId())) {
            throw new UnauthorizedException("data-partition-id header is mandatory");
        }

    }
}
