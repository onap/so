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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "vnf_recipe")
public class VnfRecipe implements Serializable, Recipe {

	private static final long serialVersionUID = 768026109321305392L;

	@Id
	@Column(name = "id")
	private Integer id;

	@BusinessKey
	@Column(name = "NF_ROLE")
	private String nfRole;

	@Column(name = "VNF_PARAM_XSD")
	private String paramXsd;

	@Column(name = "VF_MODULE_ID")
	private String vfModuleId;

	@BusinessKey
	@Column(name = "ACTION")
	protected String action;

	@Column(name = "description")
	private String description;

	@BusinessKey
	@Column(name = "ORCHESTRATION_URI")
	protected String orchestrationUri;

	@Column(name = "RECIPE_TIMEOUT")
	private Integer recipeTimeout;

	@BusinessKey
	@Column(name = "SERVICE_TYPE")
	private String serviceType;

	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("vnfType", nfRole).append("paramXsd", paramXsd)
				.append("vfModuleId", vfModuleId).append("action", action).append("description", description)
				.append("orchestrationUri", orchestrationUri).append("recipeTimeout", recipeTimeout)
				.append("serviceType", serviceType).append("created", created).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VnfRecipe)) {
			return false;
		}
		VnfRecipe castOther = (VnfRecipe) other;
		return new EqualsBuilder().append(nfRole, castOther.nfRole).append(action, castOther.action)
				.append(orchestrationUri, castOther.orchestrationUri).append(serviceType, castOther.serviceType)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(nfRole).append(action).append(orchestrationUri).append(serviceType)
				.toHashCode();
	}

	public String getNfRole() {
		return nfRole;
	}

	public void setNfRole(String nfRole) {
		this.nfRole = nfRole;
	}

	public String getParamXsd() {
		return paramXsd;
	}

	public void setParamXsd(String paramXsd) {
		this.paramXsd = paramXsd;
	}

	public String getVfModuleId() {
		return vfModuleId;
	}

	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrchestrationUri() {
		return orchestrationUri;
	}

	public void setOrchestrationUri(String orchestrationUri) {
		this.orchestrationUri = orchestrationUri;
	}

	public Integer getRecipeTimeout() {
		return recipeTimeout;
	}

	public void setRecipeTimeout(Integer recipeTimeout) {
		this.recipeTimeout = recipeTimeout;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public Date getCreated() {
		return created;
	}
}
