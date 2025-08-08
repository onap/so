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
package org.onap.so.bpmn.infrastructure.appc.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.springframework.beans.factory.annotation.Autowired;

public class AppcRunTasksIT extends BaseIntegrationTest {

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/";

    @Autowired
    private AppcRunTasks appcRunTasks;

    private GenericVnf genericVnf;
    private RequestContext requestContext;
    private String msoRequestId;

    @Before
    public void before() {
        genericVnf = setGenericVnf();
        msoRequestId = UUID.randomUUID().toString();
        requestContext = setRequestContext();
        requestContext.setMsoRequestId(msoRequestId);
        gBBInput.setRequestContext(requestContext);
    }

    @Test
    public void preProcessActivityWithVserversTest() throws Exception {
        final String aaiVnfJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiGenericVnfWithVservers.json")));
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + AAIVersion.LATEST + "/network/generic-vnfs/generic-vnf/testVnfId1?depth=all"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(aaiVnfJson)
                                .withStatus(200)));

        final String aaiVserverJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiVserverFullQueryResponse.json")));
        wireMockServer.stubFor(get(urlEqualTo("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/mtn23a/tenants/tenant/e6beab145f6b49098277ac163ac1b4f3/vservers/vserver/48bd7f11-408f-417c-b834-b41c1b98f7d7"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(aaiVserverJson)
                                .withStatus(200)));
        wireMockServer.stubFor(get(urlEqualTo("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/mtn23a/tenants/tenant/e6beab145f6b49098277ac163ac1b4f3/vservers/vserver/1b3f44e5-d96d-4aac-bd9a-310e8cfb0af5"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(aaiVserverJson)
                                .withStatus(200)));
        wireMockServer.stubFor(get(urlEqualTo("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/mtn23a/tenants/tenant/e6beab145f6b49098277ac163ac1b4f3/vservers/vserver/14551849-1e70-45cd-bc5d-a256d49548a2"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(aaiVserverJson)
                                .withStatus(200)));

        appcRunTasks.preProcessActivity(execution);
        String vserverIdList = execution.getVariable("vserverIdList");
        String expectedVserverIdList =
                "{\"vserverIds\":\"[\\\"1b3f44e5-d96d-4aac-bd9a-310e8cfb0af5\\\",\\\"14551849-1e70-45cd-bc5d-a256d49548a2\\\",\\\"48bd7f11-408f-417c-b834-b41c1b98f7d7\\\"]\"}";
        String vmIdList = execution.getVariable("vmIdList");
        String expectedVmIdList =
                "{\"vmIds\":\"[\\\"http://VSERVER-link.com\\\",\\\"http://VSERVER-link.com\\\",\\\"http://VSERVER-link.com\\\"]\"}";

        assertEquals(vserverIdList, expectedVserverIdList);
        assertEquals(vmIdList, expectedVmIdList);
        assertEquals(execution.getVariable("actionQuiesceTraffic"), Action.QuiesceTraffic);
        assertEquals(execution.getVariable("rollbackQuiesceTraffic"), false);
    }

    @Test
    public void preProcessActivityNoVserversTest() throws Exception {
        final String aaiVnfJson = new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "aaiGenericVnf.json")));
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/v15/network/generic-vnfs/generic-vnf/testVnfId1?depth=all")).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json").withBody(aaiVnfJson).withStatus(200)));
        appcRunTasks.preProcessActivity(execution);
        assertNull(execution.getVariable("vmIdList"));
        assertNull(execution.getVariable("vServerIdList"));
        assertEquals(execution.getVariable("actionQuiesceTraffic"), Action.QuiesceTraffic);
        assertEquals(execution.getVariable("rollbackQuiesceTraffic"), false);
    }

    @Test
    public void runAppcCommandTest() {
        Action action = Action.QuiesceTraffic;
        ControllerSelectionReference controllerSelectionReference = new ControllerSelectionReference();
        controllerSelectionReference.setControllerName("testName");
        controllerSelectionReference.setActionCategory(action.toString());
        controllerSelectionReference.setVnfType("testVnfType");

        doReturn(controllerSelectionReference).when(catalogDbClient)
                .getControllerSelectionReferenceByVnfTypeAndActionCategory(genericVnf.getVnfType(),
                        Action.QuiesceTraffic.toString());

        execution.setVariable("aicIdentity", "testAicIdentity");

        String vnfId = genericVnf.getVnfId();
        genericVnf.setIpv4OamAddress("testOamIpAddress");
        String payload = "{\"testName\":\"testValue\",}";
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setPayload(payload);
        gBBInput.getRequestContext().setRequestParameters(requestParameters);

        String controllerType = "testName";
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", "testVnfName1");
        payloadInfo.put("aicIdentity", "testAicIdentity");
        payloadInfo.put("vnfHostIpAddress", "testOamIpAddress");
        payloadInfo.put("vserverIdList", null);
        payloadInfo.put("vfModuleId", null);
        payloadInfo.put("identityUrl", null);
        payloadInfo.put("vmIdList", null);

        doNothing().when(appCClient).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);

        appcRunTasks.runAppcCommand(execution, action);
        verify(appCClient, times(1)).runAppCCommand(action, msoRequestId, vnfId, Optional.of(payload), payloadInfo,
                controllerType);
    }
}
