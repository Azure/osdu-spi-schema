package org.opengroup.osdu.schema.stepdefs.model;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpResponse {
    @Builder.Default
    Map<String, List<String>> responseHeaders = new HashMap<>();
    private int code;
    private Exception exception;
    private String body;
}
