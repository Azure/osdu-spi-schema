package org.opengroup.osdu.schema.validation.version.model;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

public class SchemaPatchRefComp implements Comparator<SchemaPatch> {

	@Override
	public int compare(SchemaPatch patch1, SchemaPatch patch2) {
		
		String attributeName1 = StringUtils.substringAfterLast(patch1.getPath(), "/");
		String attributeName2 = StringUtils.substringAfterLast(patch2.getPath(), "/");
		
		if("$ref".equals(attributeName1)) {
			return 1;
		}else if("$ref".equals(attributeName2)) {
			return -1;
		}
		
		return 0;
	}

}