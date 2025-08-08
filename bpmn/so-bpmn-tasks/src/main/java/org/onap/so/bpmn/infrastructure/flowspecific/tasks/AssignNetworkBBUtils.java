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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.SDNCNetworkResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignNetworkBBUtils {

    private static final Logger logger = LoggerFactory.getLogger(AssignNetworkBBUtils.class);
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private AAINetworkResources aaiNetworkResources;
    @Autowired
    private SDNCNetworkResources sdncNetworkResources;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * BPMN access method. Verify if network name was provided as input to BB
     * 
     * @param execution
     * @return
     */
    public boolean networkFoundByName(BuildingBlockExecution execution) {

        // TODO - populate logic after iTrack MSO-2143 implemented
        return false;
    }

    /**
     * BPMN access method to extract Cloud Region data
     * 
     * @param execution
     * @throws Exception
     */
    public void getCloudRegion(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            String cloudRegionSdnc;
            String cloudRegionPo = cloudRegion.getLcpCloudRegionId();
            if ("2.5".equalsIgnoreCase(cloudRegion.getCloudRegionVersion())) {
                cloudRegionSdnc = "AAIAIC25";
            } else {
                cloudRegionSdnc = cloudRegionPo;
            }
            execution.setVariable("cloudRegionPo", cloudRegionPo);
            execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method. Process silent success scenario
     * 
     * @param execution
     */
    public void processSilentSuccess(BuildingBlockExecution execution) {
        String msg = "Silent success processing network assign";
        logger.info(msg);
    }

    /**
     * BPMN access method. Process silent success scenario
     * 
     * @param execution
     */
    public void failOrchestrationStatus(BuildingBlockExecution execution) {
        BpmnError error = new BpmnError("Failed orchestration status verificaiton");
        exceptionUtil.buildAndThrowWorkflowException(execution, 7000, error);
    }
}
