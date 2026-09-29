package org.opengroup.osdu.schema.validation.version;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.opengroup.osdu.schema.exceptions.SchemaVersionException;
import org.opengroup.osdu.schema.validation.version.model.SchemaBreakingChanges;
import org.opengroup.osdu.schema.validation.version.model.SchemaPatch;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaPatchVersionValidatorTest {

	@InjectMocks 
	private SchemaPatchVersionValidator patchVersionValidator;

	@Test 
	public void testGetType() {
		assertTrue(patchVersionValidator.getType() == SchemaValidationType.PATCH);
	}

	@Test 
	public void testHandleBreakingChanges_NoException() {
		try {
			patchVersionValidator.handleBreakingChanges(new ArrayList<SchemaBreakingChanges>());
			assertTrue(true);
		} catch (SchemaVersionException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testHandleBreakingChanges_Exception() {
		List<SchemaBreakingChanges> breakingChanges = new ArrayList<SchemaBreakingChanges>();
		breakingChanges.add(new SchemaBreakingChanges(new SchemaPatch(), "It's breaking change"));
		assertThrows(SchemaVersionException.class, () -> patchVersionValidator.handleBreakingChanges(breakingChanges));
	}
}
