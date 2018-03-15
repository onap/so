/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.json.JSONObject;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandlerinfra.Messages;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.MsoRequest;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.DelE2ESvcResp;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceDeleteRequest;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceRequest;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.E2EUserParam;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.GetE2EServiceInstanceResponse;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.ModelType;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.serviceinstancebeans.RequestInfo;
import org.openecomp.mso.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.serviceinstancebeans.SubscriberInfo;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.properties.MsoDatabaseException;
import org.openecomp.mso.requestsdb.OperationStatus;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbSessionFactoryManager;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/e2eServiceInstances")
@Api(value = "/e2eServiceInstances", description = "API Requests for E2E Service Instances")
public class E2EServiceInstances {

	private HashMap<String, String> instanceIdMap = new HashMap<>();
	private static MsoLogger msoLogger = MsoLogger
			.getMsoLogger(MsoLogger.Catalog.APIH);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger();
	public static final String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";
	private ServiceInstancesRequest sir = null;

	public static final String END_OF_THE_TRANSACTION = "End of the transaction, the final response is: ";
	public static final String EXCEPTION_CREATING_DB_RECORD = "Exception while creating record in DB";
	public static final String EXCEPTION_COMMUNICATE_BPMN_ENGINE = "Exception while communicate with BPMN engine";

	/**
	 * POST Requests for E2E Service create Instance on a version provided
	 */

	@POST
	@Path("/{version:[vV][3-5]}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create an E2E Service Instance on a version provided", response = Response.class)
	public Response createE2EServiceInstance(String request,
			@PathParam("version") String version) {

		return processE2EserviceInstances(request, Action.createInstance, null,
				version);
	}
	
	/**
	 * PUT Requests for E2E Service update Instance on a version provided
	 */

	@PUT
	@Path("/{version:[vV][3-5]}/{serviceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update an E2E Service Instance on a version provided and serviceId", response = Response.class)
	public Response updateE2EServiceInstance(String request,
			@PathParam("version") String version,
			@PathParam("serviceId") String serviceId) {
		
		instanceIdMap.put("serviceId", serviceId);

		return updateE2EserviceInstances(request, Action.updateInstance, instanceIdMap,
				version);
	}

	/**
	 * DELETE Requests for E2E Service delete Instance on a specified version
	 * and serviceId
	 */

	@DELETE
	@Path("/{version:[vV][3-5]}/{serviceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete E2E Service Instance on a specified version and serviceId", response = Response.class)
	public Response deleteE2EServiceInstance(String request,
			@PathParam("version") String version,
			@PathParam("serviceId") String serviceId) {

		instanceIdMap.put("serviceId", serviceId);

		return deleteE2EserviceInstances(request, Action.deleteInstance,
				instanceIdMap, version);
	}

	@GET
	@Path("/{version:[vV][3-5]}/{serviceId}/operations/{operationId}")
	@ApiOperation(value = "Find e2eServiceInstances Requests for a given serviceId and operationId", response = Response.class)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getE2EServiceInstances(
			@PathParam("serviceId") String serviceId,
			@PathParam("version") String version,
			@PathParam("operationId") String operationId) {
		return getE2EServiceInstances(serviceId, operationId);
	}

	private Response getE2EServiceInstances(String serviceId, String operationId) {
		RequestsDatabase requestsDB = RequestsDatabase.getInstance();

		GetE2EServiceInstanceResponse e2eServiceResponse = new GetE2EServiceInstanceResponse();

		MsoRequest msoRequest = new MsoRequest(serviceId);

		long startTime = System.currentTimeMillis();

		OperationStatus operationStatus = null;

		try {
			operationStatus = requestsDB.getOperationStatus(serviceId,
					operationId);

		} catch (Exception e) {
			msoLogger
					.error(MessageEnum.APIH_DB_ACCESS_EXC,
							MSO_PROP_APIHANDLER_INFRA,
							"",
							"",
							MsoLogger.ErrorCode.AvailabilityError,
							"Exception while communciate with Request DB - Infra Request Lookup",
							e);
			msoRequest
					.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
					e.getMessage(),
					ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL, Messages.errors
							.get(ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB));
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with Request DB");
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) response.getEntity());
			return response;

		}

		if (operationStatus == null) {
			Response resp = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_NO_CONTENT, MsoException.ServiceException,
					"E2E serviceId " + serviceId + " is not found in DB",
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError,
					"Null response from RequestDB when searching by serviceId");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.DataNotFound,
					"Null response from RequestDB when searching by serviceId");
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) resp.getEntity());
			return resp;

		}

		e2eServiceResponse.setOperationStatus(operationStatus);

		return Response.status(200).entity(e2eServiceResponse).build();
	}

	private Response deleteE2EserviceInstances(String requestJSON,
			Action action, HashMap<String, String> instanceIdMap, String version) {
		// TODO should be a new one or the same service instance Id
		String requestId = instanceIdMap.get("serviceId");
		long startTime = System.currentTimeMillis();
		msoLogger.debug("requestId is: " + requestId);
		E2EServiceInstanceDeleteRequest e2eDelReq = null;

		MsoRequest msoRequest = new MsoRequest(requestId);

		ObjectMapper mapper = new ObjectMapper();
		try {
			e2eDelReq = mapper.readValue(requestJSON,
					E2EServiceInstanceDeleteRequest.class);

		} catch (Exception e) {

			msoLogger.debug("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException,
					"Mapping of request to JSON object failed.  "
							+ e.getMessage(), ErrorNumbers.SVC_BAD_PARAMETER,
					null);
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.SchemaError,
					"Mapping of request to JSON object failed");
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		}

		CatalogDatabase db = null;
		RecipeLookupResult recipeLookupResult = null;
		try {
			db = CatalogDatabase.getInstance();
			//TODO  Get the service template model version uuid from AAI.
			recipeLookupResult = getServiceInstanceOrchestrationURI(db, null, action);
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError,
					"Exception while communciate with Catalog DB", e);
			msoRequest
					.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
					"No communication to catalog DB " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL, Messages.errors
							.get(ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
			msoRequest.createRequestRecord(Status.FAILED, action);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with DB");
			msoLogger.debug(END_OF_THE_TRANSACTION
					+ (String) response.getEntity());
			return response;
		} finally {
			closeCatalogDB(db);
		}
		if (recipeLookupResult == null) {
			msoLogger.error(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.DataError, "No recipe found in DB");
			msoRequest
					.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_NOT_FOUND, MsoException.ServiceException,
					"Recipe does not exist in catalog DB",
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
			msoRequest.createRequestRecord(Status.FAILED, action);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.DataNotFound,
					"No recipe found in DB");
			msoLogger.debug(END_OF_THE_TRANSACTION
					+ (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		}

		RequestClient requestClient = null;
		HttpResponse response = null;

		long subStartTime = System.currentTimeMillis();
		// String sirRequestJson = mapReqJsonToSvcInstReq(e2eSir, requestJSON);

		try {
			requestClient = RequestClientFactory.getRequestClient(
					recipeLookupResult.getOrchestrationURI(),
					MsoPropertiesUtils.loadMsoProperties());

			JSONObject jjo = new JSONObject(requestJSON);
			jjo.put("operationId", UUIDChecker.generateUUID(msoLogger));

			String bpmnRequest = jjo.toString();

			// Capture audit event
			msoLogger
					.debug("MSO API Handler Posting call to BPEL engine for url: "
							+ requestClient.getUrl());
			String serviceId = instanceIdMap.get("serviceId");
			String serviceInstanceType = e2eDelReq.getServiceType();
			response = requestClient.post(requestId, false,
					recipeLookupResult.getRecipeTimeout(), action.name(),
					serviceId, null, null, null, null, null, serviceInstanceType,
					null, null, null, bpmnRequest, recipeLookupResult.getRecipeParamXsd());

			msoLogger.recordMetricEvent(subStartTime,
					MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
					"Successfully received response from BPMN engine", "BPMN",
					recipeLookupResult.getOrchestrationURI(), null);
		} catch (Exception e) {
			msoLogger.recordMetricEvent(subStartTime,
					MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.CommunicationError,
					"Exception while communicate with BPMN engine", "BPMN",
					recipeLookupResult.getOrchestrationURI(), null);
			Response resp = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
					"Failed calling bpmn " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoConfigurationError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError,
					"Exception while communicate with BPMN engine");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.CommunicationError,
					"Exception while communicate with BPMN engine");
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) resp.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return resp;
		}

		if (response == null) {
			Response resp = msoRequest.buildServiceErrorResponse(
					HttpStatus.SC_BAD_GATEWAY, MsoException.ServiceException,
					"bpelResponse is null",
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR,
					MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError,
					"Null response from BPEL");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.InternalError,
					"Null response from BPMN");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) resp.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return resp;
		}

		ResponseHandler respHandler = new ResponseHandler(response,
				requestClient.getType());
		int bpelStatus = respHandler.getStatus();

		return beplStatusUpdate(requestId, startTime, msoRequest,
				requestClient, respHandler, bpelStatus, action, instanceIdMap);
	}

	private Response updateE2EserviceInstances(String requestJSON, Action action,
			HashMap<String, String> instanceIdMap, String version) {

		String requestId = instanceIdMap.get("serviceId");
		long startTime = System.currentTimeMillis();
		msoLogger.debug("requestId is: " + requestId);
		E2EServiceInstanceRequest e2eSir = null;

		MsoRequest msoRequest = new MsoRequest(requestId);
		ObjectMapper mapper = new ObjectMapper();
		try {
			e2eSir = mapper.readValue(requestJSON, E2EServiceInstanceRequest.class);

		} catch (Exception e) {
          
          this.createOperationStatusRecordForError(action, requestId);
		  
			msoLogger.debug("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Mapping of request to JSON object failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		mapReqJsonToSvcInstReq(e2eSir, requestJSON);
		sir.getRequestDetails().getRequestParameters().setaLaCarte(true);
		try {
			msoRequest.parse(sir, instanceIdMap, action, version, requestJSON);
		} catch (Exception e) {
			msoLogger.debug("Validation failed: ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Error parsing request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId() != null) {
				msoLogger.debug("Logging failed message to the database");
				this.createOperationStatusRecordForError(action, requestId);
			}
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Validation of the input request failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}
		
		//check for the current operation status
		Response resp = checkE2ESvcInstStatus(action, requestId, startTime, msoRequest);
		if(resp != null && resp.getStatus() != 200) {
			return resp;
		}
		
		CatalogDatabase db = null;
		RecipeLookupResult recipeLookupResult = null;
		try {
			db = CatalogDatabase.getInstance();
			recipeLookupResult = getServiceInstanceOrchestrationURI(db, e2eSir.getService().getServiceUuid(), action);
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
			
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with DB");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		} finally {
			closeCatalogDB(db);
		}

		if (recipeLookupResult == null) {
			msoLogger.error(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.DataError, "No recipe found in DB");
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, "Recipe does not exist in catalog DB",
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
		
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound,
					"No recipe found in DB");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		}

		String serviceInstanceType = e2eSir.getService().getServiceType();

		String serviceId = "";
		RequestClient requestClient = null;
		HttpResponse response = null;

		long subStartTime = System.currentTimeMillis();
		String sirRequestJson = mapReqJsonToSvcInstReq(e2eSir, requestJSON);

		try {
			requestClient = RequestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI(),
					MsoPropertiesUtils.loadMsoProperties());

			// Capture audit event
			msoLogger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());

			response = requestClient.post(requestId, false, recipeLookupResult.getRecipeTimeout(), action.name(),
					serviceId, null, null, null, null, null, serviceInstanceType, null, null, null, sirRequestJson,
					recipeLookupResult.getRecipeParamXsd());

			msoLogger.recordMetricEvent(subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
					"Successfully received response from BPMN engine", "BPMN", recipeLookupResult.getOrchestrationURI(),
					null);
		} catch (Exception e) {
			msoLogger.debug("Exception while communicate with BPMN engine", e);
			msoLogger.recordMetricEvent(subStartTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN",
					recipeLookupResult.getOrchestrationURI(), null);
			Response getBPMNResp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoConfigurationError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
					"Exception while communicate with BPMN engine");
			msoLogger.debug("End of the transaction, the final response is: " + (String) getBPMNResp.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return getBPMNResp;
		}

		if (response == null) {
			Response getBPMNResp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException, "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError, "Null response from BPEL");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError,
					"Null response from BPMN");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) getBPMNResp.getEntity());
			return getBPMNResp;
		}

		ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
		int bpelStatus = respHandler.getStatus();

		return beplStatusUpdate(requestId, startTime, msoRequest, requestClient, respHandler, bpelStatus, action, instanceIdMap);
	}

	private Response checkE2ESvcInstStatus(Action action, String requestId, long startTime, MsoRequest msoRequest) {
		OperationStatus curStatus = null;
//		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
		String requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
		try {
			if (!(requestId == null && "service".equals(requestScope) && (action == Action.updateInstance))) {			    
				curStatus = chkSvcInstOperStatusbySvcId(requestId);
			}
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.DataError, "Error during current operation status check ", e);

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
					MsoException.ServiceException, e.getMessage(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);

			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Error during current operation status check");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		if (curStatus != null && !curStatus.getProgress().equals("100")) {
			String chkMessage = "Error: Locked instance - This " + requestScope + " (" + requestId + ") "
					+ "now being worked with a status of " + curStatus.getProgress() + " (ServiceName - "
					+ curStatus.getServiceName()
					+ "). The existing request must finish or be cleaned up before proceeding.";

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_CONFLICT,
					MsoException.ServiceException, chkMessage, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);

			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict,
					chkMessage);

			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());

			createOperationStatusRecordForError(action, requestId);

			return response;
		}
		
		return Response.status(200).entity(null).build();
	}
	
	private Response processE2EserviceInstances(String requestJSON, Action action,
			HashMap<String, String> instanceIdMap, String version) {

		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis();
		msoLogger.debug("requestId is: " + requestId);
		E2EServiceInstanceRequest e2eSir = null;

		MsoRequest msoRequest = new MsoRequest(requestId);
		ObjectMapper mapper = new ObjectMapper();
		try {
			e2eSir = mapper.readValue(requestJSON, E2EServiceInstanceRequest.class);

		} catch (Exception e) {
          //TODO update the service name
          this.createOperationStatusRecordForError(action, requestId);
		  
			msoLogger.debug("Mapping of request to JSON object failed : ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Mapping of request to JSON object failed.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Mapping of request to JSON object failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		mapReqJsonToSvcInstReq(e2eSir, requestJSON);
		sir.getRequestDetails().getRequestParameters().setaLaCarte(true);
		try {
			msoRequest.parse(sir, instanceIdMap, action, version, requestJSON);
		} catch (Exception e) {
			msoLogger.debug("Validation failed: ", e);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_REQUEST,
					MsoException.ServiceException, "Error parsing request.  " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER, null);
			if (msoRequest.getRequestId() != null) {
				msoLogger.debug("Logging failed message to the database");
				//TODO update the service name
		          this.createOperationStatusRecordForError(action, requestId);
			}
			msoLogger.error(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.SchemaError, requestJSON, e);
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.SchemaError,
					"Validation of the input request failed");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}
		
		OperationStatus dup = null;
		String instanceName = sir.getRequestDetails().getRequestInfo().getInstanceName();
		String requestScope = sir.getRequestDetails().getModelInfo().getModelType().name();
		try {
			if (!(instanceName == null && "service".equals(requestScope)
					&& (action == Action.createInstance || action == Action.activateInstance))) {
			  //TODO : Need to check for the duplicate record from the operation status,
			  //TODO : commenting this check for unblocking current testing for now...  induces dead code...
				dup = chkDuplicateServiceNameInOperStatus( instanceName);
			}
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.DataError, "Error during duplicate check ", e);

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
					MsoException.ServiceException, e.getMessage(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);

			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Error during duplicate check");
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			return response;
		}

		if (dup != null) {
			// Found the duplicate record. Return the appropriate error.
			String instance = null;
			if (instanceName != null) {
				instance = instanceName;
			} else {
				instance = instanceIdMap.get(requestScope + "InstanceId");
			}
			String dupMessage = "Error: Locked instance - This " + requestScope + " (" + instance + ") "
					+ "already has a request being worked with a status of " + dup.getProgress() + " (ServiceId - "
					+ dup.getServiceId() + "). The existing request must finish or be cleaned up before proceeding.";

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_CONFLICT,
					MsoException.ServiceException, dupMessage, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, null);

			msoLogger.warn(MessageEnum.APIH_DUPLICATE_FOUND, dupMessage, "", "", MsoLogger.ErrorCode.SchemaError,
					"Duplicate request - Subscriber already has a request for this service");
			
			
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict,
					dupMessage);
			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		}
		
		CatalogDatabase db = null;
		RecipeLookupResult recipeLookupResult = null;
		try {
			db = CatalogDatabase.getInstance();
			recipeLookupResult = getServiceInstanceOrchestrationURI(db, e2eSir.getService().getServiceUuid(), action);
		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, "No communication to catalog DB " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoDatabaseAccessError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
			
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
					"Exception while communciate with DB");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		} finally {
			closeCatalogDB(db);
		}

		if (recipeLookupResult == null) {
			msoLogger.error(MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.DataError, "No recipe found in DB");
			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException, "Recipe does not exist in catalog DB",
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR, null);
		
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound,
					"No recipe found in DB");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) response.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return response;
		}
//		try {
//			msoRequest.createRequestRecord(Status.PENDING, action);
//			//createOperationStatusRecord(action, requestId);
//		} catch (Exception e) {
//			msoLogger.error(MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "",
//					MsoLogger.ErrorCode.SchemaError, "Exception while creating record in DB", e);
//			msoRequest.setStatus(org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
//			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
//					MsoException.ServiceException, "Exception while creating record in DB " + e.getMessage(),
//					ErrorNumbers.SVC_BAD_PARAMETER, null);
//			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError,
//					"Exception while creating record in DB");
//			msoLogger.debug("End of the transaction, the final response is: " + (String) response.getEntity());
//			return response;
//		}

		String serviceInstanceType = e2eSir.getService().getServiceType();

		String serviceId = "";
		RequestClient requestClient = null;
		HttpResponse response = null;

		long subStartTime = System.currentTimeMillis();
		String sirRequestJson = mapReqJsonToSvcInstReq(e2eSir, requestJSON);

		try {
			requestClient = RequestClientFactory.getRequestClient(recipeLookupResult.getOrchestrationURI(),
					MsoPropertiesUtils.loadMsoProperties());

			// Capture audit event
			msoLogger.debug("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl());

			response = requestClient.post(requestId, false, recipeLookupResult.getRecipeTimeout(), action.name(),
					serviceId, null, null, null, null, null, serviceInstanceType, null, null, null, sirRequestJson,
					recipeLookupResult.getRecipeParamXsd());

			msoLogger.recordMetricEvent(subStartTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
					"Successfully received response from BPMN engine", "BPMN", recipeLookupResult.getOrchestrationURI(),
					null);
		} catch (Exception e) {
			msoLogger.recordMetricEvent(subStartTime, MsoLogger.StatusCode.ERROR,
					MsoLogger.ResponseCode.CommunicationError, "Exception while communicate with BPMN engine", "BPMN",
					recipeLookupResult.getOrchestrationURI(), null);
			Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException, "Failed calling bpmn " + e.getMessage(),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			alarmLogger.sendAlarm("MsoConfigurationError", MsoAlarmLogger.CRITICAL,
					Messages.errors.get(ErrorNumbers.NO_COMMUNICATION_TO_BPEL));
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.AvailabilityError, "Exception while communicate with BPMN engine");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.CommunicationError,
					"Exception while communicate with BPMN engine");
			msoLogger.debug("End of the transaction, the final response is: " + (String) resp.getEntity());
			createOperationStatusRecordForError(action, requestId);
			return resp;
		}

		if (response == null) {
			Response resp = msoRequest.buildServiceErrorResponse(HttpStatus.SC_BAD_GATEWAY,
					MsoException.ServiceException, "bpelResponse is null", ErrorNumbers.SVC_NO_SERVER_RESOURCES, null);
			msoLogger.error(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, MSO_PROP_APIHANDLER_INFRA, "", "",
					MsoLogger.ErrorCode.BusinessProcesssError, "Null response from BPEL");
			msoLogger.recordAuditEvent(startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.InternalError,
					"Null response from BPMN");
			msoLogger.debug(END_OF_THE_TRANSACTION + (String) resp.getEntity());
			return resp;
		}

		ResponseHandler respHandler = new ResponseHandler(response, requestClient.getType());
		int bpelStatus = respHandler.getStatus();

		return beplStatusUpdate(requestId, startTime, msoRequest, requestClient, respHandler, bpelStatus, action, instanceIdMap);
	}

	private void closeCatalogDB(CatalogDatabase db) {
		if (db != null) {
			db.close();
		}
	}

	private Response beplStatusUpdate(String requestId, long startTime,
			MsoRequest msoRequest, RequestClient requestClient,
			ResponseHandler respHandler, int bpelStatus, Action action,
			HashMap<String, String> instanceIdMap) {
		// BPMN accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {
			String camundaJSONResponseBody = respHandler.getResponseBody();
			msoLogger
					.debug("Received from Camunda: " + camundaJSONResponseBody);

			// currently only for delete case we update the status here
			if (action == Action.deleteInstance) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					DelE2ESvcResp jo = mapper.readValue(
							camundaJSONResponseBody, DelE2ESvcResp.class);
					String operationId = jo.getOperationId();
    				this.createOperationStatusRecord("DELETE", requestId,
								operationId);
				} catch (Exception ex) {
					msoLogger.error(MessageEnum.APIH_BPEL_RESPONSE_ERROR,
							requestClient.getUrl(), "", "",
							MsoLogger.ErrorCode.BusinessProcesssError,
							"Response from BPEL engine is failed with HTTP Status="
									+ bpelStatus);
				}
			}
			msoLogger.recordAuditEvent(startTime,
					MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
					"BPMN accepted the request, the request is in progress");
			msoLogger.debug(END_OF_THE_TRANSACTION + camundaJSONResponseBody);
			return Response.status(HttpStatus.SC_ACCEPTED)
					.entity(camundaJSONResponseBody).build();
		} else {
			List<String> variables = new ArrayList<>();
			variables.add(bpelStatus + "");
			String camundaJSONResponseBody = respHandler.getResponseBody();
			if (camundaJSONResponseBody != null
					&& !camundaJSONResponseBody.isEmpty()) {
				Response resp = msoRequest.buildServiceErrorResponse(
						bpelStatus, MsoException.ServiceException,
						"Request Failed due to BPEL error with HTTP Status= %1 "
								+ '\n' + camundaJSONResponseBody,
						ErrorNumbers.SVC_DETAILED_SERVICE_ERROR, variables);
				msoLogger.error(MessageEnum.APIH_BPEL_RESPONSE_ERROR,
						requestClient.getUrl(), "", "",
						MsoLogger.ErrorCode.BusinessProcesssError,
						"Response from BPEL engine is failed with HTTP Status="
								+ bpelStatus);
				msoLogger.recordAuditEvent(startTime,
						MsoLogger.StatusCode.ERROR,
						MsoLogger.ResponseCode.InternalError,
						"Response from BPMN engine is failed");
				msoLogger.debug(END_OF_THE_TRANSACTION
						+ (String) resp.getEntity());
				return resp;
			} else {
				Response resp = msoRequest
						.buildServiceErrorResponse(
								bpelStatus,
								MsoException.ServiceException,
								"Request Failed due to BPEL error with HTTP Status= %1",
								ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
								variables);
				msoLogger.error(MessageEnum.APIH_BPEL_RESPONSE_ERROR,
						requestClient.getUrl(), "", "",
						MsoLogger.ErrorCode.BusinessProcesssError,
						"Response from BPEL engine is empty");
				msoLogger.recordAuditEvent(startTime,
						MsoLogger.StatusCode.ERROR,
						MsoLogger.ResponseCode.InternalError,
						"Response from BPEL engine is empty");
				msoLogger.debug(END_OF_THE_TRANSACTION
						+ (String) resp.getEntity());
				return resp;
			}
		}
	}

	/**
	 * Getting recipes from catalogDb
	 * 
	 * @param db the catalog db
	 * @param serviceModelUUID the service model version uuid
	 * @param action the action for the service
	 * @return the service recipe result
	 */
	private RecipeLookupResult getServiceInstanceOrchestrationURI(
			CatalogDatabase db, String serviceModelUUID, Action action) {

		RecipeLookupResult recipeLookupResult = getServiceURI(db, serviceModelUUID, action);

		if (recipeLookupResult != null) {
			msoLogger.debug("Orchestration URI is: "
					+ recipeLookupResult.getOrchestrationURI()
					+ ", recipe Timeout is: "
					+ Integer.toString(recipeLookupResult.getRecipeTimeout()));
		} else {
			msoLogger.debug("No matching recipe record found");
		}
		return recipeLookupResult;
	}

	/**
	 * Getting recipes from catalogDb
	 * If Service recipe is not set, use default recipe, if set , use special recipe.
	 * @param db the catalog db
	 * @param serviceModelUUID the service version uuid
	 * @param action the action of the service.
	 * @return the service recipe result.
	 */
	private RecipeLookupResult getServiceURI(CatalogDatabase db, String serviceModelUUID, Action action) {

		String defaultServiceModelName = "UUI_DEFAULT";

		Service defaultServiceRecord = db
				.getServiceByModelName(defaultServiceModelName);
		ServiceRecipe defaultRecipe = db.getServiceRecipeByModelUUID(
		        defaultServiceRecord.getModelUUID(), action.name());
		//set recipe as default generic recipe
		ServiceRecipe recipe = defaultRecipe;
		//check the service special recipe 
		if(null != serviceModelUUID && ! serviceModelUUID.isEmpty()){
		      ServiceRecipe serviceSpecialRecipe = db.getServiceRecipeByModelUUID(
		              serviceModelUUID, action.name());
		      if(null != serviceSpecialRecipe){
		          //set service special recipe.
		          recipe = serviceSpecialRecipe;
		      }
		}	
		
		if (recipe == null) {
			return null;
		}
		return new RecipeLookupResult(recipe.getOrchestrationUri(),
				recipe.getRecipeTimeout(), recipe.getServiceParamXSD());

	}

	/**
	 * Converting E2EServiceInstanceRequest to ServiceInstanceRequest and
	 * passing it to camunda engine.
	 * 
	 * @param e2eSir
	 * @return
	 */
	private String mapReqJsonToSvcInstReq(E2EServiceInstanceRequest e2eSir,
			String requestJSON) {

		sir = new ServiceInstancesRequest();

		String returnString = null;
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();

		// ModelInvariantId
		modelInfo.setModelInvariantId(e2eSir.getService().getServiceInvariantUuid());

		// modelNameVersionId
		modelInfo.setModelNameVersionId(e2eSir.getService().getServiceUuid());

		// String modelInfoValue =
		// e2eSir.getService().getParameters().getNodeTemplateName();
		// String[] arrayOfInfo = modelInfoValue.split(":");
		// String modelName = arrayOfInfo[0];
		// String modelVersion = arrayOfInfo[1];

		// TODO: To ensure, if we dont get the values from the UUI
		String modelName = "voLTE";
		String modelVersion = "1.0";
		// modelName
		modelInfo.setModelName(modelName);

		// modelVersion
		modelInfo.setModelVersion(modelVersion);

		// modelType
		modelInfo.setModelType(ModelType.service);

		// setting modelInfo to requestDetails
		requestDetails.setModelInfo(modelInfo);

		SubscriberInfo subscriberInfo = new SubscriberInfo();

		// globalsubscriberId
		subscriberInfo.setGlobalSubscriberId(e2eSir.getService().getGlobalSubscriberId());

		// setting subscriberInfo to requestDetails
		requestDetails.setSubscriberInfo(subscriberInfo);

		RequestInfo requestInfo = new RequestInfo();

		// instanceName
		requestInfo.setInstanceName(e2eSir.getService().getName());

		// source
		requestInfo.setSource("UUI");

		// suppressRollback
		requestInfo.setSuppressRollback(true);

		// setting requestInfo to requestDetails
		requestDetails.setRequestInfo(requestInfo);

		RequestParameters requestParameters = new RequestParameters();

		// subscriptionServiceType
		requestParameters.setSubscriptionServiceType("MOG");

		// Userparams
		List<E2EUserParam> userParams;
		// userParams =
		// e2eSir.getService().getParameters().getRequestParameters().getUserParams();
		List<Map<String, Object>> userParamList = new ArrayList<>();
		Map<String, Object> userParamMap = new HashMap<>();
		// complete json request updated in the camunda
		userParamMap.put("UUIRequest", requestJSON);
		userParamMap.put("ServiceInstanceName", e2eSir.getService().getName());

		// Map<String, String> userParamMap3 = null;
		// for (E2EUserParam userp : userParams) {
		// userParamMap.put(userp.getName(), userp.getValue());
		//
		// }
		userParamList.add(userParamMap);
		requestParameters.setUserParams(userParamList);

		// setting requestParameters to requestDetails
		requestDetails.setRequestParameters(requestParameters);

		sir.setRequestDetails(requestDetails);

		// converting to string
		ObjectMapper mapper = new ObjectMapper();
		try {
			returnString = mapper.writeValueAsString(sir);
		} catch (IOException e) {
			msoLogger
					.debug("Exception while converting ServiceInstancesRequest object to string",
							e);
		}

		return returnString;
	}

	private void createOperationStatusRecordForError(Action action,
			String requestId) throws MsoDatabaseException {

		AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager();

		Session session = null;
		try {

			session = requestsDbSessionFactoryManager.getSessionFactory()
					.openSession();
			session.beginTransaction();

			OperationStatus os = new OperationStatus();
			os.setOperation(action.name());
			os.setOperationContent("");
			os.setOperationId("");
			os.setProgress("100");
			os.setReason("");
			os.setResult("error");
			os.setServiceId(requestId);
			os.setUserId("");
			Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
			Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
			os.setFinishedAt(endTimeStamp);
			os.setOperateAt(startTimeStamp);

			session.save(os);
			session.getTransaction().commit();

		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_INSERT_EXC, "", "",
					MsoLogger.ErrorCode.DataError,
					"Exception when creation record request in Operation", e);
			throw new MsoDatabaseException(
					"Data did inserted in Operatus Status Table for failure", e);
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private void createOperationStatusRecord(String actionNm, String serviceId,
			String operationId) throws MsoDatabaseException {

		AbstractSessionFactoryManager requestsDbSessionFactoryManager = new RequestsDbSessionFactoryManager();

		Session session = null;
		try {

			session = requestsDbSessionFactoryManager.getSessionFactory()
					.openSession();
			session.beginTransaction();

			OperationStatus os = new OperationStatus();
			os.setOperation(actionNm);
			os.setOperationContent("");
			os.setOperationId(operationId);
			os.setProgress("0");
			os.setReason("");
			os.setResult("processing");
			os.setServiceId(serviceId);
			// TODO : to be updated...
			os.setUserId("");
			Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
			Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
			os.setFinishedAt(endTimeStamp);
			os.setOperateAt(startTimeStamp);

			session.save(os);
			session.getTransaction().commit();

		} catch (Exception e) {
			msoLogger.error(MessageEnum.APIH_DB_INSERT_EXC, "", "",
					MsoLogger.ErrorCode.DataError,
					"Exception when creation record request in Operation", e);
			throw new MsoDatabaseException(
					"Data did inserted in Operatus Status Table", e);
		} finally {
			if (null != session) {
				session.close();
			}
		}
	}

	private OperationStatus chkSvcInstOperStatusbySvcId(String serviceId) {
		OperationStatus svcInstanceOperStatus = (RequestsDatabase.getInstance())
				.getOperationStatusByServiceId(serviceId);

		return svcInstanceOperStatus;
	}

	private OperationStatus chkDuplicateServiceNameInOperStatus(
			String serviceName) {
		OperationStatus dupServiceName = (RequestsDatabase.getInstance())
				.getOperationStatusByServiceName(serviceName);

		return dupServiceName;
	}
}
