package org.opengroup.osdu.schema.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.model.SchemaUpsertResponse;
import org.opengroup.osdu.schema.service.ISchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/schemas/system")
@Tag(name = "system-schema-api", description = "System Schema API - System Schema related endpoints")
public class SystemSchemaController {

    @Autowired
    ISchemaService schemaService;

    @Operation(summary = "${systemSchemaApi.upsertSystemSchema.summary}", description = "${systemSchemaApi.upsertSystemSchema.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "system-schema-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema updated", content = { @Content(schema = @Schema(implementation = SchemaInfo.class)) }),
            @ApiResponse(responseCode = "201", description = "Schema created", content = { @Content(schema = @Schema(implementation = SchemaInfo.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PutMapping()
    @PreAuthorize("@authorizationFilterSA.hasPermissions()")
    public ResponseEntity<SchemaInfo> upsertSystemSchema(@Valid @RequestBody SchemaRequest schemaRequest)
            throws ApplicationException, BadRequestException {
        SchemaUpsertResponse upsertResp = schemaService.upsertSystemSchema(schemaRequest);
        ResponseEntity<SchemaInfo> response = new ResponseEntity<>(upsertResp.getSchemaInfo(), upsertResp.getHttpCode());
        return response;
    }
}
