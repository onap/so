/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.filter.base;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractAuditLogFilter<GenericRequest, GenericResponse> extends MDCSetup {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractAuditLogFilter.class);

    protected void pre(SimpleMap headers, GenericRequest request, HttpServletRequest httpServletRequest) {
        logger.info("In pre method headers : {}", headers);
        try {
            String requestId = getRequestId(headers);
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
            setInvocationId(headers);
            setServiceName(request);
            setMDCPartnerName(headers);
            setServerFQDN();
            setClientIPAddress(httpServletRequest);
            setInstanceID();
            setEntryTimeStamp();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
            additionalPreHandling(request);
            setLogTimestamp();
            setElapsedTime();
            logEntering();
        } catch (Exception e) {
            logger.warn("Error in AbstractInboundFilter pre", e);
        }
    }

    protected void post(GenericResponse response) {
        try {
            MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, MDC.get(ONAPLogConstants.MDCs.SERVER_INVOCATION_ID));
            int responseCode = getResponseCode(response);
            setResponseStatusCode(responseCode);
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseCode));
            setResponseDescription(responseCode);
            setLogTimestamp();
            setElapsedTime();
            additionalPostHandling(response);
            logExiting();
        } catch (Exception e) {
            logger.warn("Error in AbstractInboundFilter post", e);
        } finally {
            MDC.clear();
        }
    }

    protected abstract int getResponseCode(GenericResponse response);

    protected abstract void setServiceName(GenericRequest request);

    protected void additionalPreHandling(GenericRequest request) {
        // override to add additional pre handling
    }

    protected void additionalPostHandling(GenericResponse response) {
        // override to add additional post handling
    }

    protected void logEntering() {
        logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
    }

    protected void logExiting() {
        logger.info(ONAPLogConstants.Markers.EXIT, "Exiting.");
    }

}
