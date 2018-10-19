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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

import java.io.IOException;

import com.google.common.base.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.onap.so.bpmn.infrastructure.pnf.implementation.AaiConnection;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of "Check AAI for correlation_id" task in CreateAndActivatePnfResource.bpmn
 *
 * Inputs:
 * - correlationId - String
 *
 * Outputs:
 * - aaiContainsInfoAboutPnf - local Boolean
 */

public class CheckAaiForCorrelationIdDelegate implements JavaDelegate {
	private static MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, CheckAaiForCorrelationIdDelegate.class);
    private AaiConnection aaiConnection;

    @Autowired
    public void setAaiConnection(AaiConnection aaiConnection) {
        this.aaiConnection = aaiConnection;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String correlationId = (String) execution.getVariable(CORRELATION_ID);
        if (Strings.isNullOrEmpty(correlationId)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 500, CORRELATION_ID + " is not set");
        }
        try {
            boolean isEntry = aaiConnection.getEntryFor(correlationId).isPresent();
            execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, isEntry);
        } catch (IOException e) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, e.getMessage());
        }
    }
}
