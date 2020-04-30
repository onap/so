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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Optional;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.flowspecific.exceptions.UnassignNetworkException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnassignNetworkBB {

    private static final Logger logger = LoggerFactory.getLogger(UnassignNetworkBB.class);
    private static String messageCannotPerformUnassign =
            "Cannot perform Unassign Network. Network is still related to ";
    private static String messageErrorRollback = " Rollback is not possible. Please restore data manually.";

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    private NetworkBBUtils networkBBUtils;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private AAINetworkResources aaiNetworkResources;

    /**
     * BPMN access method to prepare overall error messages.
     * 
     * @param execution - BuildingBlockExecution
     * @param relatedToValue - String, ex: vf-module
     * @return void - nothing
     *
     */

    public void checkRelationshipRelatedTo(BuildingBlockExecution execution, String relatedToValue) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            AAIResultWrapper aaiResultWrapper = aaiNetworkResources.queryNetworkWrapperById(l3network);
            Optional<org.onap.aai.domain.yang.L3Network> network =
                    aaiResultWrapper.asBean(org.onap.aai.domain.yang.L3Network.class);
            if (networkBBUtils.isRelationshipRelatedToExists(network, relatedToValue)) {
                String msg = messageCannotPerformUnassign + relatedToValue;
                execution.setVariable("ErrorUnassignNetworkBB", msg);
                logger.error("ErrorUnassignNetworkBB: {}", msg);
                throw new UnassignNetworkException(msg);
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to getCloudRegionId
     * 
     * @param execution - BuildingBlockExecution
     * @return void - nothing
     *
     */

    public void getCloudSdncRegion(BuildingBlockExecution execution) {
        try {
            String cloudRegionSdnc = networkBBUtils.getCloudRegion(execution, SourceSystem.SDNC);
            execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to prepare overall error messages.
     * 
     * @param execution - BuildingBlockExecution
     * @return void - nothing
     */
    public void errorEncountered(BuildingBlockExecution execution) {
        String msg;
        boolean isRollbackNeeded =
                execution.getVariable("isRollbackNeeded") != null ? execution.getVariable("isRollbackNeeded") : false;
        if (isRollbackNeeded) {
            msg = execution.getVariable("ErrorUnassignNetworkBB") + messageErrorRollback;
        } else {
            msg = execution.getVariable("ErrorUnassignNetworkBB");
        }
        exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
    }


}
