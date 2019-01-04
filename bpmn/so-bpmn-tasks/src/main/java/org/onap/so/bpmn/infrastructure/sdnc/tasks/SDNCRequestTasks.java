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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
 
@Component
public class SDNCRequestTasks {
	
	private static final Logger logger = LoggerFactory.getLogger(SDNCRequestTasks.class);
	
	private static final String SDNC_REQUEST = "SDNCRequest";
	private static final String MESSAGE = "_MESSAGE";
	private static final String CORRELATOR = "_CORRELATOR";
	protected static final String IS_CALLBACK_COMPLETED = "isCallbackCompleted";
	
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	
	@Autowired
	private SDNCClient sdncClient;
	
	public void createCorrelationVariables (DelegateExecution execution) {
		SDNCRequest request = (SDNCRequest)execution.getVariable(SDNC_REQUEST);
		execution.setVariable(request.getCorrelationName()+CORRELATOR, request.getCorrelationValue());
		execution.setVariable("sdncTimeout", request.getTimeOut());
	}
	
	public void callSDNC (DelegateExecution execution) {
		SDNCRequest request = (SDNCRequest)execution.getVariable(SDNC_REQUEST);
		try {
			String response = sdncClient.post(request.getSDNCPayload(),request.getTopology());
			String finalMessageIndicator = JsonPath.read(response, "$.output.ack-final-indicator");		
			execution.setVariable("isSDNCCompleted", convertIndicatorToBoolean(finalMessageIndicator));
		} catch(PathNotFoundException e) {
			logger.error("Error Parsing SDNC Response", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,"Error Parsing SDNC Response");
		} catch (MapperException e) {
			logger.error("Error Parsing SDNC Response", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,"Error Parsing SDNC Response");
		} catch (BadResponseException e) {
			logger.error("Error Reading SDNC Response", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "Error Reading SDNC Response");
		}		
	}
	
	public void processCallback (DelegateExecution execution) {
		try {
			SDNCRequest request = (SDNCRequest)execution.getVariable(SDNC_REQUEST);
			String asyncRequest = (String) execution.getVariable(request.getCorrelationName()+MESSAGE);
			String finalMessageIndicator = JsonPath.read(asyncRequest, "$.input.ack-final-indicator");		
			boolean isCallbackCompleted = convertIndicatorToBoolean(finalMessageIndicator);
			execution.setVariable(IS_CALLBACK_COMPLETED, isCallbackCompleted);
		} catch (Exception e) {
			logger.error("Error procesing SDNC callback", e);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "Error procesing SDNC callback");
		}
	}
	
	public void handleTimeOutException (DelegateExecution execution) {		
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, "Error timed out waiting on SDNC Async-Response");
	}

	protected boolean convertIndicatorToBoolean(String finalMessageIndicator) {
		return "Y".equals(finalMessageIndicator);
	}
	
}
