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

import java.security.GeneralSecurityException;
import java.util.Comparator;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.CryptoUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * JavaBean JSON class for a Cloudify Manager.  This bean represents a Cloudify
 * node through which TOSCA-based VNFs may be deployed.  Each CloudSite in the
 * CloudConfig may have a Cloudify Manager for deployments using TOSCA blueprints.
 * Cloudify Managers may support multiple Cloud Sites, but each site will have
 * at most one Cloudify Manager.
 * 
 * This does not replace the ability to use the CloudSite directly via Openstack.
 *
 * Note that this is only used to access Cloud Configurations loaded from a
 * JSON config file, so there are no explicit setters.
 *
 * @author JC1348
 */
public class CloudifyManager {
	
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA, CloudifyManager.class);

	@BusinessKey
	@JsonProperty
	private String id;
	
	@BusinessKey
	@JsonProperty ("cloudify_url")
	private String cloudifyUrl;
	
	@BusinessKey
	@JsonProperty("username")
	private String username;
	
	@BusinessKey
	@JsonProperty("password")
	private String password;
	
	@BusinessKey
	@JsonProperty("version")
	private String version;

	public CloudifyManager() {}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCloudifyUrl() {
		return cloudifyUrl;
	}

	public void setCloudifyUrl(String cloudifyUrl) {
		this.cloudifyUrl = cloudifyUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
        return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public CloudifyManager clone() {
		CloudifyManager cloudifyManagerCopy = new CloudifyManager();
		cloudifyManagerCopy.id = this.id;
		cloudifyManagerCopy.cloudifyUrl = this.cloudifyUrl;
		cloudifyManagerCopy.username = this.username;
		cloudifyManagerCopy.password = this.password;
		cloudifyManagerCopy.version = this.version;
		return cloudifyManagerCopy;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
				.append("cloudifyUrl", getCloudifyUrl()).append("username", getUsername())
				.append("password", getPassword()).append("version", getVersion()).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		CloudifyManager castOther = (CloudifyManager) other;
		return new EqualsBuilder().append(getId(), castOther.getId())
				.append(getCloudifyUrl(), castOther.getCloudifyUrl()).append(getUsername(), castOther.getUsername())
				.append(getPassword(), castOther.getPassword()).append(getVersion(), castOther.getVersion()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 31).append(getId()).append(getCloudifyUrl()).append(getUsername())
				.append(getPassword()).append(getVersion()).toHashCode();
	}
}