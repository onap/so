/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandler.common;


import java.io.IOException;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.camundabeans.CamundaResponse;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class ResponseHandler {
    private static Logger logger = LoggerFactory.getLogger(ResponseHandler.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public CamundaResponse getCamundaResponse(ResponseEntity<String> camundaResponse) throws ValidateException {
        String responseBody = camundaResponse.getBody();
        CamundaResponse response = null;

        try {
            response = mapper.readValue(responseBody, CamundaResponse.class);
        } catch (IOException | NullPointerException e) {
            logger.error("Cannot parse Camunda Response: ", e);
            throw new ValidateException.Builder(
                    "Cannot parse Camunda ResponseBody. BPMN HTTP status: " + camundaResponse.getStatusCodeValue(),
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, ErrorNumbers.SVC_BAD_PARAMETER).cause(e).build();
        }
        return response;
    }

    public void acceptedResponse(ResponseEntity<String> response) throws BPMNFailureException {
        int status = setStatus(response.getStatusCodeValue());
        if (status != HttpStatus.SC_ACCEPTED) {
            logger.info("Camunda did not return a valid response");
            throw new BPMNFailureException.Builder(String.valueOf(status), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.ERROR_FROM_BPEL).build();
        }
    }

    public void acceptedOrNoContentResponse(ResponseEntity<String> response) throws BPMNFailureException {
        int status = setStatus(response.getStatusCodeValue());
        if (status != HttpStatus.SC_NO_CONTENT && status != HttpStatus.SC_ACCEPTED) {
            logger.info("Camunda did not return a valid response");
            throw new BPMNFailureException.Builder(String.valueOf(status), HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    ErrorNumbers.ERROR_FROM_BPEL).build();
        }
    }

    public int setStatus(int statusCode) {
        int httpStatus;
        switch (statusCode) {
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_OK:
                httpStatus = HttpStatus.SC_ACCEPTED;
                break;
            case HttpStatus.SC_BAD_REQUEST:
                httpStatus = HttpStatus.SC_BAD_REQUEST;
                break;
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
                httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                break;
            case HttpStatus.SC_NOT_FOUND:
                httpStatus = HttpStatus.SC_NOT_IMPLEMENTED;
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                httpStatus = HttpStatus.SC_BAD_GATEWAY;
                break;
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                httpStatus = HttpStatus.SC_SERVICE_UNAVAILABLE;
                break;
            case HttpStatus.SC_NO_CONTENT:
                httpStatus = HttpStatus.SC_NO_CONTENT;
                break;
            default:
                httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                break;
        }
        return httpStatus;
    }
}
