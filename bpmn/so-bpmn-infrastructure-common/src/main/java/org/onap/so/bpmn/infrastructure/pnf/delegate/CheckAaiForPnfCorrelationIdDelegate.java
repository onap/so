/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of "Check AAI for pnf_correlation_id" task in CreateAndActivatePnfResource.bpmn
 *
 * Inputs: - pnfCorrelationId - String
 *
 * Outputs: - aaiContainsInfoAboutPnf - local Boolean
 */
@Component
public class CheckAaiForPnfCorrelationIdDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CheckAaiForPnfCorrelationIdDelegate.class);

    private PnfManagement pnfManagement;

    @Autowired
    public void setPnfManagement(PnfManagement pnfManagement) {
        this.pnfManagement = pnfManagement;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(PNF_CORRELATION_ID);
        if (pnfCorrelationId == null) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 500, PNF_CORRELATION_ID + " is not set");
        }
        try {
            boolean isEntry = pnfManagement.getEntryFor(pnfCorrelationId).isPresent();
            logger.debug("AAI entry is found for pnf correlation id {}: {}", PNF_CORRELATION_ID, isEntry);
            execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, isEntry);
        } catch (IOException e) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, e.getMessage());
        }
    }
}
