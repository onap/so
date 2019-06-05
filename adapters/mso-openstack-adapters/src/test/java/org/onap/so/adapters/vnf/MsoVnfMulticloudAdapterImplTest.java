/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.Holder;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;

public class MsoVnfMulticloudAdapterImplTest extends BaseRestTestUtils {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private MsoVnfPluginAdapterImpl instance;

    @Autowired
    private CloudConfig cloudConfig;

    private static final String UPDATE_STACK_RESPONSE =
            "{\"template_type\": \"heat\", \"workload_id\": " + "\"workload-id\"}";
    private static final String GET_CREATE_STACK_RESPONSE = "{\"template_type\": \"heat\", \"workload_id\": "
            + "\"workload-id\", \"workload_status\": \"CREATE_COMPLETE\"}";
    private static final String GET_UPDATE_STACK_RESPONSE = "{\"template_type\": \"heat\", \"workload_id\": "
            + "\"workload-id\", \"workload_status\": \"UPDATE_COMPLETE\"}";

    private static final String MULTICLOUD_CREATE_PATH = "/api/multicloud/v1/CloudOwner/MTN13/infra_workload";
    private static final String MULTICLOUD_UPDATE_PATH =
            "/api/multicloud/v1/CloudOwner/MTN13/infra_workload/workload-id";
    private static final String MULTICLOUD_GET_PATH_BY_NAME =
            "/api/multicloud/v1/CloudOwner/MTN13/infra_workload/vfname";
    private static final String MULTICLOUD_GET_PATH_BY_ID =
            "/api/multicloud/v1/CloudOwner/MTN13/infra_workload/workload-id";

    @Before
    public void before() throws Exception {
        super.orchestrator = "multicloud";
        super.cloudEndpoint = "/api/multicloud/v1/CloudOwner/MTN13/infra_workload";
        super.setUp();
    }

    @Test
    public void createVfModule() throws Exception {

        Map<String, Object> stackInputs = new HashMap<>();
        stackInputs.put("oof_directives", "{}");
        stackInputs.put("sdnc_directives", "{}");
        stackInputs.put("user_directives", "{}");
        stackInputs.put("generic_vnf_id", "genVNFID");
        stackInputs.put("vf_module_id", "vfMODULEID");

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_NAME)).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_ID)).inScenario("CREATE")
                .whenScenarioStateIs("CREATING").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(GET_CREATE_STACK_RESPONSE).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_ID)).inScenario("CREATE")
                .whenScenarioStateIs("UPDATING").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(GET_UPDATE_STACK_RESPONSE).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH)).inScenario("CREATE")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("MulticloudResponse_Stack_Create.json").withStatus(HttpStatus.SC_CREATED))
                .willSetStateTo("CREATING"));

        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_UPDATE_PATH))
                .inScenario("CREATE").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(UPDATE_STACK_RESPONSE).withStatus(HttpStatus.SC_ACCEPTED))
                .willSetStateTo("UPDATING"));

        try {
            instance.createVfModule("MTN13", "CloudOwner", "123", "vf", "v1", "genericVnfId", "vfname", "vfModuleId",
                    "create", null, "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", stackInputs, true, true, true,
                    msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
        } catch (VnfException e) {
            fail("createVfModule success expected, failed with exception: " + e.toString());
        }
        wireMockServer.resetScenarios();
    }

    @Test
    public void createVfModule2() throws Exception {

        Map<String, Object> stackInputs = new HashMap<>();
        stackInputs.put("oof_directives", "{}");
        stackInputs.put("sdnc_directives", "{}");
        stackInputs.put("user_directives", "{}");
        stackInputs.put("generic_vnf_id", "genVNFID");
        stackInputs.put("vf_module_id", "vfMODULEID");

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_NAME)).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_ID)).inScenario("CREATE")
                .whenScenarioStateIs("CREATING").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(GET_CREATE_STACK_RESPONSE).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH_BY_ID)).inScenario("CREATE")
                .whenScenarioStateIs("UPDATING").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(GET_UPDATE_STACK_RESPONSE).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH)).inScenario("CREATE")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("MulticloudResponse_Stack_Create2.json").withStatus(HttpStatus.SC_CREATED))
                .willSetStateTo("CREATING"));

        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_UPDATE_PATH))
                .inScenario("CREATE").willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(UPDATE_STACK_RESPONSE).withStatus(HttpStatus.SC_ACCEPTED))
                .willSetStateTo("UPDATING"));

        try {
            instance.createVfModule("MTN13", "CloudOwner", "123", "vf", "v1", "genericVnfId", "vfname", "vfModuleId",
                    "create", null, "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", stackInputs, true, true, true,
                    msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
        } catch (VnfException e) {
            fail("createVfModule success expected, failed with exception: " + e.toString());
        }
        wireMockServer.resetScenarios();
    }

    @Test
    public void createVfModuleAlreadyExists() throws Exception {

        Map<String, Object> stackInputs = new HashMap<>();
        stackInputs.put("oof_directives", "{}");
        stackInputs.put("sdnc_directives", "{}");
        stackInputs.put("user_directives", "{}");
        stackInputs.put("generic_vnf_id", "genVNFID");
        stackInputs.put("vf_module_id", "vfMODULEID");

        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        wireMockServer.stubFor(
                get(urlEqualTo("/api/multicloud/v1/CloudOwner/MTN13/infra_workload?name=vfname")).willReturn(aResponse()
                        // .withHeader()
                        .withBodyFile("MulticloudGetStackExists.json").withStatus(HttpStatus.SC_OK)));

        try {
            instance.createVfModule("MTN13", "CloudOwner", "123", "vf", "v1", "genericVnfId", "vfname", "vfModuleId",
                    "create", null, "234", "9b339a61-69ca-465f-86b8-1c72c582b8e8", stackInputs, true, true, true,
                    msoRequest, new Holder<>(), new Holder<>(), new Holder<>());
        } catch (VnfException e) {
            assertTrue(e.toString().contains(
                    "Resource vfname already exists in owner/cloud/tenant CloudOwner/MTN13/123 with ID vfname/vfname"));
            return;
        }
        fail("VnfAlreadyExists Exception expected!");
    }

    @Test
    public void deleteVfModule() throws Exception {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        wireMockServer.stubFor(get(urlPathEqualTo("/api/multicloud/v1/CloudOwner/MTN13/infra_workload/workload-id"))
                .willReturn(aResponse().withBodyFile("MulticloudResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(delete(urlPathEqualTo("/api/multicloud/v1/CloudOwner/MTN13/infra_workload/workload-id"))
                .willReturn(aResponse().withStatus(HttpStatus.SC_NO_CONTENT)));

        instance.deleteVfModule("MTN13", "CloudOwner", "123", "workload-id", msoRequest, new Holder<>());
    }

    @Test
    public void queryVfModule() throws Exception {
        MsoRequest msoRequest = new MsoRequest();
        msoRequest.setRequestId("12345");
        msoRequest.setServiceInstanceId("12345");

        wireMockServer.stubFor(get(urlPathEqualTo("/api/multicloud/v1/CloudOwner/MTN13/infra_workload/workload-id"))
                .willReturn(aResponse().withBodyFile("MulticloudResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

        instance.queryVnf("MTN13", "CloudOwner", "123", "workload-id", msoRequest, new Holder<>(), new Holder<>(),
                new Holder<>(), new Holder<>());
    }

    // TODO Error Tests
}
