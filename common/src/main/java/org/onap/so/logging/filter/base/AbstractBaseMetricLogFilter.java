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
import org.slf4j.*;
import java.util.UUID;

public abstract class AbstractBaseMetricLogFilter<Request, Response> extends MDCSetup {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractBaseMetricLogFilter.class);
    protected final String partnerName;
    protected static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");

    public AbstractBaseMetricLogFilter() {
        partnerName = getPartnerName();
    }

    protected abstract String getTargetServiceName(Request request);

    protected abstract int getHttpStatusCode(Response response);

    protected abstract String getResponseCode(Response response);

    protected abstract String getTargetEntity(Request request);

    protected void pre(Request request) {
        logger.info("In AbstractBaseMetricLogFilter pre method request : {}", request.toString());
        try {
            setupMDC(request);
            extractRequestID();
            setInvocationId();
            additionalPre(request);
            logRequest();
        } catch (Exception e) {
            logger.warn("Error in AbstractBaseMetricLogFilter pre", e);
        }
    }

    protected void additionalPre(Request request) {
        // override to add application specific logic
    }

    protected String setInvocationId() {
        String invocationId = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID, invocationId);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationId);
        return invocationId;
    }

    protected void setupMDC(Request request) {
        MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP, getCurrentTimeStamp());
        MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, getTargetServiceName(request));
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());

        if (MDC.get(ONAPLogConstants.MDCs.TARGET_ENTITY) == null) {
            String targetEntity = getTargetEntity(request);
            if (targetEntity != null) {
                MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity);
            } else {
                MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, Constants.DefaultValues.UNKNOWN_TARGET_ENTITY);
            }
        }
        setServerFQDN();
        setLogTimestamp();
        setElapsedTimeInvokeTimestamp();
    }

    protected String extractRequestID() {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
            logger.trace("No value found in MDC when checking key {} value will be set to {}",
                    ONAPLogConstants.MDCs.REQUEST_ID, requestId);
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);
        }
        return requestId;
    }

    protected void post(Request request, Response response) {
        logger.info("In AbstractBaseMetricLogFilter post method response : {}", response);
        try {
            setLogTimestamp();
            setElapsedTimeInvokeTimestamp();
            setResponseStatusCode(getHttpStatusCode(response));
            setResponseDescription(getHttpStatusCode(response));
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, getResponseCode(response));
            additionalPost(request, response);
            logResponse();
            clearClientMDCs();
        } catch (Exception e) {
            logger.warn("Error in AbstractBaseMetricLogFilter post", e);
        }
    }

    protected void additionalPost(Request request, Response response) {
        // override to add application specific logic
    }

    protected String getPartnerName() {
        return getProperty(Constants.Property.PARTNER_NAME);
    }

    protected void logRequest() {
        logger.info(ONAPLogConstants.Markers.INVOKE, "Invoke");
    }

    protected void logResponse() {
        logger.info(INVOKE_RETURN, "InvokeReturn");
    }

}
