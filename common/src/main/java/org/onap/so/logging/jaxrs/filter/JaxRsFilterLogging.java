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

package org.onap.so.logging.jaxrs.filter;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.LogConstants;
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
    private MDCSetup mdcSetup;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        try {
            MultivaluedMap<String, String> headers = containerRequest.getHeaders();
            setRequestId(headers);
            containerRequest.setProperty("requestId", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
            setInvocationId(headers);
            setServiceName(containerRequest);
            setMDCPartnerName(headers);
            mdcSetup.setServerFQDN();
            mdcSetup.setClientIPAddress(httpServletRequest);
            mdcSetup.setInstanceUUID();
            mdcSetup.setEntryTimeStamp();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
            MDC.put(LogConstants.URI_BASE, containerRequest.getUriInfo().getBaseUri().toString());            
            logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        } catch (Exception e) {
            logger.warn("Error in incoming JAX-RS Inteceptor", e);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        try {
            setResponseStatusCode(responseContext);
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION,payloadMessage(responseContext));      
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE,String.valueOf(responseContext.getStatus()));
            logger.info(ONAPLogConstants.Markers.EXIT, "Exiting.");
            MDC.clear();
        } catch ( Exception e) {
            MDC.clear();
            logger.warn("Error in outgoing JAX-RS Inteceptor", e);
        } 
    }

    private void setResponseStatusCode(ContainerResponseContext responseContext) {
        String statusCode;
        if(Response.Status.Family.familyOf(responseContext.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){		
            statusCode=ONAPLogConstants.ResponseStatus.COMPLETED.toString();
        }else{							
            statusCode= ONAPLogConstants.ResponseStatus.ERROR.toString();				
        }			
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    } 

    private String payloadMessage(ContainerResponseContext responseContext) throws IOException {
        String message = "";
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


    private void setRequestId(MultivaluedMap<String, String> headers){
        String requestId=headers.getFirst(ONAPLogConstants.Headers.REQUEST_ID);
        if(requestId == null || requestId.isEmpty())
            requestId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,requestId);
    }

    private void setInvocationId(MultivaluedMap<String, String> headers){
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, findInvocationId(headers));
    }

    private void setMDCPartnerName(MultivaluedMap<String, String> headers){
        String partnerName=headers.getFirst(ONAPLogConstants.Headers.PARTNER_NAME);
        if(partnerName == null || partnerName.isEmpty())
            partnerName = "";
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME,partnerName);
    }
    
    private String findInvocationId(MultivaluedMap<String, String> headers) {
        String invocationId = headers.getFirst(ONAPLogConstants.Headers.INVOCATION_ID);
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        return invocationId;
    }

    private void setServiceName(ContainerRequestContext containerRequest){
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, containerRequest.getUriInfo().getPath());
    }

    private void clearClientMDCs() {
        MDC.remove(ONAPLogConstants.MDCs.INVOCATION_ID);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION);
        MDC.remove(ONAPLogConstants.MDCs.RESPONSE_CODE);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_ENTITY);
        MDC.remove(ONAPLogConstants.MDCs.PARTNER_NAME);
        MDC.remove(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME);
    }



}
