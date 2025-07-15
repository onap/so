/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.onap.aaiclient.spring.compat;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class RestTemplateInitializerTest {

    @Test
    @Disabled("Test not yet working")
    public void thatRestTemplateCanBeRetrieved() {
        RestTemplate restTemplate = RestTemplateInitializer.create();
        assertNotNull(restTemplate);

        // there is no direct way to assert the configured base url of the template
        // here we are provoking an exception that contains the base url in it's
        // message. Obviously this has to match the aai.endpoint that is defined
        // in the aai.properties file
        Exception exception = assertThrows(ResourceAccessException.class, () -> {
            restTemplate.getForEntity("", String.class);
        });

        assertTrue(exception.getMessage().contains("http://localhost:8443"));
    }
}
