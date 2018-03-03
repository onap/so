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

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Workflow Context Holder instance which can be accessed elsewhere either in groovy scripts or Java
 * @version 1.0
 *
 */
public class WorkflowContextHolder {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private static final String logMarker = "[WORKFLOW-CONTEXT-HOLDER]";
	private static WorkflowContextHolder instance = null;

	/**
	 * Delay Queue which holds workflow context holder objects
	 */
	private final DelayQueue<WorkflowContext> responseQueue = new DelayQueue<>();
	private final TimeoutThread timeoutThread = new TimeoutThread();

	private WorkflowContextHolder() {
		timeoutThread.start();
	}

	/**
	 * Singleton holder which eliminates hot lock
	 * Since the JVM synchronizes static method there is no synchronization needed for this method
	 * @return
	 */
	public static synchronized WorkflowContextHolder getInstance() {
		if (instance == null) {
			instance = new WorkflowContextHolder();
		}
		return instance;
	}
	
	public void put(WorkflowContext context) {
		msoLogger.debug(logMarker + " Adding context to the queue: "
			+ context.getRequestId());
		responseQueue.put(context);
	}
	
	public void remove(WorkflowContext context) {
		msoLogger.debug(logMarker + " Removing context from the queue: "
			+ context.getRequestId());
		responseQueue.remove(context);
	}
	
	public WorkflowContext getWorkflowContext(String requestId) {
		// Note: DelayQueue interator is threadsafe
		for (WorkflowContext context : responseQueue) {
			if (requestId.equals(context.getRequestId())) {
				msoLogger.debug("Found context for request id: " + requestId);
				return context;
			}
		}

		msoLogger.debug("Unable to find context for request id: " + requestId);
		return null;
	}
	
	/**
	 * Builds the callback response object to respond to client
	 * @param processKey
	 * @param processInstanceId
	 * @param requestId
	 * @param callbackResponse
	 * @return
	 */
	public Response processCallback(String processKey, String processInstanceId,
			String requestId, WorkflowCallbackResponse callbackResponse) {
		WorkflowResponse workflowResponse = new WorkflowResponse();
		WorkflowContext workflowContext = getWorkflowContext(requestId);

		if (workflowContext == null) {
			msoLogger.debug("Unable to correlate workflow context for request id: " + requestId
				+ ":processInstance Id:" + processInstanceId
				+ ":process key:" + processKey);
			workflowResponse.setMessage("Fail");
			workflowResponse.setMessageCode(400);
			workflowResponse.setResponse("Unable to correlate workflow context, bad request. Request Id: " + requestId);
			return Response.serverError().entity(workflowResponse).build();
		}

		responseQueue.remove(workflowContext);

		msoLogger.debug("Using callback response for request id: " + requestId);
		workflowResponse.setResponse(callbackResponse.getResponse());
		workflowResponse.setProcessInstanceID(processInstanceId);
		workflowResponse.setMessageCode(callbackResponse.getStatusCode());
		workflowResponse.setMessage(callbackResponse.getMessage());
		sendWorkflowResponseToClient(processKey, workflowContext, workflowResponse);
		return Response.ok().entity(workflowResponse).build();
	}
	
	/**
	 * Send the response to client asynchronously when invoked by the BPMN process
	 * @param processKey
	 * @param workflowContext
	 * @param workflowResponse
	 */
	private void sendWorkflowResponseToClient(String processKey, WorkflowContext workflowContext,
			WorkflowResponse workflowResponse) {
		msoLogger.debug(logMarker + "Sending the response for request id: " + workflowContext.getRequestId());
		recordEvents(processKey, workflowResponse, workflowContext.getStartTime());
		Response response = Response.status(workflowResponse.getMessageCode()).entity(workflowResponse).build();
		AsynchronousResponse asyncResp = workflowContext.getAsynchronousResponse();
		asyncResp.setResponse(response);
	}

	/**
	 * Timeout thread which monitors the delay queue for expired context and send timeout response
	 * to client
	 *git review -R
	 * */
	private class TimeoutThread extends Thread {
		public void run() {
			while (!isInterrupted()) {
				try {
					WorkflowContext requestObject = responseQueue.take();
					msoLogger.debug("Time remaining for request id: " + requestObject.getRequestId() + ":" + requestObject.getDelay(TimeUnit.MILLISECONDS));
					msoLogger.debug("Preparing timeout response for " + requestObject.getProcessKey() + ":" + ":" + requestObject.getRequestId());
					WorkflowResponse response = new WorkflowResponse();
					response.setMessage("Fail");
					response.setResponse("Request timedout, request id:" + requestObject.getRequestId());
					//response.setProcessInstanceID(requestObject.getProcessInstance().getProcessInstanceId());
					recordEvents(requestObject.getProcessKey(), response, requestObject.getStartTime());
					response.setMessageCode(500);
					Response result = Response.status(500).entity(response).build();
					requestObject.getAsynchronousResponse().setResponse(result);
					msoLogger.debug("Sending timeout response for request id:" + requestObject.getRequestId() + ":response:" + response);
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					msoLogger.debug("WorkflowContextHolder timeout thread caught exception: " + e);
				msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
						MsoLogger.ErrorCode.UnknownError, "Error in WorkflowContextHolder timeout thread");
				
				}
			}

			msoLogger.debug("WorkflowContextHolder timeout thread interrupted, quitting");
		}
	}
	
	private static void recordEvents(String processKey, WorkflowResponse response,
			long startTime) {

		msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
				logMarker + response.getMessage() + " for processKey: "
				+ processKey + " with response: " + response.getResponse(), "BPMN", MDC.get(processKey), null);
		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker 
				+ response.getMessage() + " for processKey: " 
				+ processKey + " with response: " + response.getResponse());
		
	}
}
