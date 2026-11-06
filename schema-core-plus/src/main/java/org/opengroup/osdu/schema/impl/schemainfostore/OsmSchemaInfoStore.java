/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.schema.impl.schemainfostore;

import static org.opengroup.osdu.core.osm.core.model.where.condition.And.and;
import static org.opengroup.osdu.core.osm.core.model.where.predicate.Eq.eq;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.query.GetQuery;
import org.opengroup.osdu.core.osm.core.model.where.Where;
import org.opengroup.osdu.core.osm.core.model.where.predicate.Eq;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.translate.ViewResult;
import org.opengroup.osdu.core.osm.core.translate.TranslatorException;
import org.opengroup.osdu.core.osm.core.translate.TranslatorRuntimeException;
import org.opengroup.osdu.schema.configuration.PropertiesConfiguration;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.destination.provider.DestinationProvider;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.QueryParams;
import org.opengroup.osdu.schema.model.SchemaInfo;
import org.opengroup.osdu.schema.model.SchemaRequest;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.ISchemaInfoStore;
import org.springframework.stereotype.Repository;

/**
 * Repository class to register Schema in KV store.
 */

@Repository
public class OsmSchemaInfoStore implements ISchemaInfoStore {

  private static final String SYSTEM_SCHEMA_KIND = "system_schema";
  private static final String SCHEMA_INFO_PREFIX = "schemaInfo.%s";
  private static final String SCHEMA_INFO = "schemaInfo";
  private static final String SCHEMA_IDENTITY_PREFIX = String.format(SCHEMA_INFO_PREFIX,
      "schemaIdentity.%s");
  private static final String SCHEMA_ID = String.format(SCHEMA_IDENTITY_PREFIX, "id");
  private static final String SCHEMA_OSM_KIND = String.format("%s_osm",
      SchemaConstants.SCHEMA_KIND);
  private static final String SYSTEM_SCHEMA_OSM_KIND = String.format("%s_osm", SYSTEM_SCHEMA_KIND);
  private final DpsHeaders headers;
  private final DestinationProvider<Destination> destinationProvider;
  private final JaxRsDpsLog log;
  private final PropertiesConfiguration configuration;
  private final Context context;

  private final ITenantFactory tenantFactory;

  public OsmSchemaInfoStore(DpsHeaders headers,
      DestinationProvider<Destination> destinationProvider, JaxRsDpsLog log,
      PropertiesConfiguration configuration,
      Context context, ITenantFactory tenantFactory) {
    this.headers = headers;
    this.destinationProvider = destinationProvider;
    this.log = log;
    this.configuration = configuration;
    this.context = context;
    this.tenantFactory = tenantFactory;
  }

  /**
   * Method to get schemaInfo from KV store
   *
   * @param schemaId
   * @return schemaInfo object
   * @throws ApplicationException
   * @throws NotFoundException
   */
  @Override
  public SchemaInfo getSchemaInfo(String schemaId) throws ApplicationException, NotFoundException {
    SchemaRequest schemaRequest = context.findOne(
            buildQueryFor(getPrivateTenantDestination(this.headers.getPartitionId()),
                eq(SCHEMA_ID, schemaId)))
        .orElseThrow(() ->
            new NotFoundException(SchemaConstants.SCHEMA_NOT_PRESENT));
    return schemaRequest.getSchemaInfo();
  }

  /**
   * Method to get System schemaInfo from KV store
   *
   * @param schemaId
   * @return schemaInfo object
   * @throws ApplicationException
   * @throws NotFoundException
   */
  @Override
  public SchemaInfo getSystemSchemaInfo(String schemaId)
      throws ApplicationException, NotFoundException {
    SchemaRequest schemaRequest = context.findOne(
            buildQueryFor(getSystemDestination(), eq(SCHEMA_ID, schemaId)))
        .orElseThrow(() ->
            new NotFoundException(SchemaConstants.SCHEMA_NOT_PRESENT));
    return schemaRequest.getSchemaInfo();
  }

  /**
   * Method to Create schema in KV store of tenantId GCP
   *
   * @param schema
   * @return schemaInfo object
   * @throws ApplicationException
   * @throws BadRequestException
   */
  @Override
  public SchemaInfo createSchemaInfo(SchemaRequest schema)
      throws ApplicationException, BadRequestException {
    try {
      Destination tenantDestination = getPrivateTenantDestination(this.headers.getPartitionId());
      enrichSchemaInfo(schema.getSchemaInfo(), tenantDestination);

      checkEntityExistence(schema, tenantDestination);

      SchemaRequest entityFromDb;
      try {
        entityFromDb = context.createAndGet(tenantDestination, schema);

        log.info(SchemaConstants.SCHEMA_INFO_CREATED);
        return entityFromDb.getSchemaInfo();
      } catch (TranslatorRuntimeException ex) {
        log.error(MessageFormat.format(SchemaConstants.OBJECT_INVALID, ex.getMessage()), ex);
        throw new ApplicationException(SchemaConstants.INVALID_INPUT, ex);
      }
    } catch (ApplicationException e) {
      throw new ApplicationException(SchemaConstants.SCHEMA_CREATION_FAILED_INVALID_OBJECT);
    }
  }

  /**
   * Method to Create System schema in google store
   *
   * @param schema
   * @return SchemaInfo object
   * @throws ApplicationException
   * @throws BadRequestException
   */
  @Override
  public SchemaInfo createSystemSchemaInfo(SchemaRequest schema)
      throws ApplicationException, BadRequestException {
    try {
      Destination systemDestination = getSystemDestination();
      enrichSchemaInfo(schema.getSchemaInfo(), systemDestination);

      checkEntityExistence(schema, systemDestination);

      SchemaRequest entityFromDb;
      try {
        entityFromDb = context.createAndGet(systemDestination, schema);
      } catch (TranslatorRuntimeException ex) {
        log.error(MessageFormat.format(SchemaConstants.OBJECT_INVALID, ex.getMessage()), ex);
        throw new ApplicationException(SchemaConstants.INVALID_INPUT, ex);
      }
      log.info(SchemaConstants.SCHEMA_INFO_CREATED);
      return entityFromDb.getSchemaInfo();
    } catch (ApplicationException e) {
      throw new ApplicationException(SchemaConstants.SCHEMA_CREATION_FAILED_INVALID_OBJECT);
    }
  }

  /**
   * Method to update schema in KV store of tenantId GCP
   *
   * @param schema
   * @return schemaInfo object
   * @throws ApplicationException
   * @throws BadRequestException
   */
  @Override
  public SchemaInfo updateSchemaInfo(SchemaRequest schema)
      throws ApplicationException, BadRequestException {
    Destination tenantDestination = getPrivateTenantDestination(this.headers.getPartitionId());
    enrichSchemaInfo(schema.getSchemaInfo(), tenantDestination);

    SchemaRequest entityFromDb;
    try {
      entityFromDb = context.upsertAndGet(tenantDestination, schema);
    } catch (TranslatorRuntimeException ex) {
      log.error(SchemaConstants.OBJECT_INVALID);
      throw new ApplicationException("Invalid object, update failed", ex);
    }
    log.info(SchemaConstants.SCHEMA_INFO_UPDATED);
    return entityFromDb.getSchemaInfo();
  }

  /**
   * Method to update System schema in KV store
   *
   * @param schema
   * @return SchemaInfo object
   * @throws ApplicationException
   * @throws BadRequestException
   */
  @Override
  public SchemaInfo updateSystemSchemaInfo(SchemaRequest schema)
      throws ApplicationException, BadRequestException {
    Destination systemDestination = getSystemDestination();
    enrichSchemaInfo(schema.getSchemaInfo(), systemDestination);

    SchemaRequest entityFromDb;
    try {
      entityFromDb = context.upsertAndGet(systemDestination, schema);
    } catch (TranslatorRuntimeException ex) {
      log.error(SchemaConstants.OBJECT_INVALID);
      throw new ApplicationException("Invalid object, update failed", ex);
    }
    log.info(SchemaConstants.SCHEMA_INFO_UPDATED);
    return entityFromDb.getSchemaInfo();
  }

  /**
   * Method to clean schemaInfo in google datastore of tenantId GCP
   *
   * @param schemaId
   * @return status
   * @throws ApplicationException
   */
  @Override
  public boolean cleanSchema(String schemaId) throws ApplicationException {
    try {
      context.delete(SchemaRequest.class,
          getPrivateTenantDestination(this.headers.getPartitionId()), eq(SCHEMA_ID, schemaId));
      return true;
    } catch (TranslatorException ex) {
      return false;
    }
  }

  /**
   * Method to clean System schemaInfo in google datastore
   *
   * @param schemaId
   * @return status
   * @throws ApplicationException
   */
  @Override
  public boolean cleanSystemSchema(String schemaId) throws ApplicationException {
    try {
      context.delete(SchemaRequest.class, getSystemDestination(), eq(SCHEMA_ID, schemaId));
      return true;
    } catch (TranslatorException ex) {
      return false;
    }
  }

  @Override
  public String getLatestMinorVerSchema(SchemaInfo schemaInfo) throws ApplicationException {
    GetQuery<SchemaRequest> getQuery = new GetQuery<>(SchemaRequest.class,
        getPrivateTenantDestination(this.headers.getPartitionId()),
        and(
            eq(String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.AUTHORITY),
                schemaInfo.getSchemaIdentity().getAuthority()),
            eq(String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.ENTITY_TYPE),
                schemaInfo.getSchemaIdentity().getEntityType()),
            eq(String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.MAJOR_VERSION),
                schemaInfo.getSchemaIdentity().getSchemaVersionMajor()),
            eq(String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.SOURCE),
                schemaInfo.getSchemaIdentity().getSource())
        ));

    List<SchemaRequest> results = context.getResultsAsList(getQuery);

    Iterator<SchemaRequest> result = results.iterator();

    TreeMap<Long, Object> sortedMap = new TreeMap<>(Collections.reverseOrder());
    while (result.hasNext()) {
      SchemaRequest entity = result.next();
      sortedMap.put(
          entity.getSchemaInfo().getSchemaIdentity().getSchemaVersionMinor(),
          entity.getSchema());
    }
    if (sortedMap.size() != 0) {
      Entry<Long, Object> entry = sortedMap.firstEntry();
      return String.valueOf(entry.getValue());
    }
    return "";
  }

  @Override
  public List<SchemaInfo> getSchemaInfoList(QueryParams queryParams, String tenantId)
      throws ApplicationException {
    List<Eq> filterList = getFilters(queryParams);

    GetQuery<SchemaRequest>.GetQueryBuilder<SchemaRequest> queryBuilder = new GetQuery<>(
        SchemaRequest.class, this.getPrivateTenantDestination(
        headers.getPartitionId())).builder();
    if (!filterList.isEmpty()) {
      queryBuilder.where(buildFiltersFromList(filterList));
    }
    return getSchemaInfoList(queryBuilder.build());
  }

  /**
   * Get schema info list for system schemas
   *
   * @param queryParams
   * @return
   * @throws ApplicationException
   */
  @Override
  public List<SchemaInfo> getSystemSchemaInfoList(QueryParams queryParams)
      throws ApplicationException {
    List<Eq> filterList = getFilters(queryParams);

    GetQuery<SchemaRequest>.GetQueryBuilder<SchemaRequest> queryBuilder = new GetQuery<>(
        SchemaRequest.class, getSystemDestination()).builder();
    if (!filterList.isEmpty()) {
      queryBuilder.where(buildFiltersFromList(filterList));
    }
    return getSchemaInfoList(queryBuilder.build());
  }

  private List<SchemaInfo> getSchemaInfoList(GetQuery<SchemaRequest> query) {
    Gson gson = new Gson();
    List<SchemaInfo> schemaList = new LinkedList<>();
    for (ViewResult result : context.getViewResultsAsList(query,
        Collections.singletonList(SCHEMA_INFO), false)) {
      SchemaInfo schemaInfo = gson.fromJson(gson.toJson(result.get(SCHEMA_INFO)), SchemaInfo.class);
      if (schemaInfo == null) {
        throw new IllegalStateException("Schema info projection returned no schema info");
      }
      schemaList.add(schemaInfo);
    }
    return schemaList;
  }

  /**
   * Method to check whether given system schema id is unique or not in system schemas and current
   * private tenant only*
   *
   * @param schemaId
   * @param tenantId
   * @return
   */
  @Override
  public boolean isUnique(String schemaId, String tenantId) {
    try {
      GetQuery<SchemaRequest> systemSchemasQuery = buildQueryFor(getSystemDestination(),
          eq(SCHEMA_ID, schemaId));
      List<SchemaRequest> systemSchemasResult = context.getResultsAsList(systemSchemasQuery);
      if (!systemSchemasResult.isEmpty()) {
        return false;
      }
      GetQuery<SchemaRequest> privateTenantSchemasQuery =
          buildQueryFor(getPrivateTenantDestination(this.headers.getPartitionId()),
              eq(SCHEMA_ID, schemaId));
      List<SchemaRequest> privateSchemasResult = context.getResultsAsList(
          privateTenantSchemasQuery);
      if (!privateSchemasResult.isEmpty()) {
        return false;
      }
    } catch (TranslatorRuntimeException e) {
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Schema uniqueness check failed",
          String.format("Misconfigured tenant-info for %s, not possible to check schema uniqueness",
              tenantId), e);
    }
    return true;
  }

  /**
   * Method to check whether given system schema id is unique or not in system schemas and in all
   * private tenants
   *
   * @param schemaId
   * @return
   * @throws ApplicationException
   */
  @Override
  public boolean isUniqueSystemSchema(String schemaId) {
    GetQuery<SchemaRequest> systemSchemasQuery = buildQueryFor(getSystemDestination(),
        eq(SCHEMA_ID, schemaId));
    List<SchemaRequest> systemSchemasResult = context.getResultsAsList(systemSchemasQuery);
    if (!systemSchemasResult.isEmpty()) {
      return false;
    }

    List<String> privateTenantList = tenantFactory.listTenantInfo().stream()
        .map(TenantInfo::getDataPartitionId)
        .toList();

    for (String tenant : privateTenantList) {
      GetQuery<SchemaRequest> query = buildQueryFor(getPrivateTenantDestination(tenant),
          eq(SCHEMA_ID, schemaId));
      try {
        List<SchemaRequest> schemas = context.getResultsAsList(query);
        if (!schemas.isEmpty()) {
          return false;
        }
      } catch (TranslatorRuntimeException e) {
        throw new AppException(HttpStatus.SC_BAD_REQUEST, "Schema uniqueness check failed",
            String.format(
                "Misconfigured tenant-info for %s, not possible to check schema uniqueness",
                tenant), e);
      }
    }
    return true;
  }

  private Where buildFiltersFromList(List<Eq> filters) {
    return filters.size() > 1
        ? and(filters.get(0), filters.get(1), filters.toArray(filters.toArray(new Where[0])))
        : filters.get(0);

  }

  private void enrichSchemaInfo(SchemaInfo schema, Destination destination)
      throws BadRequestException {
    if (schema.getSupersededBy() != null) {
      Optional<SchemaRequest> superseded =
          context.findOne(
              buildQueryFor(destination, eq(SCHEMA_ID, schema.getSupersededBy().getId())));

      if (schema.getSupersededBy().getId() == null
          || !superseded.isPresent()) {
        log.error(SchemaConstants.INVALID_SUPERSEDEDBY_ID);
        throw new BadRequestException(SchemaConstants.INVALID_SUPERSEDEDBY_ID);
      }
    }

    schema.setDateCreated(Date.from(Instant.now()));
    schema.setCreatedBy(headers.getUserEmail());
  }

  private List<Eq> getFilters(QueryParams queryParams) {
    List<Eq> filterList = new LinkedList<>();
    if (queryParams.getAuthority() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.AUTHORITY),
          queryParams.getAuthority()));
    }
    if (queryParams.getSource() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.SOURCE),
          queryParams.getSource()));
    }
    if (queryParams.getEntityType() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.ENTITY_TYPE),
          queryParams.getEntityType()));
    }
    if (queryParams.getSchemaVersionMajor() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.MAJOR_VERSION),
          queryParams.getSchemaVersionMajor()));
    }
    if (queryParams.getSchemaVersionMinor() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.MINOR_VERSION),
          queryParams.getSchemaVersionMinor()));
    }
    if (queryParams.getSchemaVersionPatch() != null) {
      filterList.add(eq(
          String.format(SCHEMA_IDENTITY_PREFIX, SchemaConstants.PATCH_VERSION),
          queryParams.getSchemaVersionPatch()));
    }
    if (queryParams.getStatus() != null) {
      filterList.add(eq(
          String.format(SCHEMA_INFO_PREFIX, SchemaConstants.STATUS),
          queryParams.getStatus().toUpperCase()));
    }
    return filterList;
  }

  private Destination getPrivateTenantDestination(String partitionId) {
    return destinationProvider.getDestination(
        partitionId,
        partitionId,
        SCHEMA_OSM_KIND
    );
  }

  private Destination getSystemDestination() {
    return destinationProvider.getDestination(
        configuration.getSharedTenantName(),
        SchemaConstants.NAMESPACE,
        SYSTEM_SCHEMA_OSM_KIND
    );
  }

  private GetQuery<SchemaRequest> buildQueryFor(Destination destination, Where where) {
    return new GetQuery<>(SchemaRequest.class, destination, where);
  }

  private void checkEntityExistence(SchemaRequest entity, Destination destination)
      throws BadRequestException {
    SchemaRequest entityFromDb = context.getOne(buildQueryFor(destination,
        eq(SCHEMA_ID, entity.getSchemaInfo().getSchemaIdentity().getId())));
    if (ObjectUtils.isNotEmpty(entityFromDb)) {
      log.warning(SchemaConstants.SCHEMA_CREATION_FAILED);
      throw new BadRequestException(MessageFormat.format(SchemaConstants.SCHEMA_ID_EXISTS,
          entity.getSchemaInfo().getSchemaIdentity().getId()));
    }
  }

}