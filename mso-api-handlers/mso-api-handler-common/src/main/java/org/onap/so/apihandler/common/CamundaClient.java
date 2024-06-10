/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.apihandler.common;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.camundabeans.CamundaBooleanInput;
import org.onap.so.apihandler.camundabeans.CamundaInput;
import org.onap.so.apihandler.camundabeans.CamundaIntegerInput;
import org.onap.so.apihandler.camundabeans.CamundaRequest;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandler.camundabeans.CamundaVIDRequest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ClientConnectionException;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class CamundaClient {
    private static Logger logger = LoggerFactory.getLogger(CamundaClient.class);
    private static final String BASIC = "Basic ";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ResponseHandler responseHandler;

    public ResponseEntity<String> post(String camundaReqXML, String requestId, String requestTimeout,
            String schemaVersion, String serviceInstanceId, String action, String orchestrationURI)
            throws ApiException {
        String jsonReq = wrapRequest(camundaReqXML, requestId, serviceInstanceId, requestTimeout, schemaVersion,
                orchestrationURI);
        logger.info("Camunda Request Content: {}", jsonReq);

        return post(jsonReq, orchestrationURI);
    }

    public ResponseEntity<String> post(RequestClientParameter parameterObject, String orchestrationURI)
            throws ApiException {
        String jsonReq = wrapVIDRequest(parameterObject.getRequestId(), parameterObject.isBaseVfModule(),
                parameterObject.getRecipeTimeout(), parameterObject.getRequestAction(),
                parameterObject.getServiceInstanceId(), parameterObject.getPnfCorrelationId(),
                parameterObject.getVnfId(), parameterObject.getVfModuleId(), parameterObject.getVolumeGroupId(),
                parameterObject.getNetworkId(), parameterObject.getConfigurationId(), parameterObject.getServiceType(),
                parameterObject.getVnfType(), parameterObject.getVfModuleType(), parameterObject.getNetworkType(),
                parameterObject.getRequestDetails(), parameterObject.getApiVersion(), parameterObject.isaLaCarte(),
                parameterObject.getRequestUri(), parameterObject.getRecipeParamXsd(),
                parameterObject.getInstanceGroupId(), parameterObject.isGenerateIdsOnly(),
                parameterObject.getOperationType());

        return post(jsonReq, orchestrationURI);
    }

    public ResponseEntity<String> get(String url) throws ApiException {
        url = env.getRequiredProperty(CommonConstants.CAMUNDA_URL) + url;
        HttpEntity<?> requestEntity = new HttpEntity<>(setHeaders());
        try {
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (HttpStatusCodeException e) {
            logger.error("Error returned from sending GET request to BPMN", e);
            throw createBPMNFailureException(e);
        } catch (ResourceAccessException e) {
            logger.error("Error sending GET to BPMN", e);
            ClientConnectionException clientException = new ClientConnectionException.Builder(url,
                    HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).build();
            throw clientException;
        }
    }

    public ResponseEntity<String> post(String jsonReq, String url) throws ApiException {
        url = env.getRequiredProperty(CommonConstants.CAMUNDA_URL) + url;
        HttpEntity<String> request = new HttpEntity<String>(jsonReq, setHeaders());
        try {
            return restTemplate.postForEntity(url, request, String.class);
        } catch (HttpStatusCodeException e) {
            logger.error("Error returned after sending POST request to BPMN", e);
            throw createBPMNFailureException(e);
        } catch (ResourceAccessException e) {
            logger.error("Error sending POST to BPMN", e);
            ClientConnectionException clientException = new ClientConnectionException.Builder(url,
                    HttpStatus.SC_BAD_GATEWAY, ErrorNumbers.SVC_NO_SERVER_RESOURCES).build();
            throw clientException;
        }
    }

    protected String wrapRequest(String reqXML, String requestId, String serviceInstanceId, String requestTimeout,
            String schemaVersion, String url) {
        String jsonReq = null;
        try {
            CamundaRequest camundaRequest = new CamundaRequest();
            CamundaInput camundaInput = new CamundaInput();
            CamundaInput host = new CamundaInput();
            CamundaInput schema = new CamundaInput();
            CamundaInput reqid = new CamundaInput();
            CamundaInput svcid = new CamundaInput();
            CamundaInput timeout = new CamundaInput();
            camundaInput.setValue(StringUtils.defaultString(reqXML));
            host.setValue(CommonConstants.CAMUNDA_URL);
            schema.setValue(StringUtils.defaultString(schemaVersion));
            reqid.setValue(requestId);
            svcid.setValue(serviceInstanceId);
            timeout.setValue(StringUtils.defaultString(requestTimeout));
            camundaRequest.setServiceInput(camundaInput);
            camundaRequest.setHost(host);
            camundaRequest.setReqid(reqid);
            camundaRequest.setSvcid(svcid);
            camundaRequest.setSchema(schema);
            camundaRequest.setTimeout(timeout);
            ObjectMapper mapper = new ObjectMapper();

            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

            jsonReq = mapper.writeValueAsString(camundaRequest);
            logger.trace("request body is {}", jsonReq);
        } catch (Exception e) {
            logger.error("Error in APIH Wrap request", e);
        }
        return jsonReq;
    }


    protected String wrapVIDRequest(String requestId, boolean isBaseVfModule, int recipeTimeout, String requestAction,
            String serviceInstanceId, String pnfCorrelationId, String vnfId, String vfModuleId, String volumeGroupId,
            String networkId, String configurationId, String serviceType, String vnfType, String vfModuleType,
            String networkType, String requestDetails, String apiVersion, boolean aLaCarte, String requestUri,
            String paramXsd, String instanceGroupId, boolean generateIdsOnly, String operationType) {
        String jsonReq = null;

        try {
            CamundaVIDRequest camundaRequest = new CamundaVIDRequest();
            CamundaInput serviceInput = new CamundaInput();
            CamundaInput host = new CamundaInput();
            CamundaInput requestIdInput = new CamundaInput();
            CamundaBooleanInput isBaseVfModuleInput = new CamundaBooleanInput();
            CamundaIntegerInput recipeTimeoutInput = new CamundaIntegerInput();
            CamundaInput requestActionInput = new CamundaInput();
            CamundaInput serviceInstanceIdInput = new CamundaInput();
            CamundaInput operationTypeInput = new CamundaInput();
            CamundaInput pnfCorrelationIdInput = new CamundaInput();
            CamundaInput vnfIdInput = new CamundaInput();
            CamundaInput vfModuleIdInput = new CamundaInput();
            CamundaInput volumeGroupIdInput = new CamundaInput();
            CamundaInput networkIdInput = new CamundaInput();
            CamundaInput configurationIdInput = new CamundaInput();
            CamundaInput serviceTypeInput = new CamundaInput();
            CamundaInput vnfTypeInput = new CamundaInput();
            CamundaInput vfModuleTypeInput = new CamundaInput();
            CamundaInput networkTypeInput = new CamundaInput();
            CamundaBooleanInput aLaCarteInput = new CamundaBooleanInput();
            CamundaInput apiVersionInput = new CamundaInput();
            CamundaInput requestUriInput = new CamundaInput();
            CamundaInput recipeParamsInput = new CamundaInput();
            CamundaInput instanceGroupIdInput = new CamundaInput();
            CamundaBooleanInput generateIds = new CamundaBooleanInput();

            operationTypeInput.setValue(StringUtils.defaultString(operationType));
            requestIdInput.setValue(StringUtils.defaultString(requestId));
            isBaseVfModuleInput.setValue(isBaseVfModule);
            recipeTimeoutInput.setValue(recipeTimeout);
            requestActionInput.setValue(StringUtils.defaultString(requestAction));
            serviceInstanceIdInput.setValue(StringUtils.defaultString(serviceInstanceId));
            pnfCorrelationIdInput.setValue(StringUtils.defaultString(pnfCorrelationId));
            vnfIdInput.setValue(StringUtils.defaultString(vnfId));
            vfModuleIdInput.setValue(StringUtils.defaultString(vfModuleId));
            volumeGroupIdInput.setValue(StringUtils.defaultString(volumeGroupId));
            networkIdInput.setValue(StringUtils.defaultString(networkId));
            configurationIdInput.setValue(StringUtils.defaultString(configurationId));
            serviceTypeInput.setValue(StringUtils.defaultString(serviceType));
            vnfTypeInput.setValue(StringUtils.defaultString(vnfType));
            vfModuleTypeInput.setValue(StringUtils.defaultString(vfModuleType));
            networkTypeInput.setValue(StringUtils.defaultString(networkType));
            aLaCarteInput.setValue(aLaCarte);
            apiVersionInput.setValue(StringUtils.defaultString(apiVersion));
            requestUriInput.setValue(StringUtils.defaultString(requestUri));
            recipeParamsInput.setValue(paramXsd);
            instanceGroupIdInput.setValue(StringUtils.defaultString(instanceGroupId));
            generateIds.setValue(generateIdsOnly);

            serviceInput.setValue(requestDetails);
            camundaRequest.setServiceInput(serviceInput);
            camundaRequest.setHost(host);
            camundaRequest.setRequestId(requestIdInput);
            camundaRequest.setMsoRequestId(requestIdInput);
            camundaRequest.setIsBaseVfModule(isBaseVfModuleInput);
            camundaRequest.setRecipeTimeout(recipeTimeoutInput);
            camundaRequest.setRequestAction(requestActionInput);
            camundaRequest.setServiceInstanceId(serviceInstanceIdInput);
            camundaRequest.setOperationType(operationTypeInput);
            camundaRequest.setPnfCorrelationId(pnfCorrelationIdInput);
            camundaRequest.setVnfId(vnfIdInput);
            camundaRequest.setVfModuleId(vfModuleIdInput);
            camundaRequest.setVolumeGroupId(volumeGroupIdInput);
            camundaRequest.setNetworkId(networkIdInput);
            camundaRequest.setConfigurationId(configurationIdInput);
            camundaRequest.setServiceType(serviceTypeInput);
            camundaRequest.setVnfType(vnfTypeInput);
            camundaRequest.setVfModuleType(vfModuleTypeInput);
            camundaRequest.setNetworkType(networkTypeInput);
            camundaRequest.setaLaCarte(aLaCarteInput);
            camundaRequest.setApiVersion(apiVersionInput);
            camundaRequest.setRequestUri(requestUriInput);
            camundaRequest.setRecipeParams(recipeParamsInput);
            camundaRequest.setInstanceGroupId(instanceGroupIdInput);
            camundaRequest.setGenerateIds(generateIds);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

            jsonReq = mapper.writeValueAsString(camundaRequest);
            logger.trace("request body is {}", jsonReq);
        } catch (Exception e) {
            logger.error("Error in wrapVIDRequest", e);
        }
        return jsonReq;
    }

    protected HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<org.springframework.http.MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, addAuthorizationHeader(env.getRequiredProperty("mso.camundaAuth"),
                env.getRequiredProperty("mso.msoKey")));
        return headers;
    }

    protected String addAuthorizationHeader(String auth, String msoKey) {
        String basicAuth = null;
        try {
            String userCredentials = CryptoUtils.decrypt(auth, msoKey);
            if (userCredentials != null) {
                basicAuth = BASIC + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
            }
        } catch (GeneralSecurityException e) {
            logger.error("Security exception", e);
        }
        return basicAuth;
    }

    protected BPMNFailureException createBPMNFailureException(HttpStatusCodeException e) {
        ObjectMapper mapper = new ObjectMapper();
        String responseText = null;
        String message = null;
        try {
            CamundaResponse response = mapper.readValue(e.getResponseBodyAsString(), CamundaResponse.class);
            responseText = response.getResponse();
        } catch (IOException ex) {
            responseText = e.getResponseBodyAsString();
        }
        message = String.valueOf(e.getStatusCode());
        if (responseText != null && !responseText.isEmpty()) {
            message = message + " " + responseText;
        }
        BPMNFailureException bpmnException = new BPMNFailureException.Builder(message,
                responseHandler.setStatus(e.getStatusCode().value()), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR,
                (org.springframework.http.HttpStatus) e.getStatusCode()).build();
        return bpmnException;
    }

}
