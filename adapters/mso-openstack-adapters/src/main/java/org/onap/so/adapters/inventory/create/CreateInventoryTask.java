/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.inventory.create;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.audit.AAIObjectAuditList;
import org.onap.so.client.graphinventory.GraphInventoryCommonObjectMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CreateInventoryTask {

    private static final String UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI = "Unable to write all inventory to A&AI";

    private static final Logger logger = LoggerFactory.getLogger(CreateInventoryTask.class);

    @Autowired
    CreateAAIInventory createInventory;

    @Autowired
    public Environment env;

    protected void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        setupMDC(externalTask);
        boolean success = true;
        boolean inventoryException = false;
        String auditInventoryString = externalTask.getVariable("auditInventoryResult");
        AAIObjectAuditList auditInventory = null;
        try {
            GraphInventoryCommonObjectMapperProvider objectMapper = new GraphInventoryCommonObjectMapperProvider();
            auditInventory = objectMapper.getMapper().readValue(auditInventoryString, AAIObjectAuditList.class);
        } catch (Exception e) {
            logger.error("Error Parsing Audit Results", e);
        }
        if (auditInventory != null) {
            try {
                logger.info("Executing External Task Create Inventory, Retry Number: {} \n {}", auditInventory,
                        externalTask.getRetries());
                createInventory.createInventory(auditInventory);
            } catch (InventoryException e) {
                logger.error("Error during inventory of stack", e);
                success = false;
                inventoryException = true;
            } catch (Exception e) {
                logger.error("Error during inventory of stack", e);
                success = false;
            }
            if (success) {
                externalTaskService.complete(externalTask);
                logger.debug("The External Task Id: {}  Successful", externalTask.getId());
            } else if (inventoryException) {
                logger.debug("The External Task Id: {}  Failed, Retry not needed", externalTask.getId());
                externalTaskService.handleBpmnError(externalTask, "AAIInventoryFailure");
            } else {
                if (externalTask.getRetries() == null) {
                    logger.debug("The External Task Id: {}  Failed, Setting Retries to Default Start Value: {}",
                            externalTask.getId(), getRetrySequence().length);
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI,
                            UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI, getRetrySequence().length, 10000);
                } else if (externalTask.getRetries() != null && externalTask.getRetries() - 1 == 0) {
                    logger.debug("The External Task Id: {}  Failed, All Retries Exhausted", externalTask.getId());
                    externalTaskService.handleBpmnError(externalTask, "AAIInventoryFailure");
                } else {
                    logger.debug("The External Task Id: {}  Failed, Decrementing Retries: {} , Retry Delay: ",
                            externalTask.getId(), externalTask.getRetries() - 1,
                            calculateRetryDelay(externalTask.getRetries()));
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI,
                            UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI, externalTask.getRetries() - 1,
                            calculateRetryDelay(externalTask.getRetries()));
                }
                logger.debug("The External Task Id: {} Failed", externalTask.getId());
            }
        } else {
            logger.debug("The External Task Id: {}  Failed, No Audit Results Written", externalTask.getId());
            externalTaskService.handleBpmnError(externalTask, "AAIInventoryFailure");
        }
    }

    private void setupMDC(ExternalTask externalTask) {
        try {
            String msoRequestId = (String) externalTask.getVariable("mso-request-id");
            if (msoRequestId != null && !msoRequestId.isEmpty())
                MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, msoRequestId);
        } catch (Exception e) {
            logger.error("Error in setting up MDC", e);
        }
    }

    protected long calculateRetryDelay(int currentRetries) {
        int retrySequence = getRetrySequence().length - currentRetries;
        long retryMultiplier = Long.parseLong(env.getProperty("mso.workflow.topics.retryMultiplier", "6000"));
        return Integer.parseInt(getRetrySequence()[retrySequence]) * retryMultiplier;
    }

    public String[] getRetrySequence() {
        return env.getProperty("mso.workflow.topics.retrySequence", String[].class);
    }
}
