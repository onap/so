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

package org.openecomp.mso.vdu.utils;

import java.util.Map;
import java.util.HashMap;

/*
 * This Java bean class relays VDU status information in a cloud-agnostic format.
 * 
 * This bean is returned by all implementors of the MsoVduUtils interface operations
 * (instantiate, query, delete).
 */

public class VduInfo {
	// Set defaults for everything
	private String vduInstanceId = "";
	private String vduInstanceName = "";
	private VduStatus status = VduStatus.NOTFOUND;
	private Map<String,Object> outputs = new HashMap<>();
	private Map<String,Object> inputs = new HashMap<>();
	private String lastAction;
	private String actionStatus;
	private String errorMessage;
	
	public VduInfo () {
	}

	// Add more constructors as appropriate
	//
	
	public VduInfo (String id, Map<String,Object> outputs) {
		this.vduInstanceId = id;
		if (outputs != null)  this.outputs = outputs;
	}
	
	public VduInfo (String id) {
		this.vduInstanceId = id;
	}
	
	public VduInfo (String id, VduStatus status) {
		this.vduInstanceId = id;
		this.status = status;
	}
	
	public String getVnfInstanceId() {
		return vduInstanceId;
	}
	
	public void setVnfInstanceId (String id) {
		this.vduInstanceId = id;
	}
	
	public String getVnfInstanceName() {
		return vduInstanceName;
	}
	
	public void setVnfInstanceName (String name) {
		this.vduInstanceName = name;
	}
	
	public VduStatus getStatus() {
		return status;
	}
	
	public void setStatus (VduStatus status) {
		this.status = status;
	}
	
	public Map<String,Object> getOutputs () {
		return outputs;
	}
	
	public void setOutputs (Map<String,Object> outputs) {
		this.outputs = outputs;
	}
	
	public Map<String,Object> getInputs () {
		return inputs;
	}
	
	public void setInputs (Map<String,Object> inputs) {
		this.inputs = inputs;
	}

	public String getLastAction() {
		return lastAction;
	}

	public String getActionStatus() {
		return actionStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
    public String toString() {
        return "VduInfo {" +
                "id='" + vduInstanceId + '\'' +
                "name='" + vduInstanceName + '\'' +
                ", inputs='" + inputs + '\'' +
                ", outputs='" + outputs + '\'' +
                ", lastAction='" + lastAction + '\'' +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}

