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

package org.onap.so.adapters.tasks.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.audit.beans.AuditInventory;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.so.objects.audit.AAIObjectAuditList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditCreateStackService extends AbstractAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditCreateStackService.class);

    @Autowired
    public HeatStackAudit heatStackAudit;

    @Autowired
    public AuditMDCSetup mdcSetup;

    public AuditCreateStackService() {
        super();
    }

    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        AuditInventory auditInventory = externalTask.getVariable("auditInventory");
        Map<String, Object> variables = new HashMap<>();
        boolean success = false;
        try {
            Integer retryCount = externalTask.getRetries();
            logger.info("Executing External Task Audit Inventory, Retry Number: {} \n {}", auditInventory, retryCount);
            Optional<AAIObjectAuditList> auditListOpt = heatStackAudit.auditHeatStack(auditInventory.getCloudRegion(),
                    auditInventory.getCloudOwner(), auditInventory.getTenantId(), auditInventory.getHeatStackName());
            if (auditListOpt.isPresent()) {
                auditListOpt.get().setAuditType("create");
                auditListOpt.get().setHeatStackName(auditInventory.getHeatStackName());
                GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
                variables.put("auditInventoryResult", objectMapper.getMapper().writeValueAsString(auditListOpt.get()));
                success = !didCreateAuditFail(auditListOpt);
            }
        } catch (Exception e) {
            logger.error("Error during audit of stack", e);
        }
        variables.put("auditIsSuccessful", success);
        mdcSetup.setElapsedTime();
        String externalTaskId = externalTask.getId();
        if (success) {
            externalTaskService.complete(externalTask, variables);
            mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.COMPLETE.toString());
            logger.debug("The External Task Id: {}  Successful", externalTaskId);
            logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
            mdcSetup.clearClientMDCs();
        } else {
            Integer retryCount = externalTask.getRetries();
            if (retryCount == null) {
                logger.debug("The External Task Id: {}  Failed, Setting Retries to Default Start Value: {}",
                        externalTaskId, getRetrySequence().length);
                externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI,
                        UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI, getRetrySequence().length, 10000);
            } else if (retryCount == 1) {
                externalTaskService.complete(externalTask, variables);
                mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.ERROR.toString());
                logger.debug("The External Task Id: {}  Failed, All Retries Exhausted", externalTaskId);
                logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
                mdcSetup.clearClientMDCs();
            } else {
                logger.debug("The External Task Id: {}  Failed, Decrementing Retries: {} , Retry Delay: ",
                        externalTaskId, retryCount - 1, calculateRetryDelay(retryCount));
                externalTaskService.handleFailure(externalTask, UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI,
                        UNABLE_TO_FIND_ALL_V_SERVERS_AND_L_INTERACES_IN_A_AI, retryCount - 1,
                        calculateRetryDelay(retryCount));
            }
            logger.debug("The External Task Id: {} Failed", externalTaskId);
        }
    }



}
