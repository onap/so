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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
public class ContrailSubnetHostRoutes {
	
	@JsonProperty("network_ipam_refs_data_ipam_subnets_host_routes_route")
	private List<ContrailSubnetHostRoute> host_routes  = new ArrayList <ContrailSubnetHostRoute> ();

	public ContrailSubnetHostRoutes() {
	}

	public List<ContrailSubnetHostRoute> getHost_routes() {
		return host_routes;
	}

	public void setHost_routes(List<ContrailSubnetHostRoute> host_routes) {
		this.host_routes = host_routes;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder ();
		if (host_routes != null)
		{
			for (ContrailSubnetHostRoute hr : host_routes)
			{
				buf.append(hr.toString());
			}
		}
		return "ContrailSubnetHostRoutes [" + buf.toString() + "]";
	}
}
