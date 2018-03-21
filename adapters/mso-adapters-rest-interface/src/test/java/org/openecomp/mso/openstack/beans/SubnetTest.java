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

import java.util.Arrays;
import java.util.Collections;

public class SubnetTest {

    Subnet subnet = new Subnet();

    @Test
    public void getSubnetName() throws Exception {
        subnet.getSubnetName();
    }

    @Test
    public void setSubnetName() throws Exception {
        subnet.setSubnetName("test");
    }

    @Test
    public void getAllocationPools() throws Exception {
        subnet.getAllocationPools();
    }

    @Test
    public void getCidr() throws Exception {
        subnet.getCidr();
    }

    @Test
    public void getDnsNameServers() throws Exception {
        subnet.getDnsNameServers();
    }

    @Test
    public void getEnableDHCP() throws Exception {
        subnet.getEnableDHCP();
    }

    @Test
    public void getAddrFromStart() throws Exception {
        subnet.getAddrFromStart();
    }

    @Test
    public void setAddrFromStart() throws Exception {
        subnet.setAddrFromStart(true);
    }

    @Test
    public void getGatewayIp() throws Exception {
        subnet.getGatewayIp();
    }

    @Test
    public void getHostRoutes() throws Exception {
        subnet.getHostRoutes();
    }

    @Test
    public void getNeutronId() throws Exception {
        subnet.getNeutronId();
    }

    @Test
    public void getIpVersion() throws Exception {
        subnet.getIpVersion();
    }

    @Test
    public void getSubnetId() throws Exception {
        subnet.getSubnetId();
    }

    @Test
    public void setAllocationPools() throws Exception {
        subnet.setAllocationPools(Arrays.asList());
    }

    @Test
    public void setCidr() throws Exception {
        subnet.setCidr("255.255.255.0");
    }

    @Test
    public void setDnsNameServers() throws Exception {
        subnet.setDnsNameServers(Arrays.asList());
    }

    @Test
    public void setEnableDHCP() throws Exception {
        subnet.setEnableDHCP(true);
    }

    @Test
    public void setGatewayIp() throws Exception {
        subnet.setGatewayIp("192.168.0.1");
    }

    @Test
    public void setHostRoutes() throws Exception {
        subnet.setHostRoutes(Collections.emptyList());
    }

    @Test
    public void setNeutronId() throws Exception {
        subnet.setNeutronId("test");
    }

    @Test
    public void setIpVersion() throws Exception {
        subnet.setIpVersion("ipv4");
    }

    @Test
    public void setSubnetId() throws Exception {
        subnet.setSubnetId("1.0.0.0");
    }

}