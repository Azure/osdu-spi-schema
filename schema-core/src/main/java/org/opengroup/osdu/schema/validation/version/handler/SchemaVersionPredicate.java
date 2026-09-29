package org.opengroup.osdu.schema.validation.version.handler;

@FunctionalInterface
public interface SchemaVersionPredicate {

	boolean compare(String oldKind, String newKind);

}
