package org.opengroup.osdu.schema.azure.impl.messagebus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaPubSubInfo {
	private String kind;
	private String op;
}
