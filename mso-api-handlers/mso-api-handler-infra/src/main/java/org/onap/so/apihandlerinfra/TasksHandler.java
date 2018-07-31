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

package org.onap.so.apihandlerinfra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClient;
import org.onap.so.apihandler.common.RequestClientFactory;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.AlarmLoggerInfo;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tasksbeans.TaskList;
import org.onap.so.apihandlerinfra.tasksbeans.TaskVariableValue;
import org.onap.so.apihandlerinfra.tasksbeans.TaskVariables;
import org.onap.so.apihandlerinfra.tasksbeans.TasksGetResponse;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("onap/so/infra/tasks")
@Api(value="onap/so/infra/tasks",description="Queries of Manual Tasks")
@Component
public class TasksHandler {

    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH,TasksHandler.class);
       
    @Value("${mso.camunda.rest.task.uri}")
    private String requestUrl;
    
	@Autowired
	private RequestClientFactory reqClientFactory;

	@Autowired
	private ResponseBuilder builder;
	
    @Path("/{version:[vV]1}")
    @GET
    @ApiOperation(value="Finds Manual Tasks",response=Response.class)
    @Transactional
    public Response queryFilters (@QueryParam("taskId") String taskId,
                                  @QueryParam("originalRequestId") String originalRequestId,
                                  @QueryParam("subscriptionServiceType") String subscriptionServiceType,
                                  @QueryParam("nfRole") String nfRole,
                                  @QueryParam("buildingBlockName") String buildingBlockName,
                                  @QueryParam("originalRequestDate") String originalRequestDate,
                                  @QueryParam("originalRequestorId") String originalRequestorId,
                                  @PathParam("version") String version) throws ApiException {
    	Response responseBack = null;

        String requestId = UUIDChecker.generateUUID(msoLogger);
        MsoLogger.setServiceName ("ManualTasksQuery");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
		String apiVersion = version.substring(1);
        
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
       
		HttpResponse response = null;	

		try {
			requestClient = reqClientFactory.getRequestClient(requestUrl);
			// Capture audit event
			ObjectMapper mapper = new ObjectMapper();
			String camundaJsonReq = mapper.writeValueAsString(tv);
			response = requestClient.post(camundaJsonReq);

		} catch(JsonProcessingException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).build();


			ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
					HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

			throw validateException;
		} catch(IOException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).build();
			AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoConfigurationError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL)).build();



			BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(HttpStatus.SC_BAD_GATEWAY),HttpStatus.SC_BAD_GATEWAY,ErrorNumbers.SVC_NO_SERVER_RESOURCES)
					.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();

			throw bpmnFailureException;
		}
		TasksGetResponse trr = new TasksGetResponse();
		List<TaskList> taskList = new ArrayList<>();
		
		ResponseHandler respHandler = new ResponseHandler (response, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();
		if (bpelStatus == HttpStatus.SC_NO_CONTENT || bpelStatus == HttpStatus.SC_ACCEPTED) {			
			String respBody = respHandler.getResponseBody();
			if (respBody != null) {				
				JSONArray data = new JSONArray(respBody);
				
				for (int i=0; i<data.length();i++) {
					JSONObject taskEntry = data.getJSONObject(i);
					String id = taskEntry.getString("id");
					if (taskId != null && !taskId.equals(id)) {
						continue;						
					}
					// Get variables info for each task ID
					TaskList taskListEntry = null;
					taskListEntry = getTaskInfo(id);

					taskList.add(taskListEntry);				
					
				}
				trr.setTaskList(taskList);				
			}
		
		} else {

			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).build();


			BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
					.errorInfo(errorLoggerInfo).build();

			throw bpmnFailureException;
		}
		
		String jsonResponse = null;
		try {
			ObjectMapper mapper = new ObjectMapper();			
			jsonResponse = mapper.writeValueAsString(trr);
		}
		catch (JsonProcessingException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).build();


			ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
					HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

			throw validateException;
		}
		
		return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestId, jsonResponse, apiVersion);
    }    

    protected MsoLogger getMsoLogger () {
        return msoLogger;
    }
    
    // Makes a GET call to Camunda to get variables for this task
    private TaskList getTaskInfo(String taskId) throws ApiException{
    	TaskList taskList;
    	String getRequestUrl = UriBuilder.fromUri(requestUrl).path(taskId).path("variables").build().toString();
		HttpResponse getResponse;
		
		RequestClient requestClient = reqClientFactory.getRequestClient (getRequestUrl);						
		// Capture audit event
		try {
			getResponse = requestClient.get();
		}catch(IOException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).build();
			AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoConfigurationError",
					MsoAlarmLogger.CRITICAL, Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL)).build();



			BPMNFailureException validateException = new BPMNFailureException.Builder(String.valueOf(HttpStatus.SC_BAD_GATEWAY), HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES)
					.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();
			throw validateException;
		}
		ResponseHandler respHandler = new ResponseHandler (getResponse, requestClient.getType ());
		int bpelStatus = respHandler.getStatus ();
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {			
			String respBody = respHandler.getResponseBody();
			if (respBody != null) {
				taskList = buildTaskList(taskId, respBody);				
			}
			else {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).build();
				AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoConfigurationError", MsoAlarmLogger.CRITICAL, Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL)).build();



				BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(HttpStatus.SC_BAD_GATEWAY), HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES)
						.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();
				throw bpmnFailureException;
			}
			
		}
		else {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.AvailabilityError).build();
			AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoConfigurationError", MsoAlarmLogger.CRITICAL, Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_BPEL)).build();



			BPMNFailureException bpmnFailureException = new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus, ErrorNumbers.SVC_NO_SERVER_RESOURCES)
					.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();

			throw bpmnFailureException;
		}
		
    	return taskList;
    	
    }
    
    private TaskList buildTaskList(String taskId, String respBody) throws JSONException {
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
    	
    	String validResponses = getOptVariableValue(variables, "validResponses").toLowerCase();
    	List<String> items = Arrays.asList(validResponses.split("\\s*,\\s*"));
    	taskList.setValidResponses(items);
    	
    	return taskList;       	
    }
    
    private String getOptVariableValue(JSONObject variables, String name) throws JSONException {
    	String variableEntry = variables.optString(name);
    	String value = "";
    	if (!variableEntry.isEmpty()) {
    		JSONObject variableEntryJson = new JSONObject(variableEntry);
    		value = variableEntryJson.optString("value");    		
    	}
    	return value;
    }
   
   
}
