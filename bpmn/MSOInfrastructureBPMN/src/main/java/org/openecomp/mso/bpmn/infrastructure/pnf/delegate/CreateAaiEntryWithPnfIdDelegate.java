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

import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.Pnf;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.AaiConnection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of "Create AAI entry with pnf-id = correlation_id" task in CreateAndActivatePnfResource.bpmn
 *
 * Inputs:
 *  - correlationId - String
 */
public class CreateAaiEntryWithPnfIdDelegate implements JavaDelegate {

    private AaiConnection aaiConnection;

    @Autowired
    public void setAaiConnection(AaiConnection aaiConnection) {
        this.aaiConnection = aaiConnection;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String correlationId = (String) execution.getVariable(CORRELATION_ID);
        Pnf pnf = new Pnf();
        pnf.setInMaint(true);
        pnf.setPnfId(correlationId);
        pnf.setPnfName(correlationId);
        aaiConnection.createEntry(correlationId, pnf);
    }
}
