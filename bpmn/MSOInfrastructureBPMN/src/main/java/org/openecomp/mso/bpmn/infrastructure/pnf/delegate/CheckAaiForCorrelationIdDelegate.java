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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.IP_ADDRESS;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiResponse;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.CheckAaiForCorrelationIdImplementation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of "Check AAI for correlation_id" task in CreateAndActivatePnfResource.bpmn
 *
 * Inputs:
 * - correlationId - String
 *
 * Outputs:
 * - AAI_CONTAINS_INFO_ABOUT_PNF - local Boolean
 * - aaiContainsInfoAboutIp - local Boolean (only present if AAI_CONTAINS_INFO_ABOUT_PNF is true)
 * - ipAddress - String (only present if aaiContainsInfoAboutIp is true)
 */

public class CheckAaiForCorrelationIdDelegate implements JavaDelegate {

    private CheckAaiForCorrelationIdImplementation implementation = new CheckAaiForCorrelationIdImplementation();
    private AaiConnection aaiConnection;

    @Autowired
    public void setAaiConnection(AaiConnection aaiConnection) {
        this.aaiConnection = aaiConnection;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String correlationId = (String) execution.getVariable(CORRELATION_ID);
        if (correlationId == null) {
            //todo: fix Execution -> DelegateExecution in ALL groovy scripts
//            new ExceptionUtil().buildAndThrowWorkflowException(execution, 500, CORRELATION_ID + " is not set");
            throw new BpmnError("MSOWorkflowException");
        }

        AaiResponse aaiResponse = implementation.check(correlationId, aaiConnection);

        execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, aaiResponse.getContainsInfoAboutPnf());
        aaiResponse.getContainsInfoAboutIp().ifPresent(
                isIp -> execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_IP, isIp)
        );
        aaiResponse.getIpAddress().ifPresent(
                ip -> execution.setVariable(IP_ADDRESS, ip)
        );
    }
}
