package org.opengroup.osdu.schema.azure.di;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties("azure.system")
public class SystemResourceConfig {
    private String storageContainerName;
    private String cosmosDatabase;
    private String sharedTenant;
}
