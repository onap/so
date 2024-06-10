/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.infra.rest.exception.WorkflowEngineConnectionException;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WorkflowEngineConnectionMapper implements ExceptionMapper<WorkflowEngineConnectionException> {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineConnectionMapper.class);

    @Override
    public Response toResponse(WorkflowEngineConnectionException e) {
        logger.error("Workflow Engine Connection Error", e);
        RequestError error = new RequestError();
        ServiceException value = new ServiceException();
        value.setMessageId(ErrorNumbers.SVC_GENERAL_SERVICE_ERROR);
        value.setText(e.getMessage());
        error.setServiceException(value);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
