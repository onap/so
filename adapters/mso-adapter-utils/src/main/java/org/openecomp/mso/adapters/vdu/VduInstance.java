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

package org.openecomp.mso.adapters.vdu;

import java.util.Map;
import java.util.HashMap;

/*
 * This Java bean class relays VDU status information in a cloud-agnostic format.
 * 
 * This bean is returned by all implementors of the VduPlugin interface operations
 * (instantiate, query, delete).
 */

public class VduInstance {
	// Set defaults for everything
	protected String vduInstanceId;
	protected String vduInstanceName;
	protected VduStatus status;
	protected Map<String, Object> outputs = new HashMap<String, Object>();
	protected Map<String, Object> inputs = new HashMap<String, Object>();

	public String getVduInstanceId() {
		return vduInstanceId;
	}

	public void setVduInstanceId(String vduInstanceId) {
		this.vduInstanceId = vduInstanceId;
	}

	public String getVduInstanceName() {
		return vduInstanceName;
	}

	public void setVduInstanceName(String vduInstanceName) {
		this.vduInstanceName = vduInstanceName;
	}

	public VduStatus getStatus() {
		return status;
	}

	public void setStatus(VduStatus status) {
		this.status = status;
	}

	public Map<String, Object> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<String, Object> outputs) {
		this.outputs = outputs;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Object> inputs) {
		this.inputs = inputs;
	}
}