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

package org.openecomp.mso.cloud;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JavaBean JSON class for a CloudSite.  This bean represents a cloud location
 * (i.e. and LCP node) in the NVP/AIC cloud.  It will be loaded via CloudConfig
 * object, of which it is a component (a CloudConfig JSON configuration file
 * will contain multiple CloudSite definitions).
 *
 * Note that this is only used to access Cloud Configurations loaded from a
 * JSON config file, so there are no explicit setters.
 *
 */
public class CloudSite {
	@JsonProperty
	private String id;
	@JsonProperty("region_id")
	private String regionId;
	@JsonProperty("identity_service_id")
	private String identityServiceId;
	@JsonProperty("aic_version")
	private String aic_version;
	@JsonProperty("clli")
	private String clli;
	@JsonProperty("cloudify_id")
	private String cloudifyId;
	@JsonProperty("platform")
	private String platform;
	@JsonProperty("orchestrator")
	private String orchestrator;

	// Derived property (set by CloudConfig loader based on identityServiceId)
	private CloudIdentity identityService;
	// Derived property (set by CloudConfig loader based on cloudifyId)
	private CloudifyManager cloudifyManager;

	public CloudSite() {}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getRegionId() {
		return regionId;
	}
	
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getIdentityServiceId() {
		return identityServiceId;
	}

	public CloudIdentity getIdentityService () {
		return identityService;
	}

	public void setIdentityService (CloudIdentity identity) {
		this.identityService = identity;
	}
	
	public String getAic_version() {
		return aic_version;
	}

	public void setAic_version(String aic_version) {
		this.aic_version = aic_version;
	}

	public String getClli() {
		return clli;
	}

	public void setClli(String clli) {
		this.clli = clli;
	}

	public String getCloudifyId() {
		return cloudifyId;
	}

	public void setCloudifyId (String id) {
		this.cloudifyId = id;
	}

	public CloudifyManager getCloudifyManager () {
		return cloudifyManager;
	}

	public void setCloudifyManager (CloudifyManager cloudify) {
		this.cloudifyManager = cloudify;
	}
	
	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getOrchestrator() {
		return orchestrator;
	}

	public void setOrchestrator(String orchestrator) {
		this.orchestrator = orchestrator;
	}

	@Override
	public String toString() {
		return "CloudSite: id=" + id +
			", regionId=" + regionId +
			", identityServiceId=" + identityServiceId +
			", aic_version=" + aic_version +
			", clli=" + clli +
			", cloudifyId=" + cloudifyId +
			", platform=" + platform +
			", orchestrator=" + orchestrator;
	}

	@Override
	public CloudSite clone() {
		CloudSite cloudSiteCopy = new CloudSite();
		cloudSiteCopy.id = this.id;
		cloudSiteCopy.regionId = this.regionId;
		cloudSiteCopy.identityServiceId = this.identityServiceId;
		cloudSiteCopy.aic_version = this.aic_version;
		cloudSiteCopy.clli = this.clli;
		cloudSiteCopy.identityService = this.identityService.clone();
		cloudSiteCopy.cloudifyId = this.cloudifyId;
		if (this.cloudifyManager != null)  cloudSiteCopy.cloudifyManager = this.cloudifyManager.clone();
		cloudSiteCopy.platform = this.platform;
		cloudSiteCopy.orchestrator = this.orchestrator;

		return cloudSiteCopy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((identityService == null) ? 0 : identityService.hashCode());
		result = prime * result + ((identityServiceId == null) ? 0 : identityServiceId.hashCode());
		result = prime * result + ((regionId == null) ? 0 : regionId.hashCode());
		result = prime * result + ((aic_version == null) ? 0 : aic_version.hashCode());
		result = prime * result + ((clli == null) ? 0 : clli.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudSite other = (CloudSite) obj;
		if (!cmp(id, other.id))
			return false;
		if (!cmp(regionId, other.regionId))
			return false;
		if (!cmp(identityServiceId, other.identityServiceId))
			return false;
		if (!cmp(aic_version, other.aic_version))
			return false;
		if (!cmp(clli, other.clli))
			return false;
		if (!cmp(identityService, other.identityService))
			return false;
		return true;
	}
	private boolean cmp(Object a, Object b) {
		if (a == null) {
			return (b == null);
		} else {
			return a.equals(b);
		}
	}
}
