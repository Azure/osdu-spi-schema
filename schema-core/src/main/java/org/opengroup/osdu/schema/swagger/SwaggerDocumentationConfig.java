package org.opengroup.osdu.schema.swagger;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springdoc.core.customizers.OperationCustomizer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@Profile("!noswagger")
public class SwaggerDocumentationConfig {
        @Autowired
        private SwaggerConfigurationProperties configurationProperties;

        @Bean
        public OpenAPI customOpenAPI() {

                SecurityScheme securityScheme = new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Authorization")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization");
                final String securitySchemeName = "Authorization";
                SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);
                Components components = new Components().addSecuritySchemes(securitySchemeName, securityScheme);

                OpenAPI openAPI = new OpenAPI()
                                .addSecurityItem(securityRequirement)
                                .components(components)
                                .info(apiInfo())
                                .tags(tags());

                if (configurationProperties.isApiServerFullUrlEnabled())
                        return openAPI;
                return openAPI
                        .servers(Arrays.asList(new Server().url(configurationProperties.getApiServerUrl())));
        }

        /**
         * {@code SchemaRequest.schema} is a JSON object. A string {@code @Schema} example makes
         * springdoc emit {@code type: string}, so Schemathesis treats {@code ""} as valid and the
         * service rejects it. Force the published contract to {@code type: object}.
         */
        @Bean
        public OpenApiCustomizer schemaRequestObjectCustomizer() {
                return openApi -> {
                        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                                return;
                        }
                        Schema<?> schemaRequest = openApi.getComponents().getSchemas().get("SchemaRequest");
                        if (schemaRequest == null || schemaRequest.getProperties() == null) {
                                return;
                        }
                        Object rawProperty = schemaRequest.getProperties().get("schema");
                        if (!(rawProperty instanceof Schema<?> schemaProperty)) {
                                return;
                        }
                        schemaProperty.setType("object");
                        schemaProperty.setTypes(new LinkedHashSet<>(Set.of("object")));
                };
        }

        @Bean
        public OperationCustomizer customize() {
            return (operation, handlerMethod) -> {
                if (operation.getTags().contains("schema-api"))
                {
                        Parameter dataPartitionId = new Parameter()
                                .name(DpsHeaders.DATA_PARTITION_ID)
                                .description("Tenant Id")
                                .in("header")
                                .required(true)
                                .schema(new StringSchema());
                        return operation.addParametersItem(dataPartitionId);
                }

                return operation;
            };
        }

        private List<Tag> tags() {
                List<Tag> tags = new ArrayList<>();
                tags.add(new Tag().name("schema-api").description("Schema API - Core Schema related endpoints"));
                tags.add(new Tag().name("system-schema-api").description("System Schema API - System Schema related endpoints"));
                tags.add(new Tag().name("info").description("Version info endpoint"));
                return tags;
        }

        private Info apiInfo() {
                return new Info()
                        .title(configurationProperties.getApiTitle())
                        .description(configurationProperties.getApiDescription())
                        .version(configurationProperties.getApiVersion())
                        .license(new License().name(configurationProperties.getApiLicenseName()).url(configurationProperties.getApiLicenseUrl()))
                        .contact(new Contact().name(configurationProperties.getApiContactName()).email(configurationProperties.getApiContactEmail()));
        }
}
