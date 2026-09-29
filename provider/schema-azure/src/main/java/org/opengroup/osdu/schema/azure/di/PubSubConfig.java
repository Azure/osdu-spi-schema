package org.opengroup.osdu.schema.azure.di;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@ConfigurationProperties(prefix = "azure.pubsub")
@Getter
public class PubSubConfig {
	
    @Value("${azure.servicebus.topic-name}")
    private String serviceBusTopic;
    
    @Value("${azure.serviceBus.enabled}")
    private boolean serviceBusEnabled;
}
