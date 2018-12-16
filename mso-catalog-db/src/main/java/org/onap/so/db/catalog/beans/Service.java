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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "service")
public class Service implements Serializable {

	private static final long serialVersionUID = 768026109321305392L;

	@Column(name = "MODEL_NAME")
	private String modelName;

	@Column(name = "DESCRIPTION", length = 1200)
	private String description;

	@BusinessKey
	@Id
	@Column(name = "MODEL_UUID")
	private String modelUUID;

	@Column(name = "MODEL_INVARIANT_UUID")
	private String modelInvariantUUID;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Column(name = "MODEL_VERSION")
	private String modelVersion;

	@Column(name = "SERVICE_TYPE")
	private String serviceType;

	@Column(name = "SERVICE_ROLE")
	private String serviceRole;

	@Column(name = "ENVIRONMENT_CONTEXT")
	private String environmentContext;

	@Column(name = "WORKLOAD_CONTEXT")
	private String workloadContext;

	@Column(name = "SERVICE_CATEGORY")
	private String category;

	@Column(name = "RESOURCE_ORDER")
	private String resourceOrder;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "network_resource_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<NetworkResourceCustomization> networkCustomizations;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "vnf_resource_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<VnfResourceCustomization> vnfCustomizations;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "allotted_resource_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<AllottedResourceCustomization> allottedCustomizations;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "collection_resource_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<CollectionResourceCustomization> collectionResourceCustomizations;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "service_proxy_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<ServiceProxyResourceCustomization> serviceProxyCustomizations;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "configuration_customization_to_service", joinColumns = @JoinColumn(name = "SERVICE_MODEL_UUID"), inverseJoinColumns = @JoinColumn(name = "RESOURCE_MODEL_CUSTOMIZATION_UUID"))
	private List<ConfigurationResourceCustomization> configurationCustomizations;

	@OneToMany(cascade = CascadeType.ALL)
	@MapKey(name = "action")
	@JoinColumn(name = "SERVICE_MODEL_UUID")
	private Map<String, ServiceRecipe> recipes;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "TOSCA_CSAR_ARTIFACT_UUID")
	private ToscaCsar csar;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("modelName", modelName).append("description", description)
				.append("modelUUID", modelUUID).append("modelInvariantUUID", modelInvariantUUID)
				.append("created", created).append("modelVersion", modelVersion).append("serviceType", serviceType)
				.append("serviceRole", serviceRole).append("environmentContext", environmentContext)
				.append("workloadContext", workloadContext).append("category", category)
				.append("networkCustomizations", networkCustomizations).append("vnfCustomizations", vnfCustomizations)
				.append("allottedCustomizations", allottedCustomizations)
				.append("collectionResourceCustomizations", collectionResourceCustomizations)
				.append("serviceProxyCustomizations", serviceProxyCustomizations)
				.append("configurationCustomizations", configurationCustomizations).append("recipes", recipes)
				.append("csar", csar).toString();
	}

	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Service)) {
			return false;
		}
		Service castOther = (Service) other;
		return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelUUID).toHashCode();
	}

	@LinkedResource
	public List<ServiceProxyResourceCustomization> getServiceProxyCustomizations() {
		return serviceProxyCustomizations;
	}

	public void setServiceProxyCustomizations(List<ServiceProxyResourceCustomization> serviceProxyCustomizations) {
		this.serviceProxyCustomizations = serviceProxyCustomizations;
	}

	@LinkedResource
	public List<NetworkResourceCustomization> getNetworkCustomizations() {
		if (networkCustomizations == null)
			networkCustomizations = new ArrayList<>();
		return networkCustomizations;
	}

	public void setNetworkCustomizations(List<NetworkResourceCustomization> networkCustomizations) {
		this.networkCustomizations = networkCustomizations;
	}

	@LinkedResource
	public List<VnfResourceCustomization> getVnfCustomizations() {
		if (vnfCustomizations == null)
			vnfCustomizations = new ArrayList<>();
		return vnfCustomizations;
	}

	public void setVnfCustomizations(List<VnfResourceCustomization> vnfCustomizations) {
		this.vnfCustomizations = vnfCustomizations;
	}

	@LinkedResource
	public List<AllottedResourceCustomization> getAllottedCustomizations() {
		if (allottedCustomizations == null)
			allottedCustomizations = new ArrayList<>();
		return allottedCustomizations;
	}

	public void setAllottedCustomizations(List<AllottedResourceCustomization> allotedCustomizations) {
		this.allottedCustomizations = allotedCustomizations;
	}

	@LinkedResource
	public List<CollectionResourceCustomization> getCollectionResourceCustomizations() {
		if (collectionResourceCustomizations == null)
			collectionResourceCustomizations = new ArrayList<>();
		return collectionResourceCustomizations;
	}

	public void setCollectionResourceCustomizations(
			List<CollectionResourceCustomization> collectionResourceCustomizations) {
		this.collectionResourceCustomizations = collectionResourceCustomizations;
	}

	@LinkedResource
	public List<ConfigurationResourceCustomization> getConfigurationCustomizations() {
		if(configurationCustomizations == null)
			configurationCustomizations = new ArrayList<>();
		return configurationCustomizations;
	}

	public void setConfigurationCustomizations(List<ConfigurationResourceCustomization> configurationCustomizations) {
		this.configurationCustomizations = configurationCustomizations;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@LinkedResource
	public Map<String, ServiceRecipe> getRecipes() {
		return recipes;
	}

	public void setRecipes(Map<String, ServiceRecipe> recipes) {
		this.recipes = recipes;
	}

	public Date getCreated() {
		return created;
	}

	public String getModelUUID() {
		return modelUUID;
	}

	public void setModelUUID(String modelUUID) {
		this.modelUUID = modelUUID;
	}

	public String getModelInvariantUUID() {
		return modelInvariantUUID;
	}

	public void setModelInvariantUUID(String modelInvariantUUID) {
		this.modelInvariantUUID = modelInvariantUUID;
	}

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	/**
	 * @return Returns the category.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            The category to set.
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	public String getEnvironmentContext() {
		return this.environmentContext;
	}

	public void setEnvironmentContext(String environmentContext) {
		this.environmentContext = environmentContext;
	}

	@LinkedResource
	public ToscaCsar getCsar() {
		return csar;
	}

	public void setCsar(ToscaCsar csar) {
		this.csar = csar;
	}

	public String getWorkloadContext() {
		return this.workloadContext;
	}

	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
	}

	public String getResourceOrder() {
		return resourceOrder;
	}

	public void setResourceOrder(String resourceOrder) {
		this.resourceOrder = resourceOrder;
	}
}
