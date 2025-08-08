/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.StubOpenStack;
import org.onap.so.adapters.vdu.CloudInfo;
import org.onap.so.adapters.vdu.PluginAction;
import org.onap.so.adapters.vdu.VduArtifact;
import org.onap.so.adapters.vdu.VduArtifact.ArtifactType;
import org.onap.so.adapters.vdu.VduInstance;
import org.onap.so.adapters.vdu.VduModelInfo;
import org.onap.so.adapters.vdu.VduStateType;
import org.onap.so.adapters.vdu.VduStatus;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoAdapterException;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.exceptions.MsoOpenstackException;
import org.springframework.beans.factory.annotation.Autowired;
import com.woorea.openstack.heat.Heat;

public class MsoHeatUtilsITTest extends BaseTest {

    @Autowired
    private MsoHeatUtils heatUtils;

    @Test
    public void instantiateVduTest() throws MsoException, IOException {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("name/da886914-efb2-4917-b335-c8381528d90b");
        expected.setVduInstanceName("name");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.INSTANTIATED);
        status.setLastAction((new PluginAction("create", "complete", null)));
        expected.setStatus(status);

        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("MTN13");
        cloudInfo.setTenantId("tenantId");
        VduModelInfo vduModel = new VduModelInfo();
        vduModel.setModelCustomizationUUID("blueprintId");
        vduModel.setTimeoutMinutes(1);
        VduArtifact artifact = new VduArtifact();
        artifact.setName("name");
        artifact.setType(ArtifactType.MAIN_TEMPLATE);
        byte[] content = new byte[1];
        artifact.setContent(content);
        List<VduArtifact> artifacts = new ArrayList<>();
        artifacts.add(artifact);
        vduModel.setArtifacts(artifacts);
        Map<String, byte[]> blueprintFiles = new HashMap<>();
        blueprintFiles.put(artifact.getName(), artifact.getContent());
        String instanceName = "stackname";
        Map<String, Object> inputs = new HashMap<>();
        boolean rollbackOnFailure = true;

        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        StubOpenStack.mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack_Created.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/stackname/stackId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("OpenstackResponse_StackId.json").withStatus(HttpStatus.SC_OK)));

        VduInstance actual = heatUtils.instantiateVdu(cloudInfo, instanceName, inputs, vduModel, rollbackOnFailure);

        assertThat(actual, sameBeanAs(expected));
    }


    @Test
    public void queryVduTest() throws Exception {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("name/da886914-efb2-4917-b335-c8381528d90b");
        expected.setVduInstanceName("name");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.INSTANTIATED);
        status.setLastAction((new PluginAction("create", "complete", null)));
        expected.setStatus(status);

        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("mtn13");
        cloudInfo.setTenantId("tenantId");
        String instanceId = "instanceId";

        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        StubOpenStack.mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack_Created.json");

        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/instanceId"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("OpenstackResponse_StackId.json").withStatus(HttpStatus.SC_OK)));

        VduInstance actual = heatUtils.queryVdu(cloudInfo, instanceId);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void deleteVduTest() throws Exception {
        VduInstance expected = new VduInstance();
        expected.setVduInstanceId("name/stackId");
        expected.setVduInstanceName("instanceId");
        VduStatus status = new VduStatus();
        status.setState(VduStateType.DELETED);
        expected.setStatus(status);

        CloudInfo cloudInfo = new CloudInfo();
        cloudInfo.setCloudSiteId("mtn13");
        cloudInfo.setTenantId("tenantId");
        String instanceId = "instanceId";

        int timeoutInMinutes = 1;

        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/instanceId"))
                .willReturn(aResponse().withBodyFile("OpenstackResponse_StackId.json").withStatus(HttpStatus.SC_OK)));
        StubOpenStack.mockOpenStackDelete(wireMockServer, "name/da886914-efb2-4917-b335-c8381528d90b");
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/name/da886914-efb2-4917-b335-c8381528d90b"))
                .willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_DeleteComplete.json")
                        .withStatus(HttpStatus.SC_OK)));

        VduInstance actual = heatUtils.deleteVdu(cloudInfo, instanceId, timeoutInMinutes);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public final void copyBaseOutputsToInputsTest() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("str1", "str");
        Map<String, Object> otherStackOutputs = new HashMap<>();
        otherStackOutputs.put("str", "str");
        List<String> paramNames = new ArrayList<>();
        Map<String, String> aliases = new HashMap<>();
        aliases.put("str", "str");
        heatUtils.copyBaseOutputsToInputs(inputs, otherStackOutputs, null, aliases);
        Assert.assertEquals("str", otherStackOutputs.get("str"));
    }

    @Test
    public final void getHeatClientSuccessTest() throws MsoException, IOException {
        CloudSite cloudSite = getCloudSite(getCloudIdentity());
        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        Heat heatClient = heatUtils.getHeatClient("MTN13", "TEST-tenant");
        assertNotNull(heatClient);
    }

    @Test(expected = MsoOpenstackException.class)
    public final void getHeatClientOpenStackResponseException404Test() throws MsoException {
        CloudSite cloudSite = getCloudSite(getCloudIdentity());
        // mo mocks setup will cause 404 response from wiremock
        heatUtils.getHeatClient("MTN13", "TEST-tenant");
    }

    @Test(expected = MsoAdapterException.class)
    public final void getHeatClientOpenStackResponseException401Test() throws MsoException, IOException {
        CloudSite cloudSite = getCloudSite(getCloudIdentity());
        StubOpenStack.mockOpenStackResponseUnauthorized(wireMockServer, wireMockPort);
        heatUtils.getHeatClient("MTN13", "TEST-tenant");
    }

    @Test(expected = MsoOpenstackException.class)
    public final void getHeatClientOpenStackConnectExceptionTest() throws MsoException {
        CloudIdentity identity = getCloudIdentity();
        identity.setIdentityUrl("http://unreachable");
        CloudSite cloudSite = getCloudSite(identity);
        // mo mocks setup will cause 404 response from wiremock
        heatUtils.getHeatClient("MTN13", "TEST-tenant");
    }

    @Test
    public final void createStackSuccessTest() throws MsoException, IOException {
        CloudSite cloudSite = getCloudSite(getCloudIdentity());
        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
        StubOpenStack.mockOpenStackPostStack_200(wireMockServer, "OpenstackResponse_Stack_Created.json");
        StubOpenStack.mockOpenStackGet(wireMockServer, "TEST-stack/stackId");
        StackInfo stackInfo = heatUtils.createStack(cloudSite.getId(), "CloudOwner", "tenantId", "TEST-stack", null,
                "TEST-heat", new HashMap<>(), false, 1, "TEST-env", new HashMap<>(), new HashMap<>(), false, false);
        assertNotNull(stackInfo);
    }



}
