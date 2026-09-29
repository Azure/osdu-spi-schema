package org.opengroup.osdu.schema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class QueryParams {

    private String authority;
    private String source;
    private String entityType;
    private Long schemaVersionMajor;
    private Long schemaVersionMinor;
    private Long schemaVersionPatch;
    private int limit;
    private int offset;
    private String status;
    private String scope;
    private Boolean latestVersion;

}
