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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.inject.Provider;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.apihandlerinfra.Status;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestReferences;
import org.onap.so.apihandlerinfra.tenantisolationbeans.TenantSyncResponse;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.onap.so.utils.UUIDChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@Path("/onap/so/infra/cloudResources")
@Api(value="/onap/so/infra/cloudResources",description="API Requests for cloud resources - Tenant Isolation")
public class CloudOrchestration {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, CloudOrchestration.class);
	private static final String ENVIRONMENT_ID_KEY = "operationalEnvironmentId";
	
	@Autowired
	private TenantIsolationRequest tenantIsolationRequest ;

	@Autowired
	private RequestsDbClient requestsDbClient;

	@Autowired
	private Provider<TenantIsolationRunnable> tenantIsolationRunnable;

	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Create an Operational Environment",response=Response.class)
	@Transactional
	public Response createOperationEnvironment(String request, @PathParam("version") String version, @Context ContainerRequestContext requestContext) throws ApiException{
		msoLogger.debug("Received request to Create Operational Environment");
		return cloudOrchestration(request, Action.create, null, version, getRequestId(requestContext));
	}

	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments/{operationalEnvironmentId}/activate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Activate an Operational Environment",response=Response.class)
	@Transactional
	public Response activateOperationEnvironment(String request, @PathParam("version") String version, @PathParam("operationalEnvironmentId") String operationalEnvironmentId,
												@Context ContainerRequestContext requestContext) throws ApiException{
		msoLogger.debug("Received request to Activate an Operational Environment");
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put(ENVIRONMENT_ID_KEY, operationalEnvironmentId);
		return cloudOrchestration(request, Action.activate, instanceIdMap, version, getRequestId(requestContext));
	}

	@POST
	@Path("/{version:[vV][1]}/operationalEnvironments/{operationalEnvironmentId}/deactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value="Deactivate an Operational Environment",response=Response.class)
	@Transactional
	public Response deactivateOperationEnvironment(String request, @PathParam("version") String version, @PathParam("operationalEnvironmentId") String operationalEnvironmentId, @Context ContainerRequestContext requestContext) throws ApiException{
		msoLogger.debug("Received request to Deactivate an Operational Environment");
		HashMap<String, String> instanceIdMap = new HashMap<>();
		instanceIdMap.put(ENVIRONMENT_ID_KEY, operationalEnvironmentId);
		return cloudOrchestration(request, Action.deactivate, instanceIdMap, version, getRequestId(requestContext));
	}


	private Response cloudOrchestration(String requestJSON, Action action, HashMap<String, String> instanceIdMap, String version, String requestId) throws ApiException{
		MsoLogger.setLogContext(requestId, null);
	    msoLogger.info(MessageEnum.APIH_GENERATED_REQUEST_ID, requestId, "", "");
		long startTime = System.currentTimeMillis ();
		CloudOrchestrationRequest cor = null;
		tenantIsolationRequest.setRequestId(requestId);

		cor = convertJsonToCloudOrchestrationRequest(requestJSON, action, startTime, cor);

		try {
			tenantIsolationRequest.parse(cor, instanceIdMap, action);
		}catch(ValidationException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,MsoLogger.ErrorCode.SchemaError).build();


			throw new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER)
					.cause(e).errorInfo(errorLoggerInfo).build();
		}

		String instanceName = cor.getRequestDetails().getRequestInfo().getInstanceName();
		String resourceType = cor.getRequestDetails().getRequestInfo().getResourceType().name();
		InfraActiveRequests dup = null;

		dup = duplicateCheck(action, instanceIdMap, startTime, instanceName, resourceType);

		if(dup == null && (Action.activate.equals(action) || Action.deactivate.equals(action))) {
			dup = requestsDbClient.checkVnfIdStatus(cor.getOperationalEnvironmentId());
		}

		if(dup != null) {
			String instance = null;
			if(instanceName != null){
				instance = instanceName;
			}else if (instanceIdMap != null){
				instance = instanceIdMap.get(resourceType + "InstanceId");
			}

			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_FOUND, MsoLogger.ErrorCode.SchemaError).build();


			throw new DuplicateRequestException.Builder(resourceType,instance,dup.getRequestStatus(),dup.getRequestId(), HttpStatus.SC_CONFLICT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
					.errorInfo(errorLoggerInfo).build();
		}

		String instanceId = null;

		if(instanceIdMap != null && instanceIdMap.get(ENVIRONMENT_ID_KEY) != null) {
			instanceId = instanceIdMap.get(ENVIRONMENT_ID_KEY);
		} else {
			instanceId = UUIDChecker.generateUUID(msoLogger);
			tenantIsolationRequest.setOperationalEnvironmentId(instanceId);
			cor.setOperationalEnvironmentId(instanceId);
		}

		tenantIsolationRequest.createRequestRecord(Status.IN_PROGRESS, action);

		OperationalEnvironment opEnv = cor.getRequestDetails().getRequestParameters().getOperationalEnvironmentType();
		String operationalEnvType = opEnv != null ? opEnv.name() : null;

		TenantIsolationRunnable runnable = tenantIsolationRunnable.get();
		runnable.run(action, operationalEnvType, cor, requestId);

		String encodedValue;
		try {
			 encodedValue = new String(instanceId.getBytes("UTF-8"));
		} catch(UnsupportedEncodingException ex) {
			 ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,MsoLogger.ErrorCode.DataError).build();


			 throw new ValidateException.Builder("Could not encode instanceID" + ex.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER)
					 .cause(ex).errorInfo(errorLoggerInfo).build();
		}

		TenantSyncResponse tenantResponse = new TenantSyncResponse();
		RequestReferences reqReference = new RequestReferences();
		reqReference.setInstanceId(encodedValue);
		reqReference.setRequestId(requestId);
		tenantResponse.setRequestReferences(reqReference);

		return Response.ok(tenantResponse).build();
	}

	private InfraActiveRequests duplicateCheck(Action action, HashMap<String, String> instanceIdMap, long startTime,
						String instanceName, String requestScope) throws ApiException {
		try {
			return requestsDbClient.checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
		} catch (Exception e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, MsoLogger.ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();


			throw new ValidateException.Builder("Duplicate Check Request", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
					.errorInfo(errorLoggerInfo).build();
		}
	}

	private CloudOrchestrationRequest convertJsonToCloudOrchestrationRequest(String requestJSON, Action action, long startTime,
                                                                             CloudOrchestrationRequest cor) throws ApiException {
		try{
			msoLogger.debug("Converting incoming JSON request to Object");
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(requestJSON, CloudOrchestrationRequest.class);
		} catch(IOException e){

			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR,MsoLogger.ErrorCode.SchemaError).build();


			ValidateException validateException = new ValidateException.Builder("Mapping of request to JSON object failed.  " + e.getMessage(), HttpStatus.SC_BAD_REQUEST,ErrorNumbers.SVC_BAD_PARAMETER)
					.cause(e).errorInfo(errorLoggerInfo).build();
			if (tenantIsolationRequest.getRequestId () != null) {
				tenantIsolationRequest.createRequestRecord (Status.FAILED, action);
			}
			throw validateException;
		}
	}
	
	private String getRequestId(ContainerRequestContext requestContext) {
		return requestContext.getProperty("requestId").toString();
	}
}
