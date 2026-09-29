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

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import static org.springframework.util.StringUtils.hasText;
import org.springframework.web.filter.OncePerRequestFilter;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;

/**
 * Security filter for Azure Localhost Testing.
 * <p>
 * It is used for localhost testing purposes only and should not be used in production.
 * It is only enabled when the property <code>azure.localhosttesting.auth.enabled</code> is set to <code>true</code>.
 */
@Component
@ConditionalOnProperty(value = "azure.localhosttesting.auth.enabled", havingValue = "true", matchIfMissing = false)
public class AzureLocalhostTestingSecurityFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureLocalhostTestingSecurityFilter.class);

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final JSONArray DEFAULT_ROLE_CLAIM = new JSONArray().appendElement("USER");
    private static final String ROLE_PREFIX = "ROLE_";

    public AzureLocalhostTestingSecurityFilter() {
        LOGGER.info("AzureLocalhostTestingSecurityFilter is enabled.");
    }

    /**
     * Filter logic.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param filterChain Filter Chain object.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse, final FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = servletRequest.getHeader(AUTHORIZATION_HEADER_NAME);
        if (!hasText(authorizationHeader)){
            servletResponse.sendError(400, "No Authorization header found");
        }

        JWTClaimsSet claimsSet = null;
        String jwtToken = authorizationHeader.substring("Bearer ".length());
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwtToken);
            claimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            LOGGER.error("Failed to parse JWT token", e);
            servletResponse.sendError(400, "Invalid Authorization header");
        }

        LOGGER.debug("Received headers list: {}", Collections.list(servletRequest.getHeaderNames()));

        final JSONArray roles = Optional.ofNullable((JSONArray) claimsSet.getClaims().get("roles"))
            .filter(r -> !r.isEmpty())
            .orElse(DEFAULT_ROLE_CLAIM);
        
        SecurityContextHolder
                .getContext()
                .setAuthentication(
                    new PreAuthenticatedAuthenticationToken(
                        new UserPrincipal(null,null, claimsSet),
                        null,
                        rolesToGrantedAuthorities(roles)
                ));

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * To return roles.
     * @param roles Request Object.
     * @return set representation of roles.
     */
    protected Set<SimpleGrantedAuthority> rolesToGrantedAuthorities(final JSONArray roles) {
        return roles.stream()
                .filter(Objects::nonNull)
                .map(s -> new SimpleGrantedAuthority(ROLE_PREFIX + s))
                .collect(Collectors.toSet());
    }
}
