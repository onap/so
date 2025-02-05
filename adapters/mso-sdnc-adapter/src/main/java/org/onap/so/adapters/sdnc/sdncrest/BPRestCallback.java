/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.sdnc.sdncrest;

import java.net.URI;
import javax.xml.bind.DatatypeConverter;
import org.onap.so.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.utils.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Sends asynchronous messages to the BPMN WorkflowMessage service.
 */
@Component
public class BPRestCallback {
    private static final Logger logger = LoggerFactory.getLogger(BPRestCallback.class);

    private static final String CAMUNDA = "Camunda";
    private static final String MSO_INTERNAL_ERROR = "MsoInternalError";
    @Autowired
    private Environment env;

    /**
     * Sends a message to the BPMN workflow message service. The URL path is constructed using the specified message
     * type and correlator.
     * 
     * @param workflowMessageUrl the base BPMN WorkflowMessage URL
     * @param messageType the message type
     * @param correlator the message correlator
     * @param message the JSON content
     * @return true if the message was consumed successfully by the endpoint
     */
    public boolean send(String workflowMessageUrl, String messageType, String correlator, String message) {
        logger.debug(getClass().getSimpleName() + ".send(" + "workflowMessageUrl=" + workflowMessageUrl
                + " messageType=" + messageType + " correlator=" + correlator + " message=" + message + ")");

        while (workflowMessageUrl.endsWith("/")) {
            workflowMessageUrl = workflowMessageUrl.substring(0, workflowMessageUrl.length() - 1);
        }

        String endpoint = workflowMessageUrl + "/" + SDNCAdapterUtils.encodeURLPathSegment(messageType) + "/"
                + SDNCAdapterUtils.encodeURLPathSegment(correlator);

        return send(endpoint, message);
    }

    /**
     * Sends a message to the BPMN workflow message service. The specified URL must have the message type and correlator
     * already embedded in it.
     * 
     * @param url the endpoint URL
     * @param message the JSON content
     * @return true if the message was consumed successfully by the endpoint
     */
    public boolean send(String url, String message) {
        logger.debug(getClass().getSimpleName() + ".send(" + "url=" + url + " message=" + message + ")");

        logger.info(LoggingAnchor.THREE, MessageEnum.RA_CALLBACK_BPEL.toString(),
                message == null ? "[no content]" : message, CAMUNDA);
        try {
            int timeout = 60 * 1000;
            RestTemplate restTemplate = setRestTemplate(timeout);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            boolean error = setAuthorizationHeader(headers);

            HttpEntity<String> requestEntity = new HttpEntity<>(message, headers);
            if (!error) {
                postRequest(restTemplate, url, requestEntity);
            }
            logger.info(LoggingAnchor.TWO, MessageEnum.RA_CALLBACK_BPEL_COMPLETE.toString(), CAMUNDA);
            return true;
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_CALLBACK_BPEL_EXC.toString(), CAMUNDA,
                    ErrorCode.BusinessProcessError.getValue(), "Error sending callback request", e);
            return false;
        }
    }

    protected boolean setAuthorizationHeader(HttpHeaders headers) {
        boolean error = false;
        try {
            String userCredentials = CryptoUtils.decrypt(env.getProperty(Constants.BPEL_AUTH_PROP),
                    env.getProperty(Constants.ENCRYPTION_KEY_PROP));
            String authorization = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
            headers.set("Authorization", authorization);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.RA_SET_CALLBACK_AUTH_EXC.toString(), CAMUNDA,
                    ErrorCode.BusinessProcessError.getValue(), "Unable to set authorization in callback request", e);
            error = true;
        }
        return error;
    }

    private void postRequest(RestTemplate restTemplate, String url, HttpEntity<String> requestEntity) {
        ResponseEntity<String> response = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            URI uri = builder.build(true).toUri();
            response = restTemplate.postForEntity(uri, requestEntity, String.class);
        } catch (HttpStatusCodeException e) {
            logResponseError(e.getStatusCode());
        }
        if (response != null && response.getStatusCode().is3xxRedirection()) {
            logResponseError(response.getStatusCode());
        }
    }

    private void logResponseError(HttpStatus statusCode) {
        String msg = "Received error response to callback request: " + statusCode;
        logger.error(LoggingAnchor.FOUR, MessageEnum.RA_CALLBACK_BPEL_EXC.toString(), CAMUNDA,
                ErrorCode.BusinessProcessError.getValue(), msg);
    }

    protected RestTemplate setRestTemplate(int timeout) {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(timeout);
        factory.setReadTimeout(timeout);
        factory.setConnectTimeout(timeout);
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }
}
