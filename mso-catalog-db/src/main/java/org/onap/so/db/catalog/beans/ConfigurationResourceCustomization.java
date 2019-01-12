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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "configuration_customization")
public class ConfigurationResourceCustomization implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1230671937560638856L;

	@BusinessKey
	@Id
	@Column(name = "MODEL_CUSTOMIZATION_UUID")
	private String modelCustomizationUUID;

	@Column(name = "MODEL_INSTANCE_NAME")
	private String modelInstanceName;

	@Column(name = "CONFIGURATION_FUNCTION")
	private String nfFunction;

	@Column(name = "CONFIGURATION_TYPE")
	private String nfType;

	@Column(name = "CONFIGURATION_ROLE")
	private String nfRole;

	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "SERVICE_PROXY_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID")
	private String serviceProxyResourceCustomizationUUID;
		
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONFIGURATION_CUSTOMIZATION_MODEL_CUSTOMIZATION_UUID")
	private ConfigurationResourceCustomization configResourceCustomization;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONFIGURATION_MODEL_UUID")
	private ConfigurationResource configurationResource;

	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	public String getModelCustomizationUUID() {
		return modelCustomizationUUID;
	}

	public void setModelCustomizationUUID(String modelCustomizationUUID) {
		this.modelCustomizationUUID = modelCustomizationUUID;
	}

	public String getModelInstanceName() {
		return modelInstanceName;
	}

	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}

	public String getNfFunction() {
		return nfFunction;
	}

	public void setNfFunction(String nfFunction) {
		this.nfFunction = nfFunction;
	}

	public String getNfType() {
		return nfType;
	}

	public void setNfType(String nfType) {
		this.nfType = nfType;
	}

	public String getNfRole() {
		return nfRole;
	}

	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}

	public Date getCreated() {
		return created;
	}
		
	public String getServiceProxyResourceCustomizationUUID() {
		return serviceProxyResourceCustomizationUUID;
	}

	public void setServiceProxyResourceCustomizationUUID(String serviceProxyResourceCustomizationUUID) {
		this.serviceProxyResourceCustomizationUUID = serviceProxyResourceCustomizationUUID;
	}

	@LinkedResource
	public ConfigurationResourceCustomization getConfigResourceCustomization() {
		return configResourceCustomization;
	}
	
	public void setConfigResourceCustomization(ConfigurationResourceCustomization configResourceCustomization) {
		this.configResourceCustomization = configResourceCustomization;
	}

	@LinkedResource
	public ConfigurationResource getConfigurationResource() {
		return configurationResource;
	}

	public void setConfigurationResource(ConfigurationResource configurationResource) {
		this.configurationResource = configurationResource;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
				.append("modelInstanceName", modelInstanceName).append("nfFunction", nfFunction)
				.append("nfType", nfType).append("nfRole", nfRole).append("created", created)
				//.append("serviceProxyResourceCustomization", serviceProxyResourceCustomization)
				.append("configResourceCustomization", configResourceCustomization)
				.append("configurationResource", configurationResource).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ConfigurationResourceCustomization)) {
			return false;
		}
		ConfigurationResourceCustomization castOther = (ConfigurationResourceCustomization) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
	}

}
