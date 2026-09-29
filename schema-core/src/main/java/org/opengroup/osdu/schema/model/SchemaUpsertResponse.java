package org.opengroup.osdu.schema.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaUpsertResponse {

	SchemaInfo schemaInfo;
	HttpStatus httpCode;
	
}
