/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

public class Resource {

	private String resourceId;
	private WorkflowType resourceType;
	private boolean generated;
	private boolean baseVfModule;
	
	public Resource(WorkflowType resourceType, String resourceId, boolean generated){
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.generated = generated;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public WorkflowType getResourceType() {
		return resourceType;
	}
	public void setResourceType(WorkflowType resourceType) {
		this.resourceType = resourceType;
	}
	public boolean isGenerated() {
		return generated;
	}
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
	public boolean isBaseVfModule() {
		return baseVfModule;
	}
	public void setBaseVfModule(boolean baseVfModule) {
		this.baseVfModule = baseVfModule;
	}
}
