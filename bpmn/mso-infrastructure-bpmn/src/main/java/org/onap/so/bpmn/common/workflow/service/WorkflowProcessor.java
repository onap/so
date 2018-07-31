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

package org.onap.so.bpmn.common.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.logger.MsoLogger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkflowProcessor extends ProcessEngineAwareService {
	
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, WorkflowProcessor.class);
	
	protected static final String logMarker = "[WRKFLOW-RESOURCE]";
	protected static final long DEFAULT_WAIT_TIME = 30000;	//default wait time
	
	@Async
	public void startProcess( String processKey, VariableMapImpl variableMap) throws InterruptedException
	{
		MDC.getCopyOfContextMap();
		long startTime = System.currentTimeMillis();
		Map<String, Object> inputVariables = null;
		String processInstanceId = null;
		try {
			inputVariables = getInputVariables(variableMap);
			setLogContext(processKey, inputVariables);

			// This variable indicates that the flow was invoked asynchronously
			inputVariables.put("isAsyncProcess", "true");
			
			
			setLogContext(processKey, inputVariables);

			// Note: this creates a random businessKey if it wasn't specified.
			String businessKey = getBusinessKey(inputVariables);

			msoLogger.debug("***Received MSO startProcessInstanceByKey with processKey: " + processKey
					+ " and variables: " + inputVariables);

			RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessKey,
					inputVariables);
			processInstanceId = processInstance.getId();

			msoLogger.debug(logMarker + "Process " + processKey + ":" + processInstanceId + " "
					+ (processInstance.isEnded() ? "ENDED" : "RUNNING"));
		} catch (Exception e) {

			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError,
					logMarker + "Error in starting the process: " + e.getMessage());

			WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
			callbackResponse.setStatusCode(500);
			callbackResponse.setMessage("Fail");
			callbackResponse.setResponse("Error occurred while executing the process: " + e);

			WorkflowContextHolder.getInstance().processCallback(processKey, processInstanceId,
					getRequestId(inputVariables), callbackResponse);
		}
	}
	
	protected static void setLogContext(String processKey,
			Map<String, Object> inputVariables) {
		MsoLogger.setServiceName("MSO." + processKey);
		if (inputVariables != null) {
			MsoLogger.setLogContext(getKeyValueFromInputVariables(inputVariables,"mso-request-id"), getKeyValueFromInputVariables(inputVariables,"mso-service-instance-id"));
		}
	}
	
	protected static String getKeyValueFromInputVariables(Map<String,Object> inputVariables, String key) {
		if (inputVariables == null) {
			return "";
		}

		return Objects.toString(inputVariables.get(key), "N/A");
	}
	
	// Note: the business key is used to identify the process in unit tests
	protected static String getBusinessKey(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-business-key");
	}

	protected static String getRequestId(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-request-id");
	}
	
	protected static Map<String, Object> getInputVariables(VariableMapImpl variableMap) {
		Map<String, Object> inputVariables = new HashMap<>();
		@SuppressWarnings("unchecked")
		Map<String, Object> vMap = (Map<String, Object>) variableMap.get("variables");
		for (Map.Entry<String, Object> entry : vMap.entrySet()) {
			String vName = entry.getKey();
			Object value = entry.getValue();
			@SuppressWarnings("unchecked")
			Map<String, Object> valueMap = (Map<String,Object>)value; // value, type
			inputVariables.put(vName, valueMap.get("value"));
		}
		return inputVariables;
	}
	
    protected static String getOrCreate(Map<String, Object> inputVariables, String key) {
        String value = Objects.toString(inputVariables.get(key), null);
        if (value == null) {
            value = UUID.randomUUID().toString();
            inputVariables.put(key, value);
        }
        return value;
    }
    
	protected long getWaitTime(Map<String, Object> inputVariables)
	{
	    
		String timeout = Objects.toString(inputVariables.get("mso-service-request-timeout"), null);

		if (timeout != null) {
			try {
				return Long.parseLong(timeout)*1000;
			} catch (NumberFormatException nex) {
				msoLogger.debug("Invalid input for mso-service-request-timeout");
			}
		}

		return DEFAULT_WAIT_TIME;
	}
}
