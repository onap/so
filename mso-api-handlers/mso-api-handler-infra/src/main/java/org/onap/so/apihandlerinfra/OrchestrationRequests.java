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

package org.onap.so.apihandlerinfra;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;

import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;

import org.onap.so.serviceinstancebeans.GetOrchestrationListResponse;
import org.onap.so.serviceinstancebeans.GetOrchestrationResponse;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestList;
import org.onap.so.serviceinstancebeans.RequestStatus;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("onap/so/infra/orchestrationRequests")
@Api(value="onap/so/infra/orchestrationRequests",description="API Requests for Orchestration requests")
@Component
public class OrchestrationRequests {

    private static Logger logger = LoggerFactory.getLogger(OrchestrationRequests.class);
    

    @Autowired
	private RequestsDbClient requestsDbClient;

    @Autowired
    private MsoRequest msoRequest;
    
	@Autowired
	private ResponseBuilder builder;

	@GET
	@Path("/{version:[vV][4-7]}/{requestId}")
	@ApiOperation(value="Find Orchestrated Requests for a given requestId",response=Response.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response getOrchestrationRequest(@PathParam("requestId") String requestId, @PathParam("version") String version) throws ApiException{

		String apiVersion = version.substring(1);
		GetOrchestrationResponse orchestrationResponse = new GetOrchestrationResponse();


		InfraActiveRequests infraActiveRequest = null;
		List<org.onap.so.db.request.beans.RequestProcessingData> requestProcessingData = null;
		try {
			infraActiveRequest = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
	        requestProcessingData = requestsDbClient.getRequestProcessingDataBySoRequestId(requestId);

		} catch (Exception e) {
		    logger.error("Exception occurred", e);
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.AvailabilityError).build();




			ValidateException validateException = new ValidateException.Builder("Exception while communciate with Request DB - Infra Request Lookup",
					HttpStatus.SC_NOT_FOUND,ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB).cause(e).errorInfo(errorLoggerInfo).build();


			throw validateException;

		}
		
        if(infraActiveRequest == null) {

            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, ErrorCode.BusinessProcesssError).build();


            ValidateException validateException = new ValidateException.Builder("Orchestration RequestId " + requestId + " is not found in DB",
                    HttpStatus.SC_NO_CONTENT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

            throw validateException;
        }
        
        Request request = mapInfraActiveRequestToRequest(infraActiveRequest);
        if(!requestProcessingData.isEmpty()){
            request.setRequestProcessingData(mapRequestProcessingData(requestProcessingData));
        }
		request.setRequestId(requestId);
        orchestrationResponse.setRequest(request);
        
        return builder.buildResponse(HttpStatus.SC_OK, requestId, orchestrationResponse, apiVersion);
	}

	@GET
	@Path("/{version:[vV][4-7]}")
	@ApiOperation(value="Find Orchestrated Requests for a URI Information",response=Response.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response getOrchestrationRequest(@Context UriInfo ui, @PathParam("version") String version) throws ApiException{

		long startTime = System.currentTimeMillis ();
		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		List<InfraActiveRequests> activeRequests = null;

		GetOrchestrationListResponse orchestrationList = null;
		Map<String, List<String>> orchestrationMap;
		String apiVersion = version.substring(1);
		
		try {
			orchestrationMap = msoRequest.getOrchestrationFilters(queryParams);
			if (orchestrationMap.isEmpty()) {
				throw new ValidationException("At least one filter query param must be specified");
			}
		}catch(ValidationException ex){
		    logger.error("Exception occurred", ex);
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.DataError).build();
			ValidateException validateException = new ValidateException.Builder(ex.getMessage(),
					HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).cause(ex).errorInfo(errorLoggerInfo).build();
			throw validateException;

		}
			
		activeRequests = requestsDbClient.getOrchestrationFiltersFromInfraActive(orchestrationMap);

		orchestrationList = new GetOrchestrationListResponse();
		List<RequestList> requestLists = new ArrayList<>();
		
		for(InfraActiveRequests infraActive : activeRequests){
			List<RequestProcessingData> requestProcessingData = requestsDbClient.getRequestProcessingDataBySoRequestId(infraActive.getRequestId());
			RequestList requestList = new RequestList();
			Request request = mapInfraActiveRequestToRequest(infraActive);
			if(!requestProcessingData.isEmpty()){
				request.setRequestProcessingData(mapRequestProcessingData(requestProcessingData));
	        }
			requestList.setRequest(request);
			requestLists.add(requestList);
		}

		orchestrationList.setRequestList(requestLists);
		return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationList, apiVersion);
	}


	@POST
	@Path("/{version: [vV][4-7]}/{requestId}/unlock")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Unlock Orchestrated Requests for a given requestId",response=Response.class)
	@Transactional
	public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId, @PathParam("version") String version) throws ApiException{

		long startTime = System.currentTimeMillis ();
		logger.debug ("requestId is: {}", requestId);
		ServiceInstancesRequest sir = null;

		InfraActiveRequests infraActiveRequest = null;
		Request request = null;
		
		try{
			ObjectMapper mapper = new ObjectMapper();
			sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		} catch(IOException e){
		    logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
            ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : " + e.getMessage(),
                    HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

            throw validateException;

		}
		try{
			msoRequest.parseOrchestration(sir);
		} catch (Exception e) {
		    logger.error("Exception occurred", e);
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
			 ValidateException validateException = new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
	                 .errorInfo(errorLoggerInfo).build();
            throw validateException;
		}

		infraActiveRequest = requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
		if(infraActiveRequest == null) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.BusinessProcesssError).build();


			ValidateException validateException = new ValidateException.Builder("Null response from RequestDB when searching by RequestId",
					HttpStatus.SC_NOT_FOUND, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

			throw validateException;

		}else{
			String status = infraActiveRequest.getRequestStatus();
			if(status.equalsIgnoreCase("IN_PROGRESS") || status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("PENDING_MANUAL_TASK")){
				infraActiveRequest.setRequestStatus("UNLOCKED");
				infraActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
				infraActiveRequest.setRequestId(requestId);
				requestsDbClient.save(infraActiveRequest);
			}else{

				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, ErrorCode.DataError).build();


				ValidateException validateException = new ValidateException.Builder("Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked",
						HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

				throw validateException;
			}
		}
		return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
	}

    private Request mapInfraActiveRequestToRequest(InfraActiveRequests iar)  throws ApiException{

    	String requestBody = iar.getRequestBody();
    	Request request = new Request();
    	
        ObjectMapper mapper = new ObjectMapper();      

       request.setRequestId(iar.getRequestId());
       request.setRequestScope(iar.getRequestScope());
       request.setRequestType(iar.getRequestAction());
       String rollbackStatusMessage = iar.getRollbackStatusMessage();
       String flowStatusMessage = iar.getFlowStatus();
       String retryStatusMessage = iar.getRetryStatusMessage();
       

       InstanceReferences ir = new InstanceReferences();
       if(iar.getNetworkId() != null)
       	ir.setNetworkInstanceId(iar.getNetworkId());
       if(iar.getNetworkName() != null)
       	ir.setNetworkInstanceName(iar.getNetworkName());
       if(iar.getServiceInstanceId() != null)
       	ir.setServiceInstanceId(iar.getServiceInstanceId());
       if(iar.getServiceInstanceName() != null)
       	ir.setServiceInstanceName(iar.getServiceInstanceName());
       if(iar.getVfModuleId() != null)
       	ir.setVfModuleInstanceId(iar.getVfModuleId());
       if(iar.getVfModuleName() != null)
       	ir.setVfModuleInstanceName(iar.getVfModuleName());
       if(iar.getVnfId() != null)
       	ir.setVnfInstanceId(iar.getVnfId());
       if(iar.getVnfName() != null)
       	ir.setVnfInstanceName(iar.getVnfName());
       if(iar.getVolumeGroupId() != null)
       	ir.setVolumeGroupInstanceId(iar.getVolumeGroupId());
       if(iar.getVolumeGroupName() != null)
       	ir.setVolumeGroupInstanceName(iar.getVolumeGroupName());
		if(iar.getRequestorId() != null)
			ir.setRequestorId(iar.getRequestorId());
		if(iar.getInstanceGroupId() != null)
			ir.setInstanceGroupId(iar.getInstanceGroupId());
		if(iar.getInstanceGroupName() != null)
			ir.setInstanceGroupName(iar.getInstanceGroupName());
			


		request.setInstanceReferences(ir);

       RequestDetails requestDetails = null;

       if(StringUtils.isNotBlank(requestBody)) {
		   try {
			   if(requestBody.contains("\"requestDetails\":")){
				   ServiceInstancesRequest sir = mapper.readValue(requestBody, ServiceInstancesRequest.class);
				   requestDetails = sir.getRequestDetails();
			   } else {
				   requestDetails = mapper.readValue(requestBody, RequestDetails.class);
			   }
		   } catch (IOException e) {
		       logger.error("Exception occurred", e);
			   ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).build();
			   ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed : ",
					   HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();

			   throw validateException;
		   }
	   }
       request.setRequestDetails(requestDetails);
       
       if(iar.getStartTime() != null) {
	       String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getStartTime()) + " GMT";
	       request.setStartTime(startTimeStamp);
       }
       if(iar.getEndTime() != null){
    	   String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getEndTime()) + " GMT";
    	   request.setFinishTime(endTimeStamp);
       }
       String statusMessages = null;
       RequestStatus status = new RequestStatus();
       if(iar.getStatusMessage() != null){
    	  statusMessages = "STATUS: " + iar.getStatusMessage();
       }
       if(flowStatusMessage != null){
    	   if(statusMessages != null){
    		   statusMessages = statusMessages + " " + "FLOW STATUS: " + flowStatusMessage;
    	   }else{
    		   statusMessages = "FLOW STATUS: " + flowStatusMessage;
    	   }
       }
       if(retryStatusMessage != null){
    	   if(statusMessages != null){
    		   statusMessages = statusMessages + " " + "RETRY STATUS: " + retryStatusMessage;
    	   }else{
    		   statusMessages = "RETRY STATUS: " + retryStatusMessage;
    	   }
       }
       if(rollbackStatusMessage != null){
    	   if(statusMessages != null){
    		   statusMessages = statusMessages + " " + "ROLLBACK STATUS: " + rollbackStatusMessage;
    	   }else{
    		   statusMessages = "ROLLBACK STATUS: " + rollbackStatusMessage;
    	   }
       }
       if(statusMessages != null){
    	   status.setStatusMessage(statusMessages);
       }
       if(iar.getModifyTime() != null){
    	   String timeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getModifyTime()) + " GMT";
    	   status.setTimeStamp(timeStamp);
       }


       if(iar.getRequestStatus() != null){
    	   status.setRequestState(iar.getRequestStatus());
       }

       if(iar.getProgress() != null){
    	   status.setPercentProgress(iar.getProgress().intValue());
       }

       request.setRequestStatus(status);

       return request;
   }
   
   public List<org.onap.so.serviceinstancebeans.RequestProcessingData> mapRequestProcessingData(List<org.onap.so.db.request.beans.RequestProcessingData> processingData){
	   List<org.onap.so.serviceinstancebeans.RequestProcessingData> addedRequestProcessingData = new ArrayList<>();
	   org.onap.so.serviceinstancebeans.RequestProcessingData finalProcessingData = new org.onap.so.serviceinstancebeans.RequestProcessingData();
	   String currentGroupingId = null;
	   HashMap<String, String> tempMap = new HashMap<>();
	   List<HashMap<String, String>> tempList = new ArrayList<>();
	   for(RequestProcessingData data : processingData){
		   String groupingId = data.getGroupingId();
		   String tag = data.getTag();
		   if(currentGroupingId == null || !currentGroupingId.equals(groupingId)){
			   if(!tempMap.isEmpty()){
				   tempList.add(tempMap);
				   finalProcessingData.setDataPairs(tempList);
				   addedRequestProcessingData.add(finalProcessingData);
			   }
			   finalProcessingData = new org.onap.so.serviceinstancebeans.RequestProcessingData();
			   if(groupingId != null){
				   finalProcessingData.setGroupingId(groupingId);
			   }
			   if(tag != null){
				   finalProcessingData.setTag(tag);
			   }
			   currentGroupingId = groupingId;
			   tempMap = new HashMap<>();
			   tempList = new ArrayList<>();
			   if(data.getName() != null && data.getValue() != null){
				   tempMap.put(data.getName(), data.getValue());
			   }
		   }else{
			   if(data.getName() != null && data.getValue() != null){
				   tempMap.put(data.getName(), data.getValue());
			   }
		   }
	   }
	   if(tempMap.size() > 0){
		   tempList.add(tempMap);
		   finalProcessingData.setDataPairs(tempList);
	   }
	   addedRequestProcessingData.add(finalProcessingData);
	   return addedRequestProcessingData;
   }
 }
