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

package org.onap.so.bpmn.infrastructure.adapter.network.tasks;

import java.util.Map;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.NetworkAdapterResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterCreateTasks {

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private NetworkAdapterObjectMapper networkAdapterObjectMapper;
    @Autowired
    private NetworkAdapterResources networkAdapterResources;

    public void createNetwork(BuildingBlockExecution execution) {
        execution.setVariable("networkAdapterCreateRollback", false);
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);

            Map<String, String> userInput = gBBInput.getUserInput();
            String cloudRegionPo = execution.getVariable("cloudRegionPo");

            CreateNetworkRequest createNetworkRequest = networkAdapterObjectMapper.createNetworkRequestMapper(
                    gBBInput.getRequestContext(), gBBInput.getCloudRegion(), gBBInput.getOrchContext(), serviceInstance,
                    l3Network, userInput, cloudRegionPo, gBBInput.getCustomer());

            execution.setVariable("networkAdapterRequest", createNetworkRequest.toXmlString());
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void processResponseFromOpenstack(BuildingBlockExecution execution) {
        try {
            L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);

            CreateNetworkResponse createNetworkResponse = execution.getVariable("createNetworkResponse");
            if (createNetworkResponse != null) {
                l3Network.setHeatStackId(createNetworkResponse.getNetworkStackId());
                if (createNetworkResponse.getNetworkCreated()) {
                    // setting rollback TRUE only if network was actually created (not a silent success OP)
                    execution.setVariable("networkAdapterCreateRollback", true);
                }
            } else {
                throw new Exception("No response was sent back from NetworkAdapter subflow.");
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    // TODO is this even used anywhere?
    public void rollbackCreateNetwork(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            CreateNetworkResponse createNetworkResponse = execution.getVariable("createNetworkResponse");

            Map<String, String> userInput = gBBInput.getUserInput();
            String cloudRegionPo = execution.getVariable("cloudRegionPo");
            networkAdapterResources.rollbackCreateNetwork(gBBInput.getRequestContext(), gBBInput.getCloudRegion(),
                    gBBInput.getOrchContext(), serviceInstance, l3Network, userInput, cloudRegionPo,
                    createNetworkResponse);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
