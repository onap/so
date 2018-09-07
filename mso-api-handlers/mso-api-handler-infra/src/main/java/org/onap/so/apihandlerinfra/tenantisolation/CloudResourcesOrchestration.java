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

package org.onap.so.apihandlerinfra.tenantisolation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.Messages;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.AlarmLoggerInfo;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.CloudOrchestrationRequestList;
import org.onap.so.apihandlerinfra.tenantisolationbeans.CloudOrchestrationResponse;
import org.onap.so.apihandlerinfra.tenantisolationbeans.InstanceReferences;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Request;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestStatus;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoAlarmLogger;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@Path("onap/so/infra/cloudResourcesRequests")
@Api(value="onap/so/infra/cloudResourcesRequests",description="API GET Requests for cloud resources - Tenant Isolation")
public class CloudResourcesOrchestration {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, CloudResourcesOrchestration.class);

	@Autowired
	RequestsDbClient requestDbClient;

	@Autowired
	private ResponseBuilder builder;
	
	@POST
	@Path("/{version: [vV][1]}/{requestId}/unlock")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Unlock CloudOrchestration requests for a specified requestId")
	@Transactional
	public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId, @PathParam("version") String version) throws ApiException{
		TenantIsolationRequest msoRequest = new TenantIsolationRequest(requestId);
		InfraActiveRequests infraActiveRequest = null;

		CloudOrchestrationRequest cor = null;

		msoLogger.debug ("requestId is: " + requestId);
		
		try{
			ObjectMapper mapper = new ObjectMapper();
			cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		} catch(IOException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,MsoLogger.ErrorCode.SchemaError).build();

			ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,ErrorNumbers.SVC_BAD_PARAMETER)
					.cause(e).errorInfo(errorLoggerInfo).build();
			throw validateException;
		}

		try{
			msoRequest.parseOrchestration(cor);
		} catch (ValidationException e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,MsoLogger.ErrorCode.SchemaError).build();
			ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_BAD_REQUEST,ErrorNumbers.SVC_BAD_PARAMETER)
					.cause(e).errorInfo(errorLoggerInfo).build();
			throw validateException;
		}
		try {
			infraActiveRequest = requestDbClient.getInfraActiveRequestbyRequestId(requestId);
		}catch(Exception e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.AvailabilityError).build();
			AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
					Messages.getErrors().get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB)).build();
			ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
					.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();

			throw validateException;
		}
		if(infraActiveRequest == null) {

			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND,MsoLogger.ErrorCode.BusinessProcesssError).build();
			ValidateException validateException = new ValidateException.Builder("Orchestration RequestId " + requestId + " is not found in DB", HttpStatus.SC_BAD_REQUEST,ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
					.errorInfo(errorLoggerInfo).build();

			throw validateException;

		}else{
			String status = infraActiveRequest.getRequestStatus();
			if(status.equalsIgnoreCase("IN_PROGRESS") || status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("PENDING_MANUAL_TASK")){
				infraActiveRequest.setRequestStatus("UNLOCKED");
				infraActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
				infraActiveRequest.setRequestId(requestId);
				requestDbClient.save(infraActiveRequest);
			}else{
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND,MsoLogger.ErrorCode.DataError).build();
				ValidateException validateException = new ValidateException.Builder("Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked",
						HttpStatus.SC_BAD_REQUEST,ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();

				throw validateException;
			}
		}

		return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
	}

	@GET
	@Path("/{version:[vV][1]}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Get status of an Operational Environment based on filter criteria",response=Response.class)
	@Transactional
	public Response getOperationEnvironmentStatusFilter(@Context UriInfo ui, @PathParam("version") String version ) throws ApiException{
		MsoLogger.setServiceName ("getOperationEnvironmentStatusFilter");
		UUIDChecker.generateUUID(msoLogger);

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		List<String> requestIdKey = queryParams.get("requestId");
		String apiVersion = version.substring(1);
		
		if(queryParams.size() == 1 && requestIdKey != null) {
			String requestId = requestIdKey.get(0);

			CloudOrchestrationResponse cloudOrchestrationGetResponse = new CloudOrchestrationResponse();
			TenantIsolationRequest tenantIsolationRequest = new TenantIsolationRequest (requestId);
			InfraActiveRequests requestDB = null;

			try {
				requestDB = requestDbClient.getInfraActiveRequestbyRequestId(requestId);
			} catch (Exception e) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, MsoLogger.ErrorCode.AvailabilityError).build();
				AlarmLoggerInfo alarmLoggerInfo = new AlarmLoggerInfo.Builder("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
						Messages.getErrors().get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB)).build();
				ValidateException validateException = new ValidateException.Builder(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
						.errorInfo(errorLoggerInfo).alarmInfo(alarmLoggerInfo).build();

				throw validateException;
				//              TODO Will need to set Status  for  tenantIsolationRequest
				//             tenantIsolationRequest.setStatus (org.onap.so.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			}

			if(requestDB == null) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MsoLogger.ErrorCode.BusinessProcesssError).build();
				ValidateException validateException = new ValidateException.Builder("Orchestration RequestId " + requestId + " is not found in DB",
						HttpStatus.SC_NO_CONTENT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
						.errorInfo(errorLoggerInfo).build();

				throw validateException;
			}

			Request request = mapInfraActiveRequestToRequest(requestDB);
			cloudOrchestrationGetResponse.setRequest(request);
			return builder.buildResponse(HttpStatus.SC_OK, requestId, cloudOrchestrationGetResponse, apiVersion);

		} else  {
			TenantIsolationRequest tenantIsolationRequest = new TenantIsolationRequest ();
			List<InfraActiveRequests> activeRequests = null;
			CloudOrchestrationRequestList orchestrationList = null;


			Map<String, String> orchestrationMap;
			try{
				orchestrationMap = tenantIsolationRequest.getOrchestrationFilters(queryParams);
			}catch(ValidationException ex){
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.BusinessProcesssError).build();
				ValidateException validateException = new ValidateException.Builder(ex.getMessage(),
						HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_GENERAL_SERVICE_ERROR).cause(ex)
						.errorInfo(errorLoggerInfo).build();

				throw validateException;

			}
			activeRequests = requestDbClient.getCloudOrchestrationFiltersFromInfraActive(orchestrationMap);
			orchestrationList = new CloudOrchestrationRequestList();
			List<CloudOrchestrationResponse> requestLists = new ArrayList<CloudOrchestrationResponse>();

			for(InfraActiveRequests infraActive : activeRequests){

				Request request = mapInfraActiveRequestToRequest(infraActive);
				CloudOrchestrationResponse requestList = new CloudOrchestrationResponse();
				requestList.setRequest(request);
				requestLists.add(requestList);
			}
			orchestrationList.setRequestList(requestLists);

			return builder.buildResponse(HttpStatus.SC_OK, null, orchestrationList, apiVersion);
		}
	}
	
	private Request mapInfraActiveRequestToRequest(InfraActiveRequests iar) throws ApiException  {
		Request request = new Request();
		request.setRequestId(iar.getRequestId());
		request.setRequestScope(iar.getRequestScope());
		request.setRequestType(iar.getRequestAction());

		InstanceReferences ir = new InstanceReferences();

		if(iar.getOperationalEnvId() != null)
			ir.setOperationalEnvironmentId(iar.getOperationalEnvId());
		if(iar.getOperationalEnvName() != null)
			ir.setOperationalEnvName(iar.getOperationalEnvName());
		if(iar.getRequestorId() != null)
			ir.setRequestorId(iar.getRequestorId());

		request.setInstanceReferences(ir);
		String requestBody = iar.getRequestBody();
		RequestDetails requestDetails = null;

		if (requestBody != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				requestDetails = mapper.readValue(requestBody, RequestDetails.class);
			} catch (IOException e) {
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MsoLogger.ErrorCode.SchemaError).build();
				ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER)
						.cause(e).errorInfo(errorLoggerInfo).build();
				throw validateException;
			}
		}

		request.setRequestDetails(requestDetails);
		String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getStartTime()) + " GMT";
		request.setStartTime(startTimeStamp);

		RequestStatus status = new RequestStatus();
		if(iar.getStatusMessage() != null){
			status.setStatusMessage(iar.getStatusMessage());
		}

		if(iar.getEndTime() != null){
			String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(iar.getEndTime()) + " GMT";
			status.setTimeStamp(endTimeStamp);
		}

		if(iar.getRequestStatus() != null){
			status.setRequestState(iar.getRequestStatus());
		}

		if(iar.getProgress() != null){
			status.setPercentProgress(iar.getProgress().toString());
		}

		request.setRequestStatus(status);

		return request;
	}

}
