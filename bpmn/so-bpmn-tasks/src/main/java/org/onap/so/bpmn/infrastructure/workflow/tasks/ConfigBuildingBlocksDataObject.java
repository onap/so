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
import java.util.List;

public class ConfigBuildingBlocksDataObject extends BuildingBlockBase {

    private static final long serialVersionUID = 1L;
    private DelegateExecution execution;
    private List<OrchestrationFlow> orchFlows;
    private Resource resourceKey;
    private ServiceInstancesRequest sIRequest;

    public ServiceInstancesRequest getsIRequest() {
        return sIRequest;
    }

    public void setsIRequest(ServiceInstancesRequest sIRequest) {
        this.sIRequest = sIRequest;
    }

    public List<OrchestrationFlow> getOrchFlows() {
        return orchFlows;
    }

    public void setOrchFlows(List<OrchestrationFlow> orchFlows) {
        this.orchFlows = orchFlows;
    }

    public Resource getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(Resource resourceKey) {
        this.resourceKey = resourceKey;
    }

    public DelegateExecution getExecution() {
        return execution;
    }

    public void setExecution(DelegateExecution execution) {
        this.execution = execution;
    }
}
