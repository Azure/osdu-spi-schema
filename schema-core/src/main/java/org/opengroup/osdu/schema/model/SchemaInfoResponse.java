package org.opengroup.osdu.schema.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "Schema Info Response", description = "The response for a GET schema request")
public class SchemaInfoResponse {

    @ArraySchema(schema = @Schema(implementation = SchemaInfo.class))
    List<SchemaInfo> schemaInfos;

    @Schema(description = "The offset for the next query", minimum = "0")
    int offset;

    @Schema(description = "The number of schema versions in this response", minimum = "0")
    int count;

    @Schema(description = "The total number of entity type codes in the repositories", minimum = "0")
    int totalCount;
}
