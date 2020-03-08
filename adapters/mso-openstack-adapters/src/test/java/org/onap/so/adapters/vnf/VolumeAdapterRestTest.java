/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.vnf;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.QueryVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.RollbackVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupRequest;
import org.onap.so.adapters.vnfrest.UpdateVolumeGroupResponse;
import org.onap.so.adapters.vnfrest.VolumeGroupExceptionResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackWithBody_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStacksWithBody_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStacks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;

public class VolumeAdapterRestTest extends VolumeGroupAdapterCommon {

    @Test
    public void testCreateVNFVolumes() throws IOException {

        wireMockServer.stubFor(patch(urlPathEqualTo("/infraActiveRequests/62265093-277d-4388-9ba6-449838ade586"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackPostStacks_200(wireMockServer);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        CreateVolumeGroupRequest request = buildCreateVfModuleRequest();

        HttpEntity<CreateVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<CreateVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups"), HttpMethod.POST, entity,
                        CreateVolumeGroupResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testCreateVNFVolumesAsync() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackPostStacks_200(wireMockServer);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        CreateVolumeGroupRequest request = buildCreateVfModuleRequest();
        request.setNotificationUrl("http://localhost:8080");

        HttpEntity<CreateVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<CreateVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups"), HttpMethod.POST, entity,
                        CreateVolumeGroupResponse.class);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testDeleteVNFVolumes() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        DeleteVolumeGroupRequest request = buildDeleteVolumeGroupRequest();
        HttpEntity<DeleteVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<DeleteVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID),
                        HttpMethod.DELETE, entity, DeleteVolumeGroupResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testDeleteVNFVolumesAsync() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        DeleteVolumeGroupRequest request = buildDeleteVolumeGroupRequest();
        request.setNotificationUrl("http://localhost:8080");
        HttpEntity<DeleteVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<DeleteVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID),
                        HttpMethod.DELETE, entity, DeleteVolumeGroupResponse.class);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testRollbackVNFVolumes() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        RollbackVolumeGroupRequest request = buildRollbackVolumeGroupRequest();
        HttpEntity<RollbackVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<RollbackVolumeGroupResponse> response = restTemplate.exchange(
                createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID + "/rollback"),
                HttpMethod.DELETE, entity, RollbackVolumeGroupResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testRollbackVNFVolumesAsync() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        RollbackVolumeGroupRequest request = buildRollbackVolumeGroupRequest();
        request.setNotificationUrl("http://localhost:8080");
        HttpEntity<RollbackVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<RollbackVolumeGroupResponse> response = restTemplate.exchange(
                createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID + "/rollback"),
                HttpMethod.DELETE, entity, RollbackVolumeGroupResponse.class);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testQueryVNFVolumes() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStacksWithBody_200(wireMockServer, "UPDATE_COMPLETE");
        javax.ws.rs.core.UriBuilder builder = UriBuilder.fromPath("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID);
        builder.queryParam("cloudSiteId", CLOUDSITE_ID).queryParam("tenantId", TENANT_ID)
                .queryParam("volumeGroupStackId", VOUME_GROUP_NAME).queryParam("skipAAI", true)
                .queryParam("msoRequest.requestId", MSO_REQUEST_ID)
                .queryParam("msoRequest.serviceInstanceId", MSO_SERVICE_INSTANCE_ID);

        ResponseEntity<QueryVolumeGroupResponse> response = restTemplate.exchange(
                createURLWithPort(builder.build().toString()), HttpMethod.GET, null, QueryVolumeGroupResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testQueryVNFVolumesError() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStacksWithBody_200(wireMockServer, "UPDATE_COMPLETE");
        javax.ws.rs.core.UriBuilder builder = UriBuilder.fromPath("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID);
        builder.queryParam("tenantId", TENANT_ID).queryParam("volumeGroupStackId", VOUME_GROUP_NAME)
                .queryParam("skipAAI", true).queryParam("msoRequest.requestId", MSO_REQUEST_ID)
                .queryParam("msoRequest.serviceInstanceId", MSO_SERVICE_INSTANCE_ID);

        ResponseEntity<VolumeGroupExceptionResponse> response =
                restTemplate.exchange(createURLWithPort(builder.build().toString()), HttpMethod.GET, null,
                        VolumeGroupExceptionResponse.class);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testUpdateVNFVolumes() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStacksWithBody_200(wireMockServer, "CREATE_COMPLETE");
        mockOpenStackPutStack(wireMockServer, VOUME_GROUP_NAME + "/stackId", 200);
        mockOpenStackGetStackWithBody_200(wireMockServer, "UPDATE_COMPLETE");
        UpdateVolumeGroupRequest request = buildUpdateVolumeGroupRequest();
        HttpEntity<UpdateVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<UpdateVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID),
                        HttpMethod.PUT, entity, UpdateVolumeGroupResponse.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testUpdateVNFVolumesAsync() throws IOException {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStacksWithBody_200(wireMockServer, "CREATE_COMPLETE");
        mockOpenStackPutStack(wireMockServer, VOUME_GROUP_NAME + "/stackId", 200);
        mockOpenStackGetStackWithBody_200(wireMockServer, "UPDATE_COMPLETE");
        UpdateVolumeGroupRequest request = buildUpdateVolumeGroupRequest();
        request.setNotificationUrl("http://localhost:8080");
        HttpEntity<UpdateVolumeGroupRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<UpdateVolumeGroupResponse> response =
                restTemplate.exchange(createURLWithPort("/services/rest/v1/volume-groups/" + VOLUME_GROUP_ID),
                        HttpMethod.PUT, entity, UpdateVolumeGroupResponse.class);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
    }


}
