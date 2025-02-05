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
import static org.junit.Assert.assertNotNull;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreated_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackDeleteOrUpdateComplete_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackGet;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackPost;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.xml.ws.Holder;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.network.exceptions.NetworkException;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.woorea.openstack.base.client.OpenStackResponseException;

public class MSONetworkAdapterImplTest extends BaseRestTestUtils {

    public static final String NETWORK_ID = "43173f6a-d699-414b-888f-ab243dda6dfe";
    public static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";

    @Autowired
    MsoNetworkAdapterImpl impl;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void createNetworkByModelNameHeatMode() throws IOException, NetworkException {

        mockUpdateRequestDb(wireMockServer, "9733c8d1-2668-4e5f-8b51-2cacc9b662c0");

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId");

        mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack.json");

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId");

        MsoRequest request = new MsoRequest();
        request.setRequestId("9733c8d1-2668-4e5f-8b51-2cacc9b662c0");
        request.setServiceInstanceId("MIS/1806/25009/SW_INTERNET");

        Holder<String> stackId = new Holder<String>();

        impl.createNetwork("mtn13", "bef254252c5d44e6bcec65c180180ab5", "CONTRAIL30_GNDIRECT", null,
                "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001", "dvs-mtjnj-01", null, null, null, null, false,
                false, null, null, null, request, stackId, new MutableBoolean());

        assertNotNull(stackId.value);
    }

    @Test
    public void createNetworkByModelNameAlreadyExistHeatModeFailIfExistTrue() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");

        MsoRequest request = new MsoRequest();
        request.setRequestId("9733c8d1-2668-4e5f-8b51-2cacc9b662c0");
        request.setServiceInstanceId("MIS/1806/25009/SW_INTERNET");

        Holder<String> stackId = new Holder<String>();

        impl.createNetwork("mtn13", "bef254252c5d44e6bcec65c180180ab5", "CONTRAIL30_GNDIRECT", null,
                "dvspg-VCE_VPE-mtjnj40avbc", "dvs-mtjnj-01", null, null, null, null, true, false, null, null, null,
                request, stackId, new MutableBoolean());
    }


    @Test
    public void createNetworkByModelNameHeatModeQueryNetworkException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenstackGet(wireMockServer, "/mockPublicUrl/stacks/dvspg-VCE_VPE-mtjnj40avbc",
                HttpStatus.SC_INTERNAL_SERVER_ERROR);

        MsoRequest request = new MsoRequest();
        request.setRequestId("9733c8d1-2668-4e5f-8b51-2cacc9b662c0");
        request.setServiceInstanceId("MIS/1806/25009/SW_INTERNET");

        Holder<String> stackId = new Holder<String>();

        impl.createNetwork("mtn13", "bef254252c5d44e6bcec65c180180ab5", "CONTRAIL30_GNDIRECT", null,
                "dvspg-VCE_VPE-mtjnj40avbc", "dvs-mtjnj-01", null, null, null, null, true, false, null, null, null,
                request, stackId, new MutableBoolean());
    }

    @Test
    public void createNetworkByModelNameHeatModeCreateNetworkException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, "dvspg-VCE_VPE-mtjnj40avbc");

        mockOpenstackPost(wireMockServer, "/mockPublicUrl/stacks", HttpStatus.SC_INTERNAL_SERVER_ERROR);

        MsoRequest request = new MsoRequest();
        request.setRequestId("9733c8d1-2668-4e5f-8b51-2cacc9b662c0");
        request.setServiceInstanceId("MIS/1806/25009/SW_INTERNET");

        Holder<String> stackId = new Holder<String>();

        impl.createNetwork("mtn13", "bef254252c5d44e6bcec65c180180ab5", "CONTRAIL30_GNDIRECT", null,
                "dvspg-VCE_VPE-mtjnj40avbc", "dvs-mtjnj-01", null, null, null, null, false, false, null, null, null,
                request, stackId, new MutableBoolean());
    }

    @Test
    public void createNetworkByModelNameCloudSiteNotPresentError() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack.json");

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");

        MsoRequest request = new MsoRequest();
        request.setRequestId("9733c8d1-2668-4e5f-8b51-2cacc9b662c0");
        request.setServiceInstanceId("MIS/1806/25009/SW_INTERNET");

        Holder<String> stackId = new Holder<String>();

        impl.createNetwork("mtn14", "bef254252c5d44e6bcec65c180180ab5", "CONTRAIL30_GNDIRECT", null,
                "dvspg-VCE_VPE-mtjnj40avbc", "dvs-mtjnj-01", null, null, null, null, false, false, null, null, null,
                request, stackId, new MutableBoolean());

    }

    @Test
    public void deleteNetworkHeatModeSuccess() throws IOException, NetworkException {

        mockUpdateRequestDb(wireMockServer, "5a29d907-b8c7-47bf-85f3-3940c0cce0f7");

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_200(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        impl.deleteNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", request);
    }

    @Test
    public void deleteNetworkDeleteStackException() throws IOException, NetworkException {
        exception.expect(OpenStackResponseException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_500(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        impl.deleteNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", request);
    }

    @Test
    public void deleteNetworkError() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_DeleteComplete.json");

        mockOpenStackDeleteStack_200(wireMockServer);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        impl.deleteNetwork("", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", request);

    }

    @Test
    public void updateNetworkNeutronUpdateException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", NETWORK_ID, HttpStatus.SC_OK);
        mockOpenStackPutNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL31_GNDIRECT", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);
    }

    @Test
    public void updateNetworkHeatUpdateException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json", NETWORK_NAME);

        mockOpenStackPutStack(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);

    }

    @Test
    public void updateNetworkHeatQueryException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_500(wireMockServer, NETWORK_NAME);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);
    }

    @Test
    public void updateNetworkHeatStackNotFound() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetStack_404(wireMockServer, NETWORK_NAME);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL30_BASIC", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);
    }

    @Test
    public void updateNetworkNeutronQueryException() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL31_GNDIRECT", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);
    }

    @Test
    public void updateNetworkNeutronStackNotFound() throws IOException, NetworkException {
        exception.expect(NetworkException.class);

        mockOpenStackResponseAccess(wireMockServer, wireMockPort);

        mockOpenStackGetNeutronNetwork(wireMockServer, NETWORK_ID, HttpStatus.SC_NOT_FOUND);

        MsoRequest request = new MsoRequest();
        request.setRequestId("5a29d907-b8c7-47bf-85f3-3940c0cce0f7");
        request.setServiceInstanceId("ab652f96-1fc3-4fdd-8e1b-4af629bc22c0");

        Holder<String> stackId = new Holder<String>();

        impl.updateNetwork("mtn13", "2871503957144f72b3cf481b379828ec", "CONTRAIL31_GNDIRECT", null,
                "43173f6a-d699-414b-888f-ab243dda6dfe", "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0",
                "TestPhysicalNetwork", null, null, null, null, null, null, null, request, stackId);
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
