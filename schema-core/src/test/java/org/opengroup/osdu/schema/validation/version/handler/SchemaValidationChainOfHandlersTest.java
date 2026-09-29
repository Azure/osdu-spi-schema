package org.opengroup.osdu.schema.validation.version.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengroup.osdu.schema.validation.version.handler.common.AdditionalPropertiesHandler;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaValidationChainOfHandlersTest {

	@Mock
	private List<SchemaValidationHandler> chainOfValidationHandlers;

	@InjectMocks
	SchemaValidationChainOfHandlers schemaValidationChainOfHandlers;

	@Test
	public void testGetFirstHandler() {
		Mockito.when(chainOfValidationHandlers.get(Mockito.anyInt())).thenReturn( new AdditionalPropertiesHandler() );
		assertNotNull(schemaValidationChainOfHandlers.getFirstHandler());
	}
}
