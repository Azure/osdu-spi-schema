package org.opengroup.osdu.schema.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntitlementClientFactoryTest {

    @InjectMocks
    EntitlementsClientFactory entitlementsClientFactory;

    @Test
    public void test_createInstance() throws Exception {

        assertNotNull(entitlementsClientFactory.createInstance());
    }

    @Test
    public void test_getObjectType() throws Exception {

        assertNotNull(entitlementsClientFactory.getObjectType());
    }

}
