/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.openstack.beans;

import org.junit.Test;
import org.openecomp.mso.entity.MsoRequest;

import java.util.Arrays;

public class NetworkRollbackTest {

    NetworkRollback networkRollback = new NetworkRollback();

    @Test
    public void getNetworkId() throws Exception {
        networkRollback.getNetworkId();
    }

    @Test
    public void setNetworkId() throws Exception {
        networkRollback.setNetworkId("255.255.255.0");
    }

    @Test
    public void getNeutronNetworkId() throws Exception {
        networkRollback.getNeutronNetworkId();
    }

    @Test
    public void setNeutronNetworkId() throws Exception {
        networkRollback.setNeutronNetworkId("192.168.0.0");
    }

    @Test
    public void getNetworkStackId() throws Exception {
        networkRollback.getNetworkStackId();
    }

    @Test
    public void setNetworkStackId() throws Exception {
        networkRollback.setNetworkStackId("id-123");
    }

    @Test
    public void getTenantId() throws Exception {
        networkRollback.getTenantId();
    }

    @Test
    public void setTenantId() throws Exception {
        networkRollback.setTenantId("id-123");
    }

    @Test
    public void getCloudId() throws Exception {
        networkRollback.getCloudId();
    }

    @Test
    public void setCloudId() throws Exception {
        networkRollback.setCloudId("id-123");
    }

    @Test
    public void getNetworkType() throws Exception {
        networkRollback.getNetworkType();
    }

    @Test
    public void setNetworkType() throws Exception {
        networkRollback.setNetworkType("type");
    }

    @Test
    public void getModelCustomizationUuid() throws Exception {
        networkRollback.getModelCustomizationUuid();
    }

    @Test
    public void setModelCustomizationUuid() throws Exception {
        networkRollback.setModelCustomizationUuid("id-123");
    }

    @Test
    public void getNetworkCreated() throws Exception {
        networkRollback.getNetworkCreated();
    }

    @Test
    public void setNetworkCreated() throws Exception {
        networkRollback.setNetworkCreated(true);
    }

    @Test
    public void getNetworkName() throws Exception {
        networkRollback.getNetworkName();
    }

    @Test
    public void setNetworkName() throws Exception {
        networkRollback.setNetworkName("test");
    }

    @Test
    public void getPhysicalNetwork() throws Exception {
        networkRollback.getPhysicalNetwork();
    }

    @Test
    public void setPhysicalNetwork() throws Exception {
        networkRollback.setPhysicalNetwork("test");
    }

    @Test
    public void getVlans() throws Exception {
        networkRollback.getVlans();
    }

    @Test
    public void setVlans() throws Exception {
        networkRollback.setVlans(Arrays.asList(10, 20));
    }

    @Test
    public void getMsoRequest() throws Exception {
        networkRollback.getMsoRequest();
    }

    @Test
    public void setMsoRequest() throws Exception {
        networkRollback.setMsoRequest(new MsoRequest());
    }

}