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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.openpojo.business.annotation.BusinessKey;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "vf_module_customization")
public class VfModuleCustomization implements Serializable {

	public static final long serialVersionUID = -1322322139926390329L;

	@BusinessKey
	@Id
	@Column(name = "MODEL_CUSTOMIZATION_UUID")
	private String modelCustomizationUUID;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "MIN_INSTANCES")
	private Integer minInstances;

	@Column(name = "MAX_INSTANCES")
	private Integer maxInstances;

	@Column(name = "INITIAL_COUNT")
	private Integer initialCount;

	@Column(name = "AVAILABILITY_ZONE_COUNT")
	private Integer availabilityZoneCount;

	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "VOL_ENVIRONMENT_ARTIFACT_UUID")
	HeatEnvironment volumeHeatEnv;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "HEAT_ENVIRONMENT_ARTIFACT_UUID")
	HeatEnvironment heatEnvironment;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "VF_MODULE_MODEL_UUID")
	private VfModule vfModule;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "modelCustomizationUUID")
	private Set<VnfcCustomization> vnfcCustomization;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "modelCustomizationUUID")
	private Set<CvnfcCustomization> cvnfcCustomization;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "modelCustomizationUUID")
	private Set<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomization;
	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID).append("label", label)
				.append("minInstances", minInstances).append("maxInstances", maxInstances)
				.append("initialCount", initialCount).append("availabilityZoneCount", availabilityZoneCount)
				.append("created", created).append("volumeHeatEnv", volumeHeatEnv)
				.append("heatEnvironment", heatEnvironment).append("vfModule", vfModule).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof VfModuleCustomization)) {
			return false;
		}
		VfModuleCustomization castOther = (VfModuleCustomization) other;
		return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
	}

	public VfModuleCustomization() {
		super();
	}

	public String getModelCustomizationUUID() {
		return this.modelCustomizationUUID;
	}

	public void setModelCustomizationUUID(String modelCustomizationUUID) {
		this.modelCustomizationUUID = modelCustomizationUUID;
	}

	@LinkedResource
	public HeatEnvironment getVolumeHeatEnv() {
		return volumeHeatEnv;
	}

	public void setVolumeHeatEnv(HeatEnvironment volumeHeatEnv) {
		this.volumeHeatEnv = volumeHeatEnv;
	}

	@LinkedResource
	public HeatEnvironment getHeatEnvironment() {
		return heatEnvironment;
	}

	public void setHeatEnvironment(HeatEnvironment heatEnvironment) {
		this.heatEnvironment = heatEnvironment;
	}

	public Integer getMinInstances() {
		return this.minInstances;
	}

	public void setMinInstances(Integer minInstances) {
		this.minInstances = minInstances;
	}

	public Integer getMaxInstances() {
		return this.maxInstances;
	}

	public void setMaxInstances(Integer maxInstances) {
		this.maxInstances = maxInstances;
	}

	public Integer getInitialCount() {
		return this.initialCount;
	}

	public void setInitialCount(Integer initialCount) {
		this.initialCount = initialCount;
	}

	public Integer getAvailabilityZoneCount() {
		return this.availabilityZoneCount;
	}

	public void setAvailabilityZoneCount(Integer availabilityZoneCount) {
		this.availabilityZoneCount = availabilityZoneCount;
	}

	public Date getCreated() {
		return created;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@LinkedResource
	public VfModule getVfModule() {
		return this.vfModule;
	}

	public void setVfModule(VfModule vfModule) {
		this.vfModule = vfModule;
	}
	
	@LinkedResource
	public Set<VnfVfmoduleCvnfcConfigurationCustomization> getVnfVfmoduleCvnfcConfigurationCustomization() {
		if (vnfVfmoduleCvnfcConfigurationCustomization == null)
			vnfVfmoduleCvnfcConfigurationCustomization = new HashSet<>();
		return vnfVfmoduleCvnfcConfigurationCustomization;
	}
	
	public void setVnfVfmoduleCvnfcConfigurationCustomization(
			Set<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomization) {
		this.vnfVfmoduleCvnfcConfigurationCustomization = vnfVfmoduleCvnfcConfigurationCustomization;
	}
	
	@LinkedResource
	public Set<VnfcCustomization> getVnfcCustomization() {
		return vnfcCustomization;
	}
	
	public void setVnfcCustomization(
			Set<VnfcCustomization> vnfcCustomization) {
		this.vnfcCustomization = vnfcCustomization;
	}
	
	@LinkedResource
	public Set<CvnfcCustomization> getCvnfcCustomization() {
		if (cvnfcCustomization == null)
			cvnfcCustomization = new HashSet<>();
		return cvnfcCustomization;
	}

	public void setCvnfcCustomization(Set<CvnfcCustomization> cvnfcCustomization) {
		this.cvnfcCustomization = cvnfcCustomization;
	}
}
