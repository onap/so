/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.Constants.TENANT_ID;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class WorkflowExecutorService {

    private static final Logger logger = getLogger(WorkflowExecutorService.class);

    private final RuntimeService runtimeService;

    @Autowired
    public WorkflowExecutorService(final RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Async
    public void executeWorkflow(final String jobId, final String processDefinitionKey,
            final Map<String, Object> variables) {
        logger.info("Executing {} workflow with business key: {}", processDefinitionKey, jobId);
        final ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(processDefinitionKey)
                .businessKey(jobId).setVariables(variables).processDefinitionTenantId(TENANT_ID).execute();

        logger.info("Workflow running with processInstanceId: {} and business key: {}",
                processInstance.getProcessInstanceId(), processInstance.getBusinessKey());
    }

}
