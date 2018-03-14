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


import java.util.List;

import org.openecomp.mso.entity.MsoRequest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Javabean representing the rollback criteria following a "Create Network"
 * or "Update Network" operation.  This structure can be passed back to the
 * "Rollback Network" operation to undo the effects of the create/update.
 *
 * Once a network is created, the only possible update through MSO is to
 * the set of VLANs supported by the network.  The vlans attribute of the
 * rollback object contains the previous VLANs before update.
 *
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class NetworkRollback {
	private String networkId;
	private String neutronNetworkId;
	private String networkStackId;
	private String tenantId;
	private String cloudId;
	private String networkType;
	private String modelCustomizationUuid;
	private boolean networkCreated = false;
	// Previous values for updates
	private String networkName = null;
	private String physicalNetwork = null;
	private List<Integer> vlans = null;
	private MsoRequest msoRequest;

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
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getCloudId() {
		return cloudId;
	}
	public void setCloudId(String cloudId) {
		this.cloudId = cloudId;
	}

	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}

	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public boolean getNetworkCreated() {
		return networkCreated;
	}
	public void setNetworkCreated(boolean networkCreated) {
		this.networkCreated = networkCreated;
	}

	public String getNetworkName() {
		return networkName;
	}
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
	}

	public String getPhysicalNetwork() {
		return physicalNetwork;
	}
	public void setPhysicalNetwork(String physicalNetwork) {
		this.physicalNetwork = physicalNetwork;
	}

	public List<Integer> getVlans () {
		return vlans;
	}
	public void setVlans (List<Integer> vlans) {
		this.vlans = vlans;
	}

	public MsoRequest getMsoRequest() {
		return msoRequest;
	}
	public void setMsoRequest (MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}

	@Override
	public String toString() {
		return "NetworkRollback [networkId=" + networkId
				+ ", neutronNetworkId=" + neutronNetworkId + ", networkStackId="
				+ networkStackId + ", tenantId=" + tenantId + ", cloudId="
				+ cloudId + ", networkType=" + networkType
				+ ", networkCreated=" + networkCreated + ", networkName=" + networkName
				+ ", physicalNetwork=" + physicalNetwork + "]";
	}

}
