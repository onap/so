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

package org.onap.so.logging.jaxrs.filter;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.LogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
 
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
 
public class SpringClientFilter implements ClientHttpRequestInterceptor {
 
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static final String TRACE = "trace-#";
    private static final String SO = "SO";
 
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    	processRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        processResponse(response);
        return response;
    }
 
    private void processRequest(HttpRequest request, byte[] body) throws IOException {
     setInvocationId();
    	setupHeaders(request);
    	setupMDC(request);
        if (log.isDebugEnabled()) {
            log.debug("===========================request begin================================================");
            log.debug("URI         : {}", request.getURI());
            log.debug("Method      : {}", request.getMethod());
            log.debug("Headers     : {}", request.getHeaders());
            log.debug("Request body: {}", new String(body, "UTF-8"));
            log.debug("==========================request end================================================");
        }
    }
    
    private void setupHeaders(HttpRequest clientRequest) {
        HttpHeaders headers = clientRequest.getHeaders();
        headers.add(ONAPLogConstants.Headers.REQUEST_ID, extractRequestID(clientRequest));
        headers.add(ONAPLogConstants.Headers.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
        headers.add(ONAPLogConstants.Headers.PARTNER_NAME, SO);
    }
    
    private String extractRequestID(HttpRequest clientRequest) {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if(requestId == null || requestId.isEmpty() || requestId.equals(TRACE)){
            requestId = UUID.randomUUID().toString();
            log.warn("Could not Find Request ID Generating New One: {}",clientRequest.getURI());
        }
        return requestId;
    }	

    private void setupMDC(HttpRequest clientRequest) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, clientRequest.getURI().toString());       
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY,extractTargetEntity(clientRequest));
    }
    
    private String extractTargetEntity(HttpRequest clientRequest) {
    	HttpHeaders headers = clientRequest.getHeaders();
    	String headerTargetEntity = null;
    	List<String> headerTargetEntityList = headers.get(LogConstants.TARGET_ENTITY_HEADER);
    	if(headerTargetEntityList!= null && !headerTargetEntityList.isEmpty())
    		headerTargetEntity = headerTargetEntityList.get(0);
        String targetEntity = MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY);
        if(targetEntity != null &&
        		!targetEntity.isEmpty() ){
        	return targetEntity;        	
        }else if(headerTargetEntity != null &&
        		!headerTargetEntity.isEmpty()){
        	targetEntity = headerTargetEntity;
        }else{
        	targetEntity = LogConstants.UNKNOWN_TARGET_ENTITY;
        	log.warn("Could not Target Entity: {}",clientRequest.getURI());
        }
        return targetEntity;
    }	
    
    private void setInvocationId() {
        String invocationId = MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID);
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

 
    private void processResponse(ClientHttpResponse response) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("============================response begin==========================================");
            log.debug("Status code  : {}", response.getStatusCode());
            log.debug("Status text  : {}", response.getStatusText());
            log.debug("Headers      : {}", response.getHeaders());
            log.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            log.debug("=======================response end=================================================");
        }
        String statusCode;
        if(Response.Status.Family.familyOf(response.getRawStatusCode()).equals(Response.Status.Family.SUCCESSFUL)){		
            statusCode=ONAPLogConstants.ResponseStatus.COMPLETED.toString();
        }else{							
            statusCode=ONAPLogConstants.ResponseStatus.ERROR.toString();				
        }
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(response.getRawStatusCode()));
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION,"");
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
        log.info(ONAPLogConstants.Markers.INVOKE_RETURN, "InvokeReturn");
        clearClientMDCs();
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
