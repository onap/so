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

import com.woorea.openstack.heat.model.Stack;
import java.util.HashMap;
import java.util.Map;

/*
 * This Java bean class relays Heat stack status information to ActiveVOS processes.
 *
 * This bean is returned by all Heat-specific adapter operations (create, query, delete)
 */
public class StackInfo {
	private String name = "";
	private String canonicalName = "";
	private HeatStatus status;
	private Map<String, Object> outputs = new HashMap<>();
	private Map<String,Object> parameters = new HashMap<>();
	static private Map<String, HeatStatus> heatStatusMap;

	static {
		heatStatusMap = new HashMap<>();
		heatStatusMap.put("CREATE_IN_PROGRESS", HeatStatus.BUILDING);
		heatStatusMap.put("CREATE_COMPLETE", HeatStatus.CREATED);
		heatStatusMap.put("CREATE_FAILED", HeatStatus.FAILED);
		heatStatusMap.put("DELETE_IN_PROGRESS", HeatStatus.DELETING);
		heatStatusMap.put("DELETE_COMPLETE", HeatStatus.NOTFOUND);
		heatStatusMap.put("DELETE_FAILED", HeatStatus.FAILED);
		heatStatusMap.put("UPDATE_IN_PROGRESS", HeatStatus.UPDATING);
		heatStatusMap.put("UPDATE_FAILED", HeatStatus.FAILED);
		heatStatusMap.put("UPDATE_COMPLETE", HeatStatus.UPDATED);
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
		} else if (heatStatusMap.containsKey(stack.getStackStatus())) {
			this.status = heatStatusMap.get(stack.getStackStatus());
		} else {
			this.status = HeatStatus.UNKNOWN;
		}
		if (stack.getOutputs() != null) {
			this.outputs = new HashMap<>();
			stack.getOutputs().forEach(output -> outputs.put(output.getOutputKey(), output.getOutputValue()));
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

	public HeatStatus getStatus() {
		return status;
	}

	public Map<String, Object> getOutputs() {
		return outputs;
	}

	public Map<String,Object> getParameters () {
		return parameters;
	}

}

