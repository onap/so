/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
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

/**
 * For pnf DelegateExecution is being used.
 *
 * @param - DelegateExecution
 */
@Component
public class GenericCDSProcessingDE implements ControllerRunnable<DelegateExecution> {
    private static final Logger logger = LoggerFactory.getLogger(GenericCDSProcessingDE.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String CDS_ACTOR = "cds";
    private static final String PNF_SCOPE = "pnf";
    private final ExceptionBuilder exceptionBuilder;
    private final AbstractCDSProcessingBBUtils dispatcher;

    @Autowired
    public GenericCDSProcessingDE(ExceptionBuilder exceptionBuilder, AbstractCDSProcessingBBUtils cdsDispather) {
        this.exceptionBuilder = exceptionBuilder;
        this.dispatcher = cdsDispather;
    }

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        String scope = context.getControllerScope();
        return CDS_ACTOR.equalsIgnoreCase(context.getControllerActor()) && PNF_SCOPE.equalsIgnoreCase(scope);
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> context) {
        DelegateExecution delegateExecution = context.getExecution();
        try {
            String action = String.valueOf(delegateExecution.getVariable(PayloadConstants.ACTION));
            String scope = String.valueOf(delegateExecution.getVariable(PayloadConstants.SCOPE));
            GeneratePayloadForCds configurePayloadForCds = new GeneratePayloadForCds(delegateExecution, scope, action);

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

            delegateExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);

        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(delegateExecution, 7000, ex);
        }
    }

    @Override
    public void run(ControllerContext<DelegateExecution> context) {
        DelegateExecution obj = context.getExecution();
        dispatcher.constructExecutionServiceInputObject(obj);
        dispatcher.sendRequestToCDSClient(obj);
    }
}
