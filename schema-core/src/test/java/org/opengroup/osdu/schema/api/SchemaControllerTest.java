package org.opengroup.osdu.schema.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaInfoResponse;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.model.SchemaUpsertResponse;
import org.opengroup.osdu.schema.service.ISchemaService;
import org.opengroup.osdu.schema.validation.request.SchemaInfoRequestValidator;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaControllerTest {

    @Mock
    ISchemaService schemaService;
    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    SchemaInfoRequestValidator schemaInfoRequestValidator;
    @InjectMocks
    SchemaController schemaController;

    private SchemaRequest schemaRequest;

    @Test
    public void testGetSchema_SuccessScenario() throws ApplicationException, NotFoundException, BadRequestException {
        String schemaId = "testschema";
        when(schemaService.getSchema(schemaId)).thenReturn("{}");
        assertNotNull(schemaController.getSchema(schemaId));
    }

    @Test
    public void testCreateSchema()
            throws ApplicationException, NotFoundException, BadRequestException, JsonProcessingException {
        schemaRequest = getSchemaRequestObject();

        when(schemaService.createSchema(schemaRequest)).thenReturn(getSchemaInfoObject());
        assertNotNull(schemaController.createSchema(schemaRequest));

    }

    @Test
    public void testUpsertSchema_update() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestObject();

        when(schemaService.upsertSchema(schemaRequest)).thenReturn(getSchemaUpsertResponse_Updated());
        assertNotNull(schemaController.upsertSchema(schemaRequest));

    }

    @Test
    public void testUpsertSchema_create() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestObject();

        when(schemaService.upsertSchema(schemaRequest)).thenReturn(getSchemaUpsertResponse_Created());
        assertNotNull(schemaController.upsertSchema(schemaRequest));

    }
    
    @Test
    public void testUpsertSchema_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestObject();

        when(schemaService.upsertSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.upsertSchema(schemaRequest));
    }

    @Test
    public void testCreateEmptyAuthoritySchema_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestEmptyAuthority();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateEmptyEntitySchema_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestEmptySource();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateEmptySourceSchema_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestEmptyEntity();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateAuthoritySchemaSpecialCharacters_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestSpecialCharactersAuthority();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateSourceSchemaSpecialCharacters_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestSpecialCharactersSource();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateEntitySchemaSpecialCharacters_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestSpecialCharactersEntity();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testCreateAuthorityMultipleSpaces_Failed() throws ApplicationException, BadRequestException {
        schemaRequest = getSchemaRequestMultipleSpacesAuthority();

        when(schemaService.createSchema(schemaRequest)).thenThrow(BadRequestException.class);
        assertThrows(BadRequestException.class, () -> schemaController.createSchema(schemaRequest));
    }

    @Test
    public void testGetSchemaInfoList() throws ApplicationException, BadRequestException {
        // Create a custom HttpServletRequestWrapper to encapsulate the mock request
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpServletRequest);
        // Set the request attributes for the current thread
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestWrapper));
        schemaRequest = getSchemaRequestObject();

        when(schemaService.getSchemaInfoList(QueryParams.builder().authority("test").build()))
                .thenReturn(SchemaInfoResponse.builder().schemaInfos(new LinkedList<SchemaInfo>()).build());

        assertNotNull(
                schemaController.getSchemaInfoList("test", null, null, null, null, null, null, null, null, 100, 0));
        RequestContextHolder.resetRequestAttributes();
    }

    private SchemaRequest getSchemaRequestObject() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("well").id("os..wks.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("well").id("os..wks.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .build()).build();
    }
    
    private SchemaUpsertResponse getSchemaUpsertResponse_Created() {
        return SchemaUpsertResponse.builder().schemaInfo(getSchemaInfoObject()).httpCode(HttpStatus.CREATED).build();
    }
    
    private SchemaUpsertResponse getSchemaUpsertResponse_Updated() {
        return SchemaUpsertResponse.builder().schemaInfo(getSchemaInfoObject()).httpCode(HttpStatus.OK).build();
    }

    private SchemaInfo getSchemaInfoObject() {
        return SchemaInfo.builder().createdBy("creator").dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("well").id("os..wks.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("well").id("os..wks.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .build();
    }

    private SchemaRequest getSchemaRequestEmptyAuthority() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("").entityType("well").id("..wks.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("").entityType("well").id("..wks.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("wks").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestEmptyEntity() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("").id("os..well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("well").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("").id("os..well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("well").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestEmptySource() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("well").id("os.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("well").id("os.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestSpecialCharactersAuthority() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("  :").entityType("well").id("  :.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("   :").entityType("well").id("  :.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestSpecialCharactersSource() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("well").id("os.  :.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("  :").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("well").id("os.  :.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("  :").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestSpecialCharactersEntity() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("os").entityType("  :").id("os.source.  :.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("source").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("os").entityType("  :").id("os.source.  :.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("source").build())
                .build()).build();
    }

    private SchemaRequest getSchemaRequestMultipleSpacesAuthority() {
        return SchemaRequest.builder().schema(null).schemaInfo(SchemaInfo.builder().createdBy("creator")
                .dateCreated(new Date(System.currentTimeMillis()))
                .schemaIdentity(SchemaIdentity.builder().authority("     ").entityType("well").id("   .source.well.1.1")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("source").build())
                .scope(SchemaScope.INTERNAL).status(SchemaStatus.DEVELOPMENT)
                .supersededBy(SchemaIdentity.builder().authority("     ").entityType("well").id("   .source.well.1.4")
                        .schemaVersionMajor(1L).schemaVersionMinor(1L).source("source").build())
                .build()).build();
    }
}