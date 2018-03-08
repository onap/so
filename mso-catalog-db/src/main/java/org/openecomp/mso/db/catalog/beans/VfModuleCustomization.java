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

public class VfModuleCustomization implements Serializable {
	
	private String modelCustomizationUuid;
	private String vfModuleModelUuid;
	private String label;
    private Integer minInstances;
    private Integer maxInstances;
    private Integer initialCount;
    private Integer availabilityZoneCount;
    private String heatEnvironmentArtifactUuid;
    private String volEnvironmentArtifactUuid;
    private Timestamp created;
    private VfModule vfModule;
    public static final long serialVersionUID = -1322322139926390329L;

	public VfModuleCustomization() {
		super();
	}
	
	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public String getVfModuleModelUuid() {
		return this.vfModuleModelUuid;
	}
	public void setVfModuleModelUuid(String vfModuleModelUuid) {
		this.vfModuleModelUuid = vfModuleModelUuid;
	}
	public String getHeatEnvironmentArtifactUuid() {
		return this.heatEnvironmentArtifactUuid;
	}
	public void setHeatEnvironmentArtifactUuid(String heatEnvironmentArtifactUuid) {
		this.heatEnvironmentArtifactUuid = heatEnvironmentArtifactUuid;
	}
	public String getVolEnvironmentArtifactUuid() {
		return this.volEnvironmentArtifactUuid;
	}
	public void setVolEnvironmentArtifactUuid(String volEnvironmentArtifactUuid) {
		this.volEnvironmentArtifactUuid = volEnvironmentArtifactUuid;
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
	public Timestamp getCreated() {
		return created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}
	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public VfModule getVfModule() {
		return this.vfModule;
	}
	public void setVfModule(VfModule vfModule) {
		this.vfModule = vfModule;
	}

	@Override
	public String toString() {
		return "modelCustomizationUuid=" + this.modelCustomizationUuid +
			"vfModuleModelUuid=" + this.vfModuleModelUuid +
			"label=" + this.label +
			"initalCount=" + this.initialCount +
			"minInstances=" + this.minInstances +
			"maxInstances=" + this.maxInstances +
			"availabilityZoneCount=" + this.availabilityZoneCount +
			"heatEnvironmentArtifactUuid=" + this.heatEnvironmentArtifactUuid +
			"volEnvironmentArtifactUuid=" + this.volEnvironmentArtifactUuid +
			"created=" + this.created;
	}

	@Override
    public boolean equals (Object o) {
        if (!(o instanceof VfModuleCustomization)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        VfModuleCustomization vfmc = (VfModuleCustomization) o;
        if (vfmc.getModelCustomizationUuid().equals(this.getModelCustomizationUuid()) && vfmc.getVfModuleModelUuid().equals(this.getVfModuleModelUuid())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode () {
        // hash code does not have to be a unique result - only that two objects that should be treated as equal
        // return the same value. so this should work.
        int result = 0;
        result = (this.modelCustomizationUuid != null ? this.modelCustomizationUuid.hashCode() : 0) + (this.vfModuleModelUuid != null ? this.vfModuleModelUuid.hashCode() : 0);
        return result;
    }

}
