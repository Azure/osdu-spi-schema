package org.opengroup.osdu.schema.util;

import static io.restassured.RestAssured.given;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.UUID;

import org.opengroup.osdu.schema.constants.HttpConnection;
import org.opengroup.osdu.schema.constants.TestConstants;
import org.opengroup.osdu.schema.stepdefs.model.HttpRequest;
import org.opengroup.osdu.schema.stepdefs.model.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.retry.support.RetryTemplate;

public class RestAssuredClient implements HttpClient {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    RestAssuredClient() {
        // Due to a known issue in RestAssured the following deprecated methods has to
        // be used
        // https://github.com/rest-assured/rest-assured/issues/497#issuecomment-143404851
        RestAssured.config = RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam(HttpConnection.HTTP_CONNECTION_TIMEOUT, HttpConnection.CONNECTION_TIMEOUT_IN_MILLISECONDS)
                .setParam(HttpConnection.HTTP_SOCKET_TIMEOUT, HttpConnection.CONNECTION_TIMEOUT_IN_MILLISECONDS))
                .redirect(RedirectConfig.redirectConfig().followRedirects(HttpConnection.FOLLOW_REDIRECTS));
        RestAssured.urlEncodingEnabled = false;
    }

    private final RetryTemplate template = RetryTemplate.builder()
            .maxAttempts(TestConstants.HTTP_RETRY_COUNT)
            .fixedBackoff(TestConstants.HTTP_RETRY_BACKOFF_MILLIS)
            .retryOn(Arrays.asList(
                UndesiredHTTPResponseCodeException.class,
                SocketException.class,
                SocketTimeoutException.class,
                ConnectException.class,
                UnknownHostException.class,
                SSLHandshakeException.class))
            .build();

    private class UndesiredHTTPResponseCodeException extends Exception {
        public UndesiredHTTPResponseCodeException(String message) {
            super(message);
        }
    }
    public static final List<Integer> listOfRetriableStatusCodes = Arrays.asList(429, 501, 502, 503, 504);

    private RequestSpecification getRequestSpecification(HttpRequest httpRequest) {
        return new RequestSpecBuilder().setBaseUri(httpRequest.getUrl()).addHeaders(httpRequest.getRequestHeaders())
                .addQueryParams(httpRequest.getQueryParams()).addPathParams(httpRequest.getPathParams())
                .addFilter(new RequestLoggingFilter(LogDetail.URI)).addFilter(new ResponseLoggingFilter(LogDetail.BODY))
                .build();
    }

    private HttpResponse getHttpResponse(Response response) {
        final Map<String, List<String>> responseHeaders = response.getHeaders().asList().stream().collect(
                Collectors.groupingBy(Header::getName, Collectors.mapping(Header::getValue, Collectors.toList())));

        return HttpResponse.builder().code(response.getStatusCode()).responseHeaders(responseHeaders)
                .body(response.body().asString()).build();
    }

    @Override
    public HttpResponse send(HttpRequest httpRequest) {
        RequestSpecification requestSpecification = getRequestSpecification(httpRequest);

        if (httpRequest.getBody() != null) {
            requestSpecification.body(httpRequest.getBody());
        }
        
        Response finalResponse = null;
        try {
            // The template performs retries in case of UndesiredHTTPResponseCodeException occurs in the code specified within context
            finalResponse = template.execute(context -> {
                String correlationId = UUID.randomUUID().toString();
                httpRequest.getRequestHeaders().put("correlation-id", correlationId);
                LOGGER.log(Level.INFO, "HTTP Request Sending with correlation-id: " + correlationId);

                Response response = given(requestSpecification).request(httpRequest.getHttpMethod()).then().extract()
                        .response();
                if (listOfRetriableStatusCodes.contains(response.getStatusCode())) {
                    throw new UndesiredHTTPResponseCodeException(response.toString());
                }
                return response;
            });
            return getHttpResponse(finalResponse);
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception during HTTP Request Send.", e);
            return HttpResponse.builder().exception(e).build();
        } finally {
            if (finalResponse == null) {
                LOGGER.log(Level.SEVERE, "HTTP Response Received: null.");
            } else {
                String formattedResponseHeaders = 
                finalResponse.getHeaders().asList().stream()
                    .map(header -> header.getName() + "=" + header.getValue())
                    .collect(Collectors.joining(", "));
                
                LOGGER.log(Level.INFO, "HTTP Response Received: \n" +
                "Response Headers: " + formattedResponseHeaders + "\n" +
                "Response Status Code: " + finalResponse.getStatusCode() + "\n" +
                "Response Body: " + formatJson(finalResponse.getBody().asString()));
            }
        }
    }

    @Override
    public <T> T send(HttpRequest httpRequest, Class<T> classOfT) {
        HttpResponse httpResponse = send(httpRequest);
        return JsonUtils.fromJson(httpResponse.getBody(), classOfT);
    }

    private String formatJson(String json) {
        try {
            return new GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(json));
        } catch (Exception e) {
            // Return the original JSON if formatting fails
            LOGGER.log(Level.WARNING, "Failed to format JSON", e);
            return json;
        }
    }
}
