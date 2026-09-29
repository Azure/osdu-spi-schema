package org.opengroup.osdu.schema.service.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.EntityType;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.IEntityTypeStore;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityTypeServiceTest {

    @InjectMocks
    EntityTypeService entityTypeService;

    @Mock
    EntityType mockEntityType;

    @Mock
    IEntityTypeStore mockEntityStore;

    @Test
    public void testCheckAndRegisterEntityIfNotPresent() throws NotFoundException, ApplicationException {
        when(mockEntityStore.get("testEntityType")).thenReturn(getMockEntityObject());
        assertEquals(true,
                entityTypeService.checkAndRegisterEntityTypeIfNotPresent(mockEntityType.getEntityTypeId()));
    }

    @Test
    public void testCheckAndRegisterEntityIfNotPresent_ApplicationException()
            throws ApplicationException, BadRequestException {
        getMockEntityObject();
        when(mockEntityStore.create(mockEntityType)).thenThrow(ApplicationException.class);
        assertEquals(false,
                entityTypeService.checkAndRegisterEntityTypeIfNotPresent(mockEntityType.getEntityTypeId()));
    }

    @Test
    public void testCheckAndRegisterEntityIfNotPresent_BadRequestException()
            throws ApplicationException, BadRequestException {
        getMockEntityObject();
        when(mockEntityStore.create(mockEntityType)).thenThrow(BadRequestException.class);
        assertEquals(true,
                entityTypeService.checkAndRegisterEntityTypeIfNotPresent(mockEntityType.getEntityTypeId()));
    }

    private EntityType getMockEntityObject() {
        mockEntityType = new EntityType();
        mockEntityType.setEntityTypeId("test");
        return mockEntityType;
    }

}
