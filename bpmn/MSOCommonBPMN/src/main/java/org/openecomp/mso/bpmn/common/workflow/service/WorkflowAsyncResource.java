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
package org.openecomp.mso.bpmn.common.workflow.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 
 * @version 1.0
 * Asynchronous Workflow processing using JAX RS RESTeasy implementation
 * Both Synchronous and Asynchronous BPMN process can benefit from this implementation since the workflow gets executed in the background
 * and the server thread is freed up, server scales better to process more incoming requests
 * 
 * Usage: For synchronous process, when you are ready to send the response invoke the callback to write the response
 * For asynchronous process - the activity may send a acknowledgement response and then proceed further on executing the process
 */
@Path("/async")
public class WorkflowAsyncResource extends ProcessEngineAwareService {

	private static final WorkflowContextHolder contextHolder = WorkflowContextHolder.getInstance();
	protected Optional<ProcessEngineServices> pes4junit = Optional.empty();

	private final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

	private static final String logMarker = "[WRKFLOW-RESOURCE]";
	private static final long DEFAULT_WAIT_TIME = 30000;	//default wait time
	
	/**
	 * Asynchronous JAX-RS method that starts a process instance.
	 * @param asyncResponse an object that will receive the asynchronous response
	 * @param processKey the process key
	 * @param variableMap input variables to the process
	 */
	@POST
	@Path("/services/{processKey}")
	@Produces("application/json")
	@Consumes("application/json")
	public void startProcessInstanceByKey(final @Suspend(180000) AsynchronousResponse asyncResponse,
			@PathParam("processKey") String processKey, VariableMapImpl variableMap) {
	
		long startTime = System.currentTimeMillis();
		Map<String, Object> inputVariables = null;
		WorkflowContext workflowContext = null;

		try {
			inputVariables = getInputVariables(variableMap);	
			setLogContext(processKey, inputVariables);

			// This variable indicates that the flow was invoked asynchronously
			inputVariables.put("isAsyncProcess", "true");

			workflowContext = new WorkflowContext(processKey, getRequestId(inputVariables),
				asyncResponse, getWaitTime(inputVariables));

			msoLogger.debug("Adding the workflow context into holder: "
					+ workflowContext.getProcessKey() + ":"
					+ workflowContext.getRequestId() + ":"
					+ workflowContext.getTimeout());

			contextHolder.put(workflowContext);

			ProcessThread processThread = new ProcessThread(processKey, inputVariables);
			processThread.start();
		} catch (Exception e) {
			setLogContext(processKey, inputVariables);

			if (workflowContext != null) {
				contextHolder.remove(workflowContext);
			}

			msoLogger.debug(logMarker + "Exception in startProcessInstance by key");
	        WorkflowResponse response = new WorkflowResponse();
			response.setMessage("Fail" );
			response.setResponse("Error occurred while executing the process: " + e);
			response.setMessageCode(500);
			recordEvents(processKey, response, startTime);
			
			msoLogger.error (MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, logMarker 
					+ response.getMessage() + " for processKey: " 
					+ processKey + " with response: " + response.getResponse());
			
			Response errorResponse = Response.serverError().entity(response).build();
			asyncResponse.setResponse(errorResponse);
		}
	}
	
	/**
	 * 
	 * @version 1.0
	 *
	 */
	class ProcessThread extends Thread {
		private final String processKey;
		private final Map<String,Object> inputVariables;

		public ProcessThread(String processKey, Map<String, Object> inputVariables) {
			this.processKey = processKey;
			this.inputVariables = inputVariables;
		}
		
		public void run() {

			String processInstanceId = null;
			long startTime = System.currentTimeMillis();
			
			try {
				setLogContext(processKey, inputVariables);

				// Note: this creates a random businessKey if it wasn't specified.
				String businessKey = getBusinessKey(inputVariables);
				
				msoLogger.debug(logMarker + "***Received MSO startProcessInstanceByKey with processKey: "
					+ processKey + " and variables: " + inputVariables);

				msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker
						+ "Call to MSO workflow/services in Camunda. Received MSO startProcessInstanceByKey with processKey:"
						+ processKey + " and variables: " + inputVariables);
				
				RuntimeService runtimeService = getProcessEngineServices().getRuntimeService();
				ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
					processKey, businessKey, inputVariables);
				processInstanceId = processInstance.getId();

				msoLogger.debug(logMarker + "Process " + processKey + ":" + processInstanceId + " " +
						(processInstance.isEnded() ? "ENDED" : "RUNNING"));
			} catch (Exception e) {

				msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, 
						logMarker + "Error in starting the process: "+ e.getMessage());
				
				WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
				callbackResponse.setStatusCode(500);
				callbackResponse.setMessage("Fail");
				callbackResponse.setResponse("Error occurred while executing the process: " + e);

				// TODO: is the processInstanceId used by the API handler?  I don't think so.
				// It may be null here.
				WorkflowContextHolder.getInstance().processCallback(
					processKey, processInstanceId,
					getRequestId(inputVariables),
					callbackResponse);
			}
		}
	}
	
	
	/**
	 * Callback resource which is invoked from BPMN to process to send the workflow response
	 * 
	 * @param processKey
	 * @param processInstanceId
	 * @param requestId
	 * @param callbackResponse
	 * @return
	 */
	@POST
	@Path("/services/callback/{processKey}/{processInstanceId}/{requestId}")
	@Produces("application/json")
	@Consumes("application/json")
	public Response processWorkflowCallback(
			@PathParam("processKey") String processKey,
			@PathParam("processInstanceId") String processInstanceId,
			@PathParam("requestId")String requestId,
			WorkflowCallbackResponse callbackResponse) {

		msoLogger.debug(logMarker + "Process instance ID:" + processInstanceId + ":" + requestId + ":" + processKey + ":" + isProcessEnded(processInstanceId));
		msoLogger.debug(logMarker + "About to process the callback request:" + callbackResponse.getResponse() + ":" + callbackResponse.getMessage() + ":" + callbackResponse.getStatusCode());
		return contextHolder.processCallback(processKey, processInstanceId, requestId, callbackResponse);
	}
	
    private static String getOrCreate(Map<String, Object> inputVariables, String key) {
        String value = Objects.toString(inputVariables.get(key), null);
        if (value == null) {
            value = UUID.randomUUID().toString();
            inputVariables.put(key, value);
        }
        return value;
    }
	
	// Note: the business key is used to identify the process in unit tests
	private static String getBusinessKey(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-business-key");
	}

	private static String getRequestId(Map<String, Object> inputVariables) {
        return getOrCreate(inputVariables, "mso-request-id");
	}

	private long getWaitTime(Map<String, Object> inputVariables)
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
	
	private void recordEvents(String processKey, WorkflowResponse response,
			long startTime) {
		
		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + response.getMessage() + " for processKey: "
				+ processKey + " with response: " + response.getResponse(), "BPMN", MDC.get(processKey), null);
		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + response.getMessage() + "for processKey: " + processKey + " with response: " + response.getResponse());
		
	}

	private static void setLogContext(String processKey,
			Map<String, Object> inputVariables) {
		MsoLogger.setServiceName("MSO." + processKey);
		if (inputVariables != null) {
			MsoLogger.setLogContext(getKeyValueFromInputVariables(inputVariables,"mso-request-id"), getKeyValueFromInputVariables(inputVariables,"mso-service-instance-id"));
		}
	}

	private static String getKeyValueFromInputVariables(Map<String,Object> inputVariables, String key) {
		if (inputVariables == null) {
			return "";
		}

		return Objects.toString(inputVariables.get(key), "N/A");
	}

	private boolean isProcessEnded(String processInstanceId) {
		ProcessEngineServices pes = getProcessEngineServices();
		return pes.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult() == null;
	}
	
	
	private static Map<String, Object> getInputVariables(VariableMapImpl variableMap) {
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
}
