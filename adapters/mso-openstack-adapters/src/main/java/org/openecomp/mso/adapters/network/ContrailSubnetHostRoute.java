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

package org.openecomp.mso.adapters.network;


import org.openecomp.mso.openstack.beans.HostRoute;
import com.fasterxml.jackson.annotation.JsonProperty;
public class ContrailSubnetHostRoute {
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_host_routes_route_prefix")
	private String prefix;
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_host_routes_route_next_hop")
	private String nextHop;

	public ContrailSubnetHostRoute() {
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getNextHop() {
		return nextHop;
	}

	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}
	
	public void populateWith(HostRoute hostRoute)
	{
		if (hostRoute != null)
		{
			prefix = hostRoute.getPrefix();
			nextHop = hostRoute.getNextHop();
		}
	}

	@Override
	public String toString() {
		return "ContrailSubnetHostRoute [prefix=" + prefix + ", nextHop=" + nextHop + "]";
	}
	
}
