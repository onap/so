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

package org.onap.so.adapters.vnf;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackGetWithResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.xml.ws.Holder;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.adapters.vnf.exceptions.VnfNotFound;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;


public class MsoVnfAdapterImplTest extends BaseRestTestUtils {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MsoHeatUtils heatUtils;

    @Autowired
    MsoVnfAdapterImpl instance;

    String vnfName = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId";

    @Test
    public void createVnfTest() throws Exception {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockUpdateRequestDb(wireMockServer, "12345");

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", null, null, "b4ea86b4-253f-11e7-93ae-92361f002671", map, Boolean.TRUE, Boolean.TRUE,
                Boolean.FALSE, msoRequest, new Holder<>());
        assertNotNull(map);
    }

    @Test
    public void createVnfTest_NullFailIfExists() throws Exception {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockUpdateRequestDb(wireMockServer, "12345");

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", null, null, "b4ea86b4-253f-11e7-93ae-92361f002671", map, null, Boolean.TRUE, Boolean.FALSE,
                msoRequest, new Holder<>());
        assertNotNull(map);
    }

    @Test
    public void createVnfTest_HeatStatusFailed() throws Exception {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        wireMockServer.stubFor(get(
                urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("OpenstackResponse_Stack_Failed_VfModule.json")
                                .withStatus(HttpStatus.SC_OK))
                        .inScenario("HeatStatusFailure").whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo("HeatStackFailed"));

        wireMockServer.stubFor(get(
                urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
                                .withStatus(HttpStatus.SC_OK))
                        .inScenario("HeatStatusFailure").whenScenarioStateIs("HeatStackFailed")
                        .willSetStateTo("HeatStackSuccess"));

        mockUpdateRequestDb(wireMockServer, "12345");

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", null, null, "b4ea86b4-253f-11e7-93ae-92361f002671", map, Boolean.FALSE, Boolean.TRUE,
                Boolean.FALSE, msoRequest, new Holder<>());
        assertNotNull(map);
    }



    @Test
    public void createVnfTest_HeatStatusCreated() throws Exception {
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockUpdateRequestDb(wireMockServer, "12345");

        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", null, null, "b4ea86b4-253f-11e7-93ae-92361f002671", map, Boolean.TRUE, Boolean.TRUE,
                Boolean.FALSE, msoRequest, new Holder<>());
        assertNotNull(map);
    }


    @Test
    public void createVnfTest_NestedHeatStatusNotFound() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_ExceptionInGettingNestedHeat() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_NestedBaseHeatStatus_NotFound() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_ExceptionInGettingBaseNestedHeat() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer
                .stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse()
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId"))
                .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_ExceptionInCreateStack() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer
                .stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse()
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse()
                .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        VfModule vfModule = new VfModule();
        vfModule.setIsBase(false);

        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateBody("");
        heatTemplate.setTimeoutMinutes(200);
        vfModule.setModuleHeatTemplate(heatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setEnvironment("ist");
        vfModuleCustomization.setHeatEnvironment(heatEnvironment);

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "9b339a61-69ca-465f-86b8-1c72c582b8e8", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_ModelCustUuidIsNull() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer
                .stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse()
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse()
                .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        VfModule vfModule = new VfModule();
        vfModule.setIsBase(false);

        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateBody("");
        heatTemplate.setTimeoutMinutes(200);
        vfModule.setModuleHeatTemplate(heatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setEnvironment("ist");
        vfModuleCustomization.setHeatEnvironment(heatEnvironment);

        VnfResource vnfResource = new VnfResource();
        vnfResource.setAicVersionMin("1");
        vnfResource.setAicVersionMin("3");


        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "XVFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", null, map, Boolean.FALSE, Boolean.TRUE,
                Boolean.FALSE, msoRequest, new Holder<>());
    }

    @Test
    public void createVnfTest_HeatEnvironment_ContainsParameters() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_404(wireMockServer);
        wireMockServer
                .stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse()
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse()
                .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.createVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "",
                "VFMOD", "volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>());
    }

    private MsoRequest getMsoRequest() {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");
        return msoRequest;
    }

    @Test
    public void updateVnfTest_CloudSiteIdNotFound() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();

        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12",
                "VFMOD", "volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
                "88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_HeatStackNotFound() throws Exception {
        expectedException.expect(VnfNotFound.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_ExceptionInGettingNestedHeatStack() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_NestedHeatStackNotFound() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_ExceptionInGettingNestedBaseHeatStack() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/volumeGroupHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/baseVfHeatStackId",
                HttpStatus.SC_INTERNAL_SERVER_ERROR, "OpenstackResponse_Stack_Created_VfModule.json");
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_NestedBaseHeatStackNotFound() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/volumeGroupHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/baseVfHeatStackId", HttpStatus.SC_NOT_FOUND,
                "OpenstackResponse_Stack_Created_VfModule.json");
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_MissingParams() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/volumeGroupHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/baseVfHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest_UpdateStackException() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenStackGetStackVfModule_200(wireMockServer);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/volumeGroupHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/baseVfHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");

        VfModuleCustomization vfModuleCustomization = getVfModuleCustomization();
        vfModuleCustomization.getVfModule().getModuleHeatTemplate().setParameters(new HashSet<>());
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId", "88a6ca3ee0394ade9403f075db23167e",
                map, msoRequest, new Holder<Map<String, String>>(), new Holder<VnfRollback>());
    }

    @Test
    public void updateVnfTest() throws Exception {
        MsoRequest msoRequest = getMsoRequest();
        mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/" + vnfName, HttpStatus.SC_OK,
                "OpenstackResponse_Stack_UpdateComplete.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/volumeGroupHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/baseVfHeatStackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_Created_VfModule.json");
        mockOpenStackPutStack(wireMockServer, "null/stackId", HttpStatus.SC_OK);
        mockOpenstackGetWithResponse(wireMockServer, "/mockPublicUrl/stacks/null/stackId", HttpStatus.SC_OK,
                "OpenstackResponse_Stack_UpdateComplete.json");

        VfModuleCustomization vfModuleCustomization = getVfModuleCustomization();
        vfModuleCustomization.getVfModule().getModuleHeatTemplate().setParameters(new HashSet<>());
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        instance.updateVfModule("mtn13", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
                "b4ea86b4-253f-11e7-93ae-92361f002671", map, msoRequest, new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
        assertNotNull(msoRequest);
    }

    @Test
    @Ignore
    public void deleteVnfTest() throws MsoException {

        Map<String, Object> outputs = new HashMap<>();
        outputs.put("Key1", "value1");
        when(heatUtils.queryStackForOutputs("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12"))
                .thenReturn(outputs);

        MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
        MsoRequest msoRequest = getMsoRequest();
        try {
            instance.deleteVfModule("mdt1", "CloudOwner", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", null,
                    msoRequest, new Holder<Map<String, String>>());
        } catch (Exception e) {

        }
        assertNotNull(outputs);
    }

    private VfModuleCustomization getVfModuleCustomization() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        VfModule vfModule = new VfModule();
        vfModule.setIsBase(false);

        HeatTemplate heatTemplate = new HeatTemplate();
        heatTemplate.setTemplateBody("");
        heatTemplate.setTimeoutMinutes(200);
        HeatTemplateParam heatTemplateParam = new HeatTemplateParam();
        heatTemplateParam.setParamAlias("ParamAlias");
        heatTemplateParam.setRequired(true);
        heatTemplateParam.setParamName("test");
        Set set = new HashSet();
        set.add(heatTemplateParam);
        heatTemplate.setParameters(set);
        vfModule.setModuleHeatTemplate(heatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        HeatEnvironment heatEnvironment = new HeatEnvironment();
        heatEnvironment.setEnvironment("parameters:ist");
        vfModuleCustomization.setHeatEnvironment(heatEnvironment);
        return vfModuleCustomization;
    }

    public static void mockUpdateRequestDb(WireMockServer wireMockServer, String requestId) throws IOException {
        wireMockServer.stubFor(patch(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
    }


}
