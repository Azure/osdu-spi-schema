/*
 * Copyright 2020-2024 Google LLC
 * Copyright 2020-2024 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.schema.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.test.client.ClientException;
import org.opengroup.osdu.core.test.util.TestFileUtil;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@UtilityClass
public class ErrorResponseAssertions {

  /**
   * Reads the expected error JSON from {@code expectedPayloadPath} (classpath resource),
   * extracts the AppError from {@code exception}, and asserts both are equal as JsonElements.
   */
  public static void assertApiErrorResponse(String expectedPayloadPath,
      ClientException exception) throws IOException {
    String expectedJson = TestFileUtil.readClasspathResource(expectedPayloadPath);
    AppError appError = getAppError(exception);
    JsonElement expected = new Gson().fromJson(expectedJson, JsonElement.class);
    JsonElement actual = new Gson().toJsonTree(appError);
    assertEquals("Error response mismatch", expected, actual);
  }

  /** Asserts that the exception's HTTP status code matches the expected value. */
  public static void assertErrorCode(int expectedCode, ClientException exception) {
    assertEquals("Expected HTTP status code", expectedCode, exception.getStatusCode());
  }

  /**
   * Asserts that the error message in the AppError contains the given substring.
   */
  public static void assertErrorMessageContains(String substring, ClientException exception) {
    AppError appError = getAppError(exception);
    String message = appError.getMessage();
    assertNotNull("AppError.message must not be null", message);
    log.debug("Asserting error message contains '{}' in: {}", substring, message);
    assertTrue("Error message '" + message + "' does not contain '" + substring + "'",
        message.contains(substring));
  }

  private static AppError getAppError(ClientException e) {
    AppError appError = e.getError();
    assertNotNull("Expected AppError to be present in ClientException", appError);
    return appError;
  }
}
