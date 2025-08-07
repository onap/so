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
package org.onap.so.bpmn.core.domain;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HomingSolutionTest {

    private HomingSolution homingsolution = new HomingSolution();
    InventoryType inventory = InventoryType.cloud;
    VnfResource vnfresource = new VnfResource();
    License license = new License();

    @Test
    public void testHomingSolution() {
        homingsolution.setInventoryType(inventory);
        homingsolution.setRehome(true);
        homingsolution.setServiceInstanceId("serviceInstanceId");
        homingsolution.setCloudOwner("cloudOwner");
        homingsolution.setCloudRegionId("cloudRegionId");
        homingsolution.setAicClli("aicClli");
        homingsolution.setAicVersion("aicVersion");
        homingsolution.setTenant("tenant");
        homingsolution.setVnf(vnfresource);
        homingsolution.setLicense(license);
        assertEquals(homingsolution.getInventoryType(), inventory);
        assertEquals(homingsolution.isRehome(), true);
        assertEquals(homingsolution.getServiceInstanceId(), "serviceInstanceId");
        assertEquals(homingsolution.getCloudOwner(), "cloudOwner");
        assertEquals(homingsolution.getCloudRegionId(), "cloudRegionId");
        assertEquals(homingsolution.getAicClli(), "aicClli");
        assertEquals(homingsolution.getAicVersion(), "aicVersion");
        assertEquals(homingsolution.getTenant(), "tenant");
        assertEquals(homingsolution.getVnf(), vnfresource);
        assertEquals(homingsolution.getLicense(), license);

    }

}
