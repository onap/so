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

package org.openecomp.mso.adapters.valet.beans;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * This class represents the body of an Update request on a Valet Placement API call
 */
public class ValetUpdateRequest implements Serializable {
	private static final long serialVersionUID = 768026109321305392L;

	@JsonProperty("request_id")
	private String requestId;
	@JsonProperty("region_id")
	private String regionId;
	@JsonProperty("tenant_id")
	private String tenantId;
	@JsonProperty("service_instance_id")
	private String serviceInstanceId;
	@JsonProperty("vnf_id")
	private String vnfId;
	@JsonProperty("vnf_name")
	private String vnfName;
	@JsonProperty("vf_module_id")
	private String vfModuleId;
	@JsonProperty("vf_module_name")
	private String vfModuleName;
	@JsonProperty("keystone_url")
	private String keystoneUrl;
	@JsonProperty("heat_request")
	private HeatRequest heatRequest;
	
	public ValetUpdateRequest() {
		super();
	}
	
	public String getRequestId() {
		return this.requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getRegionId() {
		return this.regionId;
	}
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
	public String getTenantId() {
		return this.tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getServiceInstanceId() {
		return this.serviceInstanceId;
	}
	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	public String getVnfId() {
		return this.vnfId;
	}
	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}
	public String getVnfName() {
		return this.vnfName;
	}
	public void setVnfName(String vnfName) {
		this.vnfName = vnfName;
	}
	public String getVfModuleId() {
		return this.vfModuleId;
	}
	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}
	public String getVfModuleName() {
		return this.vfModuleName;
	}
	public void setVfModuleName(String vfModuleName) {
		this.vfModuleName = vfModuleName;
	}
	public String getKeystoneUrl() {
		return this.keystoneUrl;
	}
	public void setKeystoneUrl(String keystoneUrl) {
		this.keystoneUrl = keystoneUrl;
	}
	public HeatRequest getHeatRequest() {
		return this.heatRequest;
	}
	public void setHeatRequest(HeatRequest heatRequest) {
		this.heatRequest = heatRequest;
	}
	@Override
	public int hashCode() {
		return Objects.hash(regionId, tenantId, serviceInstanceId, vnfId, vnfName, vfModuleId, vfModuleName, keystoneUrl, heatRequest);

	}
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ValetUpdateRequest)) {
			return false;
		}
		ValetUpdateRequest vur = (ValetUpdateRequest) o;
		return Objects.equals(regionId, vur.regionId) 
				&& Objects.equals(tenantId, vur.tenantId)
				&& Objects.equals(serviceInstanceId, vur.serviceInstanceId)
				&& Objects.equals(vnfId, vur.vnfId)
				&& Objects.equals(vnfName, vur.vnfName)
				&& Objects.equals(vfModuleId, vur.vfModuleId)
				&& Objects.equals(vfModuleName, vur.vfModuleName)
				&& Objects.equals(keystoneUrl, vur.keystoneUrl)
				&& Objects.equals(heatRequest, vur.heatRequest);
	}
}
