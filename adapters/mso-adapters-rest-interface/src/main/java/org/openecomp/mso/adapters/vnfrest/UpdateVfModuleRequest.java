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

package org.openecomp.mso.adapters.vnfrest;


import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.openecomp.mso.entity.MsoRequest;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("updateVfModuleRequest")
@XmlRootElement(name = "updateVfModuleRequest")
public class UpdateVfModuleRequest extends VfRequestCommon {

	private String cloudSiteId;
	private String tenantId;

	private String vnfId;
	private String vnfType;
	private String vnfVersion;

	private String vfModuleName;
	private String vfModuleType;
	private String vfModuleId;
	private String vfModuleStackId;

	private String volumeGroupId;
	private String volumeGroupStackId;

	private String baseVfModuleId;
	private String baseVfModuleStackId;

	private String modelCustomizationUuid;

	private String requestType;
	private Boolean failIfExists;
	private Boolean backout;

	private Map<String,String> vfModuleParams = new HashMap<>();
	private MsoRequest msoRequest = new MsoRequest();

	public String getCloudSiteId() {
		return cloudSiteId;
	}

	public void setCloudSiteId(String cloudSiteId) {
		this.cloudSiteId = cloudSiteId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getVnfId() {
		return vnfId;
	}

	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}

	public String getVfModuleName() {
		return vfModuleName;
	}

	public void setVfModuleName(String vfModuleName) {
		this.vfModuleName = vfModuleName;
	}

	public String getVnfType() {
		return vnfType;
	}

	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}

	public String getVnfVersion() {
		return vnfVersion;
	}

	public void setVnfVersion(String vnfVersion) {
		this.vnfVersion = vnfVersion;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public String getVfModuleType() {
		return vfModuleType;
	}

	public void setVfModuleType(String vfModuleType) {
		this.vfModuleType = vfModuleType;
	}

	public String getVfModuleStackId() {
		return vfModuleStackId;
	}

	public void setVfModuleStackId(String vfModuleStackId) {
		this.vfModuleStackId = vfModuleStackId;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public String getVolumeGroupStackId() {
		return volumeGroupStackId;
	}

	public void setVolumeGroupStackId(String volumeGroupStackId) {
		this.volumeGroupStackId = volumeGroupStackId;
	}

	public String getBaseVfModuleId() {
		return baseVfModuleId;
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}

	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public void setBaseVfModuleId(String baseVfModuleId) {
		this.baseVfModuleId = baseVfModuleId;
	}

	public String getBaseVfModuleStackId() {
		return baseVfModuleStackId;
	}

	public void setBaseVfModuleStackId(String baseVfModuleStackId) {
		this.baseVfModuleStackId = baseVfModuleStackId;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Boolean getFailIfExists() {
		return failIfExists;
	}

	public void setFailIfExists(Boolean failIfExists) {
		this.failIfExists = failIfExists;
	}

	public Boolean getBackout() {
		return backout;
	}

	public void setBackout(Boolean backout) {
		this.backout = backout;
	}

	public Map<String, String> getVfModuleParams() {
		return vfModuleParams;
	}

	public void setVfModuleParams(Map<String, String> vfModuleParams) {
		this.vfModuleParams = vfModuleParams;
	}

	public MsoRequest getMsoRequest() {
		return msoRequest;
	}

	public void setMsoRequest(MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}
}
