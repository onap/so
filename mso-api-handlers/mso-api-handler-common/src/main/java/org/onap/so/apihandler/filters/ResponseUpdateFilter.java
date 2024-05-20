/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import java.sql.Timestamp;
import javax.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Provider
@ResponseUpdater
public class ResponseUpdateFilter implements ContainerResponseFilter {

    protected static Logger logger = LoggerFactory.getLogger(ResponseUpdateFilter.class);

    @Context
    private HttpServletRequest httpServletRequest;

    @Context
    private Providers providers;

    @Autowired
    protected RequestsDbClient infraActiveRequestsClient;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        try {
            logger.info("updating requests status");
            updateRequestDBToFailed(responseContext);
        } catch (Exception e) {
            logger.warn("Error in outgoing JAX-RS Inteceptor updating request db to failed", e);
        }
    }

    private void updateRequestDBToFailed(ContainerResponseContext responseContext) {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (requestId != null && !Response.Status.Family.familyOf(responseContext.getStatus())
                .equals(Response.Status.Family.SUCCESSFUL)) {
            InfraActiveRequests currentRequest = infraActiveRequestsClient.getInfraActiveRequestbyRequestId(requestId);
            if (currentRequest != null) {
                Timestamp endTimeStamp = new Timestamp(System.currentTimeMillis());
                RequestError error;
                try {
                    error = (RequestError) responseContext.getEntity();
                } catch (Exception e) {
                    logger.warn("Error Casting Entity to Request Error, generating unknown Error", e);
                    error = new RequestError();
                    ServiceException serviceException = new ServiceException();
                    serviceException.setText("Unknown Error Occured during processing");
                    error.setServiceException(serviceException);
                }
                if (error.getServiceException() != null && error.getServiceException().getText() != null
                        && !error.getServiceException().getText().isEmpty()) {
                    currentRequest.setStatusMessage(error.getServiceException().getText());
                } else {
                    currentRequest.setStatusMessage("Unknown Error Occured during processing");
                }
                currentRequest.setRequestStatus(Status.FAILED.toString());
                currentRequest.setEndTime(endTimeStamp);
                currentRequest.setProgress(100L);
                infraActiveRequestsClient.updateInfraActiveRequests(currentRequest);
            }
        }
    }
}

