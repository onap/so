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

package org.onap.so.apihandlerinfra.exceptions;

import java.util.List;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    private static Logger logger = LoggerFactory.getLogger(ApiExceptionMapper.class);


    private final JAXBContext context;
    private final Marshaller marshaller;

    @Context
    private HttpHeaders headers;

    public ApiExceptionMapper() {
        try {
            context = JAXBContext.newInstance(RequestError.class);
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            logger.debug("could not create JAXB marshaller");
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Response toResponse(ApiException exception) {
        logger.error("Error During API Call", exception);
        return Response.status(exception.getHttpResponseCode()).entity(buildError(exception)).build();
    }

    protected RequestError buildError(ApiException exception) {
        String errorText = exception.getMessage();
        String messageId = exception.getMessageID();
        List<String> variables = exception.getVariables();

        if (errorText.length() > 1999) {
            errorText = errorText.substring(0, 1999);
        }
        return buildServiceErrorResponse(errorText, messageId, variables);

    }

    protected RequestError buildServiceErrorResponse(String errorText, String messageId, List<String> variables) {
        RequestError requestError = new RequestError();
        ServiceException serviceException = new ServiceException();
        serviceException.setMessageId(messageId);
        serviceException.setText(errorText);
        if (variables != null) {
            for (String variable : variables) {
                serviceException.getVariables().add(variable);
            }
        }
        requestError.setServiceException(serviceException);
        return requestError;
    }

    protected void writeErrorLog(Exception e, String errorText, ErrorLoggerInfo errorLogInfo) {
        if (e != null)
            logger.error("Exception occurred", e);
        if (errorLogInfo != null)
            logger.error(errorLogInfo.getLoggerMessageType().toString(), errorLogInfo.getErrorSource(),
                    errorLogInfo.getTargetEntity(), errorLogInfo.getTargetServiceName(), errorLogInfo.getErrorCode(),
                    errorText);

    }

    public ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

}
