
package org.opengroup.osdu.schema.service.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.Source;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISourceStore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SourceServiceTest {

    @InjectMocks
    SourceService sourceService;

    @Mock
    Source mockSource;

    @Mock
    ISourceStore iSourceStore;

    @Test
    public void testCheckAndRegisterEntityIfNotPresent() throws NotFoundException, ApplicationException {
        Mockito.when(iSourceStore.get("testEntityType")).thenReturn(getMockSourceObject());
        assertEquals(true, sourceService.checkAndRegisterSourceIfNotPresent(mockSource.getSourceId()));
    }

    @Test
    public void testCheckAndRegisterEntityIfNotPresent_ApplicationException()
            throws ApplicationException, BadRequestException {
        getMockSourceObject();
        when(iSourceStore.create(mockSource)).thenThrow(ApplicationException.class);
        assertEquals(false, sourceService.checkAndRegisterSourceIfNotPresent(mockSource.getSourceId()));
    }

    @Test
    public void testCheckAndRegisterEntityIfNotPresent_BadRequestException()
            throws ApplicationException, BadRequestException {
        getMockSourceObject();
        when(iSourceStore.create(mockSource)).thenThrow(BadRequestException.class);
        assertEquals(true, sourceService.checkAndRegisterSourceIfNotPresent(mockSource.getSourceId()));
    }

    private Source getMockSourceObject() {
        mockSource = new Source();
        mockSource.setSourceId("testSourceId");
        return mockSource;
    }

}
