/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import java.io.IOException;
import java.util.List;
import org.onap.so.logger.LoggingAnchor;
import org.apache.http.HttpStatus;
import org.onap.namingservice.model.NameGenDeleteResponse;
import org.onap.namingservice.model.NameGenResponse;
import org.onap.namingservice.model.NameGenResponseError;
import org.onap.namingservice.model.Respelement;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class NamingClientResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(NamingClientResponseValidator.class);
    private static final String INSTANCE_GROUP_NAME = "instance-group-name";
    private static final String SERVICE_INSTANCE_NAME = "Service-Instance-Name";
    private static final String NO_RESPONSE_FROM_NAMING_SERVICE =
            "Error did not receive a response from Naming Service.";
    private static final String NULL_RESPONSE_FROM_NAMING_SERVICE =
            "Error received a null response from Naming Service.";
    private static final String NAMING_SERVICE_ERROR = "Error from Naming Service: %s";

    public String validateNameGenResponse(ResponseEntity<NameGenResponse> response) throws BadResponseException {
        if (response == null) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    NO_RESPONSE_FROM_NAMING_SERVICE, "BPMN", ErrorCode.UnknownError.getValue(),
                    NO_RESPONSE_FROM_NAMING_SERVICE);
            throw new BadResponseException(NO_RESPONSE_FROM_NAMING_SERVICE);
        }

        int responseCode = response.getStatusCodeValue();
        String generatedName = "";
        NameGenResponse responseBody = response.getBody();
        if (responseBody == null) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    NULL_RESPONSE_FROM_NAMING_SERVICE, "BPMN", ErrorCode.UnknownError.getValue(),
                    NULL_RESPONSE_FROM_NAMING_SERVICE);
            throw new BadResponseException(NULL_RESPONSE_FROM_NAMING_SERVICE);
        }

        if (isHttpCodeSuccess(responseCode)) {
            logger.info("Successful Response from Naming Service");
            List<Respelement> respList = responseBody.getElements();

            if (respList != null) {
                for (int i = 0; i < respList.size(); i++) {
                    Respelement respElement = respList.get(i);
                    if (respElement != null) {
                        String resourceName = respElement.getResourceName();
                        if (INSTANCE_GROUP_NAME.equals(resourceName)) {
                            generatedName = respElement.getResourceValue();
                            break;
                        } else if (SERVICE_INSTANCE_NAME.equals(resourceName)) {
                            generatedName = respElement.getResourceValue();
                            break;
                        }
                    }
                }
            }
            return generatedName;
        } else {
            NameGenResponseError error = responseBody.getError();
            String errorMessageString = NAMING_SERVICE_ERROR;
            if (error != null) {
                errorMessageString = error.getMessage();
            }
            String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(), errorMessage, "BPMN",
                    ErrorCode.DataError.getValue(), errorMessage);
            throw new BadResponseException(errorMessage);
        }
    }

    public String validateNameGenDeleteResponse(ResponseEntity<NameGenDeleteResponse> response)
            throws BadResponseException {
        if (response == null) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    NO_RESPONSE_FROM_NAMING_SERVICE, "BPMN", ErrorCode.UnknownError.getValue(),
                    NO_RESPONSE_FROM_NAMING_SERVICE);
            throw new BadResponseException(NO_RESPONSE_FROM_NAMING_SERVICE);
        }

        int responseCode = response.getStatusCodeValue();
        String responseMessage = "";
        NameGenDeleteResponse responseBody = response.getBody();
        if (responseBody == null) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(),
                    NULL_RESPONSE_FROM_NAMING_SERVICE, "BPMN", ErrorCode.UnknownError.getValue(),
                    NULL_RESPONSE_FROM_NAMING_SERVICE);
            throw new BadResponseException(NULL_RESPONSE_FROM_NAMING_SERVICE);
        }

        if (isHttpCodeSuccess(responseCode)) {
            logger.info("Successful Response from Naming Service");
            return responseMessage;
        } else {
            String errorMessageString = NAMING_SERVICE_ERROR;

            String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
            logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(), errorMessage, "BPMN",
                    ErrorCode.DataError.getValue(), errorMessage);
            throw new BadResponseException(errorMessage);
        }
    }

    private boolean isHttpCodeSuccess(int code) {
        return code >= HttpStatus.SC_OK && code < HttpStatus.SC_MULTIPLE_CHOICES || code == 0;
    }

    protected String formatError(HttpStatusCodeException e) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        NameGenResponse errorResponse = mapper.readValue(e.getResponseBodyAsString(), NameGenResponse.class);
        NameGenResponseError error = errorResponse.getError();

        String errorMessageString = null;
        if (error != null) {
            errorMessageString = error.getMessage();
        }
        String errorMessage = String.format(NAMING_SERVICE_ERROR, errorMessageString);
        logger.error(LoggingAnchor.FIVE, MessageEnum.RA_GENERAL_EXCEPTION.toString(), errorMessage, "BPMN",
                ErrorCode.DataError.getValue(), errorMessage);
        return errorMessage;
    }

}
