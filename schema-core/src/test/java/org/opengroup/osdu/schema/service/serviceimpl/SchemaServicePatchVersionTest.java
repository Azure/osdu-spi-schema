package org.opengroup.osdu.schema.service.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.logging.AuditLogger;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.provider.interfaces.messagebus.IMessageBus;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISchemaInfoStore;
import org.opengroup.osdu.schema.provider.interfaces.schemastore.ISchemaStore;
import org.opengroup.osdu.schema.service.IAuthorityService;
import org.opengroup.osdu.schema.service.IEntityTypeService;
import org.opengroup.osdu.schema.service.ISourceService;
import org.opengroup.osdu.schema.util.JSONUtil;
import org.opengroup.osdu.schema.util.SchemaResolver;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.validation.version.SchemaPatchVersionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(SchemaServicePatchVersionTest.Config.class)
@MockitoBean(types = {AuditLogger.class, JaxRsDpsLog.class, IMessageBus.class})
class SchemaServicePatchVersionTest {

    private static final String PARTITION = "opendes";
    private static final String PARENT = "osdu:wks:Parent:1.0.";

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private SchemaResolver schemaResolver;

    @MockitoBean
    private ISchemaInfoStore schemaInfoStore;

    @MockitoBean
    private ISchemaStore schemaStore;

    @MockitoBean
    private DpsHeaders headers;

    @MockitoBean
    private IAuthorityService authorityService;

    @MockitoBean
    private ISourceService sourceService;

    @MockitoBean
    private IEntityTypeService entityTypeService;

    @ParameterizedTest
    @MethodSource("patchVersionOrders")
    void shouldCreateCompatiblePatchInEitherOrder(boolean system, long incomingPatch, List<Long> existingPatches)
            throws ApplicationException, BadRequestException, NotFoundException {
        SchemaRequest request = prepareRequest(system, incomingPatch, existingPatches, false);

        SchemaInfo created = system ? schemaService.upsertSystemSchema(request).getSchemaInfo()
                : schemaService.createSchema(request);

        assertEquals(request.getSchemaInfo().getSchemaIdentity().getId(), created.getSchemaIdentity().getId());
        assertEquals(system ? SchemaScope.SHARED : SchemaScope.INTERNAL, created.getScope());
        if (system) {
            verify(schemaStore).createSystemSchema(eq(created.getSchemaIdentity().getId()), anyString());
        } else {
            verify(schemaStore).createSchema(eq(created.getSchemaIdentity().getId()), anyString());
        }
    }

    @ParameterizedTest
    @MethodSource("patchVersionOrders")
    void shouldRejectBreakingPatchInEitherOrder(boolean system, long incomingPatch, List<Long> existingPatches)
            throws ApplicationException, BadRequestException, NotFoundException {
        SchemaRequest request = prepareRequest(system, incomingPatch, existingPatches, true);

        BadRequestException error = assertThrows(BadRequestException.class, () -> {
            if (system) {
                schemaService.upsertSystemSchema(request);
            } else {
                schemaService.createSchema(request);
            }
        });

        assertTrue(error.getMessage().contains("Patch version validation failed"));
        verify(schemaStore, never()).createSchema(anyString(), anyString());
        verify(schemaStore, never()).createSystemSchema(anyString(), anyString());
    }

    static Stream<Arguments> patchVersionOrders() {
        return Stream.of(false, true).flatMap(system -> Stream.of(
                Arguments.of(system, 0L, List.of(1L)),
                Arguments.of(system, 1L, List.of(0L)),
                Arguments.of(system, 1L, List.of(0L, 2L))));
    }

    private SchemaRequest prepareRequest(boolean system, long incomingPatch, List<Long> existingPatches,
            boolean breaking) throws ApplicationException, BadRequestException, NotFoundException {
        when(headers.getPartitionId()).thenReturn(PARTITION);
        for (long patch = 0; patch <= 2; patch++) {
            when(schemaStore.getSchema(PARTITION, PARENT + patch)).thenThrow(new NotFoundException());
            when(schemaStore.getSystemSchema(PARENT + patch))
                    .thenReturn("{\"type\":\"object\",\"properties\":{\"value\":{\"type\":\"string\"}}}");
        }
        List<SchemaInfo> existing = new ArrayList<>();
        for (long patch : existingPatches) {
            SchemaInfo info = schemaInfo(patch, system);
            existing.add(info);
            String resolved = schemaResolver.resolveSchema(schema(patch, breaking));
            if (system) {
                when(schemaStore.getSystemSchema(info.getSchemaIdentity().getId())).thenReturn(resolved);
            } else {
                when(schemaStore.getSchema(PARTITION, info.getSchemaIdentity().getId())).thenReturn(resolved);
            }
        }
        SchemaRequest request = SchemaRequest.builder().schemaInfo(schemaInfo(incomingPatch, system))
                .schema(new JSONObject(schema(incomingPatch, breaking)).toMap()).build();
        if (system) {
            when(schemaInfoStore.getSystemSchemaInfo(request.getSchemaInfo().getSchemaIdentity().getId()))
                    .thenThrow(new NotFoundException());
            when(schemaInfoStore.isUniqueSystemSchema(anyString())).thenReturn(true);
            when(schemaInfoStore.getSystemSchemaInfoList(any(QueryParams.class))).thenReturn(existing);
            when(schemaInfoStore.createSystemSchemaInfo(request)).thenReturn(request.getSchemaInfo());
            when(authorityService.checkAndRegisterSystemAuthorityIfNotPresent("osdu")).thenReturn(true);
            when(sourceService.checkAndRegisterSystemSourceIfNotPresent("wks")).thenReturn(true);
            when(entityTypeService.checkAndRegisterSystemEntityTypeIfNotPresent("Child")).thenReturn(true);
        } else {
            when(schemaInfoStore.isUnique(anyString(), eq(PARTITION))).thenReturn(true);
            when(schemaInfoStore.getSchemaInfoList(any(QueryParams.class), eq(PARTITION))).thenReturn(existing);
            when(schemaInfoStore.createSchemaInfo(request)).thenReturn(request.getSchemaInfo());
            when(authorityService.checkAndRegisterAuthorityIfNotPresent("osdu")).thenReturn(true);
            when(sourceService.checkAndRegisterSourceIfNotPresent("wks")).thenReturn(true);
            when(entityTypeService.checkAndRegisterEntityTypeIfNotPresent("Child")).thenReturn(true);
        }
        return request;
    }

    private static SchemaInfo schemaInfo(long patch, boolean system) {
        return SchemaInfo.builder().status(SchemaStatus.PUBLISHED)
                .scope(system ? SchemaScope.SHARED : SchemaScope.INTERNAL)
                .schemaIdentity(SchemaIdentity.builder().authority("osdu").source("wks").entityType("Child")
                        .schemaVersionMajor(1L).schemaVersionMinor(0L).schemaVersionPatch(patch)
                        .id("osdu:wks:Child:1.0." + patch).build())
                .build();
    }

    private static String schema(long patch, boolean breaking) {
        if (breaking) {
            return "{\"type\":\"object\",\"properties\":{\"value\":{\"type\":\""
                    + (patch == 0 ? "string" : "number") + "\"}}}";
        }
        return "{\"type\":\"object\",\"properties\":{\"nested\":{\"$ref\":\"" + PARENT + patch + "\"}}}";
    }

    @Configuration
    @Import({SchemaService.class, SchemaResolver.class, SchemaUtil.class, JSONUtil.class})
    @ComponentScan(basePackageClasses = SchemaPatchVersionValidator.class)
    static class Config {
    }
}
