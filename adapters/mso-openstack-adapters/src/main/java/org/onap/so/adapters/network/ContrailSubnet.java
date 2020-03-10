/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.network;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.openstack.beans.HostRoute;
import org.onap.so.openstack.beans.Pool;
import org.onap.so.openstack.beans.Subnet;
import org.onap.so.openstack.utils.MsoCommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ContrailSubnet {

    private static final Logger logger = LoggerFactory.getLogger(ContrailSubnet.class);
    @Autowired
    private MsoCommonUtils msoCommonUtils;

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
    private final ContrailSubnetHostRoutes hostRoutes = new ContrailSubnetHostRoutes();

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

    public void setPools(List<ContrailSubnetPool> allocationPools) {
        this.allocationPools = allocationPools;
    }

    public Boolean isAddrFromStart() {
        return addrFromStart;
    }

    public void setAddrFromStart(Boolean addrFromStart) {
        this.addrFromStart = addrFromStart;
    }

    public JsonNode toJsonNode() {
        JsonNode node = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            node = mapper.convertValue(this, JsonNode.class);
        } catch (Exception e) {
            logger.error("{} {} Error creating JsonNode for Contrail Subnet: {} ", MessageEnum.RA_MARSHING_ERROR,
                    ErrorCode.SchemaError.getValue(), subnetName, e);
        }

        return node;
    }

    public String toJsonString() {
        String jsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonString = mapper.writeValueAsString(this);
        } catch (Exception e) {
            logger.error("{} {} Error creating JsonString for Contrail Subnet: {} ", MessageEnum.RA_MARSHING_ERROR,
                    ErrorCode.SchemaError.getValue(), subnetName, e);
        }

        return jsonString;
    }

    // poulate contrail subnet with input(from bopel) subnet
    public void populateWith(Subnet inputSubnet) {
        if (inputSubnet != null) {
            if (!msoCommonUtils.isNullOrEmpty(inputSubnet.getSubnetName()))
                subnetName = inputSubnet.getSubnetName();
            else
                subnetName = inputSubnet.getSubnetId();
            enableDhcp = inputSubnet.getEnableDHCP();
            defaultGateway = inputSubnet.getGatewayIp();
            if (!msoCommonUtils.isNullOrEmpty(inputSubnet.getCidr())) {
                int idx = inputSubnet.getCidr().indexOf("/");
                if (idx != -1) {
                    subnet.setIpPrefix(inputSubnet.getCidr().substring(0, idx));
                    subnet.setIpPrefixLen(inputSubnet.getCidr().substring(idx + 1));
                }
            }
            if (inputSubnet.getAllocationPools() != null) {
                for (Pool pool : inputSubnet.getAllocationPools()) {
                    if (!msoCommonUtils.isNullOrEmpty(pool.getStart())
                            && !msoCommonUtils.isNullOrEmpty(pool.getEnd())) {
                        ContrailSubnetPool csp = new ContrailSubnetPool();
                        csp.populateWith(pool);
                        allocationPools.add(csp);
                    }
                }
            }
            if (inputSubnet.getHostRoutes() != null) {
                List<ContrailSubnetHostRoute> hrList = hostRoutes.getHostRoutes();
                for (HostRoute hr : inputSubnet.getHostRoutes()) {
                    if (!msoCommonUtils.isNullOrEmpty(hr.getPrefix())
                            || !msoCommonUtils.isNullOrEmpty(hr.getNextHop())) {
                        ContrailSubnetHostRoute cshr = new ContrailSubnetHostRoute();
                        cshr.populateWith(hr);
                        hrList.add(cshr);
                    }
                }
            }
        }
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
