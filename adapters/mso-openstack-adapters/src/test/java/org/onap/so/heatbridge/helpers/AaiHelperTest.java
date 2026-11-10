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

package org.onap.so.heatbridge.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.L3Network;
import org.onap.so.spring.SpringContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Subnet;

public class AaiHelperTest {

    private AaiHelper aaiHelper;

    @Before
    public void setUp() {
        // new AaiHelper() builds an AAIResourcesClient, whose RestProperties provider reads SpringContextHelper's
        // static context in its constructor and unboxes getProperty(key, type, default).
        Environment env = mock(Environment.class);
        when(env.getProperty(anyString(), any(Class.class), any())).thenAnswer(invocation -> invocation.getArgument(2));
        ApplicationContext springContext = mock(ApplicationContext.class);
        when(springContext.getEnvironment()).thenReturn(env);
        ReflectionTestUtils.setField(SpringContextHelper.class, "context", springContext);
        aaiHelper = new AaiHelper();
    }

    @Test
    public void buildFlavor_emptySwapBecomesZero() {
        Flavor flavor = new Flavor();
        flavor.setId("f1");
        flavor.setSwap("");
        flavor.setLinks(new ArrayList<>());
        assertEquals("0", aaiHelper.buildFlavor(flavor).getFlavorSwap());
    }

    @Test
    public void buildFlavor_keepsNonEmptySwap() {
        Flavor flavor = new Flavor();
        flavor.setId("f1");
        flavor.setSwap("512");
        flavor.setLinks(new ArrayList<>());
        assertEquals("512", aaiHelper.buildFlavor(flavor).getFlavorSwap());
    }

    @Test
    public void buildSubnet_ipv6() {
        Subnet subnet = new Subnet();
        subnet.setId("s1");
        subnet.setIpversion(Subnet.IpVersion.IPV6);
        assertEquals("V6", aaiHelper.buildSubnet(subnet).getIpVersion());
    }

    @Test
    public void buildSubnet_ipv4() {
        Subnet subnet = new Subnet();
        subnet.setId("s1");
        subnet.setIpversion(Subnet.IpVersion.IPV4);
        assertEquals("V4", aaiHelper.buildSubnet(subnet).getIpVersion());
    }

    @Test
    public void buildNetwork_nullFlagsAreFalse() {
        Network network = new Network();
        network.setId("n1");
        L3Network l3Network = aaiHelper.buildNetwork(network);
        assertFalse(l3Network.isIsSharedNetwork());
        assertFalse(l3Network.isIsExternalNetwork());
        assertEquals("false", l3Network.getOperationalStatus());
    }
}
