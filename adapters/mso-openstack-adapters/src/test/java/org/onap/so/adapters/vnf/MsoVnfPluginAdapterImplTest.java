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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.VnfRollback;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;

public class MsoVnfPluginAdapterImplTest extends BaseRestTestUtils {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    MsoVnfPluginAdapterImpl msoVnfPluginAdapter;

    String vnfName = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId";

    @Test
    public void createVfModule_ModelCustUuidIsNull() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", null, map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void createVfModule_ModelCustUuidIsNotFound() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void createVfModule_VduException() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "9b339a61-69ca-465f-86b8-1c72c582b8e8", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void createVfModule_INSTANTIATED() throws Exception {
        mockOpenStackResponseAccess(wireMockPort);
        mockOpenStackGetStackVfModule_200();

        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("MTN13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                null, "baseVfHeatStackId", "9b339a61-69ca-465f-86b8-1c72c582b8e8", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void createVfModule_queryVduNotFoundWithVolumeGroupId() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockPort);
        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                "volumeGroupHeatStackId|1", "baseVfHeatStackId", "9b339a61-69ca-465f-86b8-1c72c582b8e8", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void createVfModule_CreateVduException() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockPort);
        mockOpenStackGetStackVfModule_404();
        stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
                        .withStatus(HttpStatus.SC_OK)));
        MsoRequest msoRequest = getMsoRequest();
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        msoVnfPluginAdapter.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
                "volumeGroupHeatStackId", "baseVfHeatStackId", "9b339a61-69ca-465f-86b8-1c72c582b8e8", map,
                Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
                new Holder<VnfRollback>());
    }

    @Test
    public void deleteVfModule_QueryVduException() throws Exception {
        expectedException.expect(VnfException.class);
        MsoRequest msoRequest = getMsoRequest();
        msoVnfPluginAdapter.deleteVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<Map<String, String>>());
    }

    @Test
    public void deleteVfModule_DeleteVduException() throws Exception {
        expectedException.expect(VnfException.class);
        mockOpenStackResponseAccess(wireMockPort);
        mockOpenStackGetStackVfModule_200();
        stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vSAMP12"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
                        .withStatus(HttpStatus.SC_OK)));
        stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        MsoRequest msoRequest = getMsoRequest();
        msoVnfPluginAdapter.deleteVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<Map<String, String>>());
    }

    private MsoRequest getMsoRequest() {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");
        return msoRequest;
    }

}
