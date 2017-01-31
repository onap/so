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
	private List<Integer> vlans = new ArrayList<Integer>();
	private List<String> subnets = new ArrayList<String>();
	private String shared = "";

	static Map<String,NetworkStatus> NetworkStatusMap;
	static {
		NetworkStatusMap = new HashMap<String,NetworkStatus>();
		NetworkStatusMap.put("ACTIVE", NetworkStatus.ACTIVE);
		NetworkStatusMap.put("DOWN", NetworkStatus.DOWN);
		NetworkStatusMap.put("BUILD", NetworkStatus.BUILD);
		NetworkStatusMap.put("ERROR", NetworkStatus.ERROR);
	}

	public NetworkInfo () {
	}

	public NetworkInfo (String name, NetworkStatus status) {
		this.name = name;
		this.id = name;	// Don't have an ID, so just use name
		this.status = status;
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
	public NetworkInfo (Network network)
	{
		if (network == null) {
			this.status = NetworkStatus.NOTFOUND;
			return;
		}

		this.name = network.getName();
		this.id = network.getId();

		if (network.getStatus() == null) {
			// Can this happen on a newly created network?
			this.status = NetworkStatus.UNKNOWN;
		} else if (NetworkStatusMap.containsKey(network.getStatus())) {
			this.status = NetworkStatusMap.get(network.getStatus());
		} else {
			this.status = NetworkStatus.UNKNOWN;
		}

		if (network.getProviderPhysicalNetwork() != null) {
			this.provider = network.getProviderPhysicalNetwork();
			if (network.getProviderNetworkType().equals("vlan")) {
                this.vlans.add(network.getProviderSegmentationId());
            }
		}
		else if (network.getSegments() != null && network.getSegments().size() > 0) {
			Segment s = network.getSegments().get(0);
			this.provider = s.getProviderPhysicalNetwork();
			if (s.getProviderNetworkType().equals("vlan")) {
                for (Segment s1 : network.getSegments()) {
					this.vlans.add(s1.getProviderSegmentationId());
				}
            }
		}
		this.subnets = network.getSubnets();
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

	public void setSubnets (List<String> subnets) {
		this.subnets = subnets;
	}

	public String getShared() {
		return shared;
	}

	public void setShared(String shared) {
		this.shared = shared;
	}

	@Override
    public String toString () {
		return "Network: name=" + name + ",id=" + id + ",status=" + status +
				",provider=" + provider + ",vlans=" + vlans + ",subnets=" + subnets + ",shared=" + shared;
	}
}

