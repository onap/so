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


import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.Status;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestReferences;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.TenantSyncResponse;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/cloudResources")
@Api(value="/cloudResources",description="API Requests for cloud resources - Tenant Isolation")
public class CloudOrchestration {
	
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private TenantIsolationRunnable tenantIsolation = null;
	private TenantIsolationRequest tenantIsolationRequest = null;
	private RequestsDatabase requestsDatabase = null;
	
	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create an Operational Environment",response=Response.class)
	public Response createOperationEnvironment(String request, @PathParam("version") String version) {
		msoLogger.debug("Received request to Create Operational Environment");
		return cloudOrchestration(request, Action.create, null, version);
	}
	
	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments/{operationalEnvironmentId}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Activate an Operational Environment",response=Response.class)
	public Response activateOperationEnvironment(String request, @PathParam("version") String version, @PathParam("operationalEnvironmentId") String operationalEnvironmentId) {
		msoLogger.debug("Received request to Activate an Operational Environment");
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("operationalEnvironmentId", operationalEnvironmentId);
		return cloudOrchestration(request, Action.activate, instanceIdMap, version);
	}
	
	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments/{operationalEnvironmentId}/deactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Deactivate an Operational Environment",response=Response.class)
	public Response deactivateOperationEnvironment(String request, @PathParam("version") String version, @PathParam("operationalEnvironmentId") String operationalEnvironmentId) {
		msoLogger.debug("Received request to Deactivate an Operational Environment");
		HashMap<String, String> instanceIdMap = new HashMap<String,String>();
		instanceIdMap.put("operationalEnvironmentId", operationalEnvironmentId);
		return cloudOrchestration(request, Action.deactivate, instanceIdMap, version);
	}
	
	
	private Response cloudOrchestration(String requestJSON, Action action, HashMap<String, String> instanceIdMap, String version) {
		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis ();
		CloudOrchestrationRequest cor = null;
		Response response = null;
		getTenantIsolationRequest().setRequestId(requestId);
		
		try {
			cor = convertJsonToCloudOrchestrationRequest(requestJSON, action, startTime, cor);
		} catch(Exception e) {
			response = getTenantIsolationRequest().buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, 
																		MsoException.ServiceException,
																		"Mapping of request to JSON object failed.  " + e.getMessage(),
																		ErrorNumbers.SVC_BAD_PARAMETER, 
																		null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		try {
			getTenantIsolationRequest().parse(cor, instanceIdMap, action);
		} catch(Exception e) {
			msoLogger.debug ("Validation failed: ", e);
			if (getTenantIsolationRequest().getRequestId () != null) {
				msoLogger.debug ("Logging failed message to the database");
				getTenantIsolationRequest().createRequestRecord (Status.FAILED, action);
			}
			response = getTenantIsolationRequest().buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST, 
																		MsoException.ServiceException,
																		"Error parsing request.  " + e.getMessage(),
																		ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		String instanceName = cor.getRequestDetails().getRequestInfo().getInstanceName();
		String resourceType = cor.getRequestDetails().getRequestInfo().getResourceType().name();
		InfraActiveRequests dup = null;
		String messageAppend = null;
		try {
			dup = duplicateCheck(action, instanceIdMap, startTime, instanceName, resourceType);
			
			if(dup != null) {
				messageAppend = "already has a request being worked with a status of " + dup.getRequestStatus() + " (RequestId - " + dup.getRequestId() + ").";
			}
		} catch(Exception e) {
			response = getTenantIsolationRequest().buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, 
																		MsoException.ServiceException,
																		e.getMessage(),
																		ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
																		null) ;
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		try {
			if(dup == null && (Action.activate.equals(action) || Action.deactivate.equals(action))) {
				dup = getRequestsDatabase().checkVnfIdStatus(cor.getOperationalEnvironmentId());
				if(dup != null) {
					messageAppend = "OperationalEnvironmentId is not COMPLETED.";
				}
			}
		} catch(Exception e) {
			response = getTenantIsolationRequest().buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, 
																			MsoException.ServiceException,
																			e.getMessage(),
																			ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
																			null) ;
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		if(dup != null) {
			String instance = null;
			if(instanceName != null){
				instance = instanceName;
			}else{
				instance = instanceIdMap.get(resourceType + "InstanceId");
			}
			String dupMessage =  "Error: Locked instance - This " + resourceType + " (" + instance + ") " + messageAppend + " The existing request must finish or be cleaned up before proceeding.";

			response = getTenantIsolationRequest().buildServiceErrorResponse(HttpStatus.SC_CONFLICT, 
																		MsoException.ServiceException,
																		dupMessage,
																		ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
																		null) ;

			msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND, dupMessage, "", "", MsoLogger.ErrorCode.SchemaError, dupMessage);
			getTenantIsolationRequest().createRequestRecord (Status.FAILED, action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, dupMessage);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		String instanceId = null;
		try {
			if(instanceIdMap != null && instanceIdMap.get("operationalEnvironmentId") != null) {
				instanceId = instanceIdMap.get("operationalEnvironmentId");
			} else {
				instanceId = UUIDChecker.generateUUID(msoLogger);
				getTenantIsolationRequest().setOperationalEnvironmentId(instanceId);
				cor.setOperationalEnvironmentId(instanceId);
			}
			
			msoLogger.debug("Creating record in Request DB");
			getTenantIsolationRequest().createRequestRecord(Status.IN_PROGRESS, action);
		} catch(Exception e) {
			response = getTenantIsolationRequest().buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
																		MsoException.ServiceException,
																		"Exception while creating record in DB " + e.getMessage(),
																		ErrorNumbers.SVC_BAD_PARAMETER,
																		null);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		try {
			OperationalEnvironment opEnv = cor.getRequestDetails().getRequestParameters().getOperationalEnvironmentType();
			String operationalEnvType = opEnv != null ? opEnv.name() : null;
			
			TenantIsolationRunnable runnable = getThread();
			runnable.setAction(action);
			runnable.setCor(cor);
			runnable.setOperationalEnvType(operationalEnvType);
			runnable.setRequestId(requestId);
			
			Thread thread = new Thread(runnable);
			thread.start();
		} catch(Exception e) {
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while creating a new Thread", "APIH", null, null);
			response = getTenantIsolationRequest().buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
																		MsoException.ServiceException,
																		"Failed creating a Thread " + e.getMessage (),
																		ErrorNumbers.SVC_NO_SERVER_RESOURCES,
																		null);
			getTenantIsolationRequest().updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.UnknownError, "Exception while creating a new Thread");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, "Exception while creating a new Thread");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
		
		try {
			String encodedValue  = new String(instanceId.getBytes("UTF-8"));
			msoLogger.debug ("InstanceId: " + instanceId + " encoded to " + encodedValue);
			
			TenantSyncResponse tenantResponse = new TenantSyncResponse();
			RequestReferences reqReference = new RequestReferences();
			reqReference.setInstanceId(encodedValue);
			reqReference.setRequestId(requestId);
			tenantResponse.setRequestReferences(reqReference);
			
			response = Response.ok(tenantResponse).build();
			
			msoLogger.debug ("Successful Sync response " + response.getEntity() + " with status code " + response.getStatus());
			
			return response;
		} catch(Exception e) {
			msoLogger.recordMetricEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError, "Exception while building sync response", "APIH", null, null);
			response = getTenantIsolationRequest().buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
																		MsoException.ServiceException,
																		"Failed sending Sync Response " + e.getMessage (),
																		ErrorNumbers.SVC_NO_SERVER_RESOURCES,
																		null);
			getTenantIsolationRequest().updateFinalStatus (Status.FAILED);
			msoLogger.error (MessageEnum.APIH_GENERAL_EXCEPTION, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.UnknownError, "Exception while sending sync Response");
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.UnknownError, "Exception while sending sync Response");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}
	}

	private InfraActiveRequests duplicateCheck(Action action, HashMap<String, String> instanceIdMap, long startTime,
						String instanceName, String requestScope) throws Exception {
		InfraActiveRequests dup = null;
		try {
			dup = getRequestsDatabase().checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Error during duplicate check ", e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Error during duplicate check");
			throw new Exception(e);
		}
		return dup;
	}
	
	private CloudOrchestrationRequest convertJsonToCloudOrchestrationRequest(String requestJSON, Action action, long startTime,
		CloudOrchestrationRequest cor) throws Exception {
		try{
			msoLogger.debug("Converting incoming JSON request to Object");
			ObjectMapper mapper = new ObjectMapper();
			cor = mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		} catch(Exception e){
			msoLogger.debug ("Mapping of request to JSON object failed : ", e);
			if (getTenantIsolationRequest().getRequestId () != null) {
				msoLogger.debug ("Mapping of request to JSON object failed");
				getTenantIsolationRequest().createRequestRecord (Status.FAILED, action);
			}
			msoLogger.error (MessageEnum.APIH_REQUEST_VALIDATION_ERROR, Constants.MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError, "Mapping of request to JSON object failed");
			throw new Exception(e);
		}
		return cor;
	}
	
	public TenantIsolationRequest getTenantIsolationRequest() {
		if(tenantIsolationRequest == null) {
			tenantIsolationRequest = new TenantIsolationRequest();
		}
		return tenantIsolationRequest;
	}

	public void setTenantIsolationRequest(TenantIsolationRequest tenantIsolationRequest) {
		this.tenantIsolationRequest = tenantIsolationRequest;
	}

	public RequestsDatabase getRequestsDatabase() {
		if(requestsDatabase == null) {
			requestsDatabase = RequestsDatabase.getInstance();
		}
		return requestsDatabase;
	}

	public void setRequestsDatabase(RequestsDatabase requestsDatabase) {
		this.requestsDatabase = requestsDatabase;
	}
	
	public TenantIsolationRunnable getThread() {
		if(tenantIsolation == null) {
			tenantIsolation = new TenantIsolationRunnable();
		}
		return tenantIsolation;
	}

	public void setThread(TenantIsolationRunnable thread) {
		this.tenantIsolation = thread;
	}
}
