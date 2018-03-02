package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartExecutionParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("workflow_id")
    private String workflowId;
    
    @JsonProperty("deployment_id")
    private String deploymentId;

    @JsonProperty("allow_custom_parameters")
    private boolean allowCustomParameters;

    @JsonProperty("force")
    private boolean force;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public boolean isAllowCustomParameters() {
		return allowCustomParameters;
	}

	public void setAllowCustomParameters(boolean allowCustomParameters) {
		this.allowCustomParameters = allowCustomParameters;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	@Override
    public String toString() {
        return "UpdateExecutionParams{" +
                "workflowId='" + workflowId + '\'' +
                "deploymentId='" + deploymentId + '\'' +
                "allowCustomParameters='" + allowCustomParameters + '\'' +
                "force='" + force + '\'' +
                "parameters='" + parameters + '\'' +
                '}';
    }

}
