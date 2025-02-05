/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import org.slf4j.MDC;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import java.util.Map;

public class FilteredMetricLogClientFilter extends MetricLogClientFilter {
    protected CustomFilter<ClientRequestContext, ClientResponseContext> customFilter;
    private static final String REQUEST_STATE_PROPERTY_NAME = "org.onap.so.logging.filter.base.RequestState";

    public FilteredMetricLogClientFilter(CustomFilter<ClientRequestContext, ClientResponseContext> f) {
        customFilter = f;
    }

    protected void additionalPre(ClientRequestContext request) {
        request.setProperty(REQUEST_STATE_PROPERTY_NAME, MDC.getCopyOfContextMap());
    }

    protected void additionalPost(ClientRequestContext request, ClientResponseContext response) {
        if (customFilter.shouldLog(request, response)) {
            Map<String, String> responseState = MDC.getCopyOfContextMap();
            Map<String, String> requestState = (Map<String, String>) request.getProperty(REQUEST_STATE_PROPERTY_NAME);
            MDC.setContextMap(requestState);
            logger.info(ONAPLogConstants.Markers.INVOKE, "Invoke");
            MDC.setContextMap(responseState);
            logger.info(INVOKE_RETURN, "InvokeReturn");
        }
    }

    protected void logRequest() {
        // override with empty so log entries are not duplicated
    }

    protected void logResponse() {
        // override with empty so log entries are not duplicated
    }

}
