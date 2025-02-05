/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.adapter.network;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import jakarta.ws.rs.core.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.adapters.nwrest.CreateNetworkError;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkError;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.QueryNetworkError;
import org.onap.so.adapters.nwrest.QueryNetworkResponse;
import org.onap.so.adapters.nwrest.RollbackNetworkError;
import org.onap.so.adapters.nwrest.RollbackNetworkRequest;
import org.onap.so.adapters.nwrest.RollbackNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkError;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.openstack.beans.NetworkRollback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkAdapterClientIT extends BaseIntegrationTest {

    private static final String TESTING_ID = "___TESTING___";
    private static final String AAI_NETWORK_ID = "test";
    private static final String REST_ENDPOINT = "/networks/rest/v1/networks";

    private NetworkAdapterClientImpl client = new NetworkAdapterClientImpl();
    private ObjectMapper mapper = new JettisonStyleMapperProvider().getMapper();

    @BeforeClass
    public static void setUp() {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    @Test
    public void createNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
        CreateNetworkRequest request = new CreateNetworkRequest();
        request.setCloudSiteId(TESTING_ID);

        CreateNetworkResponse mockResponse = new CreateNetworkResponse();
        mockResponse.setNetworkCreated(true);
        wireMockServer.stubFor(post(urlPathEqualTo(REST_ENDPOINT))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        CreateNetworkResponse response = client.createNetwork(request);
        assertEquals("Testing CreateVfModule response", true, response.getNetworkCreated());
    }

    @Test(expected = NetworkAdapterClientException.class)
    public void createNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
        CreateNetworkRequest request = new CreateNetworkRequest();
        request.setCloudSiteId(TESTING_ID);

        CreateNetworkError mockResponse = new CreateNetworkError();
        mockResponse.setMessage("Error in create network");
        wireMockServer.stubFor(post(urlPathEqualTo(REST_ENDPOINT))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

        client.createNetwork(request);
    }

    @Test
    public void deleteNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
        DeleteNetworkRequest request = new DeleteNetworkRequest();
        request.setCloudSiteId(TESTING_ID);

        DeleteNetworkResponse mockResponse = new DeleteNetworkResponse();
        mockResponse.setNetworkDeleted(true);

        wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        DeleteNetworkResponse response = client.deleteNetwork(AAI_NETWORK_ID, request);
        assertEquals("Testing DeleteVfModule response", true, response.getNetworkDeleted());
    }

    @Test(expected = NetworkAdapterClientException.class)
    public void deleteNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
        DeleteNetworkRequest request = new DeleteNetworkRequest();
        request.setCloudSiteId(TESTING_ID);

        DeleteNetworkError mockResponse = new DeleteNetworkError();
        mockResponse.setMessage("Error in delete network");
        wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

        client.deleteNetwork(AAI_NETWORK_ID, request);
    }

    @Test
    public void rollbackNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
        RollbackNetworkRequest request = new RollbackNetworkRequest();
        NetworkRollback rollback = new NetworkRollback();
        rollback.setCloudId(TESTING_ID);
        request.setNetworkRollback(rollback);

        RollbackNetworkResponse mockResponse = new RollbackNetworkResponse();
        mockResponse.setNetworkRolledBack(true);

        wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        RollbackNetworkResponse response = client.rollbackNetwork(AAI_NETWORK_ID, request);
        assertEquals("Testing DeleteVfModule response", true, response.getNetworkRolledBack());
    }

    @Test(expected = NetworkAdapterClientException.class)
    public void rollbackNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
        RollbackNetworkRequest request = new RollbackNetworkRequest();
        NetworkRollback rollback = new NetworkRollback();
        rollback.setCloudId(TESTING_ID);
        request.setNetworkRollback(rollback);

        RollbackNetworkError mockResponse = new RollbackNetworkError();
        mockResponse.setMessage("Error in rollback network");

        wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

        client.rollbackNetwork(AAI_NETWORK_ID, request);
    }

    @Test
    public void queryNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
        QueryNetworkResponse mockResponse = new QueryNetworkResponse();
        mockResponse.setNetworkExists(true);

        wireMockServer.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .withQueryParam("cloudSiteId", equalTo(TESTING_ID)).withQueryParam("tenantId", equalTo(TESTING_ID))
                .withQueryParam("networkStackId", equalTo("networkStackId")).withQueryParam("skipAAI", equalTo("true"))
                .withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
                .withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        QueryNetworkResponse response = client.queryNetwork(AAI_NETWORK_ID, TESTING_ID, TESTING_ID, "networkStackId",
                true, "testRequestId", "serviceInstanceId");
        assertEquals("Testing QueryVfModule response", true, response.getNetworkExists());
    }

    @Test(expected = NetworkAdapterClientException.class)
    public void queryNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
        QueryNetworkError mockResponse = new QueryNetworkError();
        mockResponse.setMessage("Error in query network");

        wireMockServer.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .withQueryParam("cloudSiteId", equalTo(TESTING_ID)).withQueryParam("tenantId", equalTo(TESTING_ID))
                .withQueryParam("networkStackId", equalTo("networkStackId")).withQueryParam("skipAAI", equalTo("true"))
                .withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
                .withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

        client.queryNetwork(AAI_NETWORK_ID, TESTING_ID, TESTING_ID, "networkStackId", true, "testRequestId",
                "serviceInstanceId");
    }

    @Test
    public void updateNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
        UpdateNetworkRequest request = new UpdateNetworkRequest();
        request.setCloudSiteId(TESTING_ID);
        request.setNetworkId("test1");

        UpdateNetworkResponse mockResponse = new UpdateNetworkResponse();
        mockResponse.setNetworkId("test1");
        wireMockServer.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        UpdateNetworkResponse response = client.updateNetwork(AAI_NETWORK_ID, request);
        assertEquals("Testing UpdateVfModule response", "test1", response.getNetworkId());
    }

    @Test
    public void updateNetworkTestAsync() throws NetworkAdapterClientException, JsonProcessingException {
        UpdateNetworkRequest request = new UpdateNetworkRequest();
        request.setCloudSiteId(TESTING_ID);
        request.setNetworkId("test1");

        UpdateNetworkResponse mockResponse = new UpdateNetworkResponse();
        mockResponse.setNetworkId("test1");
        wireMockServer.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

        Response response = client.updateNetworkAsync(AAI_NETWORK_ID, request);
        assertNotNull(response.getEntity());
    }

    @Test(expected = NetworkAdapterClientException.class)
    public void updateNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
        UpdateNetworkRequest request = new UpdateNetworkRequest();
        request.setCloudSiteId(TESTING_ID);
        request.setNetworkId("test1");

        UpdateNetworkError mockResponse = new UpdateNetworkError();
        mockResponse.setMessage("Error in update network");
        wireMockServer.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

        client.updateNetwork(AAI_NETWORK_ID, request);
    }
}
