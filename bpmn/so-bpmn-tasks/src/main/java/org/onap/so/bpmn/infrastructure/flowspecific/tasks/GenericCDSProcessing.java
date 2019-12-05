/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.PayloadConstants;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * For Vnf/Vf-Module/Service we are using BuildingBlockExecution and for Pnf we are using
 *
 * @param <T> - BuildingBlockExeution or DelegateExecutionBuildingBlock
 */
@Component
public class GenericCDSProcessing<T> implements ControllerRunnable<T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericCDSProcessing.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    private final ExceptionBuilder exceptionBuilder;
    private final ExtractPojosForBB extractPojosForBB;
    private static final String CDS_ACTOR = "cds";

    @Autowired
    public GenericCDSProcessing(ExceptionBuilder exceptionBuilder, ExtractPojosForBB extractPojosForBB) {
        this.exceptionBuilder = exceptionBuilder;
        this.extractPojosForBB = extractPojosForBB;
    }

    @Override
    public Boolean understand(ControllerContext<T> context) {
        return context.getControllerActor().equalsIgnoreCase(CDS_ACTOR);
    }

    @Override
    public Boolean ready(ControllerContext<T> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<T> context) {
        // At the moment not sure whats the purpose of prepare in the context of this class.
        // Henry can you provide any feedback on this ?
    }

    @Override
    public void run(ControllerContext<T> context) {
        if (context.getExecution() instanceof DelegateExecution) {
            DelegateExecution delegateExecution = (DelegateExecution) context.getExecution();
            try {
                String action = String.valueOf(delegateExecution.getVariable(PayloadConstants.ACTION));
                String scope = String.valueOf(delegateExecution.getVariable(PayloadConstants.SCOPE));
                GeneratePayloadForCds configurePayloadForCds =
                        new GeneratePayloadForCds(delegateExecution, scope, action);

                AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

                delegateExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);

            } catch (Exception ex) {
                logger.error("An exception occurred when creating payload for CDS request", ex);
                exceptionBuilder.buildAndThrowWorkflowException(delegateExecution, 7000, ex);
            }
        } else {
            BuildingBlockExecution buildingBlockExecution = (BuildingBlockExecution) context.getExecution();
            try {
                GeneratePayloadForCds configurePayloadForCds =
                        new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);

                AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

                buildingBlockExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);

            } catch (Exception ex) {
                logger.error("An exception occurred when creating payload for CDS request", ex);
                exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, ex);
            }
        }
    }
}
