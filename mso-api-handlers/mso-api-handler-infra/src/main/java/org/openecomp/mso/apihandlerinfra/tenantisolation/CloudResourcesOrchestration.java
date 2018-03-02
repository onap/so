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

package org.openecomp.mso.apihandlerinfra.tenantisolation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.Messages;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.Status;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.CloudOrchestrationRequestList;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.CloudOrchestrationResponse;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.InstanceReferences;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Request;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestStatus;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/cloudResourcesRequests")
@Api(value="/cloudResourcesRequests",description="API GET Requests for cloud resources - Tenant Isolation")
public class CloudResourcesOrchestration {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
	private RequestsDatabase requestsDB = null;
	
	@POST
	@Path("/{version: [vV][1]}/{requestId}/unlock")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Unlock CloudOrchestration requests for a specified requestId")
	public Response unlockOrchestrationRequest(String requestJSON, @PathParam("requestId") String requestId, @PathParam("version") String version) {
		TenantIsolationRequest msoRequest = new TenantIsolationRequest(requestId);
		InfraActiveRequests requestDB = null;
		Request request = null;
		CloudOrchestrationRequest cor = null;

		long startTime = System.currentTimeMillis ();
		msoLogger.debug ("requestId is: " + requestId);

		try{
			ObjectMapper mapper = new ObjectMapper();
			cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		} catch(Exception e){
			msoLogger.debug ("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
																	"Mapping of request to JSON object failed.  " + e.getMessage(),
																	ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId () != null) {
				msoLogger.debug ("Mapping of request to JSON object failed");
			}
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		try{
			msoRequest.parseOrchestration(cor);
		} catch (Exception e) {
			msoLogger.debug ("Validation failed: ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, MsoException.ServiceException,
																	"Error parsing request.  " + e.getMessage(),
																	ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId () != null) {
				msoLogger.debug ("Logging failed message to the database");
			}
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Validation of the input request failed");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		try {
			requestDB = getRequestsDB().getRequestFromInfraActive(requestId);

			if(requestDB == null) {
				Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
																	MsoException.ServiceException,
																	"Orchestration RequestId " + requestId + " is not found in DB",
																	ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
																	null);
				msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from RequestDB when searching by RequestId");
				msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Null response from RequestDB when searching by RequestId");
				msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
				return resp;

			}else{
				request = mapInfraActiveRequestToRequest(requestDB);
				RequestStatus reqStatus = request.getRequestStatus();
				Status status = Status.valueOf(reqStatus.getRequestState());
				if(status == Status.IN_PROGRESS || status == Status.PENDING || status == Status.PENDING_MANUAL_TASK){
					msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.UNLOCKED);
					reqStatus.setRequestState(Status.UNLOCKED.toString ());
					getRequestsDB().updateInfraStatus (requestId,
														Status.UNLOCKED.toString (),
														Constants.MODIFIED_BY_APIHANDLER);

					msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "RequestId " + requestId + " has been unlocked");

				}else{
					Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_BAD_REQUEST,
																			MsoException.ServiceException,
																			"Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked",
																			ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
																			null);
					msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked");
					msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, "Orchestration RequestId " + requestId + " has a status of " + status + " and can not be unlocked");
					msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
					return resp;
				}
			}
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Request DB - Infra Request Lookup", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
																		MsoException.ServiceException,
																		e.getMessage (),
																		ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
																		null);
			alarmLogger.sendAlarm ("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL,
					Messages.getErrors().get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with Request DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;

		}

		return Response.status (HttpStatus.SC_NO_CONTENT).entity ("").build ();
	}
	
	@GET
	@Path("/{version:[vV][1]}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Get status of an Operational Environment based on filter criteria",response=Response.class)
	public Response getOperationEnvironmentStatusFilter(@Context UriInfo ui, @PathParam("version") String version ) {
        MsoLogger.setServiceName ("getOperationEnvironmentStatusFilter");
        UUIDChecker.generateUUID(msoLogger);
        long startTime = System.currentTimeMillis ();

        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
        List<String> requestIdKey = queryParams.get("requestId");
        
        if(queryParams.size() == 1 && requestIdKey != null) {
        	msoLogger.debug ("Entered requestId GET OperationalEnvironment Request");
			String requestId = requestIdKey.get(0);
			
			CloudOrchestrationResponse cloudOrchestrationGetResponse = new CloudOrchestrationResponse();
			TenantIsolationRequest tenantIsolationRequest = new TenantIsolationRequest (requestId);
			InfraActiveRequests requestDB = null;

	        try {
	       		 requestDB = getRequestsDB().getRequestFromInfraActive(requestId);

	            } catch (Exception e) {
	                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Request DB - Infra Request Lookup", e);
	                //              TODO Will need to set Status  for  tenantIsolationRequest
	                //             tenantIsolationRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
	                Response response = tenantIsolationRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
	             		   												  MsoException.ServiceException,
	             		   												  e.getMessage (),
	                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
	                                                                       null);
	                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
	                                       MsoAlarmLogger.CRITICAL,
	                                       Messages.getErrors().get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
	                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with Request DB");
	                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
	                return response;
	            }

	        if(requestDB == null) {
	            Response resp = tenantIsolationRequest.buildServiceErrorResponse (HttpStatus.SC_NO_CONTENT,
	         		   											 MsoException.ServiceException,
	         		   											"Orchestration RequestId " + requestId + " is not found in DB",
	                                                             ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
	                                                             null);
	            msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from RequestDB when searching by RequestId");
	            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Null response from RequestDB when searching by RequestId");
	            msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
	            return resp;
	        }

	        Request request = mapInfraActiveRequestToRequest(requestDB);
	        cloudOrchestrationGetResponse.setRequest(request);
	        return Response.status(200).entity(cloudOrchestrationGetResponse).build();
	        
        } else  {
        	msoLogger.debug ("Entered GET OperationalEnvironment filter Request");
	        TenantIsolationRequest tenantIsolationRequest = new TenantIsolationRequest ();
			List<InfraActiveRequests> activeRequests = null;
			CloudOrchestrationRequestList orchestrationList = null;
			
			try{
				Map<String, String> orchestrationMap = tenantIsolationRequest.getOrchestrationFilters(queryParams);
				activeRequests = getRequestsDB().getCloudOrchestrationFiltersFromInfraActive(orchestrationMap);
				orchestrationList = new CloudOrchestrationRequestList();
				List<CloudOrchestrationResponse> requestLists = new ArrayList<CloudOrchestrationResponse>();
	
				for(InfraActiveRequests infraActive : activeRequests){
	
					Request request = mapInfraActiveRequestToRequest(infraActive);
					CloudOrchestrationResponse requestList = new CloudOrchestrationResponse();
					requestList.setRequest(request);
					requestLists.add(requestList);
				}
				orchestrationList.setRequestList(requestLists);
	
			}catch(Exception e){
		           msoLogger.debug ("Get Orchestration Request with Filters Failed : ", e);
		           Response response = tenantIsolationRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
		                   "Get CloudOrchestration Request with Filters Failed.  " + e.getMessage(),
		                   ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
		           msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Get Orchestration Request with Filters Failed : " + e);
		           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, "Get CloudOrchestration Request with Filters Failed");
		           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
		           return response;
			}
	        return Response.status(200).entity(orchestrationList).build();
        }
	}
	
    private Request mapInfraActiveRequestToRequest(InfraActiveRequests requestDB)  {
    	Request request = new Request();
    	request.setRequestId(requestDB.getRequestId());
    	request.setRequestScope(requestDB.getRequestScope());
    	request.setRequestType(requestDB.getRequestAction());

       InstanceReferences ir = new InstanceReferences();

       if(requestDB.getOperationalEnvId() != null)
    	  ir.setOperationalEnvironmentId(requestDB.getOperationalEnvId());
       if(requestDB.getOperationalEnvName() != null)
    	  ir.setOperationalEnvName(requestDB.getOperationalEnvName());
       if(requestDB.getRequestorId() != null)
			ir.setRequestorId(requestDB.getRequestorId());

	   request.setInstanceReferences(ir);
       String requestBody = requestDB.getRequestBody();
       RequestDetails requestDetails = null;

       try{
    	   ObjectMapper mapper = new ObjectMapper();
    	   requestDetails = mapper.readValue(requestBody, RequestDetails.class);

       }catch(Exception e){
       	msoLogger.debug("Exception caught mapping requestBody to RequestDetails");
       }

       request.setRequestDetails(requestDetails);
       String startTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getStartTime()) + " GMT";
       request.setStartTime(startTimeStamp);

       RequestStatus status = new RequestStatus();
       if(requestDB.getStatusMessage() != null){
    	   status.setStatusMessage(requestDB.getStatusMessage());
       }

       if(requestDB.getEndTime() != null){
    	   String endTimeStamp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(requestDB.getEndTime()) + " GMT";
    	   status.setTimeStamp(endTimeStamp);
       }

       if(requestDB.getRequestStatus() != null){
    	   status.setRequestState(requestDB.getRequestStatus());
       }

       if(requestDB.getProgress() != null){
    	   status.setPercentProgress(requestDB.getProgress().toString());
       }

       request.setRequestStatus(status);

       return request;
   }

	public RequestsDatabase getRequestsDB() {
		if(requestsDB == null) {
			requestsDB = RequestsDatabase.getInstance();
		}
		return requestsDB;
	}

	public void setRequestsDB(RequestsDatabase requestsDB) {
		this.requestsDB = requestsDB;
	}
    
    
}
