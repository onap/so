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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.onap.so.serviceinstancebeans.RequestDetails;

@JsonDeserialize(builder = ExecuteBuildingBlock.Builder.class)
public final class ExecuteBuildingBlock implements Serializable {

    private final BuildingBlock buildingBlock;
    private final String requestId;
    private final String apiVersion;
    private final String resourceId;
    private final String requestAction;
    private final String vnfType;
    private final Boolean aLaCarte;
    private final Boolean homing;
    private final WorkflowResourceIds workflowResourceIds;
    private final RequestDetails requestDetails;
    private final ConfigurationResourceKeys configurationResourceKeys;

    private ExecuteBuildingBlock(Builder builder) {
        this.buildingBlock = builder.buildingBlock;
        this.requestId = builder.requestId;
        this.apiVersion = builder.apiVersion;
        this.resourceId = builder.resourceId;
        this.requestAction = builder.requestAction;
        this.vnfType = builder.vnfType;
        this.aLaCarte = builder.aLaCarte;
        this.homing = builder.homing;
        this.workflowResourceIds = builder.workflowResourceIds;
        this.requestDetails = builder.requestDetails;
        this.configurationResourceKeys = builder.configurationResourceKeys;
    }

    public BuildingBlock getBuildingBlock() {
        return buildingBlock;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public Boolean isaLaCarte() {
        return aLaCarte;
    }

    public String getVnfType() {
        return vnfType;
    }

    public Boolean isHoming() {
        return homing;
    }

    public WorkflowResourceIds getWorkflowResourceIds() {
        return workflowResourceIds;
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }

    public ExecuteBuildingBlock copyExecuteBuildingBlock(WorkflowResourceIds workflowResourceIds) {
        return new ExecuteBuildingBlock.Builder().withWorkflowResourceIds(workflowResourceIds)
                .withBuildingBlock(buildingBlock).withRequestId(requestId).withApiVersion(apiVersion)
                .withResourceId(resourceId).withRequestAction(requestAction).withVnfType(vnfType).withaLaCarte(aLaCarte)
                .withHoming(homing).withRequestDetails(requestDetails)
                .withConfigurationResourceKeys(configurationResourceKeys).build();
    }

    public ExecuteBuildingBlock copyExecuteBuildingBlock(BuildingBlock buildingBlock) {
        return new ExecuteBuildingBlock.Builder().withBuildingBlock(buildingBlock)
                .withWorkflowResourceIds(workflowResourceIds).withRequestId(requestId).withApiVersion(apiVersion)
                .withResourceId(resourceId).withRequestAction(requestAction).withVnfType(vnfType).withaLaCarte(aLaCarte)
                .withHoming(homing).withRequestDetails(requestDetails)
                .withConfigurationResourceKeys(configurationResourceKeys).build();
    }

    public ExecuteBuildingBlock copyExecuteBuildingBlock(Boolean homing) {
        return new ExecuteBuildingBlock.Builder().withHoming(homing).withBuildingBlock(buildingBlock)
                .withWorkflowResourceIds(workflowResourceIds).withRequestId(requestId).withApiVersion(apiVersion)
                .withResourceId(resourceId).withRequestAction(requestAction).withVnfType(vnfType).withaLaCarte(aLaCarte)
                .withRequestDetails(requestDetails).withConfigurationResourceKeys(configurationResourceKeys).build();
    }

    @JsonPOJOBuilder
    public static class Builder {
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


        public Builder withBuildingBlock(BuildingBlock buildingBlock) {
            this.buildingBlock = buildingBlock;
            return this;
        }

        public Builder withRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder withResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder withRequestAction(String requestAction) {
            this.requestAction = requestAction;
            return this;
        }

        public Builder withVnfType(String vnfType) {
            this.vnfType = vnfType;
            return this;
        }

        public Builder withaLaCarte(Boolean aLaCarte) {
            this.aLaCarte = aLaCarte;
            return this;
        }

        public Builder withHoming(Boolean homing) {
            this.homing = homing;
            return this;
        }

        public Builder withWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
            this.workflowResourceIds = workflowResourceIds;
            return this;
        }

        public Builder withRequestDetails(RequestDetails requestDetails) {
            this.requestDetails = requestDetails;
            return this;
        }

        public Builder withConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
            this.configurationResourceKeys = configurationResourceKeys;
            return this;
        }

        public ExecuteBuildingBlock build() {
            return new ExecuteBuildingBlock(this);
        }

    }
}
