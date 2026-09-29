package org.opengroup.osdu.schema.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResponseHeaderFilterTest {

    @InjectMocks
    ResponseHeaderFIlter responseHeaderFIlter;

    @Mock
    DpsHeaders dpsHeaders;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    FilterChain chain;

    @Test
    public void test_ResponseHeaderFiler_Options() throws IOException, ServletException {

        Mockito.when(httpServletRequest.getMethod()).thenReturn("OPTIONS");
        org.springframework.test.util.ReflectionTestUtils.setField(responseHeaderFIlter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");
        responseHeaderFIlter.doFilter(httpServletRequest, httpServletResponse, chain);
        assertNotNull(httpServletResponse);
        responseHeaderFIlter.destroy();

    }

    @Test
    public void test_ResponseHeaderFiler_Get() throws IOException, ServletException {

        Mockito.when(httpServletRequest.getMethod()).thenReturn("GET");
        org.springframework.test.util.ReflectionTestUtils.setField(responseHeaderFIlter, "ACCESS_CONTROL_ALLOW_ORIGIN_DOMAINS", "custom-domain");
        responseHeaderFIlter.doFilter(httpServletRequest, httpServletResponse, chain);
        assertNotNull(httpServletResponse);
        responseHeaderFIlter.destroy();

    }

}
