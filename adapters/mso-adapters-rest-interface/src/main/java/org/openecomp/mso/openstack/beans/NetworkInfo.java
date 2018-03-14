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

package org.openecomp.mso.openstack.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Segment;

/*
 * This Java bean class relays Network details (including status) to ActiveVOS processes.
 *
 * This bean is returned by all Network-specific adapter operations (create, query, delete)
 */
public class NetworkInfo {
	// Set defaults for everything
	private String name = "";
	private String id = "";
	private NetworkStatus status = NetworkStatus.UNKNOWN;
	private String provider = "";
	private List<Integer> vlans = new ArrayList<>();
	private List<String> subnets = new ArrayList<>();

	static Map<String,NetworkStatus> NetworkStatusMap;
	static {
		NetworkStatusMap = new HashMap<>();
		NetworkStatusMap.put("ACTIVE", NetworkStatus.ACTIVE);
		NetworkStatusMap.put("DOWN", NetworkStatus.DOWN);
		NetworkStatusMap.put("BUILD", NetworkStatus.BUILD);
		NetworkStatusMap.put("ERROR", NetworkStatus.ERROR);
	}

	/**
	 * Capture the data from a Neutron Network object.
	 *
	 * For MSO, there are assumptions regarding all networks.
	 * - Everything will be a provider network
	 * - All provider networks are VLANs
	 * - Multiple VLANs are supported, and indicated by multi-provider segments.
	 *   Each will have the same physical network & network type "vlan".
	 *
	 * @param network
	 */
	public NetworkInfo(Network network) {
		if (network != null) {
			initFieldsWithDataFromNetwork(network);
		} else {
			status = NetworkStatus.NOTFOUND;
		}
	}

	private void initFieldsWithDataFromNetwork(Network network){
		name = network.getName();
		id = network.getId();

		if (network.getStatus() != null && NetworkStatusMap.containsKey(network.getStatus())) {
			status = NetworkStatusMap.get(network.getStatus());
		}
		if (network.getProviderPhysicalNetwork() != null) {
			provider = network.getProviderPhysicalNetwork();
			if ("vlan".equals(network.getProviderNetworkType())) {
                vlans.add(network.getProviderSegmentationId());
            }
		}
		else if (network.getSegments() != null && !network.getSegments().isEmpty()) {
			Segment s = network.getSegments().get(0);
			provider = s.getProviderPhysicalNetwork();
			if ("vlan".equals(s.getProviderNetworkType())) {
				network.getSegments().forEach(segment -> vlans.add(segment.getProviderSegmentationId()));
            }
		}
		subnets = network.getSubnets();
	}

	public String getName() {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId (String id) {
		this.id = id;
	}

	public NetworkStatus getStatus() {
		return status;
	}

	public void setStatus (NetworkStatus status) {
		this.status = status;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider (String provider) {
		this.provider = provider;
	}

	public List<Integer> getVlans () {
		return vlans;
	}

	public void setVlans (List<Integer> vlans) {
		this.vlans = vlans;
	}

	public List<String> getSubnets () {
		return subnets;
	}

	@Override
	public String toString() {
		return "NetworkInfo{" + "name='" + name + '\'' +
			", id='" + id + '\'' +
			", status=" + status +
			", provider='" + provider + '\'' +
			", vlans=" + vlans +
			", subnets=" + subnets +
			'}';
	}
}

