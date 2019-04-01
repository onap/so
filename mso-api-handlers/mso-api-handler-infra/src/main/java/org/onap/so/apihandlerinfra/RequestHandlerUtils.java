/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandler.common.RequestClient;
import org.onap.so.apihandler.common.RequestClientFactory;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandler.common.ResponseHandler;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ClientConnectionException;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.LogConstants;
import org.onap.so.logger.MessageEnum;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.UUIDChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class RequestHandlerUtils {

	private static Logger logger = LoggerFactory.getLogger(RequestHandlerUtils.class);
	
	private static final String SAVE_TO_DB = "save instance to db";
	
	@Autowired
	private Environment env;	

	@Autowired
	private RequestClientFactory reqClientFactory;
	
	@Autowired
	private RequestsDbClient infraActiveRequestsClient;
	
	@Autowired
	private ResponseBuilder builder;
	
	@Autowired
	private MsoRequest msoRequest;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private CatalogDbClient catalogDbClient;
		
	public Response postBPELRequest(InfraActiveRequests currentActiveReq, RequestClientParameter requestClientParameter, String orchestrationUri, String requestScope)throws ApiException {
		RequestClient requestClient = null;
		HttpResponse response = null;
		try {
			requestClient = reqClientFactory.getRequestClient (orchestrationUri);
			response = requestClient.post(requestClientParameter);
		} catch (Exception e) {
			
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, ErrorCode.AvailabilityError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            String url = requestClient != null ? requestClient.getUrl() : "";
            ClientConnectionException clientException = new ClientConnectionException.Builder(url, HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).cause(e).errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, clientException.getMessage());

            throw clientException;
		}

		if (response == null) {
			
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_COMMUNICATE_ERROR, ErrorCode.BusinessProcesssError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ClientConnectionException clientException = new ClientConnectionException.Builder(requestClient.getUrl(), HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).errorInfo(errorLoggerInfo).build();

            updateStatus(currentActiveReq, Status.FAILED, clientException.getMessage());

            throw clientException;
		}

		ResponseHandler respHandler = null;
        int bpelStatus = 500;
        try {
            respHandler = new ResponseHandler (response, requestClient.getType ());
            bpelStatus = respHandler.getStatus ();
        } catch (ApiException e) {
            logger.error("Exception occurred", e);
            ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
            ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
                        .errorInfo(errorLoggerInfo).build();
            updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
            throw validateException;
        }

		// BPEL accepted the request, the request is in progress
		if (bpelStatus == HttpStatus.SC_ACCEPTED) {
			ServiceInstancesResponse jsonResponse;
			CamundaResponse camundaResp = respHandler.getResponse();
			
			if("Success".equalsIgnoreCase(camundaResp.getMessage())) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					jsonResponse = mapper.readValue(camundaResp.getResponse(), ServiceInstancesResponse.class);
					jsonResponse.getRequestReferences().setRequestId(requestClientParameter.getRequestId());
					Optional<URL> selfLinkUrl = msoRequest.buildSelfLinkUrl(currentActiveReq.getRequestUrl(), requestClientParameter.getRequestId());
					if(selfLinkUrl.isPresent()){
						jsonResponse.getRequestReferences().setRequestSelfLink(selfLinkUrl.get());
					} else {
					    jsonResponse.getRequestReferences().setRequestSelfLink(null);
					}    
				} catch (IOException e) {
					logger.error("Exception occurred", e);
					ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
					ValidateException validateException = new ValidateException.Builder("Exception caught mapping Camunda JSON response to object", HttpStatus.SC_NOT_ACCEPTABLE, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
			                    .errorInfo(errorLoggerInfo).build();
					updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());
					throw validateException;
				}	
				return builder.buildResponse(HttpStatus.SC_ACCEPTED, requestClientParameter.getRequestId(), jsonResponse, requestClientParameter.getApiVersion());
			} 
		}
			
		List<String> variables = new ArrayList<>();
		variables.add(bpelStatus + "");
		String camundaJSONResponseBody = respHandler.getResponseBody ();
		if (camundaJSONResponseBody != null && !camundaJSONResponseBody.isEmpty ()) {
			
		    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();
		    BPMNFailureException bpmnException = new BPMNFailureException.Builder(String.valueOf(bpelStatus) + camundaJSONResponseBody, bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
		            .errorInfo(errorLoggerInfo).build();

		    updateStatus(currentActiveReq, Status.FAILED, bpmnException.getMessage());

		    throw bpmnException;
		} else {
		
		    ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.BusinessProcesssError).errorSource(requestClient.getUrl()).build();


		    BPMNFailureException servException = new BPMNFailureException.Builder(String.valueOf(bpelStatus), bpelStatus, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
		            .errorInfo(errorLoggerInfo).build();
		    updateStatus(currentActiveReq, Status.FAILED, servException.getMessage());

		    throw servException;
		}
	}
	
	public void updateStatus(InfraActiveRequests aq, Status status, String errorMessage) throws RequestDbFailureException{
		if ((status == Status.FAILED) || (status == Status.COMPLETE)) {
			aq.setStatusMessage (errorMessage);
			aq.setProgress(new Long(100));
			aq.setRequestStatus(status.toString());
			Timestamp endTimeStamp = new Timestamp (System.currentTimeMillis());
			aq.setEndTime (endTimeStamp);
			try{
				infraActiveRequestsClient.save(aq);
			}catch(Exception e){
				ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DB_ACCESS_EXC, ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
	            throw new RequestDbFailureException.Builder(SAVE_TO_DB, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
	                    .errorInfo(errorLoggerInfo).build();
			}
		}
	}
	
	public String deriveRequestScope(Actions action, ServiceInstancesRequest sir, String requestUri) {
		if(action == Action.inPlaceSoftwareUpdate || action == Action.applyUpdatedConfig){
			return (ModelType.vnf.name());
		}else if(action == Action.addMembers || action == Action.removeMembers){
			return(ModelType.instanceGroup.toString());
		}else{
			String requestScope;
			if(sir.getRequestDetails().getModelInfo().getModelType() == null){
				requestScope = requestScopeFromUri(requestUri);
			}else{
				requestScope = sir.getRequestDetails().getModelInfo().getModelType().name(); 
			}
			return requestScope; 
		}
	}
	
	
	public void validateHeaders(ContainerRequestContext context) throws ValidationException{
		MultivaluedMap<String, String> headers = context.getHeaders();
		if(!headers.containsKey(ONAPLogConstants.Headers.REQUEST_ID)){
			 throw new ValidationException(ONAPLogConstants.Headers.REQUEST_ID + " header", true);
		}
		if(!headers.containsKey(ONAPLogConstants.Headers.PARTNER_NAME)){
			throw new ValidationException(ONAPLogConstants.Headers.PARTNER_NAME + " header", true);
		}
		if(!headers.containsKey(REQUESTOR_ID)){
			throw new ValidationException(REQUESTOR_ID + " header", true);
		}
	}
	
	public String getRequestUri(ContainerRequestContext context, String uriPrefix){
		String requestUri = context.getUriInfo().getPath();
		String httpUrl = MDC.get(LogConstants.URI_BASE).concat(requestUri);
		MDC.put(LogConstants.HTTP_URL, httpUrl);
		requestUri = requestUri.substring(requestUri.indexOf(uriPrefix) + uriPrefix.length());
		return requestUri;
	}
	
	public InfraActiveRequests duplicateCheck(Actions action, HashMap<String, String> instanceIdMap, long startTime,
            MsoRequest msoRequest, String instanceName, String requestScope, InfraActiveRequests currentActiveReq) throws ApiException {
		InfraActiveRequests dup = null;
		try {
			if(!(instanceName==null && requestScope.equals("service") && (action == Action.createInstance || action == Action.activateInstance || action == Action.assignInstance))){
				dup = infraActiveRequestsClient.checkInstanceNameDuplicate (instanceIdMap, instanceName, requestScope);
			}
		} catch (Exception e) {
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			RequestDbFailureException requestDbFailureException = new RequestDbFailureException.Builder("check for duplicate instance", e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
					.errorInfo(errorLoggerInfo).build();
			updateStatus(currentActiveReq, Status.FAILED, requestDbFailureException.getMessage());
			throw requestDbFailureException;
		}
		return dup;
	}
	
	public boolean camundaHistoryCheck(InfraActiveRequests duplicateRecord, InfraActiveRequests currentActiveReq) throws RequestDbFailureException, ContactCamundaException{
		String requestId = duplicateRecord.getRequestId();
		String path = env.getProperty("mso.camunda.rest.history.uri") + requestId;
		String targetUrl = env.getProperty("mso.camundaURL") + path;
		HttpHeaders headers = setCamundaHeaders(env.getRequiredProperty("mso.camundaAuth"), env.getRequiredProperty("mso.msoKey")); 
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<List<HistoricProcessInstanceEntity>> response = null;
		try{
			response = restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>(){});
		}catch(HttpStatusCodeException e){
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_CHECK_EXC, ErrorCode.DataError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			ContactCamundaException contactCamundaException= new ContactCamundaException.Builder(requestId, e.toString(), HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).cause(e)
					.errorInfo(errorLoggerInfo).build();
			updateStatus(currentActiveReq, Status.FAILED, contactCamundaException.getMessage());
			throw contactCamundaException;
		}
		if(response.getBody().isEmpty()){
			updateStatus(duplicateRecord, Status.COMPLETE, "Request Completed");
		}
		for(HistoricProcessInstance instance : response.getBody()){
			if(instance.getState().equals("ACTIVE")){
				return true;
			}else{
				updateStatus(duplicateRecord, Status.COMPLETE, "Request Completed");
			}
		}	
		return false;
	}
	
	protected HttpHeaders setCamundaHeaders(String auth, String msoKey) {
		HttpHeaders headers = new HttpHeaders();
		List<org.springframework.http.MediaType> acceptableMediaTypes = new ArrayList<>();
		acceptableMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
		headers.setAccept(acceptableMediaTypes);
		try {
			String userCredentials = CryptoUtils.decrypt(auth, msoKey);
			if(userCredentials != null) {
				headers.add(HttpHeaders.AUTHORIZATION, "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes()));
			}
		} catch(GeneralSecurityException e) {
			logger.error("Security exception", e);
		}
		return headers;
	}

	public ServiceInstancesRequest convertJsonToServiceInstanceRequest(String requestJSON, Actions action, long startTime,
                                     ServiceInstancesRequest sir, MsoRequest msoRequest, String requestId, String requestUri) throws ApiException {
		try{
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(requestJSON, ServiceInstancesRequest.class);

		} catch (IOException e) {

			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

			ValidateException validateException = new ValidateException.Builder("Error mapping request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
					.errorInfo(errorLoggerInfo).build();
			String requestScope = requestScopeFromUri(requestUri);

			msoRequest.createErrorRequestRecord(Status.FAILED, requestId, validateException.getMessage(), action, requestScope, requestJSON);

			throw validateException;
		}
	}

	public void parseRequest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMap, Actions action, String version, 
			String requestJSON, Boolean aLaCarte, String requestId, InfraActiveRequests currentActiveReq) throws ValidateException, RequestDbFailureException {
		int reqVersion = Integer.parseInt(version.substring(1));
		try {
			msoRequest.parse(sir, instanceIdMap, action, version, requestJSON, reqVersion, aLaCarte);
		} catch (Exception e) {
			logger.error("failed to parse request", e);
			ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			ValidateException validateException = new ValidateException.Builder("Error parsing request: " + e.getMessage(), HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(e)
					.errorInfo(errorLoggerInfo).build();

			updateStatus(currentActiveReq, Status.FAILED, validateException.getMessage());

			throw validateException;
		}
	}
	
	public void buildErrorOnDuplicateRecord(InfraActiveRequests currentActiveReq, Actions action, HashMap<String, String> instanceIdMap, long startTime, MsoRequest msoRequest,
             String instanceName, String requestScope, InfraActiveRequests dup) throws ApiException {

		String instance = null;
		if(instanceName != null){
			instance = instanceName;
		}else{
			instance = instanceIdMap.get(requestScope + "InstanceId");
		}		
		ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_DUPLICATE_FOUND, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();

		DuplicateRequestException dupException = new DuplicateRequestException.Builder(requestScope,instance,dup.getRequestStatus(),dup.getRequestId(), HttpStatus.SC_CONFLICT, ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)
				.errorInfo(errorLoggerInfo).build();

		updateStatus(currentActiveReq, Status.FAILED, dupException.getMessage());

		throw dupException;
	}
	
	 public String getRequestId(ContainerRequestContext requestContext) throws ValidateException {
	    String requestId = null;
	    if (requestContext.getProperty("requestId") != null) {
	    	requestId = requestContext.getProperty("requestId").toString();
	    }
	    if (UUIDChecker.isValidUUID(requestId)) {
	    	return requestId;
	    } else {
	    	ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_BPEL_RESPONSE_ERROR, ErrorCode.SchemaError).errorSource(Constants.MSO_PROP_APIHANDLER_INFRA).build();
			ValidateException validateException = new ValidateException.Builder("Request Id " + requestId + " is not a valid UUID", HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER)
		                   .errorInfo(errorLoggerInfo).build();
			
			throw validateException;
	    }
	 }	
	
	 public void setInstanceId(InfraActiveRequests currentActiveReq, String requestScope, String instanceId, Map<String, String> instanceIdMap) {
		if(StringUtils.isNotBlank(instanceId)) {
			if(ModelType.service.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setServiceInstanceId(instanceId);
			} else if(ModelType.vnf.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVnfId(instanceId);
			} else if(ModelType.vfModule.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVfModuleId(instanceId);
			} else if(ModelType.volumeGroup.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setVolumeGroupId(instanceId);
			} else if(ModelType.network.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setNetworkId(instanceId);
			} else if(ModelType.configuration.name().equalsIgnoreCase(requestScope)) {
				currentActiveReq.setConfigurationId(instanceId);
			}else if(ModelType.instanceGroup.toString().equalsIgnoreCase(requestScope)){
				currentActiveReq.setInstanceGroupId(instanceId);
			}
		} else if(instanceIdMap != null && !instanceIdMap.isEmpty()) {
			if(instanceIdMap.get("serviceInstanceId") != null){
				currentActiveReq.setServiceInstanceId(instanceIdMap.get("serviceInstanceId"));
	       	}
	       	if(instanceIdMap.get("vnfInstanceId") != null){
	       		currentActiveReq.setVnfId(instanceIdMap.get("vnfInstanceId"));
	       	}
	       	if(instanceIdMap.get("vfModuleInstanceId") != null){
	       		currentActiveReq.setVfModuleId(instanceIdMap.get("vfModuleInstanceId"));
	       	}
	       	if(instanceIdMap.get("volumeGroupInstanceId") != null){
	       		currentActiveReq.setVolumeGroupId(instanceIdMap.get("volumeGroupInstanceId"));
	       	}
	       	if(instanceIdMap.get("networkInstanceId") != null){
	       		currentActiveReq.setNetworkId(instanceIdMap.get("networkInstanceId"));
	       	}
	       	if(instanceIdMap.get("configurationInstanceId") != null){
	       		currentActiveReq.setConfigurationId(instanceIdMap.get("configurationInstanceId"));
	       	}
	       	if(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID) != null){
	       		currentActiveReq.setInstanceGroupId(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID));
	       	}
		}
	 }
	 
	 public String mapJSONtoMSOStyle(String msoRawRequest, ServiceInstancesRequest serviceInstRequest, boolean isAlaCarte, Actions action) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();    	
	    mapper.setSerializationInclusion(Include.NON_NULL);    	
	    if(msoRawRequest != null){
		   	ServiceInstancesRequest sir = mapper.readValue(msoRawRequest, ServiceInstancesRequest.class);    	
		   	if(	serviceInstRequest != null && 
		   		serviceInstRequest.getRequestDetails() != null && 
		   		serviceInstRequest.getRequestDetails().getRequestParameters() != null) {
		   		if(	!isAlaCarte && Action.createInstance.equals(action)) {
		   			sir.getRequestDetails().setCloudConfiguration(serviceInstRequest.getRequestDetails().getCloudConfiguration());
		   			sir.getRequestDetails().getRequestParameters().setUserParams(serviceInstRequest.getRequestDetails().getRequestParameters().getUserParams());
		   		}
		   		sir.getRequestDetails().getRequestParameters().setUsePreload(serviceInstRequest.getRequestDetails().getRequestParameters().getUsePreload());
		   	}
		    	
		   	logger.debug("Value as string: {}", mapper.writeValueAsString(sir));
		   	return mapper.writeValueAsString(sir);
	    }
	    return null;
	}
	 
	public Optional<String> retrieveModelName(RequestParameters requestParams) {
	   	String requestTestApi = null;
	   	TestApi testApi = null;
	    	
	   	if (requestParams != null) {
	   		requestTestApi = requestParams.getTestApi();
	   	}
	    	
	   	if (requestTestApi == null) {
	   		if(requestParams != null && requestParams.getALaCarte() != null && !requestParams.getALaCarte()) {
		    	requestTestApi = env.getProperty(CommonConstants.MACRO_TEST_API);
	   		} else {
	   			requestTestApi = env.getProperty(CommonConstants.ALACARTE_TEST_API);
	   		}
	   	}
	    	
		try {
			testApi = TestApi.valueOf(requestTestApi);
			return Optional.of(testApi.getModelName());
		} catch (Exception e) {
			logger.warn("Catching the exception on the valueOf enum call and continuing", e);
			throw new IllegalArgumentException("Invalid TestApi is provided", e);
		}
	   }
	    
	public String getDefaultModel(ServiceInstancesRequest sir) {
	   	String defaultModel = sir.getRequestDetails().getRequestInfo().getSource() + "_DEFAULT";
	   	Optional<String> oModelName = retrieveModelName(sir.getRequestDetails().getRequestParameters());
		if (oModelName.isPresent()) {
			defaultModel = oModelName.get();
		}
		return defaultModel;
	}
		
	public String getServiceType(String requestScope, ServiceInstancesRequest sir, Boolean aLaCarteFlag){
		String serviceType = null;
		if(requestScope.equalsIgnoreCase(ModelType.service.toString())){
			String defaultServiceModelName = getDefaultModel(sir);
			org.onap.so.db.catalog.beans.Service serviceRecord;
			if(aLaCarteFlag){
				 serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
				 if(serviceRecord != null){
					 serviceType = serviceRecord.getServiceType();
				 }
			}else{
				serviceRecord = catalogDbClient.getServiceByID(sir.getRequestDetails().getModelInfo().getModelVersionId());
				if(serviceRecord != null){
					 serviceType = serviceRecord.getServiceType();
				 }else{
					 serviceRecord = catalogDbClient.getFirstByModelNameOrderByModelVersionDesc(defaultServiceModelName);
					 if(serviceRecord != null){
						 serviceType = serviceRecord.getServiceType();
					 }
				 }
			}
		}else{
			serviceType = msoRequest.getServiceInstanceType(sir, requestScope);
			}
		return serviceType;
	}
	
	protected String setServiceInstanceId(String requestScope, ServiceInstancesRequest sir){
		if(sir.getServiceInstanceId () != null){
			return sir.getServiceInstanceId ();
		}else if(requestScope.equalsIgnoreCase(ModelType.instanceGroup.toString())){
			RelatedInstanceList[] relatedInstances = sir.getRequestDetails().getRelatedInstanceList();
			if(relatedInstances != null){
				for(RelatedInstanceList relatedInstanceList : relatedInstances){
					RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
					if(relatedInstance.getModelInfo().getModelType() == ModelType.service){
						return relatedInstance.getInstanceId();
					}
				}
			}
		}
		return null;
	}
	
	private String requestScopeFromUri(String requestUri){
		String requestScope;
		if(requestUri.contains(ModelType.network.name())){
			requestScope = ModelType.network.name();
		}else if(requestUri.contains(ModelType.vfModule.name())){
			requestScope = ModelType.vfModule.name();
		}else if(requestUri.contains(ModelType.volumeGroup.name())){
			requestScope = ModelType.volumeGroup.name();
		}else if(requestUri.contains(ModelType.configuration.name())){
			requestScope = ModelType.configuration.name();
		}else if(requestUri.contains(ModelType.vnf.name())){
			requestScope = ModelType.vnf.name();
		}else{
			requestScope = ModelType.service.name();
		}
		return requestScope;
	}
	
}
