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

package org.onap.so.logging.cxf.interceptor;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
// import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class SOAPLoggingInInterceptor extends AbstractSoapInterceptor {

    protected static Logger logger = LoggerFactory.getLogger(SOAPLoggingInInterceptor.class);

    public SOAPLoggingInInterceptor() {
        super(Phase.READ);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            // MDCSetup mdcSetup = new MDCSetup();
            Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
            HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
            request.getRemoteAddr();

            setRequestId(headers);
            setInvocationId(headers);
            setServiceName(message);
            setMDCPartnerName(headers);
            // mdcSetup.setServerFQDN();
            // mdcSetup.setClientIPAddress(request);
            // mdcSetup.setInstanceID();
            // mdcSetup.setEntryTimeStamp();
            // mdcSetup.setLogTimestamp();
            // mdcSetup.setElapsedTime();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, "INPROGRESS");
            logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        } catch (Exception e) {
            logger.warn("Error in incoming SOAP Message Inteceptor", e);
        }
    }

    private void setServiceName(SoapMessage message) {
        String requestURI = (String) message.get(Message.REQUEST_URI);
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, requestURI);
    }

    // CXF Appears to flatten headers to lower case
    private void setMDCPartnerName(Map<String, List<String>> headers) {
        String partnerName = getValueOrDefault(headers, ONAPLogConstants.Headers.PARTNER_NAME.toLowerCase(), "UNKNOWN");
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
    }

    private void setInvocationId(Map<String, List<String>> headers) {
        String invocationId = getValueOrDefault(headers, ONAPLogConstants.Headers.INVOCATION_ID.toLowerCase(),
                UUID.randomUUID().toString());
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
        MDC.put(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID, invocationId);
    }

    private void setRequestId(Map<String, List<String>> headers) {
        String requestId = getValueOrDefault(headers, ONAPLogConstants.Headers.REQUEST_ID.toLowerCase(),
                UUID.randomUUID().toString());
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
    }

    private String getValueOrDefault(Map<String, List<String>> headers, String headerName, String defaultValue) {
        String headerValue;
        List<String> headerList = headers.get(headerName);
        if (headerList != null && !headerList.isEmpty()) {
            headerValue = headerList.get(0);
            if (headerValue == null || headerValue.isEmpty())
                headerValue = defaultValue;
        } else
            headerValue = defaultValue;
        return headerValue;
    }

}

