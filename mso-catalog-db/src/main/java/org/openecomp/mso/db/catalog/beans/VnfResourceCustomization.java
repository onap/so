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
package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;

public class VnfResourceCustomization extends MavenLikeVersioning implements Serializable {

	private static final long serialVersionUID = 768026109321305392L;
	
	private String modelCustomizationUuid = null;
	private String modelInstanceName = null;
	private Timestamp created = null;
	private String vnfResourceModelUuid = null;
	private String vnfResourceModelUUID = null;
	private Integer minInstances;
	private Integer maxInstances;
	private Integer availabilityZoneMaxCount;
	private VnfResource vnfResource;
	private String nfFunction = null;
	private String nfType = null;
	private String nfRole = null;
	private String nfNamingCode = null;
	private String multiStageDesign = null;
    private List<VfModuleCustomization> vfModuleCustomizations;
    private Set<ServiceToResourceCustomization> serviceResourceCustomizations;

	public VnfResourceCustomization() {
	}


	public String getModelCustomizationUuid() {
		return modelCustomizationUuid;
	}

	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}

	public String getModelInstanceName() {
		return this.modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}

	public Timestamp getCreationTimestamp() {
		return this.created;
	}
	public void setCreationTimestamp(Timestamp created) {
		this.created = created;
	}

	public String getVnfResourceModelUuid() {
		return this.vnfResourceModelUuid == null ? this.vnfResourceModelUUID : this.vnfResourceModelUuid;
	}
	public void setVnfResourceModelUuid(String vnfResourceModelUuid) {
		this.vnfResourceModelUuid = vnfResourceModelUuid;
	}
	public String getVnfResourceModelUUID() {
		return this.vnfResourceModelUUID;
	}
	public void setVnfResourceModelUUID(String vnfResourceModelUUID) {
		this.vnfResourceModelUUID = vnfResourceModelUUID;
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

	public Integer getAvailabilityZoneMaxCount() {
		return this.availabilityZoneMaxCount;
	}
	public void setAvailabilityZoneMaxCount(Integer availabilityZoneMaxCount) {
		this.availabilityZoneMaxCount = availabilityZoneMaxCount;
	}

	public VnfResource getVnfResource() {
		return this.vnfResource;
	}
	public void setVnfResource(VnfResource vnfResource) {
		this.vnfResource = vnfResource;
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

	public String getNfNamingCode() {
		return nfNamingCode;
	}

	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
	public String getMultiStageDesign() {
		return this.multiStageDesign;
	}
	public void setMultiStageDesign(String multiStageDesign) {
		this.multiStageDesign = multiStageDesign;
	}
	public List<VfModuleCustomization> getVfModuleCustomizations() {
		return this.vfModuleCustomizations;
	}
	public void setVfModuleCustomizations(List<VfModuleCustomization> vfModuleCustomizations) {
		this.vfModuleCustomizations = vfModuleCustomizations;
	}
	public void addVfModuleCustomization(VfModuleCustomization vfmc) {
		if (vfmc != null) {
			if (this.vfModuleCustomizations != null) {
				this.vfModuleCustomizations.add(vfmc);
			} else {
				this.vfModuleCustomizations = new ArrayList<>();
				this.vfModuleCustomizations.add(vfmc);
			}
		}
	}
	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Set<ServiceToResourceCustomization> getServiceResourceCustomizations() {
		return serviceResourceCustomizations;
	}

	public void setServiceResourceCustomizations(
			Set<ServiceToResourceCustomization> serviceResourceCustomizations) {
		this.serviceResourceCustomizations = serviceResourceCustomizations;
	}

	@Override
	public String toString() {
        return "VnfResourceCustomization: " + "ModelCustUuid=" + this.modelCustomizationUuid +
            ", ModelInstanceName=" + this.modelInstanceName +
            ", vnfResourceModelUuid=" + this.vnfResourceModelUUID +
            ", creationTimestamp=" + this.created +
            ", minInstances=" + this.minInstances +
            ", maxInstances=" + this.maxInstances +
            ", availabilityZoneMaxCount=" + this.availabilityZoneMaxCount +
            ", nfFunction=" + this.nfFunction +
            ", nfType=" + this.nfType +
            ", nfRole=" + this.nfRole +
            ", nfNamingCode=" + this.nfNamingCode +
            ", multiStageDesign=" + this.multiStageDesign;
	}

}
