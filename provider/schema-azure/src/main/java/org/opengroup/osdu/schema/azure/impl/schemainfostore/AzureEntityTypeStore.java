// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.schema.azure.impl.schemainfostore;

import java.text.MessageFormat;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.schema.azure.definitions.EntityTypeDoc;
import org.opengroup.osdu.schema.azure.di.SystemResourceConfig;
import org.opengroup.osdu.schema.constants.SchemaConstants;
import org.opengroup.osdu.schema.exceptions.ApplicationException;
import org.opengroup.osdu.schema.exceptions.BadRequestException;
import org.opengroup.osdu.schema.exceptions.NotFoundException;
import org.opengroup.osdu.schema.model.EntityType;
import org.opengroup.osdu.schema.provider.interfaces.schemainfostore.IEntityTypeStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import lombok.extern.java.Log;

/**
 * Repository class to register Entity type in Azure store.
 *
 *
 */
@Log
@Repository
public class AzureEntityTypeStore implements IEntityTypeStore {

    @Autowired
    private DpsHeaders headers;

    @Autowired
    private String entityTypeContainer;

    @Autowired
    private CosmosStore cosmosStore;

    @Autowired
    private String cosmosDBName;

    @Autowired
    JaxRsDpsLog log;

    @Autowired
    SystemResourceConfig systemResourceConfig;

    /**
     * Method to get entity type from azure store
     * @param entityTypeId
     * @return
     * @throws NotFoundException
     * @throws ApplicationException
     */
    @Override
    public EntityType get(String entityTypeId) throws NotFoundException, ApplicationException {

        String id = headers.getPartitionId() + ":" + entityTypeId;
        EntityTypeDoc entityTypeDoc;
        entityTypeDoc = cosmosStore.findItem(headers.getPartitionId(), cosmosDBName, entityTypeContainer, id, entityTypeId, EntityTypeDoc.class)
                .orElseThrow(() -> new NotFoundException("bad input parameter"));

        return entityTypeDoc.getEntityType();
    }

    /**
     * Method to get system Entity
     * @param entityTypeId
     * @return
     * @throws NotFoundException
     * @throws ApplicationException
     */
    @Override
    public EntityType getSystemEntity(String entityTypeId) throws NotFoundException, ApplicationException {
        EntityTypeDoc entityTypeDoc;
        entityTypeDoc = cosmosStore.findItem(systemResourceConfig.getCosmosDatabase(), entityTypeContainer, entityTypeId, entityTypeId, EntityTypeDoc.class)
                .orElseThrow(() -> new NotFoundException("bad input parameter"));

        return entityTypeDoc.getEntityType();
    }

    /**
     * Method to create entityType in azure store
     * @param entityType
     * @return
     * @throws BadRequestException
     * @throws ApplicationException
     */
    @Override
    public EntityType create(EntityType entityType) throws BadRequestException, ApplicationException {

        String id = headers.getPartitionId() + ":" + entityType.getEntityTypeId();
        try {
            EntityTypeDoc entityTypeDoc = new EntityTypeDoc(id, entityType);
            cosmosStore.createItem(headers.getPartitionId(), cosmosDBName, entityTypeContainer, id, entityTypeDoc);
        } catch (AppException ex) {
            handleAppException(ex, entityType);
        }

        log.info(SchemaConstants.ENTITY_TYPE_CREATED);
        return entityType;
    }

    /**
     * Method to create a system Entity
     * @param entityType
     * @return
     * @throws BadRequestException
     * @throws ApplicationException
     */
    @Override
    public EntityType createSystemEntity(EntityType entityType) throws BadRequestException, ApplicationException {
        try {
            EntityTypeDoc entityTypeDoc = new EntityTypeDoc(entityType.getEntityTypeId(), entityType);
            cosmosStore.createItem(systemResourceConfig.getCosmosDatabase(), entityTypeContainer, entityType.getEntityTypeId(), entityTypeDoc);
        } catch (AppException ex) {
            handleAppException(ex, entityType);
        }

        log.info(SchemaConstants.ENTITY_TYPE_CREATED);
        return entityType;
    }

    private void handleAppException(AppException ex, EntityType entityType) throws BadRequestException, ApplicationException {
        if (ex.getError().getCode() == 409) {
            log.warning(SchemaConstants.ENTITY_TYPE_EXISTS);
            throw new BadRequestException(MessageFormat.format(SchemaConstants.ENTITY_TYPE_EXISTS_EXCEPTION,
                    entityType.getEntityTypeId()));
        } else {
            log.error(MessageFormat.format(SchemaConstants.OBJECT_INVALID, ex.getMessage()));
            throw new ApplicationException(SchemaConstants.INVALID_INPUT);
        }
    }
}
