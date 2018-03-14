/*-
 * #%L
 * MSO
 * %%
 * Copyright (C) 2016 ONAP - SO
 * %%
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
 * #L%
 */

package org.openecomp.mso.apihandlerinfra;

import org.openecomp.mso.apihandlerinfra.tasksbeans.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/tasks")
@Api(value="/tasks/{version: [vV]1}",description="Queries of Manual Tasks")
public class TasksHandler {

    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    public final static String requestUrl = "mso/task/";	

    @Path("/{version:[vV]1}")
    @GET
    @ApiOperation(value="Finds Manual Tasks",response=Response.class)
    public Response queryFilters (@QueryParam("taskId") String taskId,
                                  @QueryParam("originalRequestId") String originalRequestId,
                                  @QueryParam("subscriptionServiceType") String subscriptionServiceType,
                                  @QueryParam("nfRole") String nfRole,
                                  @QueryParam("buildingBlockName") String buildingBlockName,
                                  @QueryParam("originalRequestDate") String originalRequestDate,
                                  @QueryParam("originalRequestorId") String originalRequestorId,
                                  @PathParam("version") String version) throws ParseException {
    	Response responseBack = null;
        long startTime = System.currentTimeMillis ();
        String requestId = UUIDChecker.generateUUID(msoLogger);
        MsoLogger.setServiceName ("ManualTasksQuery");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for queryFilter with taskId:" + taskId
        							+ " originalRequestId:" + originalRequestId
        							+ " subscriptionServiceType:" + subscriptionServiceType
        							+ " nfRole:" + nfRole
        							+ " buildingBlockName:" + buildingBlockName
        							+ " originalRequestDate:" + originalRequestDate
        							+ " originalRequestorId: " + originalRequestorId);
        
        // Prepare the query string to /task interface
        TaskVariables tv = new TaskVariables();
        
        List<TaskVariableValue> tvvList = new ArrayList<>();
        
        if (originalRequestId != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("originalRequestId");
        	tvv.setValue(originalRequestId);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }
        if (subscriptionServiceType != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("subscriptionServiceType");
        	tvv.setValue(subscriptionServiceType);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }
        if (nfRole != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("nfRole");
        	tvv.setValue(nfRole);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }
        if (buildingBlockName != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("buildingBlockName");
        	tvv.setValue(buildingBlockName);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }
        if (originalRequestDate != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("originalRequestDate");
        	tvv.setValue(originalRequestDate);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }
        if (originalRequestorId != null) {
        	TaskVariableValue tvv = new TaskVariableValue();
        	tvv.setName("originalRequestorId");
        	tvv.setValue(originalRequestorId);
        	tvv.setOperator("eq");
        	tvvList.add(tvv);        
        }       
      
        tv.setTaskVariables(tvvList);
       
        RequestClient requestClient = null;
        MsoRequest msoRequest = new MsoRequest(requestId);
		HttpResponse response = null;
		long subStartTime = System.currentTimeMillis();		
				
		try {
			requestClient = RequestClientFactory.getRequestClient (requestUrl, MsoPropertiesUtils.loadMsoProperties ());
			// Capture audit event
			msoLogger.debug ("MSO API Handler Post call to Camunda engine for url: " + requestClient.getUrl ());

			System.out.println("URL : " + requestClient.getUrl ());
			ObjectMapper mapper = new ObjectMapper();			
			String camundaJsonReq = mapper.writeValueAsString(tv);
			msoLogger.debug("Camunda Json Request: " + camundaJsonReq);
			response = requestClient.post(camundaJsonReq);

			msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", requestUrl, null);
		} catch (Exception e) {
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
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity (),e);
			return resp;
		}
		TasksGetResponse trr = new TasksGetResponse();
		List<TaskList> taskList = new ArrayList<>();
		
		ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();
		if (bpelStatus == HttpStatus.SC_NO_CONTENT || bpelStatus == HttpStatus.SC_ACCEPTED) {			
			msoLogger.debug ("Received good response from Camunda");
						
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN completed the request");
			String respBody = respHandler.getResponseBody();		
			if (respBody != null) {				
				JSONArray data = new JSONArray(respBody);
				
				for (int i=0; i<data.length();i++) {
					JSONObject taskEntry = data.getJSONObject(i);
					String id = taskEntry.getString("id");
					msoLogger.debug("taskId is: " + id);
					if (taskId != null && !taskId.equals(id)) {
						continue;						
					}
					// Get variables info for each task ID
					TaskList taskListEntry = null;
					try {
						taskListEntry = getTaskInfo(id);
						msoLogger.recordMetricEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successfully received response from BPMN engine", "BPMN", requestUrl, null);
					} catch (Exception e) {
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
						
						msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
						msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine");
						msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity (),e);
						return resp;
					}
					taskList.add(taskListEntry);				
					
				}
				trr.setTaskList(taskList);				
			}
		
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
			
		
		String jsonResponse = null;
		try {
			ObjectMapper mapper = new ObjectMapper();			
			jsonResponse = mapper.writeValueAsString(trr);
		}
		catch (Exception e) {
			msoLogger.debug("Unable to format response",e);
			Response resp = msoRequest.buildServiceErrorResponse(500,
					MsoException.ServiceException,
					"Request Failed due to bad response format" ,
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
					null);				
			msoLogger.error (MessageEnum.APIH_BPEL_RESPONSE_ERROR, requestClient.getUrl (), "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Bad response format");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError, "Bad response format");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
			return resp;
		}
		
        
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        responseBack = Response.status (HttpStatus.SC_ACCEPTED).entity (jsonResponse).build ();
        return responseBack;
		
    }    

    protected MsoLogger getMsoLogger () {
        return msoLogger;
    }
    
    // Makes a GET call to Camunda to get variables for this task
    private TaskList getTaskInfo(String taskId) throws Exception {
    	TaskList taskList;
    	String getRequestUrl = requestUrl + taskId + "/variables";
		HttpResponse getResponse;
		long subStartTime = System.currentTimeMillis();
		
		RequestClient requestClient = RequestClientFactory.getRequestClient (getRequestUrl, MsoPropertiesUtils.loadMsoProperties ());						
		// Capture audit event						
		msoLogger.debug ("MSO API Handler Get call to Camunda engine for url: " + requestClient.getUrl ());
		getResponse = requestClient.get();
		
		ResponseHandler respHandler = new ResponseHandler (getResponse, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {			
			msoLogger.debug ("Received good response from Camunda");
						
			msoLogger.recordAuditEvent (subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "BPMN completed the request");
			String respBody = respHandler.getResponseBody();		
			if (respBody != null) {
				taskList = buildTaskList(taskId, respBody);				
			}
			else {
				throw new Exception("Null task info from Camunda");
			}
			
		}
		else {
			throw new Exception ("Bad GET response from Camunda. Status is " + bpelStatus);
		}		
		
    	return taskList;
    	
    }
    
    private TaskList buildTaskList(String taskId, String respBody) throws ParseException {
    	TaskList taskList = new TaskList();
    	JSONObject variables = new JSONObject(respBody);
    	
    	taskList.setTaskId(taskId);
    	taskList.setType(getOptVariableValue(variables, "type"));
    	taskList.setNfRole(getOptVariableValue(variables, "nfRole"));
    	taskList.setSubscriptionServiceType(getOptVariableValue(variables, "subscriptionServiceType"));
    	taskList.setOriginalRequestId(getOptVariableValue(variables, "originalRequestId"));
    	taskList.setOriginalRequestorId(getOptVariableValue(variables, "originalRequestorId"));
    	taskList.setErrorSource(getOptVariableValue(variables, "errorSource"));
    	taskList.setErrorCode(getOptVariableValue(variables, "errorCode"));
    	taskList.setErrorMessage(getOptVariableValue(variables, "errorMessage"));
    	taskList.setBuildingBlockName(getOptVariableValue(variables, "buildingBlockName"));
    	taskList.setBuildingBlockStep(getOptVariableValue(variables, "buildingBlockStep"));  
    	taskList.setValidResponses(new JSONArray("[" + getOptVariableValue(variables, "validResponses").toLowerCase() + "]"));
    	
    	return taskList;       	
    }
    
    private String getOptVariableValue(JSONObject variables, String name) throws ParseException {
    	String variableEntry = variables.optString(name);
    	String value = "";
    	if (!variableEntry.isEmpty()) {
    		JSONObject variableEntryJson = new JSONObject(variableEntry);
    		value = variableEntryJson.optString("value");    		
    	}
    	return value;
    }
   
   
}
