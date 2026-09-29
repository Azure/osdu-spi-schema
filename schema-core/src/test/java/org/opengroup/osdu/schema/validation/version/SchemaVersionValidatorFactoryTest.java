package org.opengroup.osdu.schema.validation.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaVersionValidatorFactoryTest {
	
	@InjectMocks 
	private SchemaVersionValidatorFactory schemaVersionValidatorFactory;
	
	@Mock
	private List<VersionValidator> versionValidator;
	
	@Test
	public void testGetPatchValidator() {
		VersionValidator[] arr = { new SchemaPatchVersionValidator() };
		Mockito.when(versionValidator.stream()).thenReturn(Arrays.stream(arr));
		assertThat(schemaVersionValidatorFactory.getVersionValidator(SchemaValidationType.PATCH)).isNotNull();
	}
	
	@Test
	public void testGetMinorValidator() {
		VersionValidator[] arr = { new SchemaMinorVersionValidator() };
		Mockito.when(versionValidator.stream()).thenReturn(Arrays.stream(arr));
		assertThat(schemaVersionValidatorFactory.getVersionValidator(SchemaValidationType.MINOR)).isNotNull();
	}
	
	@Test
	public void testGetVersionValidator_Fail() {
		VersionValidator[] arr = { new SchemaMinorVersionValidator() };
		Mockito.when(versionValidator.stream()).thenReturn(Arrays.stream(arr));
		assertThrows(IllegalArgumentException.class,
				() -> schemaVersionValidatorFactory.getVersionValidator(SchemaValidationType.COMMON));
	}
}
