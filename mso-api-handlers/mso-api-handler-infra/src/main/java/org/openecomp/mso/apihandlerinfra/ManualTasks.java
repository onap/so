/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.apihandlerinfra;

import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.ApiOperation;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandlerinfra.tasksbeans.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@Path("/tasks")
public class ManualTasks {
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
	
	@POST
	@Path("/{version:[vV]1}/{taskId}/complete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Complete specified task",response=Response.class)
	public Response completeTask(String request, @PathParam("version") String version, @PathParam("taskId") String taskId) {
		
		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("requestId is: " + requestId);
		TasksRequest tr = null;
		
		MsoRequest msoRequest = new MsoRequest (requestId);
		
		try{
			ObjectMapper mapper = new ObjectMapper();
			tr= mapper.readValue(request, TasksRequest.class);
			
			if (tr.getRequestDetails() == null) {
				throw new ValidationException("requestDetails");				
			}
			if (tr.getRequestDetails().getRequestInfo() == null) {
				throw new ValidationException("requestInfo");
			}
			if (empty(tr.getRequestDetails().getRequestInfo().getSource())) {
				throw new ValidationException("source");
			}
			if (empty(tr.getRequestDetails().getRequestInfo().getRequestorId())) {
				throw new ValidationException("requestorId");
			}

		} catch(Exception e){
			msoLogger.debug ("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
					"Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, request, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}		
		
		// Transform the request to Camunda-style Complete request
		Variables variablesForComplete = new Variables();
		Value sourceValue = new Value(); 
		sourceValue.setValue(tr.getRequestDetails().getRequestInfo().getSource());
		Value responseValue = new Value(); 
		responseValue.setValue(tr.getRequestDetails().getRequestInfo().getResponseValue().name());
		Value requestorIdValue = new Value(); 
		requestorIdValue.setValue(tr.getRequestDetails().getRequestInfo().getRequestorId());
		variablesForComplete.setSource(sourceValue);
		variablesForComplete.setResponseValue(responseValue);
		variablesForComplete.setRequestorId(requestorIdValue);
		
		String camundaJsonReq = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
			camundaJsonReq = mapper.writeValueAsString(variablesForComplete);
			msoLogger.debug("Camunda Json Request: " + camundaJsonReq);
		} catch(Exception e){
			msoLogger.debug ("Mapping of JSON object to Camunda request failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
				"Mapping of JSON object to Camunda Request failed.  " + e.getMessage(),
				ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
		
			msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.UnknownError, request, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Mapping of JSON object to Camunda request failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}		
		
		RequestClient requestClient = null;
		HttpResponse response = null;
		long subStartTime = System.currentTimeMillis();
		String requestUrl = "/mso/task/" + taskId + "/complete";
		try {
			requestClient = RequestClientFactory.getRequestClient (requestUrl, MsoPropertiesUtils.loadMsoProperties ());
			// Capture audit event
			msoLogger.debug ("MSO API Handler Posting call to Camunda engine for url: " + requestClient.getUrl ());

			System.out.println("URL : " + requestClient.getUrl ());
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Mapping of JSON object to Camunda request failed");
			
			response = requestClient.post(camundaJsonReq);

			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", requestUrl, null);
		} catch (Exception e) {
		    msoLogger.debug ("Exception:", e);
			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN", requestUrl, null);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException,
					"Failed calling bpmn " + e.getMessage (),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			alarmLogger.sendAlarm ("MsoConfigurationError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
			msoRequest.updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
			return resp;
		}

		if (response == null) {
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException,
					"bpelResponse is null",
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			msoRequest.updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from BPEL");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Null response from BPMN");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
			return resp;
		}

		ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();

		// BPEL accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_NO_CONTENT || bpelStatus == HttpStatus.SC_ACCEPTED) {			
			msoLogger.debug ("Received good response from Camunda");
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.IN_PROGRESS);			
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN completed the request");
			TaskRequestReference trr = new TaskRequestReference();
			trr.setTaskId(taskId);
			String completeResp = null;
			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
				completeResp = mapper.writeValueAsString(trr);
			}
			catch (Exception e) {
				msoLogger.debug("Unable to format response",e);
				Response resp = msoRequest.buildServiceErrorResponse(bpelStatus,
						MsoException.ServiceException,
						"Request Failed due to bad response format" ,
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
						null);				
				msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Bad response format");
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Bad response format");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
				return resp;
			}
			msoLogger.debug("Response to the caller: " + completeResp);			
			msoLogger.debug ("End of the transaction, the final response is: " + (String) completeResp);
			return Response.status (HttpStatus.SC_ACCEPTED).entity (completeResp).build ();
		} else {			
				msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
				Response resp = msoRequest.buildServiceErrorResponse(bpelStatus,
						MsoException.ServiceException,
						"Request Failed due to BPEL error with HTTP Status= %1" ,
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
						null);				
				msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Response from BPEL engine is empty");
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Response from BPEL engine is empty");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
				return resp;
		}	
	
	}
	
	private static boolean empty(String s) {
  	  return (s == null || s.trim().isEmpty());
  }
		
}
