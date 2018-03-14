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

package org.openecomp.mso.openstack.beans;


import java.util.Map;
import java.util.HashMap;

import com.woorea.openstack.heat.model.Stack;

/*
 * This Java bean class relays Heat stack status information to ActiveVOS processes.
 * 
 * This bean is returned by all Heat-specific adapter operations (create, query, delete)
 */

public class StackInfo {
	// Set defaults for everything
	private String name = "";
	private String canonicalName = "";
	private HeatStatus status = HeatStatus.UNKNOWN;
	private String statusMessage = "";
	private Map<String,Object> outputs = new HashMap<>();
	private Map<String,Object> parameters = new HashMap<>();
	
	static Map<String,HeatStatus> HeatStatusMap;
	static {
		HeatStatusMap = new HashMap<>();
		HeatStatusMap.put("CREATE_IN_PROGRESS", HeatStatus.BUILDING);
		HeatStatusMap.put("CREATE_COMPLETE", HeatStatus.CREATED);
		HeatStatusMap.put("CREATE_FAILED", HeatStatus.FAILED);
		HeatStatusMap.put("DELETE_IN_PROGRESS", HeatStatus.DELETING);
		HeatStatusMap.put("DELETE_COMPLETE", HeatStatus.NOTFOUND);
		HeatStatusMap.put("DELETE_FAILED", HeatStatus.FAILED);
		HeatStatusMap.put("UPDATE_IN_PROGRESS", HeatStatus.UPDATING);
		HeatStatusMap.put("UPDATE_FAILED", HeatStatus.FAILED);
		HeatStatusMap.put("UPDATE_COMPLETE", HeatStatus.UPDATED);
	}

	public StackInfo () {
	}
	
	public StackInfo (String name, HeatStatus status, String statusMessage, Map<String,Object> outputs) {
		this.name = name;
		this.canonicalName = name;	// Don't have an ID, so just use name

		this.status = status;
		if (statusMessage != null)  this.statusMessage = statusMessage;
		if (outputs != null)  this.outputs = outputs;
	}
	
	public StackInfo (String name, HeatStatus status) {
		this.name = name;
		this.canonicalName = name;	// Don't have an ID, so just use name
		this.status = status;
	}
	
	public StackInfo (Stack stack)
	{
		if (stack == null) {
			this.status = HeatStatus.NOTFOUND;
			return;
		}
	
		this.name = stack.getStackName();
		this.canonicalName = stack.getStackName() + "/" + stack.getId();

		if (stack.getStackStatus() == null) {
			this.status = HeatStatus.INIT;
		} else if (HeatStatusMap.containsKey(stack.getStackStatus())) {
			this.status = HeatStatusMap.get(stack.getStackStatus());
		} else {
			this.status = HeatStatusMap.getOrDefault(stack.getStackStatus(), HeatStatus.UNKNOWN);
		}
		
		this.statusMessage = stack.getStackStatusReason();
		
		if (stack.getOutputs() != null) {
			this.outputs = new HashMap<>();
			for (Stack.Output output : stack.getOutputs()) {
				this.outputs.put(output.getOutputKey(), output.getOutputValue());
			}
		}
		
		this.parameters = stack.getParameters();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName (String name) {
		this.name = name;
	}
	
	public String getCanonicalName() {
		return canonicalName;
	}
	
	public void setCanonicalName (String name) {
		this.canonicalName = name;
	}
	
	public HeatStatus getStatus() {
		return status;
	}
	
	public void setStatus (HeatStatus status) {
		this.status = status;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setStatusMessage (String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	public Map<String,Object> getOutputs () {
		return outputs;
	}
	
	public void setOutputs (Map<String,Object> outputs) {
		this.outputs = outputs;
	}
	
	public Map<String,Object> getParameters () {
		return parameters;
	}
	
	public void setParameters (Map<String,Object> parameters) {
		this.parameters = parameters;
	}
	
}

