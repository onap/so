/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.StubOpenStack;
import org.springframework.beans.factory.annotation.Autowired;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Subnet;

public class NeutronClientImplTest extends BaseTest {

    @Autowired
    private NeutronClientImpl neutronClient;

    @Before
    public void before() throws IOException {
        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
    }

    @Test
    public void queryNetworkById() throws Exception {
        StubOpenStack.mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json",
                "43173f6a-d699-414b-888f-ab243dda6dfe");

        Network network = neutronClient.queryNetworkById("MTN13", "tenantId", "43173f6a-d699-414b-888f-ab243dda6dfe");

        assertEquals("net1", network.getName());
    }

    @Test
    public void querySubnetById() throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/subnets/sub-1"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("{\"subnet\":{\"id\":\"sub-1\",\"name\":\"s1\",\"cidr\":\"10.0.0.0/24\","
                                + "\"ip_version\":6,\"gateway_ip\":\"10.0.0.1\",\"enable_dhcp\":true}}")));

        Subnet subnet = neutronClient.querySubnetById("MTN13", "tenantId", "sub-1");

        assertEquals("10.0.0.0/24", subnet.getCidr());
        assertEquals(Subnet.IpVersion.IPV6, subnet.getIpversion());
        assertEquals("10.0.0.1", subnet.getGw());
    }

    @Test(expected = OpenStackResponseException.class)
    public void queryNetworkById_404() throws Exception {
        neutronClient.queryNetworkById("MTN13", "tenantId", "does-not-exist");
    }
}
