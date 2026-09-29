/*
 * Copyright 2021 Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opengroup.osdu.schema.provider.azure.di;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.schema.azure.di.EventGridConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EventGridConfigTest {

    private static String VALID_TOPIC_NAME = "topicname";
    private static String INVALID_TOPIC_NAME = "";

    @Test
    public void configurationValidationTests() {

        // Positive Case
        EventGridConfig eventGridConfig = new EventGridConfig(true, VALID_TOPIC_NAME);
        assertEquals(VALID_TOPIC_NAME, eventGridConfig.getCustomTopicName());
        assertEquals(true, eventGridConfig.isEventGridEnabled());
        
        // Positive Case
        eventGridConfig = new EventGridConfig(false, VALID_TOPIC_NAME);
        assertEquals(VALID_TOPIC_NAME, eventGridConfig.getCustomTopicName());
        assertEquals(false, eventGridConfig.isEventGridEnabled());
        
        eventGridConfig = new EventGridConfig(false, INVALID_TOPIC_NAME);
        assertEquals(INVALID_TOPIC_NAME, eventGridConfig.getCustomTopicName());
        assertEquals(false, eventGridConfig.isEventGridEnabled());


        // Negative Cases
        RuntimeException runtimeException = Assertions.assertThrows(RuntimeException.class,
                () -> new EventGridConfig(true, INVALID_TOPIC_NAME));
        assertEquals("Missing EventGrid Configuration", runtimeException.getMessage());

    }
}