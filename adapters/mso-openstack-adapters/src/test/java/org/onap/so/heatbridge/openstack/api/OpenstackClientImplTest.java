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

package org.onap.so.heatbridge.openstack.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.openstack.utils.CinderClientImpl;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.onap.so.openstack.utils.NeutronClientException;
import org.onap.so.openstack.utils.NeutronClientImpl;
import org.onap.so.openstack.utils.NovaClientImpl;
import org.springframework.test.util.ReflectionTestUtils;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;

@RunWith(MockitoJUnitRunner.class)
public class OpenstackClientImplTest {

    private static final String SITE = "RegionOne";
    private static final String TENANT = "tenant";

    @Mock
    private NovaClientImpl novaClient;
    @Mock
    private NeutronClientImpl neutronClient;
    @Mock
    private CinderClientImpl cinderClient;
    @Mock
    private MsoHeatUtils heatUtils;

    private OpenstackClientImpl osClient;

    @Before
    public void setUp() {
        osClient = new OpenstackClientImpl(novaClient, neutronClient, cinderClient, heatUtils, SITE, TENANT);
    }

    @Test
    public void getImageById_returnsNullOn404() throws Exception {
        when(novaClient.queryImageById(SITE, TENANT, "gone"))
                .thenThrow(new OpenStackResponseException("Not Found", 404));
        assertNull(osClient.getImageById("gone"));
    }

    @Test(expected = OpenStackResponseException.class)
    public void getImageById_rethrowsOtherErrors() throws Exception {
        when(novaClient.queryImageById(SITE, TENANT, "img")).thenThrow(new OpenStackResponseException("Boom", 500));
        osClient.getImageById("img");
    }

    @Test
    public void getFlavorById_returnsNullOn404() throws Exception {
        when(novaClient.queryFlavorById(SITE, TENANT, "gone"))
                .thenThrow(new OpenStackResponseException("Not Found", 404));
        assertNull(osClient.getFlavorById("gone"));
    }

    @Test(expected = OpenStackResponseException.class)
    public void getServerById_doesNotSwallow404() throws Exception {
        when(novaClient.queryServerById(SITE, TENANT, "gone"))
                .thenThrow(new OpenStackResponseException("Not Found", 404));
        osClient.getServerById("gone");
    }

    @Test(expected = OpenstackClientException.class)
    public void checkedSdkExceptionsBecomeUnchecked() throws Exception {
        when(neutronClient.queryNetworkById(SITE, TENANT, "n1")).thenThrow(new NeutronClientException("x", null));
        osClient.getNetworkById("n1");
    }

    @Test
    public void getStackBasedResources_unwrapsAndDropsNulls() throws Exception {
        Resource resource = new Resource();
        Resources resources = new Resources();
        ReflectionTestUtils.setField(resources, "list", Arrays.asList(resource, null));
        when(heatUtils.queryStackResources(SITE, TENANT, "stack/id", 2)).thenReturn(resources);

        List<Resource> result = osClient.getStackBasedResources("stack/id", 2);

        assertEquals(1, result.size());
    }
}
