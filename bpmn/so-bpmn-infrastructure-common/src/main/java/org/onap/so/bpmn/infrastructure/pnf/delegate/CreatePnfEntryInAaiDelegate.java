/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of "Create Pnf entry in AAI" task in CreateAndActivatePnfResource.bpmn
 *
 * Inputs:
 *  - correlationId - String
 *  - pnfUuid - String
 */
@Component
public class CreatePnfEntryInAaiDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreatePnfEntryInAaiDelegate.class);
    private PnfManagement pnfManagement;

    @Autowired
    public void setPnfManagement(PnfManagement pnfManagement) {
        this.pnfManagement = pnfManagement;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String correlationId = (String) execution.getVariable(CORRELATION_ID);
        String pnfUuid = (String) execution.getVariable(PNF_UUID);
        Pnf pnf = new Pnf();
        pnf.setPnfId(pnfUuid);
        pnf.setPnfName(correlationId);
        pnfManagement.createEntry(correlationId, pnf);
        logger.debug("AAI entry is created for pnf correlation id: {}, pnf uuid: {}", correlationId, pnfUuid);
    }
}
