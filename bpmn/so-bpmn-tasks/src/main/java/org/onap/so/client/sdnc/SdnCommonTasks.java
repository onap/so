/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SdnCommonTasks {

    private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCClient.class);
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_MESSAGE = "response-message";
    private static final String NO_RESPONSE_FROM_SDNC = "Error did not receive a response from SDNC.";
    private static final String BAD_RESPONSE_FROM_SDNC = "Error received a bad response from SDNC.";
    private static final String SDNC_CODE_NOT_0_OR_IN_200_299 = "Error from SDNC: %s";
    private static final String COULD_NOT_CONVERT_SDNC_POJO_TO_JSON = "ERROR: Could not convert SDNC pojo to json string.";

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
            msoLogger.error(MessageEnum.JAXB_EXCEPTION, COULD_NOT_CONVERT_SDNC_POJO_TO_JSON, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.DataError, e.getMessage());
            throw new MapperException(COULD_NOT_CONVERT_SDNC_POJO_TO_JSON);
        }
        jsonRequest = "{\"input\":" + jsonRequest + "}";
        msoLogger.info(jsonRequest);
        return jsonRequest;
    }

    /***
     * 
     * @param auth
     * @return
     */
    public HttpHeaders getHttpHeaders(String auth) {
        HttpHeaders httpHeader = new HttpHeaders();
        httpHeader.set("Authorization", auth);
        httpHeader.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
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
	public String validateSDNResponse(LinkedHashMap<?, ?> output) throws BadResponseException {
		if (CollectionUtils.isEmpty(output)) {
			msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, NO_RESPONSE_FROM_SDNC, "BPMN",
					MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NO_RESPONSE_FROM_SDNC);
			throw new BadResponseException(NO_RESPONSE_FROM_SDNC);
		}
        LinkedHashMap<?, ?> embeddedResponse =(LinkedHashMap<?, ?>) output.get("output");
        String responseCode = "";
        String responseMessage = "";
        if (embeddedResponse != null) {
        	responseCode = (String) embeddedResponse.get(RESPONSE_CODE);
            responseMessage = (String) embeddedResponse.get(RESPONSE_MESSAGE);
        }
        
		msoLogger.info("ResponseCode: " + responseCode + " ResponseMessage: " + responseMessage);
		int code = StringUtils.isNotEmpty(responseCode) ? Integer.parseInt(responseCode) : 0;
		if (isHttpCodeSuccess(code)) {
			msoLogger.info("Successful Response from SDNC");
			return responseMessage;
		} else {
			String errorMessage = String.format(SDNC_CODE_NOT_0_OR_IN_200_299, responseMessage);
			msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, errorMessage, "BPMN", MsoLogger.getServiceName(),
					MsoLogger.ErrorCode.DataError, errorMessage);
			throw new BadResponseException(errorMessage);
		}
	}
    
    /***
     * 
     * @param output
     * @return
     * @throws BadResponseException
     */
    public String validateSDNGetResponse(LinkedHashMap<?, ?> output) throws BadResponseException {
        if (CollectionUtils.isEmpty(output)) {
            msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, NO_RESPONSE_FROM_SDNC, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, NO_RESPONSE_FROM_SDNC);
            throw new BadResponseException(NO_RESPONSE_FROM_SDNC);
        }
        ObjectMapper objMapper = new ObjectMapper();
        msoLogger.debug("Using object mapper");
        String stringOutput = "";
        try {
        	stringOutput = objMapper.writeValueAsString(output);
        }
        catch (Exception e) {
        	msoLogger.error(MessageEnum.RA_RESPONSE_FROM_SDNC, BAD_RESPONSE_FROM_SDNC, "BPMN", MsoLogger.getServiceName(), MsoLogger.ErrorCode.UnknownError, BAD_RESPONSE_FROM_SDNC);
            throw new BadResponseException(BAD_RESPONSE_FROM_SDNC);
        }
        msoLogger.debug("Received from GET request: " + stringOutput);
        return stringOutput;
    }

    private boolean isHttpCodeSuccess(int code) {
        return code >= HttpStatus.SC_OK && code < HttpStatus.SC_MULTIPLE_CHOICES || code == 0;
    }
}
