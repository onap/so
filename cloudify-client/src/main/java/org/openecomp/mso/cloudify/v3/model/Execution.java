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

package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonRootName("execution")
public class Execution implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("blueprint_id")
    private String blueprintId;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("deployment_id")
    private String deploymentId;

    @JsonProperty("error")
    private String error;
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("is_system_workflow")
    private boolean isSystemWorkflow;
    
    @JsonProperty("parameters")
    private Map<String, Object> parameters;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("tenant_name")
    private String tenantName;
    
    @JsonProperty("workflow_id")
    private String workflowId;

	public String getBlueprintId() {
		return blueprintId;
	}

	public void setBlueprintId(String blueprintId) {
		this.blueprintId = blueprintId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSystemWorkflow() {
		return isSystemWorkflow;
	}

	public void setSystemWorkflow(boolean isSystemWorkflow) {
		this.isSystemWorkflow = isSystemWorkflow;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	@Override
    public String toString() {
        return "Execution{" +
                "id='" + id + '\'' +
                ", blueprintId='" + blueprintId + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", deploymentId='" + deploymentId + '\'' +
                ", error=" + error +
                ", isSystemWorkflow=" + isSystemWorkflow +
                ", status=" + status +
                ", tenantName='" + tenantName + '\'' +
                ", parameters=" + parameters +
                '}';
    }


}
