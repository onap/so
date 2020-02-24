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
 * For pnf, DelegateExecution is being used.
 *
 * @param - DelegateExecution
 */
@Component
public class GenericPnfCDSProcessingDE implements ControllerRunnable<DelegateExecution> {
    private static final Logger logger = LoggerFactory.getLogger(GenericPnfCDSProcessingDE.class);
    private static final String EXECUTION_OBJECT = "executionObject";
    private static final String ASSIGN_ACTION = "config-assign";
    private static final String DEPLOY_ACTION = "config-deploy";

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    @Autowired
    private AbstractCDSProcessingBBUtils cdsDispather;

    @Autowired
    private GeneratePayloadForCds generatePayloadForCds;

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        final String scope = context.getControllerScope();
        return PayloadConstants.CDS_ACTOR.equalsIgnoreCase(context.getControllerActor())
                && PayloadConstants.PNF_SCOPE.equalsIgnoreCase(scope)
                && !(ASSIGN_ACTION.equalsIgnoreCase(context.getControllerAction())
                        || DEPLOY_ACTION.equalsIgnoreCase(context.getControllerAction()));
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> context) {
        DelegateExecution delegateExecution = context.getExecution();
        try {
            AbstractCDSPropertiesBean abstractCDSPropertiesBean =
                    generatePayloadForCds.buildCdsPropertiesBean(delegateExecution);

            delegateExecution.setVariable(EXECUTION_OBJECT, abstractCDSPropertiesBean);

        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(delegateExecution, 7000, ex);
        }
    }

    @Override
    public void run(ControllerContext<DelegateExecution> context) {
        DelegateExecution obj = context.getExecution();
        cdsDispather.constructExecutionServiceInputObject(obj);
        cdsDispather.sendRequestToCDSClient(obj);
    }
}
