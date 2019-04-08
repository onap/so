/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FlowCompletionTasks {

    private static final Logger logger = LoggerFactory.getLogger(FlowCompletionTasks.class);

    @Autowired
    private RequestsDbClient requestDbclient;

    public void updateRequestDbStatus(BuildingBlockExecution execution) {
        try {
            String requestId = execution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId();
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);

            WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
            if (workflowException == null) {
                request.setStatusMessage("RequestCompletedSuccessfully");
                request.setRequestStatus("COMPLETE");

            } else {
                request.setStatusMessage(workflowException.getErrorMessage());
                request.setRequestStatus("FAILED");
            }
            request.setProgress(100L);
            request.setLastModifiedBy("CamundaBPMN");

            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception e) {
            logger.error("Unable to save the updated request status to the DB", e);
        }
    }


}
