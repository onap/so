/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.bpmn.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class CamundaRestApiTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void fetchAndLockExternalTasksSucceeds() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"workerId\":\"test\",\"maxTasks\":1,"
                + "\"topics\":[{\"topicName\":\"none\",\"lockDuration\":1000}]}";

        ResponseEntity<String> response = restTemplate.withBasicAuth("test", "test").postForEntity(
                "/sobpmnengine/external-task/fetchAndLock", new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).as(response.toString()).isTrue();
        assertThat(response.getBody()).isEqualTo("[]");
    }
}
