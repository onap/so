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
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericCDSProcessing {

    private static final Logger logger = LoggerFactory.getLogger(GenericCDSProcessing.class);
    private final ExceptionBuilder exceptionBuilder;
    private final ExtractPojosForBB extractPojosForBB;

    @Autowired
    public GenericCDSProcessing(ExceptionBuilder exceptionBuilder, ExtractPojosForBB extractPojosForBB) {
        this.exceptionBuilder = exceptionBuilder;
        this.extractPojosForBB = extractPojosForBB;
    }

    // For PNF currently we do not have BuildingBlockExecution therefore we have to pass scope and action through BPMN
    // process.
    public <T> void buildPayloadBasedOnScopeAndAction(T execution) {

        if (execution instanceof DelegateExecution) {
            DelegateExecution delegateExecution = (DelegateExecution) execution;
            try {
                Object action = delegateExecution.getVariable("action");
                Object scope = delegateExecution.getVariable("scope");
                GeneratePayloadForCds configurePayloadForCds = new GeneratePayloadForCds((DelegateExecution) delegateExecution, String.valueOf(scope), String.valueOf(action));

                AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

                delegateExecution.setVariable("executionObject", abstractCDSPropertiesBean);

            } catch (Exception ex) {
                logger.error("An exception occurred when creating payload for CDS request", ex);
                exceptionBuilder.buildAndThrowWorkflowException(delegateExecution, 7000, ex);
            }
        }
        else{
            BuildingBlockExecution buildingBlockExecution = (BuildingBlockExecution) execution;
            try {
                GeneratePayloadForCds configurePayloadForCds =
                        new GeneratePayloadForCds(buildingBlockExecution, extractPojosForBB);

                AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

                buildingBlockExecution.setVariable("executionObject", abstractCDSPropertiesBean);

            } catch (Exception ex) {
                logger.error("An exception occurred when creating payload for CDS request", ex);
                exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, ex);
            }
        }
    }
}
