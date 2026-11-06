/*
 * Copyright 2017-2026, The Open Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opengroup.osdu.core.common.http.HttpConfiguration;
import org.opengroup.osdu.schema.config.WebConfiguration;
import org.opengroup.osdu.schema.security.WebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Runs the service's web and security configuration on a real Tomcat to cover the behaviour that
 * changes with Spring Boot 4 / Spring Framework 7: os-core-common's Jackson 3 mapper as the HTTP
 * JSON mapper, trailing slash handling and the Spring Security firewall.
 */
@SpringBootTest(
    classes = Boot4HttpCompatibilityTest.App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.server.port=")
class Boot4HttpCompatibilityTest {

  private final HttpClient client = HttpClient.newHttpClient();

  @LocalServerPort
  private int port;

  @Autowired
  private JsonMapper jsonMapper;

  @ParameterizedTest
  @ValueSource(strings = {"/probe", "/probe/"})
  void shouldUseCommonJsonMapperWithEitherTrailingSlash(String path) throws Exception {
    HttpResponse<String> response = post(path + "?mode=update",
        "{\"zeta\":\"z\",\"count\":null,\"alpha\":null,\"unknown\":1}");

    assertEquals(200, response.statusCode());
    // count: null is read as 0 and alpha: null is left out, as with the Jackson 2 mapper from os-core-common 9.x.
    assertEquals(jsonMapper.readTree("{\"zeta\":\"update\",\"count\":0}"), jsonMapper.readTree(response.body()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/probe;jsessionid=x", "/a//probe", "/probe/%2e%2e/probe"})
  void shouldRejectMaliciousPathsWithFirewall(String path) throws Exception {
    assertEquals(400, post(path + "?mode=update", "{}").statusCode());
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @Configuration
  @EnableAutoConfiguration
  @Import({HttpConfiguration.class, WebSecurity.class, WebConfiguration.class, App.ProbeController.class})
  static class App {

    @RestController
    static class ProbeController {
      @PostMapping(value = "/probe", produces = MediaType.APPLICATION_JSON_VALUE)
      Probe echo(@RequestParam("mode") String mode, @RequestBody Probe probe) {
        probe.zeta = mode;
        return probe;
      }
    }
  }

  public static class Probe {
    public String zeta;
    public int count;
    public String alpha;
  }
}
