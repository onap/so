/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterUpdateTasks {

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private NetworkAdapterObjectMapper networkAdapterObjectMapper;
    @Autowired
    private ExceptionBuilder exceptionUtil;

    public void updateNetwork(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);

            UpdateNetworkRequest updateNetworkRequest = networkAdapterObjectMapper.createNetworkUpdateRequestMapper(
                    gBBInput.getRequestContext(), gBBInput.getCloudRegion(), gBBInput.getOrchContext(), serviceInstance,
                    l3Network, gBBInput.getUserInput(), gBBInput.getCustomer());
            execution.setVariable("networkAdapterRequest", updateNetworkRequest.toXmlString());

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void processResponseFromOpenstack(BuildingBlockExecution execution) {
        try {
            UpdateNetworkResponse updateNetworkResponse = execution.getVariable("updateNetworkResponse");
            if (updateNetworkResponse == null) {
                throw new Exception("No response was sent back from NetworkAdapterRestV1 subflow.");
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
