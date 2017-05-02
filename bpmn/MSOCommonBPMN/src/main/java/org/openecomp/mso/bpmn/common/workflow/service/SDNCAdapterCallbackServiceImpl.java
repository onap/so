/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.core.Context;
import javax.xml.ws.WebServiceContext;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCAdapterResponse;
import org.openecomp.mso.bpmn.common.adapter.sdnc.SDNCCallbackAdapterPortType;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
/**
 * @version 1.0
 *  
 */
@WebService(serviceName="SDNCAdapterCallbackService", targetNamespace="http://org.openecomp/workflow/sdnc/adapter/schema/v1")
public class SDNCAdapterCallbackServiceImpl implements SDNCCallbackAdapterPortType {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);
	private final int DEFAULT_RETRY_ATTEMPTS = 60;
	private final int DEFAULT_SLEEP_TIME = 500;

	private final String logMarker = "[SDNC-CALLBACK]";

	@Context WebServiceContext wsContext;

	private volatile ProcessEngineServices pes4junit = null;

	@WebMethod(operationName = "SDNCAdapterCallback")
    @WebResult(name = "SDNCAdapterResponse", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1", partName = "SDNCAdapterCallbackResponse")
    public SDNCAdapterResponse sdncAdapterCallback(
            @WebParam(name = "SDNCAdapterCallbackRequest", targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/schema/v1", partName = "SDNCAdapterCallbackRequest")
            SDNCAdapterCallbackRequest sdncAdapterCallbackRequest) {

		//Callback URL to use http://localhost:28080/mso/SDNCAdapterCallbackService
		ProcessEngineServices pes = getProcessEngineServices();
		RuntimeService runtimeService = pes.getRuntimeService();
		String receivedRequestId = sdncAdapterCallbackRequest.getCallbackHeader().getRequestId();
		MsoLogger.setServiceName("MSO." + "sdncAdapter");
		MsoLogger.setLogContext(receivedRequestId, "N/A");
		msoLogger.debug(logMarker + "Received callback response:" + sdncAdapterCallbackRequest.toString());
		SDNCAdapterResponse sdncAdapterResponse;
		long startTime = System.currentTimeMillis();

		/*Correlating the response with the running instance*/

		// NOTE: the following loop is a workaround for problems we've had
		// with reliability of the runtime service.  It seems that queries
		// sometimes return results, and sometimes they don't.  This might
		// be a problem in mysql only.  We aren't sure if it affects camunda
		// on oracle or mariadb.  The workaround is to repeat the request
		// a number of times until it succeeds.  If it doesn't succeed after
		// 60 tries, then we give up.

		int maxAttempts = DEFAULT_RETRY_ATTEMPTS;
		int attempt = 1;
		int sleepTime = DEFAULT_SLEEP_TIME;

		Map<String,String> bpmnProperties = getMSOBPMNURNProperties();
		if (bpmnProperties != null) {
			try {
				maxAttempts = Integer.parseInt(bpmnProperties.get("mso.callbackRetryAttempts"));
				msoLogger.debug(logMarker + "mso.callbackRetryAttempts=" + maxAttempts);
				sleepTime = Integer.parseInt(bpmnProperties.get("mso.callbackRetrySleepTime"));
				msoLogger.debug(logMarker + "mso.callbackRetrySleepTime:" + sleepTime);
			} catch (Exception ex) {
				
			msoLogger.debug (logMarker 						
						+ "Error parsing mso.callbackRetrySleepTime/mso.callbackRetryAttempts:" 
						+ sleepTime + ":" 
						+ maxAttempts);
				
			}
		}

		/* Check to make sure the process instance is reay for correlation*/
		try{
			isReadyforCorrelation(runtimeService, receivedRequestId, maxAttempts, sleepTime );
		}catch(Exception e){
			String msg =
				"SDNC Adapter Callback Service received a SDNC Adapter Callback Request with RequestId '"
						+ receivedRequestId
						+ "' but that RequestId doesn't exist or has timed out waiting for the callback";
			sdncAdapterResponse = new SDNCAdapterExceptionResponse(e);
			
			msoLogger.error (MessageEnum.BPMN_SDNC_CALLBACK_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
					MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
			
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker 
					+ "Completed the execution of MSO SDNCAdapterCallbackService." );
			
			msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
					logMarker + "Completed the execution of MSO SDNCAdapterCallbackService.", "BPMN", 
					MsoLogger.getServiceName(), "sdncAdapterCallback");
			
			return sdncAdapterResponse;
		}

		msoLogger.debug(logMarker + "*** Received MSO sdncAdapterCallbackService ******");
		
		msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Call to MSO sdncAdapterCallbackService");		
		
		msoLogger.debug(logMarker + "Callback response string:\n"  + sdncAdapterCallbackRequest.toString());

		String reqId = receivedRequestId;
		Map<String,Object> variables = new HashMap<String,Object>();
		variables.put("SDNCA_requestId", reqId );
		variables.put("sdncAdapterCallbackRequest", sdncAdapterCallbackRequest.toString());
		while (true) {
			try {
				// sdncAdapterCallbackRequest is the message event name (defined in the bpmn process)
				runtimeService.createMessageCorrelation("sdncAdapterCallbackRequest")
					.setVariables(variables)
					.processInstanceVariableEquals("SDNCA_requestId", reqId).correlate();
				sdncAdapterResponse = new SDNCAdapterResponse();
				msoLogger.debug(logMarker + "***** Completed processing of MSO sdncAdapterCallbackService ******");
				
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker 
						+ "Completed the execution of MSO SDNCAdapterCallbackService.");
				
				msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
						logMarker + "Completed the execution of MSO SDNCAdapterCallbackService.", "BPMN", 
						MsoLogger.getServiceName(), "sdncAdapterCallback");
				
				return sdncAdapterResponse;
			} catch(MismatchingMessageCorrelationException e) {
				msoLogger.debug(logMarker + "[CORM]correlation id mismatch (attempt " + attempt + "/" + maxAttempts + ")");
				if (attempt == maxAttempts) {
					// Couldn't correlate requestId to any active flow
					//MsoLogger logger = MsoLogger.getMsoLogger("SDNCAdapterCallbackService");
					String msg =
						"SDNC Adapter Callback Service received a SDNC Adapter Callback Request with RequestId '"
								+ receivedRequestId
								+ "' but that RequestId could not be correlated to any active process - ignoring the Request";
					sdncAdapterResponse = new SDNCAdapterExceptionResponse(e);
					
					msoLogger.error (MessageEnum.BPMN_SDNC_CALLBACK_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
							MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
					
					msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker 
							+ "Completed the execution of MSO SDNCAdapterCallbackService." );
					
					msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
							logMarker + "Completed the execution of MSO SDNCAdapterCallbackService.", "BPMN", 
							MsoLogger.getServiceName(), "sdncAdapterCallback");
					
					return sdncAdapterResponse;
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e2) {
					String msg =
						"SDNC Adapter Callback Service received a SDNC Adapter Callback Request with RequestId '"
								+ receivedRequestId
								+ "' but correlation was interrupted";
					sdncAdapterResponse = new SDNCAdapterExceptionResponse(e);
					
					msoLogger.error (MessageEnum.BPMN_SDNC_CALLBACK_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
							MsoLogger.ErrorCode.UnknownError, logMarker + ":" + msg, e);
					
					msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, logMarker 
							+ "Completed the execution of MSO SDNCAdapterCallbackService.");
					
					msoLogger.recordMetricEvent ( startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, 
							logMarker + "Completed the execution of MSO SDNCAdapterCallbackService.", "BPMN", 
							MsoLogger.getServiceName(), "sdncAdapterCallback");
					
					return sdncAdapterResponse;
				}
			}

			attempt++;
		}
	}


	private Map<String,String> getMSOBPMNURNProperties() {
		PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
		Map<String,String> props = propertyConfiguration.getProperties("mso.bpmn.urn.properties");
		return props;
	}

	private void isReadyforCorrelation(RuntimeService runtimeService,
			String receivedRequestId, int retries, int sleepTime){
		ExecutionQuery waitingInstances = null;
		long waitingInstancesCount = 0;

		//Workaround for performance testing, explicit wait for a second for the transactions to be committed
		//Also check to make sure the process didn't timeout before trying to correlate
		
		do{
		  waitingInstances = runtimeService.createExecutionQuery() //
					.messageEventSubscriptionName("sdncAdapterCallbackRequest")
					.processVariableValueEquals("SDNCA_requestId", receivedRequestId);
		  waitingInstancesCount = waitingInstances.count();
		  retries--;
		  msoLogger.debug(logMarker + "waitingInstancesCount: " + waitingInstancesCount);
		  try {
				Thread.sleep(sleepTime);
			  } catch (InterruptedException e) {
				
				msoLogger.error (MessageEnum.BPMN_SDNC_CALLBACK_EXCEPTION, "BPMN", MsoLogger.getServiceName(), 
						MsoLogger.ErrorCode.UnknownError, logMarker, e);
				
			  }
		}while (waitingInstancesCount==0 && retries > 0); 
		if(waitingInstancesCount > 0){
			msoLogger.debug(logMarker + "waitingInstancesCount before timeout check: " + waitingInstancesCount);
			waitingInstancesCount = waitingInstances.processVariableValueEquals("asynchronousResponseTimeout", false).count();
			msoLogger.debug(logMarker + "waitingInstancesCount after timeout check: " + waitingInstancesCount);
			if(waitingInstancesCount<=0){
				msoLogger.debug(logMarker + "detected timeout on flow to correlate");
				throw new IllegalStateException("process timed out");
			}
		}else{
			//flow may have already ended, so can't check timeout variable. Throw exception?
			msoLogger.debug(logMarker + "no flow to correlate to");
			throw new IllegalStateException("no flow to correlate to");
		}
	}

	private ProcessEngineServices getProcessEngineServices() {
		if (pes4junit == null) {
			return BpmPlatform.getDefaultProcessEngine();
		} else {
			return pes4junit;
		}
	}

	@WebMethod(exclude=true)
	public void setProcessEngineServices4junit(ProcessEngineServices pes) {
		pes4junit = pes;
	}

	public class SDNCAdapterExceptionResponse extends SDNCAdapterResponse {
		private Exception ex;

		public SDNCAdapterExceptionResponse(Exception ex) {
			super();
			this.ex = ex;
		}

		public Exception getException() {
			return ex;
		}
	}
}
