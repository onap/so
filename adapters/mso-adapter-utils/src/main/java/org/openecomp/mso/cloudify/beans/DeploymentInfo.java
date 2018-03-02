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

package org.openecomp.mso.cloudify.beans;

import java.util.Map;
import java.util.HashMap;

import org.openecomp.mso.cloudify.v3.model.Deployment;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;
import org.openecomp.mso.cloudify.v3.model.Execution;

/*
 * This Java bean class relays Heat stack status information to ActiveVOS processes.
 * 
 * This bean is returned by all Heat-specific adapter operations (create, query, delete)
 */

public class DeploymentInfo {
	// Set defaults for everything
	private String id = "";
	private DeploymentStatus status = DeploymentStatus.NOTFOUND;
	private Map<String,Object> outputs = new HashMap<String,Object>();
	private Map<String,Object> inputs = new HashMap<String,Object>();
	private String lastAction;
	private String actionStatus;
	private String errorMessage;
	
	public DeploymentInfo () {
	}
	
	public DeploymentInfo (String id, Map<String,Object> outputs) {
		this.id = id;
		if (outputs != null)  this.outputs = outputs;
	}
	
	public DeploymentInfo (String id) {
		this.id = id;
	}
	
	public DeploymentInfo (String id, DeploymentStatus status) {
		this.id = id;
		this.status = status;
	}

	public DeploymentInfo (Deployment deployment) {
		this(deployment, null, null);
	}

	/**
	 * Construct a DeploymentInfo object from a deployment and the latest Execution action
	 * @param deployment
	 * @param execution
	 */
	public DeploymentInfo (Deployment deployment, DeploymentOutputs outputs, Execution execution)
	{
		if (deployment == null) {
			this.id = null;
			return;
		}
	
		this.id = deployment.getId();

		if (outputs != null)
			this.outputs = outputs.getOutputs();
		
		if (deployment.getInputs() != null)
			this.inputs = deployment.getInputs();
		
		if (execution != null) {
			this.lastAction = execution.getWorkflowId();
			this.actionStatus = execution.getStatus();
			this.errorMessage = execution.getError();
			
			// Compute the status based on the last workflow
			if (lastAction.equals("install")) {
				if (actionStatus.equals("terminated"))
					this.status = DeploymentStatus.INSTALLED;
				else if (actionStatus.equals("failed"))
					this.status = DeploymentStatus.FAILED;
				else if (actionStatus.equals("started") || actionStatus.equals("pending"))
					this.status = DeploymentStatus.INSTALLING;
				else
					this.status = DeploymentStatus.UNKNOWN;
			}
			else if (lastAction.equals("uninstall")) {
				if (actionStatus.equals("terminated"))
					this.status = DeploymentStatus.CREATED;
				else if (actionStatus.equals("failed"))
					this.status = DeploymentStatus.FAILED;
				else if (actionStatus.equals("started") || actionStatus.equals("pending"))
					this.status = DeploymentStatus.UNINSTALLING;
				else
					this.status = DeploymentStatus.UNKNOWN;
			}
			else {
				// Could have more cases in the future for different actions.
				this.status = DeploymentStatus.UNKNOWN;
			}
		}
		else {
			this.status = DeploymentStatus.CREATED;
		}
	}
	
	public String getId() {
		return id;
	}
	
	public void setId (String id) {
		this.id = id;
	}
	
	public DeploymentStatus getStatus() {
		return status;
	}
	
	public void setStatus (DeploymentStatus status) {
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

	public void saveExecutionStatus (Execution execution) {
		this.lastAction = execution.getWorkflowId();
		this.actionStatus = execution.getStatus();
		this.errorMessage = execution.getError();
	}
	
	@Override
    public String toString() {
        return "DeploymentInfo {" +
                "id='" + id + '\'' +
                ", inputs='" + inputs + '\'' +
                ", outputs='" + outputs + '\'' +
                ", lastAction='" + lastAction + '\'' +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}

