/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Map;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
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
public class CreateNetwork {


    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private NetworkAdapterObjectMapper networkAdapterObjectMapper;

    /**
     * BPMN access method to build CreateNetworkRequest object
     * 
     */
    public void buildCreateNetworkRequest(BuildingBlockExecution execution) throws Exception {
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

            // set CreateNetowrkRequest object on execution to be re-used within current BB
            execution.setVariable("createNetworkRequest", createNetworkRequest);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
