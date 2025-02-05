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

public abstract class AbstractMetricLogFilter<Request, Response, RequestHeaders>
        extends AbstractBaseMetricLogFilter<Request, Response> {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractMetricLogFilter.class);

    public AbstractMetricLogFilter() {
        super();
    }

    protected abstract void addHeader(RequestHeaders requestHeaders, String headerName, String headerValue);

    protected void pre(Request request, RequestHeaders requestHeaders) {
        logger.info("In AbstractMetricLogFilter pre method request : {}", request.toString());
        logger.info("In AbstractMetricLogFilter pre method requestHeaders : {}", requestHeaders);
        try {
            setupMDC(request);
            setupHeaders(request, requestHeaders, extractRequestID(), setInvocationId());
            additionalPre(request);
            additionalPre(request, requestHeaders);
            logRequest();
        } catch (Exception e) {
            logger.warn("Error in AbstractMetricLogFilter pre", e);
        }
    }

    protected void additionalPre(Request request, RequestHeaders requestHeaders) {
        // override to add application specific logic
    }

    protected void setupHeaders(Request clientRequest, RequestHeaders requestHeaders, String requestId,
            String invocationId) {
        addHeader(requestHeaders, ONAPLogConstants.Headers.REQUEST_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.HEADER_REQUEST_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.TRANSACTION_ID, requestId);
        addHeader(requestHeaders, Constants.HttpHeaders.ECOMP_REQUEST_ID, requestId);
        addHeader(requestHeaders, ONAPLogConstants.Headers.PARTNER_NAME, partnerName);
        logger.trace("Setting X-InvocationID header for outgoing request: {}", invocationId);
        addHeader(requestHeaders, ONAPLogConstants.Headers.INVOCATION_ID, invocationId);
    }

}
