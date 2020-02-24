/*
 * ============LICENSE_START======================================================= Copyright (C) 2020 Nokia. All rights
 * reserved. ================================================================================ Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericPnfCDSProcessingBB implements ControllerRunnable<BuildingBlockExecution> {

    private static final Logger logger = LoggerFactory.getLogger(GenericPnfCDSProcessingBB.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String ASSIGN_ACTION = "config-assign";
    private static final String DEPLOY_ACTION = "config-deploy";

    private ExceptionBuilder exceptionBuilder;
    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;
    private GeneratePayloadForCds generatePayloadForCds;

    @Autowired
    public GenericPnfCDSProcessingBB(ExceptionBuilder exceptionBuilder,
            AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils, GeneratePayloadForCds generatePayloadForCds) {
        this.exceptionBuilder = exceptionBuilder;
        this.abstractCDSProcessingBBUtils = abstractCDSProcessingBBUtils;
        this.generatePayloadForCds = generatePayloadForCds;
    }

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> controllerContext) {
        return PayloadConstants.CDS_ACTOR.equalsIgnoreCase(controllerContext.getControllerActor())
                && PayloadConstants.PNF_SCOPE.equalsIgnoreCase(controllerContext.getControllerScope())
                && !(ASSIGN_ACTION.equalsIgnoreCase(controllerContext.getControllerAction())
                        || DEPLOY_ACTION.equalsIgnoreCase(controllerContext.getControllerAction()));
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> controllerContext) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> controllerContext) {
        BuildingBlockExecution buildingBlockExecution = controllerContext.getExecution();
        try {
            AbstractCDSPropertiesBean abstractCDSPropertiesBean =
                    generatePayloadForCds.buildCdsPropertiesBean(buildingBlockExecution);
            buildingBlockExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);
        } catch (Exception exception) {
            logger.error("An exception occurred when creating payload for CDS request", exception);
            exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, exception);
        }
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> controllerContext) {
        BuildingBlockExecution buildingBlockExecution = controllerContext.getExecution();
        abstractCDSProcessingBBUtils.constructExecutionServiceInputObject(buildingBlockExecution);
        abstractCDSProcessingBBUtils.sendRequestToCDSClient(buildingBlockExecution);
    }
}
