package org.opengroup.osdu.schema.validation.version;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
public class SchemaMinorVersionValidatorTest {

	@InjectMocks 
	SchemaMinorVersionValidator minorVersionValidator;

	@Test 
	public void testGetType() {
		assertTrue(minorVersionValidator.getType() == SchemaValidationType.MINOR);
	}

	@Test 
	public void testHandleBreakingChanges_NoException() {
		try {
			minorVersionValidator.handleBreakingChanges(new ArrayList<SchemaBreakingChanges>());
			assertTrue(true);
		} catch (SchemaVersionException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testHandleBreakingChanges_Exception() {
		List<SchemaBreakingChanges> breakingChanges = new ArrayList<SchemaBreakingChanges>();
		breakingChanges.add(new SchemaBreakingChanges(new SchemaPatch(), "It's breaking change"));
		assertThrows(SchemaVersionException.class, () -> minorVersionValidator.handleBreakingChanges(breakingChanges));
	}
}
