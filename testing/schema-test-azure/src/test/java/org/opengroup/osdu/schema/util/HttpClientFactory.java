package org.opengroup.osdu.schema.util;

public class HttpClientFactory {
    private static HttpClient httpClient = null;

    public static HttpClient getInstance() {
        if (httpClient == null) {
            httpClient = new RestAssuredClient();
        }
        return httpClient;
    }
}
