package org.opengroup.osdu.schema.stepdefs.model;



import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class HttpRequest {
    public static final String PATCH = "PATCH";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String GET = "GET";
    public static final String DELETE = "DELETE";

    String httpMethod;
    String url;
    String body;

    @Builder.Default
    Map<String, String> requestHeaders = new HashMap<>();

    @Builder.Default
    Map<String, ?> queryParams = new HashMap<>();

    @Builder.Default
    Map<String, ?> pathParams = new HashMap<>();

    @Override
    public String toString() {
        return String.format("%s, httpMethod=%s", url, httpMethod);
    }
}
