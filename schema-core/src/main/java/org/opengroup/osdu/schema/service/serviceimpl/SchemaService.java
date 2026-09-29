package org.opengroup.osdu.schema.service.serviceimpl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.enums.SchemaScope;
import org.opengroup.osdu.schema.enums.SchemaStatus;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NoSchemaFoundException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.exceptions.SchemaVersionException;
import org.opengroup.osdu.schema.logging.AuditLogger;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaIdentity;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaInfoResponse;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.model.SchemaUpsertResponse;
import org.opengroup.osdu.schema.provider.interfaces.messagebus.IMessageBus;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISchemaInfoStore;
import org.opengroup.osdu.schema.provider.interfaces.schemastore.ISchemaStore;
import org.opengroup.osdu.schema.service.IAuthorityService;
import org.opengroup.osdu.schema.service.IEntityTypeService;
import org.opengroup.osdu.schema.service.ISchemaService;
import org.opengroup.osdu.schema.service.ISourceService;
import org.opengroup.osdu.schema.util.SchemaComparatorByVersion;
import org.opengroup.osdu.schema.util.SchemaResolver;
import org.opengroup.osdu.schema.util.SchemaUtil;
import org.opengroup.osdu.schema.validation.version.SchemaValidationType;
import org.opengroup.osdu.schema.validation.version.SchemaVersionValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;

/**
 * Schema Service to register, get and update schema.
 *
 */
@Service
@RequiredArgsConstructor
public class SchemaService implements ISchemaService {

    private final AuditLogger auditLogger;

    private final ISchemaInfoStore schemaInfoStore;

    private final ISchemaStore schemaStore;

    private final IAuthorityService authorityService;

    private final ISourceService sourceService;

    private final IEntityTypeService entityTypeService;

    private final SchemaUtil schemaUtil;

    private SchemaResolver schemaResolver;

    private final SchemaVersionValidatorFactory versionValidatorFactory;

    final JaxRsDpsLog log;

    private final IMessageBus messageBus;

    @Autowired
    public void setSchemaResolver(SchemaResolver schemaResolver) {
        this.schemaResolver = schemaResolver;
    }

    final DpsHeaders headers;

    /**
     * Method to get schema
     *
     * @param schemaId
     * @return schema object.
     * @throws ApplicationException
     */
    @Override
    public Object getSchema(String schemaId) throws BadRequestException, NotFoundException, ApplicationException {
        Object schema = "";
        String dataPartitionId = headers.getPartitionId();
        validateSchemaId(schemaId);
        log.info(SchemaConstants.SCHEMA_GET_STARTED);
        try {
            schema = schemaStore.getSchema(dataPartitionId, schemaId);
        } catch (NotFoundException e) {
            schema = schemaStore.getSystemSchema(schemaId);
        }

        auditLogger.schemaRetrievedSuccess(Collections.singletonList(schema.toString()));
        return schema;
    }

    private void validateSchemaId(String schemaId) throws BadRequestException {
        if (StringUtils.isEmpty(schemaId)) {
            auditLogger.schemaRetrievedFailure(Collections.singletonList(schemaId));
            log.error(SchemaConstants.EMPTY_ID);
            throw new BadRequestException(SchemaConstants.EMPTY_ID);
        }
    }

    /**
     * Method to create schema
     *
     * @param schemaRequest request
     * @return schemaInfo.
     * @throws JSONException
     * @throws JsonProcessingException
     */
    @Override
    public SchemaInfo createSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return this.createSchemaInternal(schemaRequest, false);
    }

    private SchemaInfo createSchemaInternal(SchemaRequest schemaRequest, Boolean isSystemSchema) throws ApplicationException, BadRequestException {
        String schemaId = createAndSetSchemaId(schemaRequest);

        if (isUniqueSchema(schemaId, isSystemSchema)) {
            setScope(schemaRequest,isSystemSchema);

            String schema = resolveAndCheckBreakingChanges(schemaRequest, isSystemSchema);

            Boolean authority = this.checkAndRegisterAuthorityIfNotPresent(
                    schemaRequest.getSchemaInfo().getSchemaIdentity().getAuthority(), isSystemSchema);
            Boolean source = this.checkAndRegisterSourceIfNotPresent(
                    schemaRequest.getSchemaInfo().getSchemaIdentity().getSource(), isSystemSchema);
            Boolean entity = this.checkAndRegisterEntityTypeIfNotPresent(
                    schemaRequest.getSchemaInfo().getSchemaIdentity().getEntityType(), isSystemSchema);

            if (authority && source && entity) {
                log.info(SchemaConstants.SCHEMA_CREATION_STARTED);
                try {
                    SchemaInfo schemaInfo = this.createSchemaInfo(schemaRequest, isSystemSchema);
                    this.createSchema(schemaId, schema, isSystemSchema);
                    if(isSystemSchema) {
                        auditLogger.systemSchemaRegisteredSuccess(Collections.singletonList(schemaRequest.toString()));
                        messageBus.publishMessageForSystemSchema(schemaId, SchemaConstants.SCHEMA_CREATE_EVENT_TYPE);
                    }
                    else
                    {
                        auditLogger.schemaRegisteredSuccess(Collections.singletonList(schemaRequest.toString()));
                        messageBus.publishMessage(schemaId, SchemaConstants.SCHEMA_CREATE_EVENT_TYPE);
                    }
                    return schemaInfo;
                } catch (ApplicationException ex) {
                    if(isSystemSchema) {
                        auditLogger.systemSchemaRegisteredFailure(Collections.singletonList(schemaRequest.toString()));
                    } else {
                        auditLogger.schemaRegisteredFailure(Collections.singletonList(schemaRequest.toString()));
                    }
                    log.warning(SchemaConstants.SCHEMA_CREATION_FAILED);
                    this.cleanSchema(schemaId, isSystemSchema);
                    this.cleanSchemaProject(schemaId, isSystemSchema);
                    log.info(SchemaConstants.SCHEMA_CREATE_CLEAN);
                    throw ex;
                }
            } else {
                log.error("The schema could not be created due invalid authority,source or entityType");
                throw new ApplicationException(SchemaConstants.INTERNAL_SERVER_ERROR);
            }
        } else {
            throw new BadRequestException(SchemaConstants.SCHEMA_ID_EXISTS);
        }
    }

    private SchemaInfo createSystemSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return this.createSchemaInternal(schemaRequest, true);
    }

    /**
     * Method to update schema. Schemas that are in Development state can be
     * updated. This method checks for the presence of schema based on the schema Id
     * provided in input Payload, if found updates both the schemaInfo and schema.
     *
     * @param schemaRequest
     * @return schemaInfo.
     * @throws IOException
     * @throws JsonProcessingException
     * @throws JSONException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @Override
    public SchemaInfo updateSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return this.updateSchemaInternal(schemaRequest, false);
    }

    public SchemaInfo updateSchemaInternal(SchemaRequest schemaRequest, Boolean isSystemSchema) throws ApplicationException, BadRequestException {
        String createdSchemaId = createAndSetSchemaId(schemaRequest);
        SchemaInfo schemaInfo = null;
        try {
            schemaInfo = this.getSchemaInfo(createdSchemaId, isSystemSchema);
        } catch (NotFoundException e) {
            log.error(SchemaConstants.INVALID_SCHEMA_UPDATE);
            if (!SchemaStatus.DEVELOPMENT.equals(schemaRequest.getSchemaInfo().getStatus()) && !isSystemSchema)
                throw new BadRequestException(SchemaConstants.SCHEMA_PUT_CREATE_EXCEPTION);
            throw new NoSchemaFoundException(SchemaConstants.INVALID_SCHEMA_UPDATE);
        }

        if (SchemaStatus.DEVELOPMENT.equals(schemaInfo.getStatus())) {
            log.info(MessageFormat.format(SchemaConstants.SCHEMA_UPDATION_STARTED, createdSchemaId));
            setScope(schemaRequest, isSystemSchema);
            String schema = resolveAndCheckBreakingChanges(schemaRequest, isSystemSchema);
            SchemaInfo schInfo;
            if (isSystemSchema) {
                schInfo = schemaInfoStore.updateSystemSchemaInfo(schemaRequest);
                schemaStore.createSystemSchema(schemaRequest.getSchemaInfo().getSchemaIdentity().getId(), schema);
                messageBus.publishMessageForSystemSchema(createdSchemaId, SchemaConstants.SCHEMA_UPDATE_EVENT_TYPE);
                auditLogger.systemSchemaUpdatedSuccess(Collections.singletonList(schemaRequest.toString()));
            } else {
                schInfo = schemaInfoStore.updateSchemaInfo(schemaRequest);
                schemaStore.createSchema(schemaRequest.getSchemaInfo().getSchemaIdentity().getId(), schema);
                messageBus.publishMessage(createdSchemaId, SchemaConstants.SCHEMA_UPDATE_EVENT_TYPE);
                auditLogger.schemaUpdatedSuccess(Collections.singletonList(schemaRequest.toString()));
            }
            log.info(SchemaConstants.SCHEMA_UPDATED);
            return schInfo;
        } else {
            if (isSystemSchema) {
                auditLogger.systemSchemaUpdatedFailure(Collections.singletonList(schemaRequest.toString()));
            } else {
                auditLogger.schemaUpdatedFailure(Collections.singletonList(schemaRequest.toString()));
            }
            log.error(SchemaConstants.SCHEMA_UPDATE_ERROR);
            throw new BadRequestException(SchemaConstants.SCHEMA_UPDATE_EXCEPTION);
        }
    }


    private SchemaInfo updateSystemSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return this.updateSchemaInternal(schemaRequest, true);
    }

    private String createSchemaId(SchemaRequest schemaRequest) {
        SchemaIdentity schemaIdentity = schemaRequest.getSchemaInfo().getSchemaIdentity();
        return new StringBuilder().append(schemaIdentity.getAuthority()).append(":").append(schemaIdentity.getSource())
                .append(":").append(schemaIdentity.getEntityType()).append(":")
                .append(schemaIdentity.getSchemaVersionMajor()).append(".")
                .append(schemaIdentity.getSchemaVersionMinor()).append(".")
                .append(schemaIdentity.getSchemaVersionPatch()).toString();
    }

    private String createAndSetSchemaId(SchemaRequest schemaRequest) {
        String schemaId = createSchemaId(schemaRequest);
        schemaRequest.getSchemaInfo().getSchemaIdentity().setId(schemaId);
        return schemaId;
    }

    private String resolveAndCheckBreakingChanges(SchemaRequest schemaRequest, Boolean isSystemSchema) throws ApplicationException, BadRequestException {

        Gson gson = new Gson();
        String schemaInRequestPayload = gson.toJson(schemaRequest.getSchema());
        String fullyResolvedInputSchema = schemaResolver.resolveSchema(schemaInRequestPayload);
        compareFullyResolvedSchema(schemaRequest.getSchemaInfo(), fullyResolvedInputSchema, isSystemSchema);
        return fullyResolvedInputSchema;
    }

    @Override
    public SchemaInfoResponse getSchemaInfoList(QueryParams queryParams)
            throws BadRequestException, ApplicationException {

        List<SchemaInfo> schemaList = new LinkedList<>();

        latestVersionMajorMinorFiltersCheck(queryParams);

        if (queryParams.getScope() != null) {

            if (queryParams.getScope().equalsIgnoreCase(SchemaScope.SHARED.toString())) {
                getSchemaInfos(queryParams, schemaList, true);
            }

            else if (queryParams.getScope().equalsIgnoreCase(SchemaScope.INTERNAL.toString())) {
                getSchemaInfos(queryParams, schemaList, false);
            }
        } else {
            // Fetch all the system schemas satisfying this query parameters
            getSchemaInfos(queryParams, schemaList, true);
            // Fetch all the private schemas satisfying the query parameters and add
            // to the list of system schemas.
            getSchemaInfos(queryParams, schemaList, false);
        }

        if (queryParams.getLatestVersion() != null && queryParams.getLatestVersion()) {
            schemaList = getLatestVersionSchemaList(schemaList);
        }

        Comparator<SchemaInfo> compareByCreatedDate = (s1,s2) -> s1.getDateCreated().compareTo(s2.getDateCreated());

        List<SchemaInfo> schemaFinalList = schemaList.stream().sorted(compareByCreatedDate)
                .skip(queryParams.getOffset())
                .limit(queryParams.getLimit()).collect(Collectors.toList());

        if (schemaFinalList.isEmpty()){
            auditLogger.searchSchemaFailure(Collections.singletonList(queryParams.toString()));
        } else {
            auditLogger.searchSchemaSuccess(schemaFinalList.stream()
                    .map(SchemaInfo::toString)
                    .collect(Collectors.toList()));
        }
        return SchemaInfoResponse.builder().schemaInfos(schemaFinalList).count(schemaFinalList.size())
                .offset(queryParams.getOffset()).totalCount(schemaList.size()).build();
    }

    @Override
    public SchemaUpsertResponse upsertSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return upsertSchemaInternal(schemaRequest, false);
    }

    private SchemaUpsertResponse upsertSchemaInternal(SchemaRequest schemaRequest, Boolean isSystemSchema) throws ApplicationException, BadRequestException {
        SchemaInfo response = null;
        HttpStatus httpCode = HttpStatus.BAD_REQUEST;
        SchemaUpsertResponse.SchemaUpsertResponseBuilder upsertBuilder = SchemaUpsertResponse.builder();
        try {
            if (isSystemSchema) {
                response = updateSystemSchema(schemaRequest);
            } else {
                response = updateSchema(schemaRequest);
            }
            httpCode = HttpStatus.OK;
        } catch (NoSchemaFoundException noSchemaFound) {
            log.info("Schema with Id " + schemaRequest.getSchemaInfo().getSchemaIdentity().getId() + " could not be found, creating it");
            try {
                if (isSystemSchema) {
                    response = createSystemSchema(schemaRequest);
                } else {
                    response = createSchema(schemaRequest);
                }

                httpCode = HttpStatus.CREATED;
            }catch (BadRequestException badreqEx) {
                //If there is same schema-id for other tenant then throw different error message
                if(SchemaConstants.SCHEMA_ID_EXISTS.equals(badreqEx.getMessage()))
                    throw new BadRequestException(SchemaConstants.INVALID_UPDATE_OPERATION);

                throw badreqEx;
            }
        }
        return upsertBuilder.schemaInfo(response).httpCode(httpCode).build();
    }

    public SchemaUpsertResponse upsertSystemSchema(SchemaRequest schemaRequest) throws ApplicationException, BadRequestException {
        return this.upsertSchemaInternal(schemaRequest, true);
    }

    private void getSchemaInfos(QueryParams queryParams, List<SchemaInfo> schemaList, Boolean isSystemSchema)
            throws ApplicationException {
        if (isSystemSchema) {
            schemaInfoStore.getSystemSchemaInfoList(queryParams).forEach(schemaList::add);
        } else {
            String tenant = headers.getPartitionId();
            schemaInfoStore.getSchemaInfoList(queryParams, tenant).forEach(schemaList::add);
        }
    }

    private void latestVersionMajorMinorFiltersCheck(QueryParams queryParams) throws BadRequestException {
        if (queryParams.getLatestVersion() != null && queryParams.getLatestVersion()) {

            if (queryParams.getSchemaVersionMajor() == null && queryParams.getSchemaVersionMinor() != null)
                throw new BadRequestException(SchemaConstants.LATESTVERSION_MINORFILTER_WITHOUT_MAJOR);

            if (queryParams.getSchemaVersionMinor() == null && queryParams.getSchemaVersionPatch() != null)
                throw new BadRequestException(SchemaConstants.LATESTVERSION_PATCHFILTER_WITHOUT_MINOR);

        }
    }

    /**
     * Method to set the scope of the schema according to the tenant
     *
     * @param schemaRequest
     * @param isSystemSchema
     */
    private void setScope(SchemaRequest schemaRequest, Boolean isSystemSchema) {
        if (isSystemSchema) {
            schemaRequest.getSchemaInfo().setScope(SchemaScope.SHARED);
        } else {
            schemaRequest.getSchemaInfo().setScope(SchemaScope.INTERNAL);
        }
    }


    private List<SchemaInfo> getLatestVersionSchemaList(List<SchemaInfo> filteredSchemaList) {

        List<SchemaInfo> latestSchemaList = new ArrayList<>();
        Map<String, SchemaInfo> latestSchemaMap = new HashMap<>();
        SchemaComparatorByVersion schemaComparatorByVersion = new SchemaComparatorByVersion();

        for(SchemaInfo schemaInfo :filteredSchemaList) {

            String key = getGroupingKey(schemaInfo);
            latestSchemaMap.computeIfAbsent(key, k -> schemaInfo);

            SchemaInfo value = latestSchemaMap.get(key);

            if(schemaComparatorByVersion.compare(schemaInfo, value) >= 0)
                latestSchemaMap.put(key, schemaInfo);

        }

        latestSchemaList.addAll(latestSchemaMap.values());

        return latestSchemaList;
    }

    /***
     * This method creates a key based on Athority:Source:EntityType
     *
     * @param schemaInfo SchemaInfo whose key is to be formed
     * @return String based key formed using Athority:Source:EntityType
     */
    private String getGroupingKey(SchemaInfo schemaInfo){
        return String.join(":", schemaInfo.getSchemaIdentity().getAuthority(),
                schemaInfo.getSchemaIdentity().getSource(),
                schemaInfo.getSchemaIdentity().getEntityType());
    }

    private void compareFullyResolvedSchema(SchemaInfo inputSchemaInfo, String resolvedInputSchema, Boolean isSystemSchema) throws BadRequestException, ApplicationException {
        try {
            SchemaInfo[] schemaInfoToCompareWith = schemaUtil.findSchemaToCompare(inputSchemaInfo, isSystemSchema);

            for(SchemaInfo existingSchemaInfo : schemaInfoToCompareWith) {
                if(null == existingSchemaInfo)
                    continue;

                String existingSchemaInStore;

                if (isSystemSchema) {
                    existingSchemaInStore = schemaStore.getSystemSchema(existingSchemaInfo.getSchemaIdentity().getId()).toString();
                } else {
                    existingSchemaInStore = getSchema(existingSchemaInfo.getSchemaIdentity().getId()).toString();
                }

                try {
                    //Compare Major version of the schemas are different
                    if(inputSchemaInfo.getSchemaIdentity().getSchemaVersionMajor().compareTo(existingSchemaInfo.getSchemaIdentity().getSchemaVersionMajor()) != 0) {
                        continue;
                        //Compare Minor version is greater or smaller
                    }else if(inputSchemaInfo.getSchemaIdentity().getSchemaVersionMinor().compareTo(existingSchemaInfo.getSchemaIdentity().getSchemaVersionMinor()) < 0){
                        versionValidatorFactory.getVersionValidator(SchemaValidationType.MINOR).validateVersionChange(resolvedInputSchema, existingSchemaInStore);
                    }else if(inputSchemaInfo.getSchemaIdentity().getSchemaVersionMinor().compareTo(existingSchemaInfo.getSchemaIdentity().getSchemaVersionMinor()) > 0) {
                        versionValidatorFactory.getVersionValidator(SchemaValidationType.MINOR).validateVersionChange(existingSchemaInStore, resolvedInputSchema);
                    }else {
                        versionValidatorFactory.getVersionValidator(SchemaValidationType.PATCH).validateVersionChange(existingSchemaInStore, resolvedInputSchema);
                    }
                }catch (SchemaVersionException exc) {
                    log.error("Failed to resolve the schema and find breaking changes. Reason :" + exc.getMessage());

                    String message = MessageFormat.format(exc.getMessage(), StringUtils.substringAfterLast(inputSchemaInfo.getSchemaIdentity().getId(), SchemaConstants.SCHEMA_KIND_DELIMITER),
                            StringUtils.substringAfterLast(existingSchemaInfo.getSchemaIdentity().getId(), SchemaConstants.SCHEMA_KIND_DELIMITER));
                    throw new BadRequestException(message);
                }
            }


        }  catch ( NotFoundException e) {
            throw new ApplicationException("Schema not found to evaluate breaking changes.");
        }catch (JSONException exc) {
            log.error("Failed to resolve the schema and find breaking changes. Reason :"+exc.getMessage());
            throw new BadRequestException("Bad Input, invalid json");
        }

    }

    private SchemaInfo createSchemaInfo(SchemaRequest schemaRequest, Boolean isSystemSchema) throws ApplicationException, BadRequestException {
        if (isSystemSchema) {
            return schemaInfoStore.createSystemSchemaInfo(schemaRequest);
        } else {
            return schemaInfoStore.createSchemaInfo(schemaRequest);
        }
    }

    private Boolean cleanSchema(String schemaId, Boolean isSystemSchema) throws ApplicationException {
        if(isSystemSchema) {
            return schemaInfoStore.cleanSystemSchema(schemaId);
        } else {
            return schemaInfoStore.cleanSchema(schemaId);
        }
    }

    private String createSchema(String schemaId, String schema, Boolean isSystemSchema) throws ApplicationException {
        if (isSystemSchema) {
            return schemaStore.createSystemSchema(schemaId, schema);
        } else {
            return schemaStore.createSchema(schemaId, schema);
        }
    }

    private Boolean cleanSchemaProject(String schemaId, Boolean isSystemSchema) throws ApplicationException {
        if (isSystemSchema) {
            return schemaStore.cleanSystemSchemaProject(schemaId);
        } else {
            return schemaStore.cleanSchemaProject(schemaId);
        }
    }

    private Boolean checkAndRegisterAuthorityIfNotPresent(String authorityId, Boolean isSystemSchema) {
        if (isSystemSchema) {
            return authorityService.checkAndRegisterSystemAuthorityIfNotPresent(authorityId);
        } else {
            return authorityService.checkAndRegisterAuthorityIfNotPresent(authorityId);
        }
    }

    private Boolean checkAndRegisterSourceIfNotPresent(String sourceId, Boolean isSystemSchema) {
        if (isSystemSchema) {
            return sourceService.checkAndRegisterSystemSourceIfNotPresent(sourceId);
        } else {
            return sourceService.checkAndRegisterSourceIfNotPresent(sourceId);
        }
    }

    private Boolean checkAndRegisterEntityTypeIfNotPresent(String entityTypeId, Boolean isSystemSchema) {
        if (isSystemSchema) {
            return entityTypeService.checkAndRegisterSystemEntityTypeIfNotPresent(entityTypeId);
        } else {
            return entityTypeService.checkAndRegisterEntityTypeIfNotPresent(entityTypeId);
        }
    }

    private Boolean isUniqueSchema(String schemaId, Boolean isSystemSchema) throws ApplicationException {
        if (isSystemSchema) {
            return schemaInfoStore.isUniqueSystemSchema(schemaId);
        } else {
            return schemaInfoStore.isUnique(schemaId, headers.getPartitionId());
        }
    }

    private SchemaInfo getSchemaInfo(String schemaId, Boolean isSystemSchema) throws ApplicationException, NotFoundException {
        if (isSystemSchema) {
            return schemaInfoStore.getSystemSchemaInfo(schemaId);
        } else {
            return schemaInfoStore.getSchemaInfo(schemaId);
        }
    }
}