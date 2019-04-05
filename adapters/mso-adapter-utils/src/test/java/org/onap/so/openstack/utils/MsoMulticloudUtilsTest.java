/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Samsung Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.openstack.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.BaseTest;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.VduException;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;

public class MsoMulticloudUtilsTest extends BaseTest {

    @Autowired
    private MsoMulticloudUtils multicloudUtils;

    @InjectMocks
    private MsoMulticloudUtils multicloudUtilsMock;

    @Mock
    private CloudConfig cloudConfigMock;

    private static final String CREATE_STACK_RESPONSE = "{\"template_type\": \"TEST-template\", \"workload_id\": "
        + "\"TEST-workload\", \"template_response\": {\"stack\": {\"id\": \"TEST-stack\", \"links\": []}}}";
    private static final String UPDATE_STACK_RESPONSE = "{\"template_type\": \"TEST-template\", \"workload_id\": "
            + "\"TEST-workload\"}";
    private static final String GET_CREATE_STACK_RESPONSE = "{\"template_type\": \"TEST-template\", \"workload_id\": "
            + "\"TEST-workload\", \"workload_status\": \"CREATE_COMPLETE\"}";
    private static final String GET_UPDATE_STACK_RESPONSE = "{\"template_type\": \"TEST-template\", \"workload_id\": "
            + "\"TEST-workload\", \"workload_status\": \"UPDATE_COMPLETE\"}";

    private static final String MULTICLOUD_CREATE_PATH = "/api/multicloud/v1/CloudOwner/MTN14/infra_workload";
    private static final String MULTICLOUD_UPDATE_PATH = "/api/multicloud/v1/CloudOwner/MTN14/infra_workload/TEST-workload";
    private static final String MULTICLOUD_GET_PATH = "/api/multicloud/v1/CloudOwner/MTN14/infra_workload/TEST-workload";

    @Test
    public void createStackSuccess() throws MsoException, IOException {
        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH)).inScenario("CREATE")
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(CREATE_STACK_RESPONSE)
                .withStatus(HttpStatus.SC_CREATED))
            .willSetStateTo("CREATING"));
        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH))
                .inScenario("CREATE").whenScenarioStateIs("CREATING")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody(GET_CREATE_STACK_RESPONSE)
                    .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_UPDATE_PATH)).inScenario("CREATE")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody(UPDATE_STACK_RESPONSE)
                    .withStatus(HttpStatus.SC_ACCEPTED))
                .willSetStateTo("UPDATING"));
        wireMockServer.stubFor(get(urlPathEqualTo(MULTICLOUD_GET_PATH))
                .inScenario("CREATE").whenScenarioStateIs("UPDATING")
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody(GET_UPDATE_STACK_RESPONSE)
                    .withStatus(HttpStatus.SC_OK)));
        StackInfo result = multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
            "TEST-heat", new HashMap<>(), true, 200, "TEST-env",
            new HashMap<>(), new HashMap<>(), false);
        wireMockServer.resetScenarios();
        assertNotNull(result);
        assertEquals("TEST-stack", result.getName());
    }

    @Test
    public void deleteStack() throws MsoException {
        StackInfo result = multicloudUtils.deleteStack("MTN13", "CloudOwner", "TEST-tenant", "instanceId");
        assertNotNull(result);
        assertTrue(HeatStatus.NOTFOUND == result.getStatus());
    }

    @Test
    public void queryStack() throws MsoException {
        StackInfo result = multicloudUtils.queryStack("MTN13", "CloudOwner", "TEST-tenant", "instanceId");
        assertTrue(HeatStatus.NOTFOUND == result.getStatus());
    }

    @Test(expected = VduException.class)
    public void updateVdu() throws MsoException {
        multicloudUtils.updateVdu(new CloudInfo(), "instanceId", new HashMap<>(), new  VduModelInfo(),
            false);
    }

    @Test
    public void deleteVdu() throws VduException {
        CloudInfo cloudInfo = new CloudInfo("cloudSiteId", "cloudOwner", "tenantId", "tenantName");
        VduInstance vduInstance = multicloudUtils.deleteVdu(cloudInfo, "instanceId", 3);
        assertNotNull(vduInstance);
        assertTrue(VduStateType.DELETED == vduInstance.getStatus().getState());
    }

    @Ignore @Test
    public void createStackMulticloudClientIsNull() {
        try {
            multicloudUtilsMock.cloudConfig = cloudConfigMock;
            CloudSite cloudSite = new CloudSite();
            cloudSite.setIdentityService(new CloudIdentity());
            when(cloudConfigMock.getCloudSite("MTN13")).
                thenReturn(Optional.of(cloudSite));
            multicloudUtilsMock.createStack("MNT14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
                "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
                new HashMap<>(), new HashMap<>(), false);
        } catch (MsoException e) {
            assertEquals("0 : Multicloud client could not be initialized", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackBadRequest() {
        try {
            wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.SC_BAD_REQUEST)));
            multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
                "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
                new HashMap<>(), new HashMap<>(), false);
        } catch (MsoException e) {
            assertEquals("0 : Bad Request", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackEmptyResponseEntity() throws MsoException {
        wireMockServer.stubFor(post(urlPathEqualTo(MULTICLOUD_CREATE_PATH))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withStatus(HttpStatus.SC_CREATED)));
        StackInfo result = multicloudUtils.createStack("MTN14", "CloudOwner", "TEST-tenant", "TEST-stack", new VduModelInfo(),
            "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
            new HashMap<>(), new HashMap<>(), false);
        assertNotNull(result);
        assertEquals("TEST-stack/", result.getName());
    }
}
