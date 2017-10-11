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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ResponseHandler;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.E2EServiceInstanceRequest;
import org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans.E2EUserParam;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.apihandlerinfra.serviceinstancebeans.SubscriberInfo;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/e2eServiceInstances")
@Api(value="/e2eServiceInstances",description="API Requests for E2E Service Instances")
public class E2EServiceInstances {

	private HashMap<String, String> instanceIdMap = new HashMap<String,String>();
	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
	private static MsoAlarmLogger alarmLogger = new MsoAlarmLogger ();
	public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

	public E2EServiceInstances() {
	}
	
	/**
     *POST Requests for E2E Service create Instance on a version provided
     */

	@POST
	@Path("/{version:[vV][3-5]}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create a E2E Service Instance on a version provided",response=Response.class)
	public Response createE2EServiceInstance(String request, @PathParam("version") String version) {

        return E2EserviceInstances(request, Action.createInstance,	null, version);
	}

	/**
     *DELETE Requests for E2E Service delete Instance on a specified version and serviceId
     */
	
	@DELETE
	@Path("/{version:[vV][3-5]}/{serviceId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Delete E2E Service Instance on a specified version and serviceId",response=Response.class)
	public Response deleteE2EServiceInstance(String request, @PathParam("version") String version, @PathParam("serviceId") String serviceId) {

        instanceIdMap.put("serviceId", serviceId);

        return E2EserviceInstances(request, Action.deleteInstance, null, version);
	}
	
	private Response E2EserviceInstances(String requestJSON, Action action,
        HashMap<String, String> instanceIdMap, String version) {

		String requestId = UUIDChecker.generateUUID(msoLogger);
		long startTime = System.currentTimeMillis();
		msoLogger.debug("requestId is: " + requestId);
		E2EServiceInstanceRequest sir = null;

		MsoRequest msoRequest = new MsoRequest(requestId);
		ObjectMapper mapper = new ObjectMapper();
		try {
			sir = mapper
					.readValue(requestJSON, E2EServiceInstanceRequest.class);

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
			return response;
		}

		InfraActiveRequests dup = null;
		String instanceName = sir.getService().getName();
		String requestScope = sir.getService().getParameters().getNodeType();

		try {
			if(!(instanceName==null && "service".equals(requestScope) && (action == Action.createInstance || action
					== Action.activateInstance))){
				dup = (RequestsDatabase.getInstance()).checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
			}
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DUPLICATE_CHECK_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "Error during duplicate check ", e);

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, MsoException.ServiceException,
					e.getMessage(),
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
					null) ;


			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Error during duplicate check");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		if (dup != null) {
			// Found the duplicate record. Return the appropriate error.
			String instance = null;
			if(instanceName != null){
				instance = instanceName;
			}else{
				instance = instanceIdMap.get(requestScope + "InstanceId");
			}
			String dupMessage = "Error: Locked instance - This " + requestScope + " (" + instance + ") " + "already has a request being worked with a status of " + dup.getRequestStatus() + " (RequestId - " + dup.getRequestId() + "). The existing request must finish or be cleaned up before proceeding.";

			Response response = msoRequest.buildServiceErrorResponse(HttpStatus.SC_CONFLICT, MsoException.ServiceException,
					dupMessage,
					ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
					null) ;


			msoLogger.warn (MessageEnum.APIH_DUPLICATE_FOUND, dupMessage, "", "", MsoLogger.ErrorCode.SchemaError, "Duplicate request - Subscriber already has a request for this service");
			msoRequest.createRequestRecord (Status.FAILED, action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.Conflict, dupMessage);
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		CatalogDatabase db = null;
		RecipeLookupResult recipeLookupResult = null;
		try {
			db = CatalogDatabase.getInstance();
			recipeLookupResult = getServiceInstanceOrchestrationURI(db, sir, action);
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.AvailabilityError, "Exception while communciate with Catalog DB", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException,
					"No communication to catalog DB " + e.getMessage (),
					ErrorNumbers.SVC_NO_SERVER_RESOURCES,
					null);
			alarmLogger.sendAlarm ("MsoDatabaseAccessError",
					MsoAlarmLogger.CRITICAL,
					Messages.errors.get (ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB));
			msoRequest.createRequestRecord (Status.FAILED,action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while communciate with DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		} finally {
			if(db != null) {
			    db.close();
			}
		}

		if (recipeLookupResult == null) {
			msoLogger.error (MessageEnum.APIH_DB_ATTRIBUTE_NOT_FOUND, MSO_PROP_APIHANDLER_INFRA, "", "", MsoLogger.ErrorCode.DataError, "No recipe found in DB");
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_NOT_FOUND,
					MsoException.ServiceException,
					"Recipe does not exist in catalog DB",
					ErrorNumbers.SVC_GENERAL_SERVICE_ERROR,
					null);
			msoRequest.createRequestRecord (Status.FAILED, action);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "No recipe found in DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());

			return response;
		}

		try {
			msoRequest.createRequestRecord (Status.PENDING, action);
		} catch (Exception e) {
			msoLogger.error (MessageEnum.APIH_DB_ACCESS_EXC_REASON, "Exception while creating record in DB", "", "", MsoLogger.ErrorCode.SchemaError, "Exception while creating record in DB", e);
			msoRequest.setStatus (org.openecomp.mso.apihandlerinfra.vnfbeans.RequestStatusType.FAILED);
			Response response = msoRequest.buildServiceErrorResponse (HttpStatus.SC_INTERNAL_SERVER_ERROR,
					MsoException.ServiceException,
					"Exception while creating record in DB " + e.getMessage(),
					ErrorNumbers.SVC_BAD_PARAMETER,
					null);
			msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "Exception while creating record in DB");
			msoLogger.debug ("End of the transaction, the final response is: " + (String) response.getEntity ());
			return response;
		}

		String modelInfo = sir.getService().getParameters().getNodeTemplateName();
		String[] arrayOfInfo = modelInfo.split(":");
		String serviceInstanceType = arrayOfInfo[0];



		String serviceId = "";

		RequestClient requestClient = null;
		HttpResponse response = null;

		long subStartTime = System.currentTimeMillis();
		String sirRequestJson = mappingObtainedRequestJSONToServiceInstanceRequest(sir);

		try {
			requestClient = RequestClientFactory.getRequestClient (recipeLookupResult.getOrchestrationURI (), MsoPropertiesUtils.loadMsoProperties ());

			// Capture audit event
			msoLogger.debug ("MSO API Handler Posting call to BPEL engine for url: " + requestClient.getUrl ());

			response = requestClient.post(requestId, false,
					recipeLookupResult.getRecipeTimeout(),
					action.name(), serviceId, null, null, null, null, serviceInstanceType,
					null, null, null, sirRequestJson);

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
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) resp.getEntity());
			return resp;
		}

		ResponseHandler respHandler = new ResponseHandler(response,
				requestClient.getType());
		int bpelStatus = respHandler.getStatus();

		// BPEL accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {
			String camundaJSONResponseBody = respHandler.getResponseBody();
			msoLogger
			.debug("Received from Camunda: " + camundaJSONResponseBody);
			(RequestsDatabase.getInstance()).updateInfraStatus(requestId,
					Status.IN_PROGRESS.toString(),
					Constants.PROGRESS_REQUEST_IN_PROGRESS,
					Constants.MODIFIED_BY_APIHANDLER);

			msoLogger.recordAuditEvent(startTime,
					MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
					"BPMN accepted the request, the request is in progress");
			msoLogger.debug("End of the transaction, the final response is: "
					+ (String) camundaJSONResponseBody);
			return Response.status(HttpStatus.SC_ACCEPTED)
					.entity(camundaJSONResponseBody).build();
		} else {
			List<String> variables = new ArrayList<String>();
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
				msoLogger
				.debug("End of the transaction, the final response is: "
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
				msoLogger
				.debug("End of the transaction, the final response is: "
						+ (String) resp.getEntity());
				return resp;
			}
		}
	}

	private RecipeLookupResult getServiceInstanceOrchestrationURI(
			CatalogDatabase db, E2EServiceInstanceRequest sir, Action action) {

		RecipeLookupResult recipeLookupResult = null;

		recipeLookupResult = getServiceURI(db, sir, action);

		if (recipeLookupResult != null) {
			msoLogger.debug ("Orchestration URI is: " + recipeLookupResult.getOrchestrationURI() + ", recipe Timeout is: " + Integer.toString(recipeLookupResult.getRecipeTimeout ()));
		}
		else {
			msoLogger.debug("No matching recipe record found");
		}
		return recipeLookupResult;
	}

	private RecipeLookupResult getServiceURI(CatalogDatabase db,
			E2EServiceInstanceRequest sir, Action action) {

		String defaultServiceModelName = "UUI_DEFAULT";

		Service serviceRecord = null;
		ServiceRecipe recipe = null;

		serviceRecord = db.getServiceByModelName(defaultServiceModelName);
		recipe = db.getServiceRecipeByModelUUID(serviceRecord.getModelUUID(), action.name());

		if (recipe == null) {
			return null;
		}
		return new RecipeLookupResult(recipe.getOrchestrationUri(),
				recipe.getRecipeTimeout());

	}

	private String mappingObtainedRequestJSONToServiceInstanceRequest(E2EServiceInstanceRequest e2eSir){

		ServiceInstancesRequest sir = new ServiceInstancesRequest();

		String returnString = null;
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		
		//ModelInvariantId
		modelInfo.setModelInvariantId(e2eSir.getService().getServiceDefId());
		
		//modelNameVersionId
		modelInfo.setModelNameVersionId(e2eSir.getService().getTemplateId());
		
		String modelInfoValue = e2eSir.getService().getParameters().getNodeTemplateName();
		String[] arrayOfInfo = modelInfoValue.split(":");
		String modelName = arrayOfInfo[0];
		String modelVersion = arrayOfInfo[1];
		
		//modelName
		modelInfo.setModelName(modelName);
		
		//modelVersion
		modelInfo.setModelVersion(modelVersion);
		
		//modelType
		//if(ModelType.service.equals(e2eSir.getService().getParameters().getNodeType())){
			modelInfo.setModelType(ModelType.service);
		//}
		
		//setting modelInfo to requestDetails
		requestDetails.setModelInfo(modelInfo);
		
		SubscriberInfo subscriberInfo = new SubscriberInfo();

		//globalsubscriberId
		subscriberInfo.setGlobalSubscriberId(e2eSir.getService().getParameters().getGlobalSubscriberId());

		//subscriberName
		subscriberInfo.setSubscriberName(e2eSir.getService().getParameters().getSubscriberName());
		
		//setting subscriberInfo to requestDetails
		requestDetails.setSubscriberInfo(subscriberInfo);
		
		RequestInfo requestInfo = new RequestInfo();
		
		//instanceName
		requestInfo.setInstanceName(e2eSir.getService().getName());

		//source
		requestInfo.setSource("UUI");

		//suppressRollback
		requestInfo.setSuppressRollback(true);

		//setting requestInfo to requestDetails
		requestDetails.setRequestInfo(requestInfo);
		
		RequestParameters requestParameters = new RequestParameters();
		
		//subscriptionServiceType
		requestParameters.setSubscriptionServiceType("MOG");

		//Userparams
		List<E2EUserParam> userParams;
		userParams = e2eSir.getService().getParameters().getRequestParameters().getUserParams();
		List<Map<String, String>> userParamList = new ArrayList<>();
		Map<String,String> userParamMap= new HashMap<>();
		for(E2EUserParam userp: userParams){
			userParamMap.put(userp.getName(), userp.getValue());
			userParamList.add(userParamMap);
		}

		requestParameters.setUserParams(userParamList);
		
		//setting requestParameters to requestDetails
		requestDetails.setRequestParameters(requestParameters);
		
		sir.setRequestDetails(requestDetails);

		//converting to string
		ObjectMapper mapper = new ObjectMapper();
		try {
			returnString = mapper.writeValueAsString(sir);
		} catch (IOException e) {
			msoLogger.debug("Exception while converting ServiceInstancesRequest object to string", e);
		}

		return returnString;
	}
}