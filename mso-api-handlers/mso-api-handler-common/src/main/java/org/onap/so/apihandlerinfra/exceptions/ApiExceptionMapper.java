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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;

import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        return Response.status(exception.getHttpResponseCode()).entity(buildErrorString(exception)).build();
    }

    protected String buildErrorString(ApiException exception) {
        String errorText = exception.getMessage();
        String messageId = exception.getMessageID();
        List<String> variables = exception.getVariables();
        ErrorLoggerInfo errorLoggerInfo = exception.getErrorLoggerInfo();



        if (errorText.length() > 1999) {
            errorText = errorText.substring(0, 1999);
        }


        
        List<MediaType> typeList = Optional.ofNullable(headers.getAcceptableMediaTypes()).orElse(new ArrayList<>());
        List<String> typeListString = typeList.stream().map(item -> item.toString()).collect(Collectors.toList());
        MediaType type;
        if (typeListString.stream().anyMatch(item -> item.contains(MediaType.APPLICATION_XML))) {
        	type = MediaType.APPLICATION_XML_TYPE;
        } else if (typeListString.stream().anyMatch(item -> typeListString.contains(MediaType.APPLICATION_JSON))) {
        	type = MediaType.APPLICATION_JSON_TYPE;
        } else {
        	type = MediaType.APPLICATION_JSON_TYPE;
        }

        return buildServiceErrorResponse(errorText,messageId,variables, type);

    }

    protected String buildServiceErrorResponse(String errorText, String messageId, List<String> variables, MediaType type){
        RequestError re = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId(messageId);
        se.setText(errorText);
        if (variables != null) {
            for (String variable : variables) {
                se.getVariables().add(variable);
            }
        }
        re.setServiceException(se);
        String requestErrorStr;

        ObjectMapper mapper = createObjectMapper();
    	
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        try {
        	if (MediaType.APPLICATION_JSON_TYPE.equals(type)) {
        		requestErrorStr = mapper.writeValueAsString(re);
            } else {
            	StringWriter sw = new StringWriter();
            	this.getMarshaller().marshal(re, sw);
            	requestErrorStr = sw.toString();
            }
        } catch (JsonProcessingException | JAXBException e) {
            String errorMsg = "Exception in buildServiceErrorResponse writing exceptionType to string " + e.getMessage();
            logger.error("{} {} {} {}", MessageEnum.GENERAL_EXCEPTION.toString(), "BuildServiceErrorResponse",
                ErrorCode.DataError.getValue(), errorMsg, e);
            return errorMsg;
        }

        return requestErrorStr;
    }

    protected void writeErrorLog(Exception e, String errorText, ErrorLoggerInfo errorLogInfo) {
        if( e!= null)
            logger.error("Exception occurred", e);
        if(errorLogInfo != null)
            logger.error(errorLogInfo.getLoggerMessageType().toString(), errorLogInfo.getErrorSource(), errorLogInfo.getTargetEntity(), errorLogInfo.getTargetServiceName(), errorLogInfo.getErrorCode(), errorText);
  
    }

    public ObjectMapper createObjectMapper(){
        return new ObjectMapper();
    }
    
    public Marshaller getMarshaller() {
    	return marshaller;
    }
    
}
