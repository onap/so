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

    private static final long serialVersionUID = 2L;
    private BuildingBlock buildingBlock;
    private String requestId;
    private String apiVersion;
    private String resourceId;
    private String requestAction;
    private String vnfType;
    private Boolean aLaCarte;
    private Boolean homing = false;
    private WorkflowResourceIds workflowResourceIds;
    private RequestDetails requestDetails;
    private ConfigurationResourceKeys configurationResourceKeys;

    public BuildingBlock getBuildingBlock() {
        return buildingBlock;
    }

    public ExecuteBuildingBlock setBuildingBlock(BuildingBlock buildingBlock) {
        this.buildingBlock = buildingBlock;
        return this;
    }

    public String getRequestId() {
        return requestId;
    }

    public ExecuteBuildingBlock setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public ExecuteBuildingBlock setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public ExecuteBuildingBlock setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public ExecuteBuildingBlock setRequestAction(String requestAction) {
        this.requestAction = requestAction;
        return this;
    }

    public Boolean isaLaCarte() {
        return aLaCarte;
    }

    public ExecuteBuildingBlock setaLaCarte(Boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
        return this;
    }

    public String getVnfType() {
        return vnfType;
    }

    public ExecuteBuildingBlock setVnfType(String vnfType) {
        this.vnfType = vnfType;
        return this;
    }

    public Boolean isHoming() {
        return homing;
    }

    public ExecuteBuildingBlock setHoming(Boolean homing) {
        this.homing = homing;
        return this;
    }

    public WorkflowResourceIds getWorkflowResourceIds() {
        return workflowResourceIds;
    }

    public ExecuteBuildingBlock setWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
        this.workflowResourceIds = workflowResourceIds;
        return this;
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public ExecuteBuildingBlock setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
        return this;
    }

    public ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }

    public ExecuteBuildingBlock setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
        this.configurationResourceKeys = configurationResourceKeys;
        return this;
    }
}
