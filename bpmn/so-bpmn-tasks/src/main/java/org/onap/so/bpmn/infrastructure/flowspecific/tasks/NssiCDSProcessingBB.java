/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used in context of Building Block flow for configuration of vnf/vfmodule/service.
 *
 * @param - BuildingBlockExecution
 */
@Component
public class NssiCDSProcessingBB implements ControllerRunnable<BuildingBlockExecution> {
    private static final Logger logger = LoggerFactory.getLogger(NssiCDSProcessingBB.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    public static final String CDS_ACTOR = "cds";
    public static final String NSSI_SCOPE = "nssi";

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    @Autowired
    private AbstractCDSProcessingBBUtils cdsDispather;

    @Autowired
    private GeneratePayloadForCds generatePayloadForCds;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        String scope = context.getControllerScope();
        return CDS_ACTOR.equalsIgnoreCase(context.getControllerActor()) && NSSI_SCOPE.equalsIgnoreCase(scope);
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        BuildingBlockExecution buildingBlockExecution = context.getExecution();
        try {
            AbstractCDSPropertiesBean abstractCDSPropertiesBean =
                    generatePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);
            buildingBlockExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);
        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, ex);
        }
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        BuildingBlockExecution obj = context.getExecution();
        cdsDispather.constructExecutionServiceInputObjectBB(obj);
        cdsDispather.sendRequestToCDSClientBB(obj);
    }
}
