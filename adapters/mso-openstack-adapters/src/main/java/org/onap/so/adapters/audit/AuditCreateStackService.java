/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AuditCreateStackService extends AbstractAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditCreateStackService.class);

    @Autowired
    public HeatStackAudit heatStackAudit;

    @Autowired
    public Environment env;

    protected void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        AuditInventory auditInventory = externalTask.getVariable("auditInventory");
        Map<String, Object> variables = new HashMap<>();
        setupMDC(externalTask);
        boolean success = false;
        try {
            logger.info("Executing External Task Audit Inventory, Retry Number: {} \n {}", auditInventory,
                    externalTask.getRetries());
            Optional<AAIObjectAuditList> auditListOpt = heatStackAudit.auditHeatStack(auditInventory.getCloudRegion(),
                    auditInventory.getCloudOwner(), auditInventory.getTenantId(), auditInventory.getHeatStackName());
            if (auditListOpt.isPresent()) {
                GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
                variables.put("auditInventoryResult", objectMapper.getMapper().writeValueAsString(auditListOpt.get()));
                success = !didCreateAuditFail(auditListOpt);
            }
        } catch (Exception e) {
            logger.error("Error during audit of stack", e);
        }
        variables.put("auditIsSuccessful", success);
        if (success) {
            externalTaskService.complete(externalTask, variables);
            logger.debug("The External Task Id: {}  Successful", externalTask.getId());
        } else {
            if (externalTask.getRetries() == null) {
                logger.debug("The External Task Id: {}  Failed, Setting Retries to Default Start Value: {}",
                        externalTask.getId(), getRetrySequence().length);
                externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI,
                        UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI, getRetrySequence().length, 10000);
            } else if (externalTask.getRetries() != null && externalTask.getRetries() - 1 == 0) {
                logger.debug("The External Task Id: {}  Failed, All Retries Exhausted", externalTask.getId());
                externalTaskService.complete(externalTask, variables);
            } else {
                logger.debug("The External Task Id: {}  Failed, Decrementing Retries: {} , Retry Delay: ",
                        externalTask.getId(), externalTask.getRetries() - 1,
                        calculateRetryDelay(externalTask.getRetries()));
                externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI,
                        UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI, externalTask.getRetries() - 1,
                        calculateRetryDelay(externalTask.getRetries()));
            }
            logger.debug("The External Task Id: {} Failed", externalTask.getId());
        }
    }



}
