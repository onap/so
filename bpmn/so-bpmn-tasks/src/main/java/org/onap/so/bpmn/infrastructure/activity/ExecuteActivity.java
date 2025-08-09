/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.bpmn.infrastructure.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.onap.so.logger.LoggingAnchor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionBBFailure;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionBBTasks;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("ExecuteActivity")
public class ExecuteActivity implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteActivity.class);
    private static final String G_BPMN_REQUEST = "bpmnRequest";
    private static final String VNF_TYPE = "vnfType";
    private static final String G_ACTION = "requestAction";
    private static final String G_REQUEST_ID = "mso-request-id";
    private static final String VNF_ID = "vnfId";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String WORKFLOW_SYNC_ACK_SENT = "workflowSyncAckSent";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String EXECUTE_BUILDING_BLOCK = "ExecuteBuildingBlock";
    private static final String RETRY_COUNT = "retryCount";
    private static final String A_LA_CARTE = "aLaCarte";
    private static final String SUPPRESS_ROLLBACK = "suppressRollback";
    private static final String WORKFLOW_EXCEPTION = "WorkflowException";
    private static final String HANDLING_CODE = "handlingCode";
    private static final String ABORT_HANDLING_CODE = "Abort";

    private static final String SERVICE_TASK_IMPLEMENTATION_ATTRIBUTE = "implementation";
    private static final String ACTIVITY_PREFIX = "activity:";
    private static final String EXECUTE_ACTIVITY_ERROR_MESSAGE = "ExecuteActivityErrorMessage";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private WorkflowActionBBFailure workflowActionBBFailure;
    @Autowired
    private WorkflowActionBBTasks workflowActionBBTasks;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        final String requestId = (String) execution.getVariable(G_REQUEST_ID);
        WorkflowException workflowException;
        String handlingCode;
        try {
            Boolean workflowSyncAckSent = (Boolean) execution.getVariable(WORKFLOW_SYNC_ACK_SENT);
            if (workflowSyncAckSent == null || workflowSyncAckSent == false) {
                workflowActionBBTasks.sendSyncAck(execution);
                execution.setVariable(WORKFLOW_SYNC_ACK_SENT, Boolean.TRUE);
            }
            final String implementationString =
                    execution.getBpmnModelElementInstance().getAttributeValue(SERVICE_TASK_IMPLEMENTATION_ATTRIBUTE);
            logger.debug("activity implementation String: {}", implementationString);
            if (!implementationString.startsWith(ACTIVITY_PREFIX)) {
                buildAndThrowException(execution, "Implementation attribute has a wrong format");
            }
            String activityName = implementationString.replaceFirst(ACTIVITY_PREFIX, "");
            logger.info("activityName is: {}", activityName);

            BuildingBlock buildingBlock = buildBuildingBlock(activityName);
            ExecuteBuildingBlock executeBuildingBlock = buildExecuteBuildingBlock(execution, requestId, buildingBlock);

            Map<String, Object> variables = new HashMap<>();

            if (execution.getVariables() != null) {
                execution.getVariables().forEach((key, value) -> {
                    if (value instanceof Serializable) {
                        variables.put(key, value);
                    }
                });
            }

            variables.put(BUILDING_BLOCK, executeBuildingBlock);
            variables.put(G_REQUEST_ID, requestId);
            variables.put(RETRY_COUNT, 1);
            variables.put(A_LA_CARTE, true);
            variables.put(SUPPRESS_ROLLBACK, true);

            ProcessInstanceWithVariables buildingBlockResult =
                    runtimeService.createProcessInstanceByKey(EXECUTE_BUILDING_BLOCK).setVariables(variables)
                            .executeWithVariablesInReturn();
            VariableMap variableMap = buildingBlockResult.getVariables();

            workflowException = (WorkflowException) variableMap.get(WORKFLOW_EXCEPTION);
            if (workflowException != null) {
                logger.error("Workflow exception is: {}", workflowException.getErrorMessage());
            }

            handlingCode = (String) variableMap.get(HANDLING_CODE);
            logger.debug("Handling code: {}", handlingCode);

            execution.setVariable(WORKFLOW_EXCEPTION, workflowException);
        } catch (Exception e) {
            logger.error("BPMN exception on activity execution: {}", e.getMessage());
            workflowException = new WorkflowException(EXECUTE_BUILDING_BLOCK, 7000, e.getMessage());
            handlingCode = ABORT_HANDLING_CODE;
        }

        if (workflowException != null && handlingCode != null && handlingCode.equals(ABORT_HANDLING_CODE)) {
            logger.debug("Aborting execution of the custom workflow");
            buildAndThrowException(execution, workflowException.getErrorMessage());
        }

    }

    protected BuildingBlock buildBuildingBlock(String activityName) {
        return new BuildingBlock().setBpmnFlowName(activityName).setMsoId(UUID.randomUUID().toString()).setKey("")
                .setIsVirtualLink(false).setVirtualLinkKey("");
    }

    protected ExecuteBuildingBlock buildExecuteBuildingBlock(DelegateExecution execution, String requestId,
            BuildingBlock buildingBlock) throws IOException {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId((String) execution.getVariable(SERVICE_INSTANCE_ID));
        workflowResourceIds.setVnfId((String) execution.getVariable(VNF_ID));
        String bpmnRequest = (String) execution.getVariable(G_BPMN_REQUEST);
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        RequestDetails requestDetails = sIRequest.getRequestDetails();
        return new ExecuteBuildingBlock().setaLaCarte(true).setRequestAction((String) execution.getVariable(G_ACTION))
                .setResourceId((String) execution.getVariable(VNF_ID))
                .setVnfType((String) execution.getVariable(VNF_TYPE)).setWorkflowResourceIds(workflowResourceIds)
                .setRequestId(requestId).setBuildingBlock(buildingBlock).setRequestDetails(requestDetails);
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
        logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
                ErrorCode.UnknownError.getValue(), msg, ex);
        execution.setVariable(EXECUTE_ACTIVITY_ERROR_MESSAGE, msg);
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(EXECUTE_ACTIVITY_ERROR_MESSAGE, msg);
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }
}
