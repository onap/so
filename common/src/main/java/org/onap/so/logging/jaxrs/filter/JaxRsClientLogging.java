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


import org.apache.commons.io.IOUtils;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.utils.TargetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
@Priority(0)
public class JaxRsClientLogging implements ClientRequestFilter,ClientResponseFilter {
    
    @Context 
    private Providers providers;

    private static final String TRACE = "trace-#";
    private static final String SO = "SO";
    private static Logger logger = LoggerFactory.getLogger(JaxRsClientLogging.class);

    public void setTargetService(TargetEntity targetEntity){
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity.toString());
    }

    @Override
    public void filter(ClientRequestContext clientRequest) {
        try{
            setupMDC(clientRequest);
            setupHeaders(clientRequest);
            logger.info(ONAPLogConstants.Markers.INVOKE, "Invoke");
        } catch (Exception e) {
            logger.warn("Error in incoming JAX-RS Inteceptor", e);
        }
    }

    private void setupHeaders(ClientRequestContext clientRequest) {
        MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
        headers.add(ONAPLogConstants.Headers.REQUEST_ID, extractRequestID(clientRequest));
        headers.add(ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
        headers.add(ONAPLogConstants.Headers.PARTNER_NAME, SO);
    }

    private void setupMDC(ClientRequestContext clientRequest) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, clientRequest.getUri().toString());
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        setInvocationId();
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY,MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY));
    }

    private String extractRequestID(ClientRequestContext clientRequest) {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if(requestId == null || requestId.isEmpty() || requestId.equals(TRACE)){
            requestId = UUID.randomUUID().toString();
            logger.warn("Could not Find Request ID Generating New One: {}",clientRequest.getUri().getPath());
        }
        return requestId;
    }	

    private void setInvocationId() {
        String invocationId = MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID);
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }


    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {

        try {
            String statusCode;
            if(Response.Status.Family.familyOf(responseContext.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){		
                statusCode=ONAPLogConstants.ResponseStatus.COMPLETED.toString();
            }else{							
                statusCode=ONAPLogConstants.ResponseStatus.ERROR.toString();				
            }
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseContext.getStatus()));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
            logger.info(ONAPLogConstants.Markers.INVOKE_RETURN, "InvokeReturn");
            clearClientMDCs();
        } catch ( Exception e) {
            logger.warn("Error in outgoing JAX-RS Inteceptor", e);
        }
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
        MDC.remove(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP);
    }

}
