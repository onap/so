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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "cvnfc_customization")
public class CvnfcCustomization implements Serializable {

	private static final long serialVersionUID = -3772469944364616486L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@BusinessKey
	@Column(name = "MODEL_CUSTOMIZATION_UUID")
	private String modelCustomizationUUID;
	
	@Column(name = "MODEL_INSTANCE_NAME")
	private String modelInstanceName;
	
	@Column(name = "MODEL_UUID")
	private String modelUUID;
	
	@Column(name = "MODEL_INVARIANT_UUID")
	private String modelInvariantUUID;	
	
	@Column(name = "MODEL_VERSION")
	private String modelVersion;
	
	@Column(name = "MODEL_NAME")
	private String modelName;
	
	@Column(name = "TOSCA_NODE_TYPE")
	private String toscaNodeType;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "NFC_FUNCTION")
	private String nfcFunction;
	
	@Column(name = "NFC_NAMING_CODE")
	private String nfcNamingCode;
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID")
	private VfModuleCustomization vfModuleCustomization;	
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "VNFC_CUST_MODEL_CUSTOMIZATION_UUID")
	private VnfcCustomization vnfcCustomization;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID")
	private VnfResourceCustomization vnfResourceCustomization;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "modelCustomizationUUID")
	private Set<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomization;

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof CvnfcCustomization)) {
			return false;
		}
		CvnfcCustomization castOther = (CvnfcCustomization) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("modelCustomizationUUID", modelCustomizationUUID)
				.append("modelInstanceName", modelInstanceName).append("modelUUID", modelUUID)
				.append("modelInvariantUUID", modelInvariantUUID).append("modelVersion", modelVersion)
				.append("modelName", modelName).append("toscaNodeType", toscaNodeType)
				.append("description", description).append("nfcFunction", nfcFunction)
				.append("nfcNamingCode", nfcNamingCode).append("created", created)
				.append("vfModuleCustomization", vfModuleCustomization).append("vnfcCustomization", vnfcCustomization)
				.append("vnfResourceCustomization", vnfResourceCustomization)
				.append("vnfVfmoduleCvnfcConfigurationCustomization", vnfVfmoduleCvnfcConfigurationCustomization)
				.toString();
	}

	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}
	
	@LinkedResource
	public Set<VnfVfmoduleCvnfcConfigurationCustomization> getVnfVfmoduleCvnfcConfigurationCustomization() {
		return vnfVfmoduleCvnfcConfigurationCustomization;
	}

	public void setVnfVfmoduleCvnfcConfigurationCustomization(
			Set<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomization) {
		this.vnfVfmoduleCvnfcConfigurationCustomization = vnfVfmoduleCvnfcConfigurationCustomization;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNfcFunction() {
		return nfcFunction;
	}

	public void setNfcFunction(String nfcFunction) {
		this.nfcFunction = nfcFunction;
	}

	public String getNfcNamingCode() {
		return nfcNamingCode;
	}

	public void setNfcNamingCode(String nfcNamingCode) {
		this.nfcNamingCode = nfcNamingCode;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public VfModuleCustomization getVfModuleCustomization() {
		return vfModuleCustomization;
	}

	public void setVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
		this.vfModuleCustomization = vfModuleCustomization;
	}

	public VnfcCustomization getVnfcCustomization() {
		return vnfcCustomization;
	}

	public void setVnfcCustomization(VnfcCustomization vnfcCustomization) {
		this.vnfcCustomization = vnfcCustomization;
	}

	public VnfResourceCustomization getVnfResourceCustomization() {
		return vnfResourceCustomization;
	}

	public void setVnfResourceCustomization(VnfResourceCustomization vnfResourceCustomization) {
		this.vnfResourceCustomization = vnfResourceCustomization;
	}
}
