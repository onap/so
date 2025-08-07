/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.openstack.beans;

import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.entity.MsoRequest;

public class NetworkRollbackTest {
    @Mock
    MsoRequest ms = new MsoRequest();

    @InjectMocks
    NetworkRollback nr = new NetworkRollback();

    @Test
    public void test() {
        List<Integer> vlans = new ArrayList();
        vlans.add(1);
        vlans.add(2);
        nr.setCloudId("cloudId");
        nr.setModelCustomizationUuid("modelCustomizationUuid");
        nr.setNetworkId("networkId");
        nr.setNetworkName("networkName");
        nr.setNetworkStackId("networkStackId");
        nr.setNetworkType("networkType");;
        nr.setNeutronNetworkId("neutronNetworkId");
        nr.setPhysicalNetwork("physicalNetwork");
        nr.setTenantId("tenantId");
        nr.setNetworkCreated(false);
        nr.setVlans(vlans);
        nr.setMsoRequest(ms);
        assert (nr.getCloudId().equals("cloudId"));
        assert (nr.getModelCustomizationUuid().equals("modelCustomizationUuid"));
        assert (nr.getNetworkId().equals("networkId"));
        assert (nr.getNetworkName().equals("networkName"));
        assert (nr.getNetworkStackId().equals("networkStackId"));
        assert (nr.getNeutronNetworkId().equals("neutronNetworkId"));
        assert (nr.getPhysicalNetwork().equals("physicalNetwork"));
        assert (nr.getNetworkType().equals("networkType"));
        assert (nr.getTenantId().equals("tenantId"));
        assert (nr.getMsoRequest().equals(ms));
        assertFalse(nr.getNetworkCreated());
        assert (nr.getVlans().equals(vlans));
        assert (nr.toString() != null);
    }
}
