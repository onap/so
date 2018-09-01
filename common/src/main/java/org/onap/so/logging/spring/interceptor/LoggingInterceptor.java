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
    MDCSetup mdcSetup;
    
    @Context 
    private Providers providers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Map<String, String> headers = Collections.list(((HttpServletRequest) request).getHeaderNames())
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
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, "INPROGRESS");
        logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        return true;
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

    private void setResponseStatusCode(HttpServletResponse response) {
        String statusCode;
        if(Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL)){     
            statusCode=ONAPLogConstants.ResponseStatus.COMPLETED.toString();
        }else{                          
            statusCode= ONAPLogConstants.ResponseStatus.ERROR.toString();               
        }           
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    }

    private void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }

    private void setRequestId(Map<String, String> headers) {
        String requestId=headers.get(ONAPLogConstants.Headers.REQUEST_ID);
        if(requestId == null || requestId.isEmpty())
            requestId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID,requestId);
    }

    private void setInvocationId(Map<String, String> headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID);
        if(invocationId == null || invocationId.isEmpty())
            invocationId =UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    private void setMDCPartnerName(Map<String, String> headers) {
        String partnerName=headers.get(ONAPLogConstants.Headers.PARTNER_NAME);
        if(partnerName == null || partnerName.isEmpty())
            partnerName = "";
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME,partnerName);
    }
    

}