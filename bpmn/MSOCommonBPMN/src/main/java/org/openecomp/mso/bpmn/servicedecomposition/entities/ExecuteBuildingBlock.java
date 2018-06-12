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

package org.openecomp.mso.bpmn.servicedecomposition.entities;

import java.io.Serializable;

public class ExecuteBuildingBlock implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BuildingBlock buildingBlock;
	private String requestId;
	private String apiVersion;
	private String resourceId;
	private String requestAction;
	private String vnfType;
	private boolean aLaCarte;
	private boolean homing;
	private WorkflowResourceIds workflowResourceIds;
	
	public BuildingBlock getBuildingBlock() {
		return buildingBlock;
	}
	public void setBuildingBlock(BuildingBlock buildingBlock) {
		this.buildingBlock = buildingBlock;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getRequestAction() {
		return requestAction;
	}
	public void setRequestAction(String requestAction) {
		this.requestAction = requestAction;
	}
	public boolean isaLaCarte() {
		return aLaCarte;
	}
	public void setaLaCarte(boolean aLaCarte) {
		this.aLaCarte = aLaCarte;
	}
	public String getVnfType() {
		return vnfType;
	}
	public void setVnfType(String vnfType) {
		this.vnfType = vnfType;
	}
	public boolean isHoming() {
		return homing;
	}
	public void setHoming(boolean homing) {
		this.homing = homing;
	}
	public WorkflowResourceIds getWorkflowResourceIds() {
		return workflowResourceIds;
	}
	public void setWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
		this.workflowResourceIds = workflowResourceIds;
	}
}
