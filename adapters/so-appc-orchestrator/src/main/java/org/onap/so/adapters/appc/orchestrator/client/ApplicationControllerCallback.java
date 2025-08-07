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

package org.onap.so.adapters.appc.orchestrator.client;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.onap.appc.client.lcm.api.ResponseHandler;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.client.lcm.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationControllerCallback<T> implements ResponseHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationControllerCallback.class);

    private final ExternalTask externalTask;
    private final ExternalTaskService externalTaskService;
    private final ApplicationControllerSupport appCSupport;

    public ApplicationControllerCallback(ExternalTask externalTask, ExternalTaskService externalTaskService,
            ApplicationControllerSupport appCSupport) {
        this.externalTask = externalTask;
        this.externalTaskService = externalTaskService;
        this.appCSupport = appCSupport;
    }

    @Override
    public void onResponse(T response) {
        logger.info("ON RESPONSE IN CALLBACK");

        Status status = appCSupport.getStatusFromGenericResponse(response);

        logger.info("Status code is: " + status.getCode());
        logger.info("Status message is: " + status.getMessage());

        if (appCSupport.getFinalityOf(status)) {
            logger.debug("Obtained final status, complete the task");
            completeExternalTask(externalTask, externalTaskService, status);
        } else {
            logger.debug("Intermediate status, continue the task");
        }
    }

    @Override
    public void onException(AppcClientException exception) {

        logger.info("ON EXCEPTION IN CALLBACK");
        logger.info("Exception from APPC: " + exception.getMessage());
        Status exceptionStatus = appCSupport.buildStatusFromAppcException(exception);
        completeExternalTask(externalTask, externalTaskService, exceptionStatus);
    }

    private void completeExternalTask(ExternalTask externalTask, ExternalTaskService externalTaskService,
            Status status) {

        if (appCSupport.getCategoryOf(status).equals(StatusCategory.NORMAL)) {
            externalTaskService.complete(externalTask);
            logger.debug("The External Task Id: {} Successful", externalTask.getId());
        } else {
            logger.debug("The External Task Id: {} Failed", externalTask.getId());
            externalTaskService.handleBpmnError(externalTask, "MSOWorkflowException", status.getMessage());
        }
    }
}
