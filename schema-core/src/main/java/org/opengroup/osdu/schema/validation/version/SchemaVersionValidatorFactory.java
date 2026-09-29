package org.opengroup.osdu.schema.validation.version;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaVersionValidatorFactory {

	@Autowired
	private List<VersionValidator> versionValidator;

	public  VersionValidator getVersionValidator(SchemaValidationType validatorType) {
		Optional<VersionValidator> schemaValidator = versionValidator.stream().filter(validator -> validator.getType().equals(validatorType))
				.findAny();
		
		return schemaValidator.orElseThrow(() ->  new IllegalArgumentException("Unknown validator type: " + validatorType));
	}
}
