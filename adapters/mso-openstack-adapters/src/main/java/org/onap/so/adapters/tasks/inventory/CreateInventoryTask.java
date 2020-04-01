/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 *
 * Copyright (C) 2019 IBM
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

package org.onap.so.adapters.tasks.inventory;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.externaltasks.logging.AuditMDCSetup;
import org.onap.so.utils.ExternalTaskUtils;
import org.onap.so.utils.RetrySequenceLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CreateInventoryTask extends ExternalTaskUtils {

    private static final String UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI = "Unable to write all inventory to A&AI";

    private static final Logger logger = LoggerFactory.getLogger(CreateInventoryTask.class);

    private static final String AAI_INVENTORY_FAILURE = "AAIInventoryFailure";

    @Autowired
    CreateAAIInventory createInventory;

    @Autowired
    public AuditMDCSetup mdcSetup;

    public CreateInventoryTask() {
        super(RetrySequenceLevel.SHORT);
    }


    public void executeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        mdcSetup.setupMDC(externalTask);
        boolean inventoryException = false;
        String externalTaskId = externalTask.getId();
        CloudInformation cloudInformation = externalTask.getVariable("cloudInformation");
        boolean success = true;
        if (cloudInformation != null) {
            Integer retryCount = externalTask.getRetries();
            try {
                logger.info("Executing External Task Create Inventory, Retry Number: {} \n {}", cloudInformation,
                        retryCount);
                createInventory.heatbridge(cloudInformation);
            } catch (Exception e) {
                logger.error("Error during inventory of stack", e);
                success = false;
            }
            mdcSetup.setElapsedTime();
            if (success) {
                externalTaskService.complete(externalTask);
                mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.COMPLETE.toString());
                logger.debug("The External Task Id: {}  Successful", externalTaskId);
                logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
                mdcSetup.clearClientMDCs();
            } else {
                if (retryCount == null) {
                    logger.error("The External Task Id: {}  Failed, Setting Retries to Default Start Value: {}",
                            externalTaskId, getRetrySequence().length);
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI,
                            UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI, getRetrySequence().length, 10000);
                } else if (retryCount != null && retryCount - 1 == 0) {
                    externalTaskService.handleBpmnError(externalTask, AAI_INVENTORY_FAILURE);
                    mdcSetup.setResponseCode(ONAPLogConstants.ResponseStatus.ERROR.toString());
                    logger.error("The External Task Id: {}  Failed, All Retries Exhausted", externalTaskId);
                    logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
                } else {
                    logger.error("The External Task Id: {}  Failed, Decrementing Retries: {} , Retry Delay: ",
                            externalTaskId, retryCount - 1, calculateRetryDelay(retryCount));
                    externalTaskService.handleFailure(externalTask, UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI,
                            UNABLE_TO_WRITE_ALL_INVENTORY_TO_A_AI, retryCount - 1, calculateRetryDelay(retryCount));
                }
                logger.error("The External Task Id: {} Failed", externalTaskId);
            }
        } else {
            logger.error("The External Task Id: {}  Failed, No Cloud Information Provided", externalTaskId);
            externalTaskService.handleBpmnError(externalTask, AAI_INVENTORY_FAILURE);
        }
    }
}
