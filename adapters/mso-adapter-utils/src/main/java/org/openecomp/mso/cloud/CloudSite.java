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


import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
	@BusinessKey
	private String id;
	@JsonProperty("region_id")
	@BusinessKey
	private String regionId;
	@JsonProperty("identity_service_id")
	@BusinessKey
	private String identityServiceId;
	@JsonProperty("aic_version")
	@BusinessKey
	private String aicVersion;
	@JsonProperty("clli")
	@BusinessKey
	private String clli;
	@JsonProperty("cloudify_id")
	@BusinessKey
	private String cloudifyId;
	@JsonProperty("platform")
	@BusinessKey
	private String platform;
	@JsonProperty("orchestrator")
	@BusinessKey
	private String orchestrator;
	
	// Derived property (set by CloudConfig loader based on identityServiceId)
	private CloudIdentity identityService;
	// Derived property (set by CloudConfig loader based on cloudifyId)
	private CloudifyManager cloudifyManager;
	
	public String getId() {
		return this.id;
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
	
	public void setIdentityServiceId(String identityServiceId) {
		this.identityServiceId = identityServiceId;
	}
	public String getAicVersion() {
		return aicVersion;
	}

	public void setAicVersion(String aicVersion) {
		this.aicVersion = aicVersion;
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

	public CloudIdentity getIdentityService () {
		return identityService;
	}

	public void setIdentityService (CloudIdentity identity) {
		this.identityService = identity;
	}
	
	public CloudifyManager getCloudifyManager () {
		return cloudifyManager;
	}

	public void setCloudifyManager (CloudifyManager cloudify) {
		this.cloudifyManager = cloudify;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("regionId", getRegionId())
				.append("identityServiceId", getIdentityServiceId()).append("aicVersion", getAicVersion())
				.append("clli", getClli()).append("cloudifyId", getCloudifyId()).append("platform", getPlatform())
				.append("orchestrator", getOrchestrator()).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		CloudSite castOther = (CloudSite) other;
		return new EqualsBuilder().append(getRegionId(), castOther.getRegionId())
				.append(getIdentityServiceId(), castOther.getIdentityServiceId())
				.append(getAicVersion(), castOther.getAicVersion()).append(getClli(), castOther.getClli()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 31).append(getRegionId()).append(getIdentityServiceId()).append(getAicVersion())
				.append(getClli()).toHashCode();
	}
}