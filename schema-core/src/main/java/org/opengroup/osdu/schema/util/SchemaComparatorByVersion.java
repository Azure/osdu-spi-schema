package org.opengroup.osdu.schema.util;

import java.util.Comparator;

import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;

public class SchemaComparatorByVersion implements Comparator<SchemaInfo> {

	@Override
	public int compare(SchemaInfo sch1, SchemaInfo sch2) {
		SchemaIdentity schemaIdentity1 = sch1.getSchemaIdentity();
		SchemaIdentity schemaIdentity2 = sch2.getSchemaIdentity();

		Comparator<SchemaIdentity> compareByMajor = (s1,s2) -> s1.getSchemaVersionMajor().compareTo(s2.getSchemaVersionMajor());

		Comparator<SchemaIdentity> compareByMinor = (s1,s2) -> s1.getSchemaVersionMinor().compareTo(s2.getSchemaVersionMinor());

		Comparator<SchemaIdentity> compareByPatch = (s1,s2) -> s1.getSchemaVersionPatch().compareTo(s2.getSchemaVersionPatch());

		return compareByMajor.thenComparing(compareByMinor).thenComparing(compareByPatch).compare(schemaIdentity1, schemaIdentity2);
	}


}