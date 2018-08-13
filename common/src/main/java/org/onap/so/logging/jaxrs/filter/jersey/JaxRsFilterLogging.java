/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.jaxrs.filter.jersey;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import org.onap.so.logger.MsoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Priority(1)
@Provider
@Component
public class JaxRsFilterLogging implements ContainerRequestFilter,ContainerResponseFilter {

    protected static Logger logger = LoggerFactory.getLogger(JaxRsFilterLogging.class); 

    @Context
    private HttpServletRequest httpServletRequest;

    @Context 
    private Providers providers;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void filter(ContainerRequestContext containerRequest) {		

        try {
            String clientID = null;
            //check headers for request id
            MultivaluedMap<String, String> headers = containerRequest.getHeaders();
            String requestId = findRequestId(headers);
            containerRequest.setProperty("requestId", requestId);
            if(headers.containsKey(MsoLogger.CLIENT_ID)){
                clientID = headers.getFirst(MsoLogger.CLIENT_ID);
            }else{
                clientID = "UNKNOWN";
                headers.add(MsoLogger.CLIENT_ID, clientID);
            }
            
            String remoteIpAddress = "";
            if (httpServletRequest != null) {			
                remoteIpAddress = httpServletRequest.getRemoteAddr();
            } 
            Instant instant = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX" )
                    .withLocale( Locale.US )
                    .withZone( ZoneId.systemDefault() );

            String partnerName =  headers.getFirst(MsoLogger.HEADER_FROM_APP_ID );
            if(partnerName == null || partnerName.isEmpty())
                partnerName="UNKNOWN";	

            MDC.put(MsoLogger.REQUEST_ID,requestId);
            MDC.put(MsoLogger.INVOCATION_ID,requestId);
            MDC.put(MsoLogger.FROM_APP_ID,partnerName);	 
            MDC.put(MsoLogger.SERVICE_NAME, containerRequest.getUriInfo().getPath());
            MDC.put(MsoLogger.INVOCATION_ID, findInvocationId(headers));
            MDC.put(MsoLogger.STATUSCODE, MsoLogger.INPROGRESS);
            MDC.put(MsoLogger.BEGINTIME, formatter.format(instant));
            MDC.put(MsoLogger.PARTNERNAME,partnerName);			
            MDC.put(MsoLogger.REMOTE_HOST, String.valueOf(remoteIpAddress));
            MDC.put(MsoLogger.STARTTIME, String.valueOf(System.currentTimeMillis()));
            logger.debug(MsoLogger.ENTRY, "Entering.");
        } catch (Exception e) {
            logger.warn("Error in incoming JAX-RS Inteceptor", e);
        }
    }


    private String findRequestId(MultivaluedMap<String, String> headers) {
        String requestId = (String) headers.getFirst(MsoLogger.HEADER_REQUEST_ID );
        if(requestId == null || requestId.isEmpty()){
            if(headers.containsKey(MsoLogger.ONAP_REQUEST_ID)){
                requestId = headers.getFirst(MsoLogger.ONAP_REQUEST_ID);
            }else if(headers.containsKey(MsoLogger.ECOMP_REQUEST_ID)){
                requestId = headers.getFirst(MsoLogger.ECOMP_REQUEST_ID);
            }else{
                requestId = UUID.randomUUID().toString();
            }
        }
        return requestId;
    }
    
    private String findInvocationId(MultivaluedMap<String, String> headers) {
        String invocationId = (String) headers.getFirst(MsoLogger.INVOCATION_ID_HEADER );
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        return invocationId;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        try {
            Instant instant = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX" )
                    .withLocale( Locale.US )
                    .withZone( ZoneId.systemDefault() );
            String startTime= MDC.get(MsoLogger.STARTTIME);
            long elapsedTime;
            try {
                elapsedTime = System.currentTimeMillis() - Long.parseLong(startTime);
            }catch(NumberFormatException e){
                elapsedTime = 0;
            }
            String statusCode;
            if(Response.Status.Family.familyOf(responseContext.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){		
                statusCode=MsoLogger.COMPLETE;
            }else{							
                statusCode= MsoLogger.StatusCode.ERROR.toString();				
            }			

            MDC.put(MsoLogger.RESPONSEDESC,payloadMessage(responseContext));
            MDC.put(MsoLogger.STATUSCODE, statusCode);
            MDC.put(MsoLogger.RESPONSECODE,String.valueOf(responseContext.getStatus()));			
            MDC.put(MsoLogger.TIMER, String.valueOf(elapsedTime));
            MDC.put(MsoLogger.ENDTIME,formatter.format(instant));			
            logger.debug(MsoLogger.EXIT, "Exiting.");
        } catch ( Exception e) {
            logger.warn("Error in outgoing JAX-RS Inteceptor", e);
        } 
    } 

    private String payloadMessage(ContainerResponseContext responseContext) throws IOException {
        String message = new String();
        if (responseContext.hasEntity()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();           
            Class<?> entityClass = responseContext.getEntityClass();
            Type entityType = responseContext.getEntityType();
            Annotation[] entityAnnotations = responseContext.getEntityAnnotations();
            MediaType mediaType = responseContext.getMediaType();
            @SuppressWarnings("unchecked")
            MessageBodyWriter<Object> bodyWriter = (MessageBodyWriter<Object>) providers.getMessageBodyWriter(entityClass, 
                    entityType, 
                    entityAnnotations, 
                    mediaType);
            bodyWriter.writeTo(responseContext.getEntity(), 
                    entityClass, 
                    entityType, 
                    entityAnnotations, 
                    mediaType, 
                    responseContext.getHeaders(), 
                    baos); 
            message = message.concat(new String(baos.toByteArray()));
        }
        return message;
    }
}
