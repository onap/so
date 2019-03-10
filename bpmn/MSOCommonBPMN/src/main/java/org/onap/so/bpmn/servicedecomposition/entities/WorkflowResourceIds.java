/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WorkflowResourceIds implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8591599114353940105L;
	private String serviceInstanceId;
	private String vnfId;
	private String networkId;
	private String volumeGroupId;
	private String vfModuleId;
	private String networkCollectionId;
	private String configurationId;
	private String instanceGroupId;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("serviceInstanceId", serviceInstanceId).append("vnfId", vnfId)
				.append("networkId", networkId).append("volumeGroupId", volumeGroupId).append("vfModuleId", vfModuleId)
				.append("networkCollectionId", networkCollectionId).append("configurationId", configurationId)
				.toString();
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getNetworkCollectionId() {
		return networkCollectionId;
	}

	public void setNetworkCollectionId(String networkCollectionId) {
		this.networkCollectionId = networkCollectionId;
	}

	public String getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(String configurationId) {
		this.configurationId = configurationId;
	}

	public String getInstanceGroupId() {
		return instanceGroupId;
	}

	public void setInstanceGroupId(String instanceGroupId) {
		this.instanceGroupId = instanceGroupId;
	}
}
