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

    @Autowired
    public GenericCDSProcessing(ExtractPojosForBB extractPojosForBB, ExceptionBuilder exceptionBuilder) {
        this.exceptionBuilder = exceptionBuilder;
    }

    // Use this method for components which supports BuildingBlock.
    public void buildPayloadBasedOnScopeAndAction(BuildingBlockExecution execution) {
        try {
            GeneratePayloadForCds configurePayloadForCds = new GeneratePayloadForCds(execution);

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

            execution.setVariable("executionObject", abstractCDSPropertiesBean);

        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    // For PNF currently we do not have BuildingBlockExecution therefore we have to pass scope and action through BPMN
    // process.
    public void buildPayloadBasedOnScopeAndAction(DelegateExecution execution, String scope, String action) {
        try {

            GeneratePayloadForCds configurePayloadForCds = new GeneratePayloadForCds(execution, scope, action);

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = configurePayloadForCds.buildCdsPropertiesBean();

            execution.setVariable("executionObject", abstractCDSPropertiesBean);

        } catch (Exception ex) {
            logger.error("An exception occurred when creating payload for CDS request", ex);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
