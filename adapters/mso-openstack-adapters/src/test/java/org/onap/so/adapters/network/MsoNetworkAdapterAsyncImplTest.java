/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.onap.so.bpmn.mock.StubOpenStack.getBodyFromFile;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreatedVUSP_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreated_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackDeleteOrUpdateComplete_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStacks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutPublicUrlStackByNameAndID_NETWORK2_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccessQueryNetwork;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.NetworkRollback;
import org.onap.so.openstack.beans.Subnet;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.tomakehurst.wiremock.WireMockServer;

public class MsoNetworkAdapterAsyncImplTest extends BaseRestTestUtils {
    @Autowired
    MsoNetworkAdapterAsyncImpl impl;

    @Test
    public void healthCheckATest() {
        MsoNetworkAdapterAsyncImpl mNAAimpl = new MsoNetworkAdapterAsyncImpl();
        mNAAimpl.healthCheckA();
    }

    @Test
    public void rollbackNetworkATest() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/notificationUrl"))
                .withRequestBody(containing("<completed>true</completed>"))
                .willReturn(aResponse().withBody(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:net=\"http://org.onap.so/networkNotify\">\n"
                                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n"
                                + "      <net:rollbackNetworkNotificationResponse>\n"
                                + "      </net:rollbackNetworkNotificationResponse>\n" + "   </soapenv:Body>\n"
                                + "</soapenv:Envelope>")
                        .withStatus(HttpStatus.SC_OK)));
        NetworkRollback nrb = getNetworkRollback("mtn13");
        impl.rollbackNetworkA(nrb, "messageId", "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void rollbackNetworkATest_NotifyException() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(
                post(urlPathEqualTo("/notificationUrl")).withRequestBody(containing("<completed>true</completed>"))
                        .willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
        NetworkRollback nrb = getNetworkRollback("mtn13");
        impl.rollbackNetworkA(nrb, "messageId", "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    private NetworkRollback getNetworkRollback(String cloudId) {
        NetworkRollback nrb = new NetworkRollback();
        nrb.setCloudId(cloudId);
        nrb.setMsoRequest(new MsoRequest());
        nrb.setModelCustomizationUuid("3bdbb104-476c-483e-9f8b-c095b3d3068c");
        nrb.setNetworkCreated(true);
        nrb.setNetworkId("networkId");
        nrb.setNetworkName("networkName");
        nrb.setNetworkStackId("networkStackId");
        nrb.setNetworkType("networkType");
        nrb.setNeutronNetworkId("neutronNetworkId");
        nrb.setPhysicalNetwork("physicalNetwork");
        nrb.setTenantId("tenantId");
        nrb.setVlans(new ArrayList<>());
        return nrb;
    }

    @Test
    public void rollbackNetworkATestNetworkException() {
        NetworkRollback nrb = getNetworkRollback("cloudId");

        impl.rollbackNetworkA(nrb, "messageId", "http://localhost");
    }

    @Test
    public void noRollbackNetworkATest() {
        impl.rollbackNetworkA(null, "messageId", "http://localhost");
    }


    @Test
    public void deleteNetworkATest() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/notificationUrl"))
                .withRequestBody(containing("<completed>true</completed>"))
                .willReturn(aResponse().withBody(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:net=\"http://org.onap.so/networkNotify\">\n"
                                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n"
                                + "      <net:deleteNetworkNotificationResponse>\n"
                                + "      </net:deleteNetworkNotificationResponse>\n" + "   </soapenv:Body>\n"
                                + "</soapenv:Envelope>")
                        .withStatus(HttpStatus.SC_OK)));
        impl.deleteNetworkA("mtn13", "tenantId", "networkType", "modelCustomizationUuid", "networkId", "messageId",
                new MsoRequest(), "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void deleteNetworkATest_NotifyException() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(
                post(urlPathEqualTo("/notificationUrl")).withRequestBody(containing("<completed>true</completed>"))
                        .willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
        impl.deleteNetworkA("mtn13", "tenantId", "networkType", "modelCustomizationUuid", "networkId", "messageId",
                new MsoRequest(), "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void deleteNetworkATest_NetworkException() {
        impl.deleteNetworkA("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
                "messageId", new MsoRequest(), "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void updateNetworkATest() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");
        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_UpdateComplete.json");
        mockOpenStackPutPublicUrlStackByNameAndID_NETWORK2_200(wireMockServer);
        wireMockServer.stubFor(post(urlPathEqualTo("/notificationUrl"))
                .withRequestBody(containing("updateNetworkNotification"))
                .willReturn(aResponse().withBody(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:net=\"http://org.onap.so/networkNotify\">\n"
                                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n"
                                + "      <net:updateNetworkNotificationResponse>\n"
                                + "      </net:updateNetworkNotificationResponse>\n" + "   </soapenv:Body>\n"
                                + "</soapenv:Envelope>")
                        .withStatus(HttpStatus.SC_OK)));
        HashMap<String, String> networkParams = new HashMap<String, String>();
        networkParams.put("shared", "true");
        networkParams.put("external", "false");
        impl.updateNetworkA("mtn13", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "dvspg-VCE_VPE-mtjnj40avbc", "dvspg-VCE_VPE-mtjnj40avbc", "physicalNetworkName", new ArrayList<>(),
                new ArrayList<Subnet>(), networkParams, "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void updateNetworkATest_NotifyExcpetion() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        mockOpenStackGetStackCreated_200(wireMockServer, "OpenstackResponse_Stack_Created.json",
                "dvspg-VCE_VPE-mtjnj40avbc");
        mockOpenStackGetStackDeleteOrUpdateComplete_200(wireMockServer, "OpenstackResponse_Stack_UpdateComplete.json");
        mockOpenStackPutPublicUrlStackByNameAndID_NETWORK2_200(wireMockServer);
        HashMap<String, String> networkParams = new HashMap<String, String>();
        networkParams.put("shared", "true");
        networkParams.put("external", "false");
        impl.updateNetworkA("mtn13", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "dvspg-VCE_VPE-mtjnj40avbc", "dvspg-VCE_VPE-mtjnj40avbc", "physicalNetworkName", new ArrayList<>(),
                new ArrayList<>(), networkParams, "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void updateNetworkATest_NetworkException() {
        impl.updateNetworkA("cloudSiteId", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "networkId", "dvspg-VCE_VPE-mtjnj40avbc", "physicalNetworkName", new ArrayList<>(), new ArrayList<>(),
                new HashMap<String, String>(), "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void queryNetworkATest() throws IOException {
        mockOpenStackResponseAccessQueryNetwork(wireMockServer, wireMockPort);
        impl.queryNetworkA("mtn13", "tenantId", "networkId", "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void createNetworkATest() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/notificationUrl"))
                .withRequestBody(containing("createNetworkNotification"))
                .willReturn(aResponse().withBody(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:net=\"http://org.onap.so/networkNotify\">\n"
                                + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n"
                                + "      <net:createNetworkNotificationResponse>\n"
                                + "      </net:createNetworkNotificationResponse>\n" + "   </soapenv:Body>\n"
                                + "</soapenv:Envelope>")
                        .withStatus(HttpStatus.SC_OK)));
        mockOpenStackGetStackCreatedVUSP_200(wireMockServer);
        mockOpenStackPostStacks_200(wireMockServer);
        mockOpenStackPostStacks_200(wireMockServer);
        HashMap<String, String> networkParams = new HashMap<String, String>();
        impl.createNetworkA("mtn13", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0", "physicalNetworkName", new ArrayList<>(), false,
                false, new ArrayList<>(), networkParams, "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void createNetworkATest_NotifyException() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/v2.0/tokens")).withRequestBody(containing("tenantId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(getBodyFromFile("OpenstackResponse_Access.json", wireMockPort, "/mockPublicUrl"))
                        .withStatus(HttpStatus.SC_OK)));
        mockOpenStackGetStackCreatedVUSP_200(wireMockServer);
        mockOpenStackPostStacks_200(wireMockServer);
        HashMap<String, String> networkParams = new HashMap<String, String>();
        networkParams.put("shared", "true");
        networkParams.put("external", "false");
        impl.createNetworkA("mtn13", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0", "physicalNetworkName", new ArrayList<>(), false,
                false, new ArrayList<>(), networkParams, "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

    @Test
    public void createNetworkATest_NetworkException() {
        impl.createNetworkA("mtn13", "tenantId", "networkType", "3bdbb104-476c-483e-9f8b-c095b3d3068c",
                "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0", "physicalNetworkName", new ArrayList<>(), false,
                false, new ArrayList<>(), new HashMap<String, String>(), "messageId", new MsoRequest(),
                "http://localhost:" + wireMockPort + "/notificationUrl");
    }

}
