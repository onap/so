/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlockBase;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import java.io.Serializable;
import java.util.List;

public class ConfigBuildingBlocksDataObject extends BuildingBlockBase<ConfigBuildingBlocksDataObject>
        implements Serializable {

    private static final long serialVersionUID = 3L;
    private DelegateExecution execution;
    private List<OrchestrationFlow> orchFlows;
    private Resource resourceKey;
    private ServiceInstancesRequest sIRequest;
    private ReplaceInstanceRelatedInformation replaceInformation;

    public ServiceInstancesRequest getsIRequest() {
        return sIRequest;
    }

    public ConfigBuildingBlocksDataObject setsIRequest(ServiceInstancesRequest sIRequest) {
        this.sIRequest = sIRequest;
        return this;
    }

    public List<OrchestrationFlow> getOrchFlows() {
        return orchFlows;
    }

    public ConfigBuildingBlocksDataObject setOrchFlows(List<OrchestrationFlow> orchFlows) {
        this.orchFlows = orchFlows;
        return this;
    }

    public Resource getResourceKey() {
        return resourceKey;
    }

    public ConfigBuildingBlocksDataObject setResourceKey(Resource resourceKey) {
        this.resourceKey = resourceKey;
        return this;
    }

    public DelegateExecution getExecution() {
        return execution;
    }

    public ConfigBuildingBlocksDataObject setExecution(DelegateExecution execution) {
        this.execution = execution;
        return this;
    }

    public ReplaceInstanceRelatedInformation getReplaceInformation() {
        return replaceInformation;
    }

    public ConfigBuildingBlocksDataObject setReplaceInformation(ReplaceInstanceRelatedInformation replaceInformation) {
        this.replaceInformation = replaceInformation;
        return this;
    }



}
