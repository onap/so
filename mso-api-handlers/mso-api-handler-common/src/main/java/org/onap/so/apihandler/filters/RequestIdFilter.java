/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
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
package org.onap.so.apihandler.filters;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpStatus;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Priority(2)
@Provider
@Component
public class RequestIdFilter implements ContainerRequestFilter {

    protected static Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);

    @Autowired
    private RequestsDbClient infraActiveRequestsClient;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);

        InfraActiveRequests infraActiveRequests = infraActiveRequestsClient.getInfraActiveRequestbyRequestId(requestId);

        if (infraActiveRequests != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(HttpStatus.SC_BAD_REQUEST));
            logger.error("RequestID exists in RequestDB.InfraActiveRequests : " + requestId);
        }
    }
}
