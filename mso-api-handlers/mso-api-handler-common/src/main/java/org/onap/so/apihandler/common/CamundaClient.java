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
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.camundabeans.CamundaBooleanInput;
import org.onap.so.apihandler.camundabeans.CamundaInput;
import org.onap.so.apihandler.camundabeans.CamundaIntegerInput;
import org.onap.so.apihandler.camundabeans.CamundaRequest;
import org.onap.so.apihandler.camundabeans.CamundaVIDRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CamundaClient extends RequestClient {
    private static Logger logger = LoggerFactory.getLogger(CamundaClient.class);
    private static final String CAMUNDA_URL_MESAGE = "Camunda url is: ";
    private static final String CAMUNDA_RESPONSE = "Response is: {}";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic ";

    public CamundaClient() {
        super(CommonConstants.CAMUNDA);
    }


    @Override
    public HttpResponse post(String camundaReqXML, String requestId, String requestTimeout, String schemaVersion,
            String serviceInstanceId, String action) throws IOException {
        HttpPost post = new HttpPost(url);
        logger.debug(CAMUNDA_URL_MESAGE + url);
        String jsonReq = wrapRequest(camundaReqXML, requestId, serviceInstanceId, requestTimeout, schemaVersion);

        StringEntity input = new StringEntity(jsonReq);
        input.setContentType(CommonConstants.CONTENT_TYPE_JSON);
        logger.info("Camunda Request Content: {}", jsonReq);


        post.setEntity(input);
        setupHeaders(post);

        HttpResponse response = client.execute(post);
        logger.debug(CAMUNDA_RESPONSE, response);

        return response;
    }


    private void setupHeaders(HttpPost post) {
        post.addHeader(ONAPLogConstants.Headers.REQUEST_ID, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        post.addHeader(ONAPLogConstants.Headers.INVOCATION_ID, UUID.randomUUID().toString());
        addAuthorizationHeader(post);
    }

    @Override
    public HttpResponse post(String jsonReq) throws IOException {
        HttpPost post = new HttpPost(url);
        logger.debug(CAMUNDA_URL_MESAGE + url);

        StringEntity input = new StringEntity(jsonReq);
        input.setContentType(CommonConstants.CONTENT_TYPE_JSON);
        setupHeaders(post);
        addAuthorizationHeader(post);
        post.setEntity(input);
        HttpResponse response = client.execute(post);
        logger.debug(CAMUNDA_RESPONSE, response);

        return response;
    }

    @Override
    public HttpResponse post(RequestClientParameter parameterObject) throws IOException {
        HttpPost post = new HttpPost(url);
        logger.debug(CAMUNDA_URL_MESAGE + url);
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

        StringEntity input = new StringEntity(jsonReq);
        input.setContentType(CommonConstants.CONTENT_TYPE_JSON);
        setupHeaders(post);
        addAuthorizationHeader(post);
        post.setEntity(input);
        HttpResponse response = client.execute(post);
        logger.debug(CAMUNDA_RESPONSE, response);

        return response;
    }

    @Override
    public HttpResponse get() {
        return null;
    }

    protected String wrapRequest(String reqXML, String requestId, String serviceInstanceId, String requestTimeout,
            String schemaVersion) {
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
            host.setValue(parseURL());
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
            logger.error("Error in APIH Warp request", e);
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
            logger.error("Error in APIH Warp request", e);
        }
        return jsonReq;
    }

    private String parseURL() {
        String[] parts = url.split(":");
        String host = "";
        if (parts.length >= 2) {
            host = parts[1];
            if (host.length() > 2) {
                host = host.substring(2);
            }
        }
        return host;
    }

    private void addAuthorizationHeader(HttpPost post) {
        String encryptedCredentials;
        if (props != null) {
            encryptedCredentials = props.getProperty(CommonConstants.CAMUNDA_AUTH);
            if (encryptedCredentials != null) {
                String userCredentials = getEncryptedPropValue(encryptedCredentials, CommonConstants.DEFAULT_BPEL_AUTH,
                        props.getProperty(CommonConstants.ENCRYPTION_KEY_PROP));
                if (userCredentials != null) {
                    post.addHeader(AUTHORIZATION,
                            BASIC + new String(DatatypeConverter.printBase64Binary(userCredentials.getBytes())));
                }
            }
        }
    }
}
