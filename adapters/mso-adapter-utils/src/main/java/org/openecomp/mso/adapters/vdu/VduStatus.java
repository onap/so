/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

public class VduStatus {
	
	private VduStateType state;	
	private String errorMessage;
	private PluginAction lastAction;	
	
	public VduStateType getState() {
		return state;
	}
	public void setState(VduStateType state) {
		this.state = state;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public PluginAction getLastAction() {
		return lastAction;
	}
	public void setLastAction(PluginAction lastAction) {
		this.lastAction = lastAction;
	}
	public void setLastAction (String action, String status, String rawCloudMessage) {
		lastAction = new PluginAction();
		lastAction.setAction (action);
		lastAction.setStatus (status);
		lastAction.setRawMessage(rawCloudMessage);
	}
	
}