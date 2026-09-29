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

package org.opengroup.osdu.schema.azure.di;

import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosContainerConfig {

    @Value("${authority.container.name}")
    private String authorityContainerName;

    @Value("${entity-type.container.name}")
    private String entityTypeContainerName;

    @Value("${schema-info.container.name}")
    private String schemaInfoContainerName;

    @Value("${source.container.name}")
    private String sourceContainerName;
    
    @Value("${azure.cosmosdb.database}")
    private String cosmosDBName;
    
    @Value("${azure.storage.container-name}")
    private String storageContainer;
    
    @Bean
    @Named("STORAGE_CONTAINER_NAME")
    public String containerName() {
        return storageContainer;
    }
    
    @Bean
    @Named("COSMOS_DB_NAME")
    public String cosmosDBName() {
        return cosmosDBName;
    }

    @Bean
    public String authorityContainer() {
        return authorityContainerName;
    }

    @Bean
    public String entityTypeContainer() {
        return entityTypeContainerName;
    }

    @Bean
    public String schemaInfoContainer() {
        return schemaInfoContainerName;
    }

    @Bean
    public String sourceContainer() {
        return sourceContainerName;
    }
}