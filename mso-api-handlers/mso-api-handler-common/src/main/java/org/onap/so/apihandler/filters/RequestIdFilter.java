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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestIdException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Priority(2)
@Provider
@Component
public class RequestIdFilter implements ContainerRequestFilter {

    private static Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);

    @Autowired
    private RequestsDbClient infraActiveRequestsClient;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        String requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        UriInfo uriInfo = context.getUriInfo();
        String requestURI = uriInfo.getPath();

        if (!requestURI.contains("orchestrationRequests")) {
            logger.info("Checking if requestId: {} already exists in requestDb InfraActiveRequests table", requestId);
            InfraActiveRequests infraActiveRequests =
                    infraActiveRequestsClient.getInfraActiveRequestbyRequestId(requestId);

            if (infraActiveRequests != null) {
                logger.error(
                        "RequestId: {} already exists in RequestDB InfraActiveRequests table, throwing DuplicateRequestIdException",
                        requestId);
                throw new DuplicateRequestIdException(createRequestError(requestId, "InfraActiveRequests"));
            }
        }
    }

    protected String createRequestError(String requestId, String requestTable) {
        ObjectMapper mapper = new ObjectMapper();
        RequestError error = new RequestError();
        ServiceException serviceException = new ServiceException();
        serviceException.setMessageId(ErrorNumbers.SVC_BAD_PARAMETER);
        serviceException
                .setText("RequestId: " + requestId + " already exists in the RequestDB " + requestTable + " table");
        error.setServiceException(serviceException);
        String errorMessage = null;

        try {
            errorMessage = mapper.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            return "Unable to write requestError to String when requestId already exists in the RequestDb due to error: "
                    + e.getMessage();
        }
        return errorMessage;
    }
}
