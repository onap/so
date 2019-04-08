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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import org.onap.so.serviceinstancebeans.RequestDetails;

public class ExecuteBuildingBlock implements Serializable {
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
    private Boolean aLaCarte;
    private Boolean homing;
    private WorkflowResourceIds workflowResourceIds;
    private RequestDetails requestDetails;
    private ConfigurationResourceKeys configurationResourceKeys;

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

    public Boolean isaLaCarte() {
        return aLaCarte;
    }

    public void setaLaCarte(Boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public Boolean isHoming() {
        return homing;
    }

    public void setHoming(Boolean homing) {
        this.homing = homing;
    }

    public WorkflowResourceIds getWorkflowResourceIds() {
        return workflowResourceIds;
    }

    public void setWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
        this.workflowResourceIds = workflowResourceIds;
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }

    public ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }

    public void setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
        this.configurationResourceKeys = configurationResourceKeys;
    }
}
