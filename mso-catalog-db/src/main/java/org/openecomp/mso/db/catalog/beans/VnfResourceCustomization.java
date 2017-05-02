/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import java.sql.Timestamp;

public class VnfResourceCustomization {

	private String modelCustomizationUuid;
	private String modelInstanceName;
	private Integer ecompHomed;
	private String homingPolicy;
	private Timestamp creationTimestamp;
	private String vnfResourceModelUuid;
	private Integer minInstances;
	private Integer maxInstances;
	private Integer availabilityZoneMaxCount;
	private VnfResource vnfResource;

	public VnfResourceCustomization() {
	}

	public String getModelCustomizationUuid() {
		return this.modelCustomizationUuid;
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

	public Integer getEcompHomed() {
		return this.ecompHomed;
	}
	public void setEcompHomed(Integer ecompHomed) {
		this.ecompHomed = ecompHomed;
	}

	public String getHomingPolicy() {
		return this.homingPolicy;
	}
	public void setHomingPolicy(String homingPolicy) {
		this.homingPolicy = homingPolicy;
	}

	public Timestamp getCreationTimestamp() {
		return this.creationTimestamp;
	}
	public void setCreationTimestamp(Timestamp creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public String getVnfResourceModelUuid() {
		return this.vnfResourceModelUuid;
	}
	public void setVnfResourceModelUuid(String vnfResourceModelUuid) {
		this.vnfResourceModelUuid = vnfResourceModelUuid;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("VnfResourceCustomization: ");
		sb.append("ModelCustUuid=" + this.modelCustomizationUuid);
		sb.append(", ModelInstanceName=" + this.modelInstanceName);
		sb.append(", ecompHomed=" + this.ecompHomed);
		sb.append(", homingPolicy=" + this.homingPolicy);
		sb.append(", vnfResourceModelUuid=" + this.vnfResourceModelUuid);
		sb.append(", creationTimestamp=" + this.creationTimestamp);
		sb.append(", minInstances=" + this.minInstances);
		sb.append(", maxInstances=" + this.maxInstances);
		sb.append(", availabilityZoneMaxCount=" + this.availabilityZoneMaxCount);
		sb.append(", vnfResource:\n" + this.vnfResource.toString());
		return sb.toString();
	}



}
