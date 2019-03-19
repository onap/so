/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.logger.ErrorCode;
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
	
	private static final String SERVICE_TASK_IMPLEMENTATION_ATTRIBUTE = "implementation";
	private static final String ACTIVITY_PREFIX = "activity:";
	
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {		
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);		
		
		try {
			final String implementationString = execution.getBpmnModelElementInstance().getAttributeValue(SERVICE_TASK_IMPLEMENTATION_ATTRIBUTE);
			logger.debug("activity implementation String: {}", implementationString);
			if (!implementationString.startsWith(ACTIVITY_PREFIX)) {
				buildAndThrowException(execution, "Implementation attribute has a wrong format");
			}
			String activityName = implementationString.replaceFirst(ACTIVITY_PREFIX, "");
			logger.info("activityName is: {}", activityName);
			
			BuildingBlock buildingBlock = buildBuildingBlock(activityName);
			ExecuteBuildingBlock executeBuildingBlock = buildExecuteBuildingBlock(execution, requestId, buildingBlock);
						
			Map<String, Object> variables = new HashMap<>();
			variables.put("buildingBlock", executeBuildingBlock);
			variables.put("mso-request-id", requestId);
			variables.put("retryCount", 1);	
			variables.put("aLaCarte", true);	
		
		    ProcessInstanceWithVariables buildingBlockResult = runtimeService.createProcessInstanceByKey("ExecuteBuildingBlock").setVariables(variables).executeWithVariablesInReturn();
			VariableMap variableMap = buildingBlockResult.getVariables();
			
			WorkflowException workflowException = (WorkflowException) variableMap.get("WorklfowException");
			if (workflowException != null) {
				logger.error("Workflow exception is: {}", workflowException.getErrorMessage());
			}
			execution.setVariable("WorkflowException", workflowException);
		}
		catch (Exception e) {
			buildAndThrowException(execution, e.getMessage());		
		}
	}
	
	protected BuildingBlock buildBuildingBlock(String activityName) {
		BuildingBlock buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName(activityName);
		buildingBlock.setMsoId(UUID.randomUUID().toString());
		buildingBlock.setKey("");
		buildingBlock.setIsVirtualLink(false);
		buildingBlock.setVirtualLinkKey("");
		return buildingBlock;
	}
	
	protected ExecuteBuildingBlock buildExecuteBuildingBlock(DelegateExecution execution, String requestId, 
			BuildingBlock buildingBlock) throws Exception {
		ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
		String bpmnRequest = (String) execution.getVariable(G_BPMN_REQUEST);
		ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
		RequestDetails requestDetails = sIRequest.getRequestDetails();
		executeBuildingBlock.setaLaCarte(true);
		executeBuildingBlock.setRequestAction((String) execution.getVariable(G_ACTION));
		executeBuildingBlock.setResourceId((String) execution.getVariable(VNF_ID));
		executeBuildingBlock.setVnfType((String) execution.getVariable(VNF_TYPE));
		WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
		workflowResourceIds.setServiceInstanceId((String) execution.getVariable(SERVICE_INSTANCE_ID));
		workflowResourceIds.setVnfId((String) execution.getVariable(VNF_ID));
		executeBuildingBlock.setWorkflowResourceIds(workflowResourceIds);
		executeBuildingBlock.setRequestId(requestId);
		executeBuildingBlock.setBuildingBlock(buildingBlock);
		executeBuildingBlock.setRequestDetails(requestDetails);
		return executeBuildingBlock;
	}
	
	protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
		logger.error("{} {} {} {} {}", MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(), msg, "BPMN",
			ErrorCode.UnknownError.getValue(), msg, ex);
		execution.setVariable("ExecuteActivityErrorMessage", msg);
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
	}

	protected void buildAndThrowException(DelegateExecution execution, String msg) {
		logger.error(msg);
		execution.setVariable("ExecuteActuvityErrorMessage", msg);
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
	}
}
