package org.opengroup.osdu.schema.provider.azure.service.serviceimpl;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipal;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.schema.azure.service.serviceimpl.AuthorizationServiceForServiceAdminImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceForServiceAdminImplTest {
    @Mock
    private Authentication auth;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationServiceForServiceAdminImpl authorizationService;

    @Before
    public void setup() {
        securityContext = Mockito.mock(SecurityContext.class);
        auth = Mockito.mock(Authentication.class);
    }

    private UserPrincipal createAADUserPrincipal(String claimName, String claimValue, String issuer) {
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                //.subject("subject")
                .claim(claimName, claimValue)
                .issuer(issuer)
                .build();
        final JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new Payload(jwtClaimsSet.toString()));
        return new UserPrincipal("token", jwsObject, jwtClaimsSet);
    }

    private DummyAuthToken createSAuthToken(final String email, final String appcode) {
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim("email", email)
                .claim("appcode", appcode)
                .issuer("sauth-preview.slb.com")
                .build();
        final JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new Payload(jwtClaimsSet.toString()));
        return new DummyAuthToken(jwsObject, jwtClaimsSet);
    }

    private void createSAuthTokenSetSecurityContext(final String email, final String appcode) {
        DummyAuthToken dummyAuthToken = createSAuthToken(email, appcode);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAuthToken);
    }

    private UserPrincipal createAADUserPrincipalSetSecurityContext(String claimName, String claimValue, String issuer) {
        UserPrincipal dummyAADPrincipal = createAADUserPrincipal(claimName, claimValue, issuer);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAADPrincipal);
        return dummyAADPrincipal;
    }

    @Test
    public void shouldReturnFalseWhenSAuthTokenIsSetInContext() {
        createSAuthTokenSetSecurityContext("email", null);
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnTrueWhenAADTokenIsSetInContext_AndIssuerIsAAD() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getAadIssuer());
        assertTrue(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnTrueWhenAADTokenIsSetInContext_AndIssuerIsAADV2() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getAadIssuerV2());
        assertTrue(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnFalseWhenAADTokenIsSetInContext_AndIssuerIsNotAAD() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getNonAadIssuer());
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Getter
    public class DummyAuthToken {
        private final JWSObject jwsObject;
        private final JWTClaimsSet claimsSet;

        public DummyAuthToken(JWSObject jwsObject, JWTClaimsSet claimsSet) {
            this.jwsObject = jwsObject;
            this.claimsSet = claimsSet;
        }

        public String getIssuer() {
            return claimsSet.getIssuer();
        }
    }
}
