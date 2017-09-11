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

package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;



public class ServiceInstancesRequest {

	private RequestDetails requestDetails;
	private String serviceInstanceId;
	private String vnfInstanceId;
	private String networkInstanceId;
	private String volumeGroupInstanceId;
	private String vfModuleInstanceId;

	public RequestDetails getRequestDetails() {
		return requestDetails;
	}

	public void setRequestDetails(RequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public String getVnfInstanceId() {
		return vnfInstanceId;
	}

	public void setVnfInstanceId(String vnfInstanceId) {
		this.vnfInstanceId = vnfInstanceId;
	}

	public String getNetworkInstanceId() {
		return networkInstanceId;
	}

	public void setNetworkInstanceId(String networkInstanceId) {
		this.networkInstanceId = networkInstanceId;
	}

	public String getVolumeGroupInstanceId() {
		return volumeGroupInstanceId;
	}

	public void setVolumeGroupInstanceId(String volumeGroupInstanceId) {
		this.volumeGroupInstanceId = volumeGroupInstanceId;
	}

	public String getVfModuleInstanceId() {
		return vfModuleInstanceId;
	}

	public void setVfModuleInstanceId(String vfModuleInstanceId) {
		this.vfModuleInstanceId = vfModuleInstanceId;
	}

	@Override
	public String toString() {
		return "ServiceInstancesRequest [requestDetails=" + requestDetails
				+ ", serviceInstanceId=" + serviceInstanceId
				+ ", vnfInstanceId=" + vnfInstanceId + ", networkInstanceId="
				+ networkInstanceId + ", volumeGroupInstanceId="
				+ volumeGroupInstanceId + ", vfModuleInstanceId="
				+ vfModuleInstanceId + "]";
	}

}