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
package org.openecomp.mso.apihandlerinfra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;

import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.GetOrchestrationListResponse;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.GetOrchestrationResponse;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.InstanceReferences;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.Request;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestList;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestStatus;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

@Path("/orchestrationRequests/v2")
public class OrchestrationRequests {

    public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();

	/**
	 * 
	 */
	public OrchestrationRequests() {
		// TODO Auto-generated constructor stub
	}

	@GET
	@Path("/{requestId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@PathParam("requestId") String requestId) {
		
		GetOrchestrationResponse orchestrationResponse = new GetOrchestrationResponse();
		
		MsoRequest msoRequest = new MsoRequest (requestId);
		
		long startTime = System.currentTimeMillis ();
		
		InfraActiveRequests requestDB = null;
        
        try {
       		 requestDB = RequestsDatabase.getRequestFromInfraActive(requestId);
       		 
            } catch (Exception e) {
                msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Request DB - Infra Request Lookup", e);
                msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
                Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
             		   												  MsoException.ServiceException,
             		   												  e.getMessage (),
                                                                       ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB,
                                                                       null);
                alarmLogger.sendAlarm ("MsoDatabaseAccessError",
                                       MsoAlarmLogger.CRITICAL,
                                       Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
                msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with Request DB");
                msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
                return response;
            	
            }
        
        if(requestDB == null) {
            Response resp = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NO_CONTENT,
         		   											 MsoException.ServiceException,
         		   											"Orchestration RequestId " + requestId + " is not found in DB",                                                            
                                                             ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                                                             null);
            msoLogger.error (MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Null response from RequestDB when searching by RequestId");
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Null response from RequestDB when searching by RequestId");
            msoLogger.debug ("End of the transaction, the final response is: " + (String) resp.getEntity ());
            return resp;
        	
        }
  
        Request request = mapInfraActiveRequestToRequest(requestDB);
         
        orchestrationResponse.setRequest(request);
		
        return Response.status(200).entity(orchestrationResponse).build();
	}
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrchestrationRequest(@Context UriInfo ui) {
		
		long startTime = System.currentTimeMillis ();
		
		MsoRequest msoRequest = new MsoRequest();
		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		
		List<InfraActiveRequests> activeRequests = null;
		
		GetOrchestrationListResponse orchestrationList = null;
		
		
		try{
		
			Map<String, List<String>> orchestrationMap = msoRequest.getOrchestrationFilters(queryParams);
			
			activeRequests = RequestsDatabase.getOrchestrationFiltersFromInfraActive(orchestrationMap);
			
			orchestrationList = new GetOrchestrationListResponse();

			List<RequestList> requestLists = new ArrayList<RequestList>();
						
			for(InfraActiveRequests infraActive : activeRequests){
				
				Request request = mapInfraActiveRequestToRequest(infraActive);
				RequestList requestList = new RequestList();
				requestList.setRequest(request);

				requestLists.add(requestList);

			}
			
			orchestrationList.setRequestList(requestLists);

		}catch(Exception e){
	           msoLogger.debug ("Get Orchestration Request with Filters Failed : ", e);
	           Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException, 
	                   "Get Orchestration Request with Filters Failed.  " + e.getMessage(), 
	                   ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
	           msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Get Orchestration Request with Filters Failed : " + e);
	           msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataError, "Get Orchestration Request with Filters Failed");
	           msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
	           return response;
		}
		
			
        return Response.status(200).entity(orchestrationList).build();
	}

    private Request mapInfraActiveRequestToRequest(InfraActiveRequests requestDB)  {
    	
    	  
        Request request = new Request();
        
        ObjectMapper mapper = new ObjectMapper();
       // mapper.configure(Feature.WRAP_ROOT_VALUE, true);
       
       request.setRequestId(requestDB.getRequestId());
       request.setRequestScope(requestDB.getRequestScope());
       request.setRequestType(requestDB.getRequestAction());
       
       InstanceReferences ir = new InstanceReferences();
       if(requestDB.getNetworkId() != null)
       	ir.setNetworkInstanceId(requestDB.getNetworkId());
       if(requestDB.getNetworkName() != null)
       	ir.setNetworkInstanceName(requestDB.getNetworkName());
       if(requestDB.getServiceInstanceId() != null)
       	ir.setServiceInstanceId(requestDB.getServiceInstanceId());
       if(requestDB.getServiceInstanceName() != null)
       	ir.setServiceInstanceName(requestDB.getServiceInstanceName());
       if(requestDB.getVfModuleId() != null)
       	ir.setVfModuleInstanceId(requestDB.getVfModuleId());
       if(requestDB.getVfModuleName() != null)
       	ir.setVfModuleInstanceName(requestDB.getVfModuleName());
       if(requestDB.getVnfId() != null)
       	ir.setVnfInstanceId(requestDB.getVnfId());
       if(requestDB.getVnfName() != null)
       	ir.setVnfInstanceName(requestDB.getVnfName());
       if(requestDB.getVolumeGroupId() != null)
       	ir.setVolumeGroupInstanceId(requestDB.getVolumeGroupId());
       if(requestDB.getVolumeGroupName() != null)
       	ir.setVolumeGroupInstanceName(requestDB.getVolumeGroupName());

       
       request.setInstanceReferences(ir);
       
       String requestBody = requestDB.getRequestBody();
              
       RequestDetails requestDetails = null;
       
       try{
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
    	   status.setFinishTime(endTimeStamp);
       }

        
       if(requestDB.getRequestStatus() != null){
    	   status.setRequestState(requestDB.getRequestStatus());
       }
       
       if(requestDB.getProgress() != null){
    	   status.setPercentProgress(requestDB.getProgress().intValue());
       }
       
       request.setRequestStatus(status);
       
       return request;
   }
    }
