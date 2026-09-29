package org.opengroup.osdu.schema.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaInfoResponse;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.model.SchemaUpsertResponse;
import org.opengroup.osdu.schema.service.ISchemaService;
import org.opengroup.osdu.schema.validation.request.SchemaInfoRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import static org.opengroup.osdu.schema.constants.SchemaConstants.GET_SCHEMA_200_RESPONSE;

@RestController
@RequestMapping(value = "schema", produces = "application/json")
@Tag(name = "schema-api", description = "Schema API - Core Schema related endpoints")
public class SchemaController {

    @Autowired
    ISchemaService schemaService;
    @Autowired
    SchemaInfoRequestValidator schemaInfoRequestValidator;

    @Operation(summary = "${schemaApi.createSchema.summary}", description = "${schemaApi.createSchema.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "schema-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schema created", content = { @Content(schema = @Schema(implementation = SchemaInfo.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PostMapping()
    @PreAuthorize("@authorizationFilter.hasRole('" + SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS + "')")
    public ResponseEntity<SchemaInfo> createSchema(@Valid @RequestBody SchemaRequest schemaRequest)
            throws ApplicationException, BadRequestException {
        return new ResponseEntity<>(schemaService.createSchema(schemaRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "${schemaApi.getSchema.summary}", description = "${schemaApi.getSchema.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "schema-api" })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Schema retrieved successfully",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object"),
                                    examples = {
                                            @ExampleObject(value = GET_SCHEMA_200_RESPONSE)
                                    }
                            )
                    }
            ),
            @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested Schema not found in repository",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @GetMapping("/{id}")
    @PreAuthorize("@authorizationFilter.hasRole('" + SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS + "')")
    public ResponseEntity<Object> getSchema(@Parameter(description = "The system id of the schema", in = ParameterIn.PATH,
                                            example = "osdu:wks:wellbore:1.0.0") @PathVariable("id") String id)
            throws ApplicationException, NotFoundException, BadRequestException {
        return new ResponseEntity<>(schemaService.getSchema(id), HttpStatus.OK);
    }

    @Operation(summary = "${schemaApi.getSchemaInfoList.summary}", description = "${schemaApi.getSchemaInfoList.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "schema-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = SchemaInfoResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad user input. Mandatory fields missing or unacceptable value passed to API",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested Schema not found in repository",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @GetMapping()
    @PreAuthorize("@authorizationFilter.hasRole('" + SchemaConstants.ENTITLEMENT_SERVICE_GROUP_VIEWERS + "')")
    public ResponseEntity<SchemaInfoResponse> getSchemaInfoList(
            @Parameter(description = "pass an optional string to search for a specific authority", example = "osdu")
            @RequestParam(required = false, name = "authority") String authority,
            @Parameter(description = "pass an optional string to search for a specific source", example = "wks")
            @RequestParam(required = false, name = "source") String source,
            @Parameter(description = "pass an optional string to search for a specific entityType", example = "wellbore")
            @RequestParam(required = false, name = "entityType") String entityType,
            @Parameter(description = "pass an optional string to search for a specific schemaVersionMajor", example = "1")
            @RequestParam(required = false, name = "schemaVersionMajor") Long schemaVersionMajor,
            @Parameter(description = "pass an optional string to search for a specific schemaVersionMinor", example = "1")
            @RequestParam(required = false, name = "schemaVersionMinor") Long schemaVersionMinor,
            @Parameter(description = "pass an optional string to search for a specific schemaVersionPatch", example = "0")
            @RequestParam(required = false, name = "schemaVersionPatch") Long schemaVersionPatch,
            @Parameter(description = "The schema status specification", example = "PUBLISHED", schema = @Schema(type = "string", defaultValue = "PUBLISHED"))
            @RequestParam(required = false, name = "status") String status,
            @Parameter(description = "The scope or schema visibility specification", example = "INTERNAL", schema = @Schema(type = "string", defaultValue = "INTERNAL"))
            @RequestParam(required = false, name = "scope") String scope,
            @Parameter(description = "if True, only return the latest version", example = "True", schema = @Schema(type = "boolean", defaultValue = "false"))
            @RequestParam(required = false, name = "latestVersion") Boolean latestVersion,
            @Parameter(description = "maximum number of schema records to return", example = "10", schema = @Schema(type = "integer", minimum = "0", maximum = "100"))
            @RequestParam(required = false, name = "limit", defaultValue = "100") int limit,
            @Parameter(description = "number of records to skip for pagination", example = "0", schema = @Schema(type = "integer", minimum = "0"))
            @RequestParam(required = false, name = "offset", defaultValue = "0") int offset)
            throws ApplicationException, BadRequestException {
        schemaInfoRequestValidator.validateRequest(schemaInfoRequestValidator.extractQueryParamsFromRequest());
        QueryParams queryParams = QueryParams.builder().authority(authority).source(source).entityType(entityType)
                .schemaVersionMajor(schemaVersionMajor).schemaVersionMinor(schemaVersionMinor)
                .schemaVersionPatch(schemaVersionPatch).limit(limit).offset(offset).scope(scope).status(status)
                .latestVersion(latestVersion).build();
        return new ResponseEntity<>(schemaService.getSchemaInfoList(queryParams), HttpStatus.OK);
    }

    @Operation(summary = "${schemaApi.upsertSchema.summary}", description = "${schemaApi.upsertSchema.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "schema-api" })
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
    @PreAuthorize("@authorizationFilter.hasRole('" + SchemaConstants.ENTITLEMENT_SERVICE_GROUP_EDITORS + "')")
    public ResponseEntity<SchemaInfo> upsertSchema(@Valid @RequestBody SchemaRequest schemaRequest)
            throws ApplicationException, BadRequestException {

        SchemaUpsertResponse upsertResp = schemaService.upsertSchema(schemaRequest);
        ResponseEntity<SchemaInfo> response = new ResponseEntity<>(upsertResp.getSchemaInfo(), upsertResp.getHttpCode());
        return response;
    }
}
