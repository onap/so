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

package org.onap.so.adapters.network;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetAllNeutronNetworks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetAllNeutronNetworks_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetNeutronNetwork_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreated_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackDeleteOrUpdateComplete_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostNeutronNetwork_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutNeutronNetwork_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackGet;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackPost;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.github.tomakehurst.wiremock.WireMockServer;

public class MSONetworkAdapterImplTest extends BaseRestTestUtils {

    public static final String NETWORK_ID = "43173f6a-d699-414b-888f-ab243dda6dfe";
    public static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";


    @Test
    public void createNetworkByModelNameHeatMode() throws IOException {

        mockUpdateRequestDb(wireMockServer, "9733c8d1-2668-4e5f-8b51-2cacc9b662c0");

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId");

        mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack.json");

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkByModelNameAlreadyExistHeatMode() throws IOException {

        mockUpdateRequestDb(wireMockServer, "9733c8d1-2668-4e5f-8b51-2cacc9b662c0");

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack.json");

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkByModelNameAlreadyExistHeatModeFailIfExistTrue() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/CreateNetwork_Fail_If_Exist_True.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }


    @Test
    public void createNetworkByModelNameHeatModeQueryNetworkException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenstackGet(wireMockServer, "/mockPublicUrl/stacks/dvspg-VCE_VPE-mtjnj40avbc",
                HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkByModelNameHeatModeCreateNetworkException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, "dvspg-VCE_VPE-mtjnj40avbc");

        mockOpenstackPost(wireMockServer, "/mockPublicUrl/stacks", HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkByModelNameCloudSiteNotPresentError() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack.json");

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/CreateNetwork_InvalidCloudSiteId.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void deleteNetworkHeatModeSuccess() throws IOException {

        mockUpdateRequestDb(wireMockServer, "5a29d907-b8c7-47bf-85f3-3940c0cce0f7");

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_200(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void deleteNetworkDeleteStackException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_500(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void deleteNetworkError() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_200(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/DeleteNetwork.xml").replace("mtn13", ""), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkNeutronUpdateException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", NETWORK_ID, HttpStatus.SC_OK);
        mockOpenStackPutNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"),
                        uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkHeatUpdateException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json", NETWORK_NAME);

        mockOpenStackPutStack(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkHeatQueryException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_500(wireMockServer, NETWORK_NAME);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkHeatStackNotFound() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, NETWORK_NAME);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkNeutronQueryException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"),
                        uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateNetworkNeutronStackNotFound() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_NOT_FOUND);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"),
                        uri, HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void queryNetworkHeatModesuccess() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json", NETWORK_ID);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"), uri,
                        HttpMethod.POST);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void queryNetworkHeatModeQueryException() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_500(wireMockServer, NETWORK_ID);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"), uri,
                        HttpMethod.POST);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void queryNetworkNeutronModeSuccess() throws IOException {

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", NETWORK_ID, HttpStatus.SC_OK);

        String uri = "/services/NetworkAdapter";
        headers.set("X-ECOMP-RequestID", "123456789456127");
        ResponseEntity<String> response =
                sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC", "CONTRAIL31_GNDIRECT"), uri,
                        HttpMethod.POST);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    public ResponseEntity<String> sendXMLRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        headers.set("Accept", MediaType.APPLICATION_XML);
        headers.set("Content-Type", MediaType.APPLICATION_XML);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));

        HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);

        return response;
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/" + JsonInput;
        String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
        return input;
    }

    public static void mockUpdateRequestDb(WireMockServer wireMockServer, String requestId) throws IOException {
        wireMockServer.stubFor(patch(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
    }
}
