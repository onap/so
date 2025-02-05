/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sdnc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.onap.so.logger.LoggingAnchor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SdnCommonTasks {

    private static final Logger logger = LoggerFactory.getLogger(SDNCClient.class);
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_MESSAGE = "response-message";
    private static final String NO_RESPONSE_FROM_SDNC = "Error did not receive a response from SDNC.";
    private static final String BAD_RESPONSE_FROM_SDNC = "Error received a bad response from SDNC.";
    private static final String SDNC_CODE_NOT_0_OR_IN_200_299 = "Error from SDNC: %s";
    private static final String COULD_NOT_CONVERT_SDNC_POJO_TO_JSON =
            "ERROR: Could not convert SDNC pojo to json string.";
    private static final String BRACKETS = LoggingAnchor.FIVE;

    /***
     * 
     * @param request
     * @return
     * @throws MapperException
     */
    public String buildJsonRequest(Object request) throws MapperException {
        String jsonRequest;
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            jsonRequest = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            logger.error(BRACKETS, MessageEnum.JAXB_EXCEPTION.toString(), COULD_NOT_CONVERT_SDNC_POJO_TO_JSON, "BPMN",
                    ErrorCode.DataError.getValue(), e.getMessage());
            throw new MapperException(COULD_NOT_CONVERT_SDNC_POJO_TO_JSON);
        }
        jsonRequest = "{\"input\":" + jsonRequest + "}";
        logger.info(jsonRequest);
        return jsonRequest;
    }

    /***
     * 
     * @param auth
     * @return
     */
    public HttpHeaders getHttpHeaders(String auth, boolean includeContentType) {
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.set("Authorization", auth);
        if (includeContentType) {
            httpHeader.setContentType(MediaType.APPLICATION_JSON);
        }
        List<MediaType> acceptMediaTypes = new ArrayList<>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeader.setAccept(acceptMediaTypes);
        return httpHeader;
    }

    /***
     * 
     * @param output
     * @return
     * @throws BadResponseException
     */
    public String validateSDNResponse(LinkedHashMap<String, Object> output) throws BadResponseException {
        if (CollectionUtils.isEmpty(output)) {
            logger.error(BRACKETS, MessageEnum.RA_RESPONSE_FROM_SDNC.toString(), NO_RESPONSE_FROM_SDNC, "BPMN",
                    ErrorCode.UnknownError.getValue(), NO_RESPONSE_FROM_SDNC);
            throw new BadResponseException(NO_RESPONSE_FROM_SDNC);
        }
        LinkedHashMap<String, Object> embeddedResponse = (LinkedHashMap<String, Object>) output.get("output");
        String responseCode = "";
        String responseMessage = "";
        if (embeddedResponse != null) {
            responseCode = (String) embeddedResponse.get(RESPONSE_CODE);
            responseMessage = (String) embeddedResponse.get(RESPONSE_MESSAGE);
        }
        ObjectMapper objMapper = new ObjectMapper();
        String jsonResponse;
        try {
            jsonResponse = objMapper.writeValueAsString(output);
            logger.debug(jsonResponse);
        } catch (JsonProcessingException e) {
            logger.warn("Could not convert SDNC Response to String", e);
            jsonResponse = "";
        }
        logger.info("ResponseCode: {} ResponseMessage: {}", responseCode, responseMessage);
        int code = StringUtils.isNotEmpty(responseCode) ? Integer.parseInt(responseCode) : 0;
        if (isHttpCodeSuccess(code)) {
            logger.info("Successful Response from SDNC");
            return jsonResponse;
        } else {
            String errorMessage = String.format(SDNC_CODE_NOT_0_OR_IN_200_299, responseMessage);
            logger.error(BRACKETS, MessageEnum.RA_RESPONSE_FROM_SDNC.toString(), errorMessage, "BPMN",
                    ErrorCode.DataError.getValue(), errorMessage);
            throw new BadResponseException(errorMessage);
        }
    }

    /***
     * 
     * @param output
     * @return
     * @throws BadResponseException
     */
    public String validateSDNGetResponse(LinkedHashMap<String, Object> output) throws BadResponseException {
        if (CollectionUtils.isEmpty(output)) {
            logger.error(BRACKETS, MessageEnum.RA_RESPONSE_FROM_SDNC.toString(), NO_RESPONSE_FROM_SDNC, "BPMN",
                    ErrorCode.UnknownError.getValue(), NO_RESPONSE_FROM_SDNC);
            throw new BadResponseException(NO_RESPONSE_FROM_SDNC);
        }
        ObjectMapper objMapper = new ObjectMapper();
        logger.debug("Using object mapper");
        String stringOutput = "";
        try {
            stringOutput = objMapper.writeValueAsString(output);
        } catch (Exception e) {
            logger.error(BRACKETS, MessageEnum.RA_RESPONSE_FROM_SDNC.toString(), BAD_RESPONSE_FROM_SDNC, "BPMN",
                    ErrorCode.UnknownError.getValue(), BAD_RESPONSE_FROM_SDNC);
            throw new BadResponseException(BAD_RESPONSE_FROM_SDNC);
        }
        logger.debug("Received from GET request: {}", stringOutput);
        return stringOutput;
    }


    private boolean isHttpCodeSuccess(int code) {
        return code >= HttpStatus.SC_OK && code < HttpStatus.SC_MULTIPLE_CHOICES || code == 0;
    }
}
