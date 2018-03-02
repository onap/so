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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.openecomp.mso.utils.CryptoUtils;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

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
	
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	@JsonProperty
	private String id;
	@JsonProperty ("cloudify_url")
	private String cloudifyUrl;
	@JsonProperty("username")
	private String username;
	@JsonProperty("password")
	private String password;
	@JsonProperty("version")
	private String version;

    private static String cloudKey = "aa3871669d893c7fb8abbcda31b88b4f";

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
        try {
            return CryptoUtils.decrypt (password, cloudKey);
        } catch (GeneralSecurityException e) {
            LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in getMsoPass", e);
            return null;
        }
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
	public String toString() {
		return "CloudifyManager: id=" + id +
			", cloudifyUrl=" + cloudifyUrl +
			", username=" + username +
			", password=" + password +
			", version=" + version;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((cloudifyUrl == null) ? 0 : cloudifyUrl.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		CloudifyManager other = (CloudifyManager) obj;
		if (!cmp(id, other.id))
			return false;
		if (!cmp(cloudifyUrl, other.cloudifyUrl))
			return false;
		if (!cmp(username, other.username))
			return false;
		if (!cmp(version, other.version))
			return false;
		if (!cmp(password, other.password))
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
