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

package org.openecomp.mso.adapters.nwrest;



import org.openecomp.mso.openstack.beans.NetworkStatus;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.annotations.providers.NoJackson;

@XmlRootElement(name = "queryNetworkResponse")
@NoJackson
public class QueryNetworkResponse {
	private String networkId;
	private String neutronNetworkId;
	private String networkStackId;
	private Boolean networkExists;
	private NetworkStatus networkStatus;
	private List<Integer> vlans;
	private List<String> routeTargets;
	private Map<String, String> subnetIdMap;
	private Map<String, String> networkOutputs;
	
	public QueryNetworkResponse() {
		super();
	}

	public QueryNetworkResponse(String networkId, String neutronNetworkId,
			String networkStackId, NetworkStatus networkStatus,
			Map<String, String> networkOutputs) {
		super();
		this.networkId = networkId;
		this.neutronNetworkId = neutronNetworkId;
		this.networkStackId = networkStackId;
		this.networkStatus = networkStatus;
		this.networkOutputs = networkOutputs;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getNeutronNetworkId() {
		return neutronNetworkId;
	}

	public void setNeutronNetworkId(String neutronNetworkId) {
		this.neutronNetworkId = neutronNetworkId;
	}

	public String getNetworkStackId() {
		return networkStackId;
	}

	public void setNetworkStackId(String networkStackId) {
		this.networkStackId = networkStackId;
	}

	public NetworkStatus getNetworkStatus() {
		return networkStatus;
	}

	public void setNetworkStatus(NetworkStatus networkStatus) {
		this.networkStatus = networkStatus;
	}

	public Boolean getNetworkExists() {
		return networkExists;
	}

	public void setNetworkExists(Boolean networkExists) {
		this.networkExists = networkExists;
	}

	public List<Integer> getVlans() {
		return vlans;
	}

	public void setVlans(List<Integer> vlans) {
		this.vlans = vlans;
	}

	public List<String> getRouteTargets() {
		return routeTargets;
	}

	public void setRouteTargets(List<String> routeTargets) {
		this.routeTargets = routeTargets;
	}

	public Map<String, String> getSubnetIdMap() {
		return subnetIdMap;
	}

	public void setSubnetIdMap(Map<String, String> subnetIdMap) {
		this.subnetIdMap = subnetIdMap;
	}

	public Map<String, String> getNetworkOutputs() {
		return networkOutputs;
	}

	public void setNetworkOutputs(Map<String, String> networkOutputs) {
		this.networkOutputs = networkOutputs;
	}
	
	public String toJsonString() {
		String jsonString = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			jsonString = mapper.writeValueAsString(this);
		}
		catch (Exception e) {}
		return jsonString;
	}
}
