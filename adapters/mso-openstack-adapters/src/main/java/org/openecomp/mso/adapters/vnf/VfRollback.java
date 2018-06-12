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

package org.openecomp.mso.adapters.vnf;



import org.openecomp.mso.entity.MsoRequest;

public class VfRollback {
	private String vnfId;
	private String tenantId;
	private String cloudSiteId;
	private boolean tenantCreated = false;
	private boolean vnfCreated = false;
	private MsoRequest msoRequest;
	private String volumeGroupName;
	private String volumeGroupId;
	private String requestType;
	private String volumeGroupHeatStackId;
	private String baseGroupHeatStackId;
	private boolean isBase = false;
	private String vfModuleStackId;


	public String getVnfId() {
		return vnfId;
	}
	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getCloudSiteId() {
		return cloudSiteId;
	}
	public void setCloudSiteId(String cloudId) {
		this.cloudSiteId = cloudId;
	}
	public boolean getTenantCreated() {
		return tenantCreated;
	}
	public void setTenantCreated(boolean tenantCreated) {
		this.tenantCreated = tenantCreated;
	}
	public boolean getVnfCreated() {
		return vnfCreated;
	}
	public void setVnfCreated(boolean vnfCreated) {
		this.vnfCreated = vnfCreated;
	}
	public MsoRequest getMsoRequest() {
		return msoRequest;
	}
	public void setMsoRequest (MsoRequest msoRequest) {
		this.msoRequest = msoRequest;
	}
	public String getVolumeGroupName() {
		return this.volumeGroupName;
	}
	public void setVolumeGroupName(String volumeGroupName) {
		this.volumeGroupName = volumeGroupName;
	}
	public String getVolumeGroupId() {
		return this.volumeGroupId;
	}
	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}
	public String getRequestType() {
		return this.requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	/*
	private String volumeGroupHeatStackId;
	private String baseGroupHeatStackId;
	private boolean isBase = false;
	*/
	public String getVolumeGroupHeatStackId() {
		return this.volumeGroupHeatStackId;
	}
	public void setVolumeGroupHeatStackId(String volumeGroupHeatStackId) {
		this.volumeGroupHeatStackId = volumeGroupHeatStackId;
	}
	
	public String getBaseGroupHeatStackId() {
		return this.baseGroupHeatStackId;
	}
	public void setBaseGroupHeatStackId(String baseGroupHeatStackId) {
		this.baseGroupHeatStackId = baseGroupHeatStackId;
	}
	
	public boolean isBase() {
		return this.isBase;
	}
	public void setIsBase(boolean isBase) {
		this.isBase = isBase;
	}
	public String getVfModuleStackId() {
		return this.vfModuleStackId;
	}
	public void setVfModuleStackId(String vfModuleStackId) {
		this.vfModuleStackId = vfModuleStackId;
	}

	@Override
    public String toString() {
		return "VfRollback: cloud=" + cloudSiteId + ", tenant=" + tenantId +
				", vnf=" + vnfId + ", tenantCreated=" + tenantCreated +
				", vnfCreated=" + vnfCreated + ", requestType = " + requestType +
				", volumeGroupHeatStackId = " + this.volumeGroupHeatStackId;
	}
}
