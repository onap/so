/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.network.beans;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ContrailSubnet {

    @JsonProperty("network_ipam_refs_data_ipam_subnets_subnet")
    private ContrailSubnetIp subnet = new ContrailSubnetIp();

    @JsonProperty("network_ipam_refs_data_ipam_subnets_default_gateway")
    private String defaultGateway;

    @JsonProperty("network_ipam_refs_data_ipam_subnets_subnet_name")
    private String subnetName;

    @JsonProperty("network_ipam_refs_data_ipam_subnets_enable_dhcp")
    private Boolean enableDhcp;

    @JsonProperty("network_ipam_refs_data_ipam_subnets_addr_from_start")
    private Boolean addrFromStart = true;
    /**
     * future - leave this commented private String subnet_uuid; private String dns_server_address; private List<String>
     * dns_nameservers; private String dhcp_option_list;
     **/

    @JsonProperty("network_ipam_refs_data_ipam_subnets_allocation_pools")
    private List<ContrailSubnetPool> allocationPools = new ArrayList<>();

    @JsonProperty("network_ipam_refs_data_ipam_subnets_host_routes")
    private ContrailSubnetHostRoutes hostRoutes = new ContrailSubnetHostRoutes();

    public ContrailSubnet() {
        super();
    }

    public String getDefaultGateway() {
        return defaultGateway;
    }

    public void setDefaultGateway(String defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    public ContrailSubnetIp getSubnet() {
        return subnet;
    }

    public void setSubnet(ContrailSubnetIp subnet) {
        this.subnet = subnet;
    }

    public Boolean isEnableDhcp() {
        return enableDhcp;
    }

    public void setEnableDhcp(Boolean enableDhcp) {
        this.enableDhcp = enableDhcp;
    }

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public List<ContrailSubnetPool> getAllocationPools() {
        return allocationPools;
    }

    public void setAllocationPools(List<ContrailSubnetPool> allocationPools) {
        this.allocationPools = allocationPools;
    }

    public Boolean isAddrFromStart() {
        return addrFromStart;
    }

    public void setAddrFromStart(Boolean addrFromStart) {
        this.addrFromStart = addrFromStart;
    }

    public ContrailSubnetHostRoutes getHostRoutes() {
        return hostRoutes;
    }

    public void setHostRoutes(ContrailSubnetHostRoutes hostRoutes) {
        this.hostRoutes = hostRoutes;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (ContrailSubnetPool pool : allocationPools) {
            buf.append(pool.toString());
        }
        return "ContrailSubnet [subnet=" + subnet.toString() + " default_gateway=" + defaultGateway + " enable_dhcp="
                + enableDhcp + " addr_from_start=" + addrFromStart + " subnet_name=" + subnetName + " allocation_pools="
                + buf + " ]";
    }

}
