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

package org.onap.so.logging.spring.interceptor;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.LogConstants;
import org.onap.so.logging.jaxrs.filter.MDCSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Autowired
    private MDCSetup mdcSetup;
    
    @Context 
    private Providers providers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
    	
        Map<String, String> headers = Collections.list((request).getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));
        setRequestId(headers);
        setInvocationId(headers);
        setServiceName(request);
        setMDCPartnerName(headers);
        mdcSetup.setClientIPAddress(request);
        mdcSetup.setEntryTimeStamp();
        mdcSetup.setInstanceUUID();
        mdcSetup.setServerFQDN();
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
        logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        if (logger.isDebugEnabled()) 
        	logRequestInformation(request);
        return true;
    }
    
    protected void logRequestInformation(HttpServletRequest request) {
    	Map<String, String> headers = Collections.list((request).getHeaderNames())
    		    .stream()
    		    .collect(Collectors.toMap(h -> h, request::getHeader));

    	logger.debug("===========================request begin================================================");
    	logger.debug("URI         : {}", request.getRequestURI());
    	logger.debug("Method      : {}", request.getMethod());
    	logger.debug("Headers     : {}", headers);
    	logger.debug("==========================request end================================================");
		
	}

	@Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        setResponseStatusCode(response);
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_DESCRIPTION,"");      
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE,String.valueOf(response.getStatus()));
        logger.info(ONAPLogConstants.Markers.EXIT, "Exiting.");
        MDC.clear();
    }

	protected void setResponseStatusCode(HttpServletResponse response) {
        String statusCode;
        if(Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){     
            statusCode=ONAPLogConstants.ResponseStatus.COMPLETED.toString();
        }else{                          
            statusCode= ONAPLogConstants.ResponseStatus.ERROR.toString();               
        }           
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    }

	protected void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }
	
	protected void setRequestId(Map<String, String> headers) {
        String requestId=headers.get(ONAPLogConstants.Headers.REQUEST_ID.toLowerCase());      
        if(requestId == null || requestId.isEmpty())
        	requestId = UUID.randomUUID().toString();    
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,requestId);
    }

	protected void setInvocationId(Map<String, String> headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID.toLowerCase());
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

	protected void setMDCPartnerName(Map<String, String> headers) {
        String partnerName=headers.get(ONAPLogConstants.Headers.PARTNER_NAME.toLowerCase());
        if(partnerName == null || partnerName.isEmpty())
            partnerName = "";
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME,partnerName);
    }
    

}
