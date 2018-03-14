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

package org.openecomp.mso.adapters.nwrest;


import java.util.List;
import org.openecomp.mso.openstack.beans.RouteTarget;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("contrailNetwork")
public class ContrailNetwork {
	private String shared   = "false";
	private String external = "false";
	private List<RouteTarget> routeTargets;
	private List<String> policyFqdns;
	private List<String> routeTableFqdns;
	
	public ContrailNetwork() {
		super();
	}

	public ContrailNetwork(String shared, String external, List<RouteTarget> routeTargets, List<String> policyFqdns, List<String> routeTableFqdns) {
		super();
		this.shared = shared;
		this.external = external;
		this.routeTargets = routeTargets;
		this.policyFqdns = policyFqdns;
		this.routeTableFqdns = routeTableFqdns;
	}

	public String getShared() {
		return shared;
	}

	public void setShared(String shared) {
		this.shared = shared;
	}

	public String getExternal() {
		return external;
	}

	public void setExternal(String external) {
		this.external = external;
	}

	public List<RouteTarget> getRouteTargets() {
		return routeTargets;
	}

	public void setRouteTargets(List<RouteTarget> routeTargets) {
		this.routeTargets = routeTargets;
	}

	public List<String> getPolicyFqdns() {
		return policyFqdns;
	}

	public void setPolicyFqdns(List<String> policyFqdns) {
		this.policyFqdns = policyFqdns;
	}
	
	public List<String> getRouteTableFqdns() {
		return routeTableFqdns;
	}

	public void setRouteTableFqdns(List<String> routeTableFqdns) {
		this.routeTableFqdns = routeTableFqdns;
	}
	
}
