/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Samsung Intellectual Property. All rights reserved.
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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.BaseTest;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
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

    @Test
    public void createStackSuccess() throws MsoException, IOException {
        stubFor(post(urlPathEqualTo("/v2.0"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(CREATE_STACK_RESPONSE)
                .withStatus(HttpStatus.SC_CREATED)));
        StackInfo result = multicloudUtils.createStack("MTN13", "CloudOwner", "TEST-tenant", "TEST-stack",
            "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
            new HashMap<>(), new HashMap<>());
        assertNotNull(result);
        assertEquals("TEST-stack", result.getName());
    }

    @Test
    public void createStackMulticloudClientIsNull() {
        try {
            multicloudUtilsMock.cloudConfig = cloudConfigMock;
            CloudSite cloudSite = new CloudSite();
            cloudSite.setIdentityService(new CloudIdentity());
            when(cloudConfigMock.getCloudSite("MTN13")).
                thenReturn(Optional.of(cloudSite));
            multicloudUtilsMock.createStack("MTN13", "CloudOwner", "TEST-tenant", "TEST-stack",
                "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
                new HashMap<>(), new HashMap<>());
        } catch (MsoException e) {
            assertEquals("0 : Multicloud client could not be initialized", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackBadRequest() {
        try {
            stubFor(post(urlPathEqualTo("/v2.0"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withStatus(HttpStatus.SC_BAD_REQUEST)));
            multicloudUtils.createStack("MTN13", "CloudOwner", "TEST-tenant", "TEST-stack",
                "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
                new HashMap<>(), new HashMap<>());
        } catch (MsoException e) {
            assertEquals("0 : Bad Request", e.toString());
            return;
        }
        fail("MsoOpenstackException expected!");
    }

    @Test
    public void createStackEmptyResponseEntity() throws MsoException {
        stubFor(post(urlPathEqualTo("/v2.0"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withStatus(HttpStatus.SC_CREATED)));
        StackInfo result = multicloudUtils.createStack("MTN13", "CloudOwner", "TEST-tenant", "TEST-stack",
            "TEST-heat", new HashMap<>(), false, 200, "TEST-env",
            new HashMap<>(), new HashMap<>());
        assertNotNull(result);
        assertEquals("TEST-stack/", result.getName());
    }
}
