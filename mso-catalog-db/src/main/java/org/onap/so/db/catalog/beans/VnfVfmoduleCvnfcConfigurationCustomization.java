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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "vnf_vfmodule_cvnfc_configuration_customization")
public class VnfVfmoduleCvnfcConfigurationCustomization implements Serializable {

	private static final long serialVersionUID = -3153216266280581103L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@BusinessKey
	@Column(name = "MODEL_CUSTOMIZATION_UUID")
	private String modelCustomizationUUID;
	
	@Column(name = "MODEL_INSTANCE_NAME")
	private String modelInstanceName;	
	
	@Column(name = "CONFIGURATION_TYPE")
	private String configurationType;	
	
	@Column(name = "CONFIGURATION_ROLE")
	private String configurationRole;	

	@Column(name = "CONFIGURATION_FUNCTION")
	private String configurationFunction;	

	@Column(name = "POLICY_NAME")
	private String policyName;	
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "CONFIGURATION_MODEL_UUID")
	private ConfigurationResource configurationResource;
	
	@BusinessKey
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "CVNFC_MODEL_CUSTOMIZATION_UUID", referencedColumnName = "MODEL_CUSTOMIZATION_UUID")
	private CvnfcCustomization cvnfcCustomization;
	
	@BusinessKey
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "VF_MODULE_MODEL_CUSTOMIZATION_UUID")
	private VfModuleCustomization vfModuleCustomization;

	@BusinessKey
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID")
	private VnfResourceCustomization vnfResourceCustomization;

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VnfVfmoduleCvnfcConfigurationCustomization)) {
			return false;
		}
		VnfVfmoduleCvnfcConfigurationCustomization castOther = (VnfVfmoduleCvnfcConfigurationCustomization) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID)
				.append(cvnfcCustomization, castOther.cvnfcCustomization)
				.append(vfModuleCustomization, castOther.vfModuleCustomization)
				.append(vnfResourceCustomization, castOther.vnfResourceCustomization).isEquals();
	}



	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).append(cvnfcCustomization)
				.append(vfModuleCustomization).append(vnfResourceCustomization).toHashCode();
	}



	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("modelCustomizationUUID", modelCustomizationUUID)
				.append("modelInstanceName", modelInstanceName).append("configurationType", configurationType)
				.append("configurationRole", configurationRole).append("configurationFunction", configurationFunction)
				.append("policyName", policyName).append("created", created)
				.append("configurationResource", configurationResource).append("cvnfcCustomization", cvnfcCustomization)
				.append("vfModuleCustomization", vfModuleCustomization)
				.append("vnfResourceCustomization", vnfResourceCustomization).toString();
	}


	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
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

	public String getConfigurationType() {
		return configurationType;
	}

	public void setConfigurationType(String configurationType) {
		this.configurationType = configurationType;
	}

	public String getConfigurationRole() {
		return configurationRole;
	}

	public void setConfigurationRole(String configurationRole) {
		this.configurationRole = configurationRole;
	}

	public String getConfigurationFunction() {
		return configurationFunction;
	}

	public void setConfigurationFunction(String configurationFunction) {
		this.configurationFunction = configurationFunction;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public ConfigurationResource getConfigurationResource() {
		return configurationResource;
	}

	public void setConfigurationResource(ConfigurationResource configurationResource) {
		this.configurationResource = configurationResource;
	}

	public CvnfcCustomization getCvnfcCustomization() {
		return cvnfcCustomization;
	}

	public void setCvnfcCustomization(CvnfcCustomization cvnfcCustomization) {
		this.cvnfcCustomization = cvnfcCustomization;
	}

	public VfModuleCustomization getVfModuleCustomization() {
		return vfModuleCustomization;
	}

	public void setVfModuleCustomization(VfModuleCustomization vfModuleCustomization) {
		this.vfModuleCustomization = vfModuleCustomization;
	}

	public VnfResourceCustomization getVnfResourceCustomization() {
		return vnfResourceCustomization;
	}

	public void setVnfResourceCustomization(VnfResourceCustomization vnfResourceCustomization) {
		this.vnfResourceCustomization = vnfResourceCustomization;
	}
}
