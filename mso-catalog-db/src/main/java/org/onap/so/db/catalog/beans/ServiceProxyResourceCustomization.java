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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "service_proxy_customization")
public class ServiceProxyResourceCustomization implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2822457299134903084L;

	@BusinessKey
	@Id
	@Column(name = "MODEL_CUSTOMIZATION_UUID")
	private String modelCustomizationUUID;

	@Column(name = "MODEL_INSTANCE_NAME")
	private String modelInstanceName;

	@Column(name = "TOSCA_NODE_TYPE")
	private String toscaNodeType;

	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_SERVICE_MODEL_UUID")
	private Service sourceService;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "SERVICE_PROXY_MODEL_UUID")
	private ServiceProxyResource serviceProxyResource;

	@OneToOne(mappedBy = "serviceProxyResourceCustomization")
	private ConfigurationResourceCustomization configResourceCustomization;

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

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	public Date getCreated() {
		return created;
	}

	@LinkedResource
	public Service getSourceService() {
		return sourceService;
	}

	public void setSourceService(Service sourceService) {
		this.sourceService = sourceService;
	}

	@LinkedResource
	public ServiceProxyResource getServiceProxyResource() {
		return serviceProxyResource;
	}

	public void setServiceProxyResource(ServiceProxyResource serviceProxyResource) {
		this.serviceProxyResource = serviceProxyResource;
	}

	@LinkedResource
	public ConfigurationResourceCustomization getConfigResourceCustomization() {
		return configResourceCustomization;
	}

	public void setConfigResourceCustomization(ConfigurationResourceCustomization configResourceCustomization) {
		this.configResourceCustomization = configResourceCustomization;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
				.append("modelInstanceName", modelInstanceName).append("toscaNodeType", toscaNodeType)
				.append("created", created).append("sourceService", sourceService)
				.append("serviceProxyResource", serviceProxyResource)
				.append("configResourceCustomization", configResourceCustomization).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ServiceProxyResourceCustomization)) {
			return false;
		}
		ServiceProxyResourceCustomization castOther = (ServiceProxyResourceCustomization) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
	}
}
