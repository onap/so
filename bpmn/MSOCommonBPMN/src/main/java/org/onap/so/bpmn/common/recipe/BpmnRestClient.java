/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.bpmn.common.recipe;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.xml.bind.DatatypeConverter;
import org.onap.so.logger.LoggingAnchor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Support to call resource recipes from the BPMN workflow. Such as call resource recipe in service workflow. <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Beijing Release 2018-02-27
 */
@Component
public class BpmnRestClient {

    private static Logger logger = LoggerFactory.getLogger(BpmnRestClient.class);

    public static final String DEFAULT_BPEL_AUTH = "admin:admin";

    public static final String ENCRYPTION_KEY_PROP = "org.onap.so.adapters.network.encryptionKey";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String CAMUNDA_AUTH = "mso.camundaAuth";

    @Autowired
    private UrnPropertiesReader urnPropertiesReader;

    private static boolean noProperties = true;

    public final synchronized boolean getNoPropertiesState() {
        return noProperties;
    }

    /**
     * post the recipe Uri <br>
     * 
     * @param recipeUri The request recipe uri
     * @param requestId the request id
     * @param recipeTimeout The recipe time out
     * @param requestAction The request action
     * @param serviceInstanceId The service instance id
     * @param serviceType The service Type
     * @param requestDetails The request Details, these information is from runtime
     * @param recipeParamXsd The recipe params, its from recipe design
     * @return The response of the recipe.
     * @throws ClientProtocolException
     * @throws IOException
     * @since ONAP Beijing Release
     */
    public HttpResponse post(String recipeUri, String requestId, int recipeTimeout, String requestAction,
            String serviceInstanceId, String serviceType, String requestDetails, String recipeParamXsd)
            throws IOException {

        HttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost(recipeUri);
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(recipeTimeout)
                .setConnectTimeout(recipeTimeout).setConnectionRequestTimeout(recipeTimeout).build();
        post.setConfig(requestConfig);
        logger.debug("call the bpmn,  url: {}", recipeUri);
        String jsonReq = wrapResourceRequest(requestId, recipeTimeout, requestAction, serviceInstanceId, serviceType,
                requestDetails, recipeParamXsd);

        StringEntity input = new StringEntity(jsonReq);
        input.setContentType(CONTENT_TYPE_JSON);
        String encryptedCredentials;
        encryptedCredentials = urnPropertiesReader.getVariable(CAMUNDA_AUTH);
        if (encryptedCredentials != null) {
            String userCredentials =
                    getEncryptedPropValue(encryptedCredentials, DEFAULT_BPEL_AUTH, ENCRYPTION_KEY_PROP);
            if (userCredentials != null) {
                post.addHeader("Authorization",
                        "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes()));
            }
        }

        post.setEntity(input);
        return client.execute(post);
    }

    /**
     * prepare the resource recipe bpmn request. <br>
     * 
     * @param requestId
     * @param recipeTimeout
     * @param requestAction
     * @param serviceInstanceId
     * @param serviceType
     * @param requestDetails
     * @param recipeParams
     * @return
     * @since ONAP Beijing Release
     */
    private String wrapResourceRequest(String requestId, int recipeTimeout, String requestAction,
            String serviceInstanceId, String serviceType, String requestDetails, String recipeParams) {
        String jsonReq = null;
        if (requestId == null) {
            requestId = "";
        }
        if (requestAction == null) {
            requestAction = "";
        }
        if (serviceInstanceId == null) {
            serviceInstanceId = "";
        }

        if (requestDetails == null) {
            requestDetails = "";
        }

        try {
            ResourceRecipeRequest recipeRequest = new ResourceRecipeRequest();
            BpmnParam resourceInput = new BpmnParam();
            BpmnParam host = new BpmnParam();
            BpmnParam requestIdInput = new BpmnParam();
            BpmnParam requestActionInput = new BpmnParam();
            BpmnParam serviceInstanceIdInput = new BpmnParam();
            BpmnParam serviceTypeInput = new BpmnParam();
            BpmnParam recipeParamsInput = new BpmnParam();
            BpmnIntegerParam recipeTimeoutInput = new BpmnIntegerParam();
            recipeTimeoutInput.setValue(recipeTimeout);

            requestIdInput.setValue(requestId);
            requestActionInput.setValue(requestAction);
            serviceInstanceIdInput.setValue(serviceInstanceId);
            serviceTypeInput.setValue(serviceType);
            recipeParamsInput.setValue(recipeParams);
            resourceInput.setValue(requestDetails);
            recipeRequest.setHost(host);
            recipeRequest.setRequestId(requestIdInput);
            recipeRequest.setRequestAction(requestActionInput);
            recipeRequest.setServiceInstanceId(serviceInstanceIdInput);
            recipeRequest.setServiceType(serviceTypeInput);
            recipeRequest.setRecipeParams(recipeParamsInput);
            recipeRequest.setResourceInput(resourceInput);
            recipeRequest.setRecipeTimeout(recipeTimeoutInput);
            jsonReq = recipeRequest.toString();
            logger.trace("request body is {}", jsonReq);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.APIH_WARP_REQUEST.toString(), "Camunda", "wrapVIDRequest",
                    ErrorCode.BusinessProcessError.getValue(), "Error in APIH Warp request", e);
        }
        return jsonReq;
    }

    /**
     * <br>
     * 
     * @param prop
     * @param defaultValue
     * @param encryptionKey
     * @return
     * @since ONAP Beijing Release
     */
    protected String getEncryptedPropValue(String prop, String defaultValue, String encryptionKey) {
        try {
            return CryptoUtils.decrypt(prop, urnPropertiesReader.getVariable(encryptionKey));
        } catch (GeneralSecurityException e) {
            logger.debug("Security exception", e);
        }
        return defaultValue;
    }

}
