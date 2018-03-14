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

package org.openecomp.mso.bpmn.core.domain;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("vfModule")
public class ModuleResource  extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public ModuleResource(){
		resourceType = ResourceType.MODULE;
	}

	/*
	 * fields specific to VF Module resource type
	 */
	private String vfModuleName;
	private String vfModuleType;
	private String heatStackId;
	private boolean hasVolumeGroup;
	private boolean isBase;
	private String vfModuleLabel;
	private int initialCount;

	/*
	 * GET && SET
	 */
	public String getVfModuleName() {
		return vfModuleName;
	}
	public void setVfModuleName(String vfModuleName) {
		this.vfModuleName = vfModuleName;
	}
	public String getHeatStackId() {
		return heatStackId;
	}
	public void setHeatStackId(String heatStackId) {
		this.heatStackId = heatStackId;
	}
	public boolean getIsBase() {
		return isBase;
	}
	public void setIsBase(boolean isBase) {
		this.isBase = isBase;
	}
	public String getVfModuleLabel() {
		return vfModuleLabel;
	}
	public void setVfModuleLabel(String vfModuleLabel) {
		this.vfModuleLabel = vfModuleLabel;
	}
	public int getInitialCount() {
		return initialCount;
	}
	public void setInitialCount(int initialCount) {
		this.initialCount = initialCount;
	}
	public String getVfModuleType() {
		return vfModuleType;
	}
	public void setVfModuleType(String vfModuleType) {
		this.vfModuleType = vfModuleType;
	}
	public boolean isHasVolumeGroup() {
		return hasVolumeGroup;
	}
	public void setHasVolumeGroup(boolean hasVolumeGroup) {
		this.hasVolumeGroup = hasVolumeGroup;
	}

}