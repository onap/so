/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.ticket.ExternalTicket;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@Qualifier("ExternalTicketCreation")
public class ExternalTicketTasks implements ExternalTicketCreation {
    private static final Logger logger = LoggerFactory.getLogger(ExternalTicketTasks.class);

    protected static final String VNF_TYPE = "vnfType";
    protected static final String DESCRIPTION = "description";
    protected static final String SERVICE_TYPE = "serviceType";
    protected static final String MSO_REQUEST_ID = "mso-request-id";
    protected static final String REQUESTOR_ID = "requestorId";
    protected static final String ERROR_CODE = "errorCode";
    protected static final String VALID_RESPONSES = "validResponses";
    protected static final String TASK_TIMEOUT = "taskTimeout";
    protected static final String RESPONSE_VALUE_TASK = "responseValueTask";
    protected static final String RESPONSE_VALUE = "responseValue";
    protected static final String WORKSTEP = "workStep";

    protected static final String TASK_VARIABLE_TYPE = "type";
    protected static final String TASK_VARIABLE_NFROLE = "nfRole";
    protected static final String TASK_VARIABLE_SUBSCRIPTION_SERVICE_TYPE = "subscriptionServiceType";
    protected static final String TASK_VARIABLE_ORIGINAL_REQUEST_ID = "originalRequestId";
    protected static final String TASK_VARIABLE_ORIGINAL_REQUESTOR_ID = "originalRequestorId";
    protected static final String TASK_VARIABLE_ERROR_SOURCE = "errorSource";
    protected static final String TASK_VARIABLE_ERROR_CODE = "errorCode";
    protected static final String TASK_VARIABLE_ERROR_MESSAGE = "errorMessage";
    protected static final String TASK_VARIABLE_BUILDING_BLOCK_NAME = "buildingBlockName";
    protected static final String TASK_VARIABLE_BUILDING_BLOCK_STEP = "buildingBlockStep";
    protected static final String TASK_VARIABLE_DESCRIPTION = "description";
    protected static final String TASK_VARIABLE_TIMEOUT = "timeout";
    protected static final String TASK_VARIABLE_VALID_RESPONSES = "validResponses";

    protected static final String BPMN_EXCEPTION = "BPMN exception: ";
    protected static final String RAINY_DAY_SERVICE_TYPE = "rainyDayServiceType";
    protected static final String RAINY_DAY_VNF_TYPE = "rainyDayVnfType";
    protected static final String RAINY_DAY_VNF_NAME = "rainyDayVnfName";
    protected static final String G_BUILDING_BLOCK_EXECUTION = "gBuildingBlockExecution";
    protected static final String WORKFLOW_EXCEPTION = "WorkflowException";

    public void createExternalTicket(BuildingBlockExecution execution) {

        logger.debug("Creating ExternalTicket()");
        try {
            ExternalTicket ticket = getExternalTicket();

            ticket.setRequestId((String) execution.getVariable(MSO_REQUEST_ID));
            ticket.setCurrentActivity((String) execution.getVariable("currentActivity"));
            ticket.setNfRole((String) execution.getVariable(VNF_TYPE));
            ticket.setDescription((String) execution.getVariable(DESCRIPTION));
            ticket.setSubscriptionServiceType((String) execution.getVariable(SERVICE_TYPE));
            ticket.setRequestorId((String) execution.getVariable(REQUESTOR_ID));
            ticket.setTimeout((String) execution.getVariable(TASK_TIMEOUT));
            ticket.setErrorSource((String) execution.getVariable("failedActivity"));
            ticket.setErrorCode((String) execution.getVariable(ERROR_CODE));
            ticket.setErrorMessage((String) execution.getVariable("errorText"));
            ticket.setWorkStep((String) execution.getVariable(WORKSTEP));

            ticket.createTicket();
        } catch (BpmnError e) {
            String msg = "BPMN error in createExternalTicket " + e.getMessage();
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
        } catch (Exception ex) {
            String msg = "Exception in createExternalTicket " + ex.getMessage();
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN",
                    ErrorCode.UnknownError.getValue());
        }

    }

    protected ExternalTicket getExternalTicket() {
        return new ExternalTicket();
    }


}
