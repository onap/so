/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.adapters.network;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.Pool;
import org.openecomp.mso.openstack.beans.Subnet;
import static org.openecomp.mso.openstack.utils.MsoCommonUtils.isNullOrEmpty;

public class ContrailSubnet {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	@JsonProperty("network_ipam_refs_data_ipam_subnets_subnet")
	private ContrailSubnetIp subnet = new ContrailSubnetIp();
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_default_gateway")
	private String default_gateway;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_subnet_name")
	private String subnet_name;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_enable_dhcp")
	private Boolean enable_dhcp;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_addr_from_start")
	private Boolean addr_from_start = true;
	
	/** future - leave this commented
	private String subnet_uuid;
	private String dns_server_address;
	private List<String> dns_nameservers;
	private String dhcp_option_list;
	private String host_routes;
	**/
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_allocation_pools")
	private List<ContrailSubnetPool> allocation_pools =  new ArrayList <ContrailSubnetPool> ();

	public ContrailSubnet() {
		super();
	}

	public String getDefault_gateway() {
		return default_gateway;
	}

	public void setDefault_gateway(String default_gateway) {
		this.default_gateway = default_gateway;
	}

	public ContrailSubnetIp getSubnet() {
		return subnet;
	}

	public void setSubnet(ContrailSubnetIp subnet) {
		this.subnet = subnet;
	}

	public Boolean isEnable_dhcp() {
		return enable_dhcp;
	}

	public void setEnable_dhcp(Boolean enable_dhcp) {
		this.enable_dhcp = enable_dhcp;
	}

	public String getSubnet_name() {
		return subnet_name;
	}

	public void setSubnet_name(String subnet_name) {
		this.subnet_name = subnet_name;
	}

	public List<ContrailSubnetPool> getAllocation_pools() {
		return allocation_pools;
	}

	public void setPools(List<ContrailSubnetPool> allocation_pools) {
		this.allocation_pools = allocation_pools;
	}

	public Boolean isAddr_from_start() {
		return addr_from_start;
	}

	public void setAddr_from_start(Boolean addr_from_start) {
		this.addr_from_start = addr_from_start;
	}

	public JsonNode toJsonNode()
	{
		JsonNode node = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.convertValue(this, JsonNode.class);
		}
		catch (Exception e)
		{
			String error = "Error creating JsonNode for Contrail Subnet:" + subnet_name;
			LOGGER.error (MessageEnum.RA_MARSHING_ERROR, error, "", "", MsoLogger.ErrorCode.SchemaError, "Exception creating JsonNode for Contrail Subnet", e);
		}
		
		return node;
	}
	
	public String toJsonString()
	{
		String jsonString = null;
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
		}
		catch (Exception e)
		{
			String error = "Error creating JsonString for Contrail Subnet:" + subnet_name;
			LOGGER.error (MessageEnum.RA_MARSHING_ERROR, error, "", "", MsoLogger.ErrorCode.SchemaError, "Exception creating JsonString for Contrail Subnet", e);
		}
		
		return jsonString;
	}
	//poulate contrail subnet with input(from bopel) subnet
	public void populateWith(Subnet i_subnet)
	{
		if (i_subnet != null)
		{
			if (!isNullOrEmpty(i_subnet.getSubnetName()))
				subnet_name = i_subnet.getSubnetName();
			else
				subnet_name = i_subnet.getSubnetId();
			enable_dhcp = i_subnet.getEnableDHCP();
			default_gateway = i_subnet.getGatewayIp();
			if (!isNullOrEmpty(i_subnet.getCidr()) )
			{
				int idx = i_subnet.getCidr().indexOf("/");
				if (idx != -1)
				{
					subnet.setIp_prefix(i_subnet.getCidr().substring(0, idx));
					subnet.setIp_prefix_len(i_subnet.getCidr().substring(idx+1));
				}
			}
			if (i_subnet.getAllocationPools() != null)
			{
				for (Pool pool : i_subnet.getAllocationPools())
				{
					if ( !isNullOrEmpty(pool.getStart()) && !isNullOrEmpty(pool.getEnd()) )
					{		
						ContrailSubnetPool csp = new ContrailSubnetPool();
						csp.populateWith(pool);
						allocation_pools.add (csp);
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		
		StringBuilder buf = new StringBuilder ();
		for (ContrailSubnetPool pool : allocation_pools)
		{
			 buf.append(pool.toString());
		}
		return "ContrailSubnet [subnet=" + subnet.toString() + " default_gateway=" + default_gateway
				+ " enable_dhcp=" + enable_dhcp +  " addr_from_start=" + addr_from_start + " subnet_name=" + subnet_name + " allocation_pools=" + buf + " ]";
	}
}
