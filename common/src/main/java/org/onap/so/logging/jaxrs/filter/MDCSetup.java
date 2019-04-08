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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCSetup {

    protected static Logger logger = LoggerFactory.getLogger(MDCSetup.class);

    private static final String INSTANCE_UUID = UUID.randomUUID().toString();

    public void setInstanceUUID() {
        MDC.put(ONAPLogConstants.MDCs.INSTANCE_UUID, INSTANCE_UUID);
    }

    public void setServerFQDN() {
        String serverFQDN = "";
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            serverFQDN = addr.toString();
        } catch (UnknownHostException e) {
            logger.warn("Cannot Resolve Host Name");
            serverFQDN = "";
        }
        MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, serverFQDN);
    }

    public void setClientIPAddress(HttpServletRequest httpServletRequest) {
        String remoteIpAddress = "";
        if (httpServletRequest != null) {
            remoteIpAddress = httpServletRequest.getRemoteAddr();
        }
        MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, remoteIpAddress);
    }

    public void setEntryTimeStamp() {
        MDC.put(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP,
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
    }

    public void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }

    public void setRequestId(Map<String, String> headers) {
        String requestId = headers.get(ONAPLogConstants.Headers.REQUEST_ID);
        if (requestId == null || requestId.isEmpty())
            requestId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
    }

    public void setInvocationId(Map<String, String> headers) {
        String invocationId = headers.get(ONAPLogConstants.Headers.INVOCATION_ID);
        if (invocationId == null || invocationId.isEmpty())
            invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
    }

    public void setMDCPartnerName(Map<String, String> headers) {
        String partnerName = headers.get(ONAPLogConstants.Headers.PARTNER_NAME);
        if (partnerName == null || partnerName.isEmpty())
            partnerName = "";
        MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
    }


    public void setResponseStatusCode(HttpServletResponse response) {
        String statusCode;
        if (Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL)) {
            statusCode = ONAPLogConstants.ResponseStatus.COMPLETED.toString();
        } else {
            statusCode = ONAPLogConstants.ResponseStatus.ERROR.toString();
        }
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, statusCode);
    }
}
