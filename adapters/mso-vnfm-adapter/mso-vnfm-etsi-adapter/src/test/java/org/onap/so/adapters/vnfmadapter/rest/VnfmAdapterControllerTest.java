/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.net.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.onap.vnfmadapter.v1.model.DeleteVnfResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VnfmAdapterControllerTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    @Test
    public void createVnf_ValidRequest_Returns202AndJobId() throws Exception {
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        final RequestEntity<CreateVnfRequest> request =
                RequestEntity.post(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myVnfId"))
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .header("X-ONAP-RequestId", "myRequestId").header("X-ONAP-InvocationID", "myInvocationId")
                        .body(createVnfRequest);
        final ResponseEntity<CreateVnfResponse> response = restTemplate.exchange(request, CreateVnfResponse.class);
        assertEquals(202, response.getStatusCode().value());
        assertNotNull(response.getBody().getJobId());
    }

    @Test
    public void createVnf_UnauthorizedUser_Returns401() throws Exception {
        final TestRestTemplate restTemplateWrongPassword = new TestRestTemplate("test", "wrongPassword");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        final RequestEntity<CreateVnfRequest> request =
                RequestEntity.post(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myVnfId"))
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .header("X-ONAP-RequestId", "myRequestId").header("X-ONAP-InvocationID", "myInvocationId")
                        .body(createVnfRequest);
        final ResponseEntity<CreateVnfResponse> response =
                restTemplateWrongPassword.exchange(request, CreateVnfResponse.class);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    public void deleteVnf_ValidRequest_Returns202AndJobId() throws Exception {
        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> response = restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(202, response.getStatusCode().value());
        assertNotNull(response.getBody().getJobId());
    }

}
