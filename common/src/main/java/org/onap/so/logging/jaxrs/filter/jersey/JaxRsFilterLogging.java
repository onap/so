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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Priority(1)
@Provider
@Component
public class JaxRsFilterLogging implements ContainerRequestFilter,ContainerResponseFilter {
	
	private static MsoLogger logger = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA,JaxRsFilterLogging.class);
	
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
			String requestId = (String) headers.getFirst(MsoLogger.HEADER_REQUEST_ID );
			if(requestId == null || requestId.isEmpty()){
				if(headers.containsKey(MsoLogger.TRANSACTION_ID)){
					requestId = headers.getFirst(MsoLogger.TRANSACTION_ID);
				}else if(headers.containsKey(MsoLogger.ECOMP_REQUEST_ID)){
					requestId = headers.getFirst(MsoLogger.ECOMP_REQUEST_ID);
				}else{
					requestId = UUID.randomUUID().toString();
					logger.warnSimple(containerRequest.getUriInfo().getPath(),"Generating RequestId for Request");
				}
			}
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
			
			MDC.put(MsoLogger.FROM_APP_ID,partnerName);	 
			MDC.put(MsoLogger.SERVICE_NAME, containerRequest.getUriInfo().getPath());
			MDC.put(MsoLogger.BEGINTIME, formatter.format(instant));
			MDC.put(MsoLogger.REQUEST_ID,requestId);
			MDC.put(MsoLogger.PARTNERNAME,partnerName);			
			MDC.put(MsoLogger.REMOTE_HOST, String.valueOf(remoteIpAddress));
			MDC.put(MsoLogger.STARTTIME, String.valueOf(System.currentTimeMillis()));
			MDC.put(MsoLogger.CLIENT_ID, clientID);
		} catch (Exception e) {
			logger.warnSimple("Error in incoming JAX-RS Inteceptor", e);
		}
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
			logger.recordAuditEvent();
		} catch ( Exception e) {
			logger.warnSimple("Error in outgoing JAX-RS Inteceptor", e);
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
