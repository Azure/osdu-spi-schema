package org.opengroup.osdu.schema.service.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.Authority;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.IAuthorityStore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthorityServiceTest {

    @InjectMocks
    AuthorityService authorityService;

    @Mock
    IAuthorityStore mockIAuthorityStore;

    @Mock
    Authority mockAuthority;

    @Test
    public void testCheckAndRegisterAuthorityIfNotPresent() throws NotFoundException, ApplicationException {
        getMockAuthorityObject();
        when(mockIAuthorityStore.get("test")).thenReturn(getMockAuthorityObject());
        assertEquals(true,
                authorityService.checkAndRegisterAuthorityIfNotPresent(mockAuthority.getAuthorityId()));
    }

    @Test
    public void testCheckAndRegisterAuthorityIfNotPresent_ApplicationException()
            throws ApplicationException, BadRequestException {
        getMockAuthorityObject();
        when(mockIAuthorityStore.create(mockAuthority)).thenThrow(ApplicationException.class);
        assertEquals(false,
                authorityService.checkAndRegisterAuthorityIfNotPresent(mockAuthority.getAuthorityId()));
    }

    @Test
    public void testCheckAndRegisterAuthorityIfNotPresent_BadRequestException()
            throws ApplicationException, BadRequestException {
        getMockAuthorityObject();
        when(mockIAuthorityStore.create(mockAuthority)).thenThrow(BadRequestException.class);
        assertEquals(true,
                authorityService.checkAndRegisterAuthorityIfNotPresent(mockAuthority.getAuthorityId()));
    }

    private Authority getMockAuthorityObject() {
        mockAuthority = new Authority();
        mockAuthority.setAuthorityId("test");
        return mockAuthority;
    }
}
