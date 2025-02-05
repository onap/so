/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.apihandlerinfra.tenantisolation.process;

import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.dmaap.DmaapOperationalEnvClient;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientObjectBuilder;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateEcompOperationalEnvironment {

    private static Logger logger = LoggerFactory.getLogger(CreateEcompOperationalEnvironment.class);

    @Autowired
    private AAIClientObjectBuilder aaiClientObjectBuilder;
    @Autowired
    private AAIClientHelper aaiHelper;
    @Autowired
    private RequestsDBHelper requestDb;
    @Autowired
    private DmaapOperationalEnvClient dmaapClient;

    public void execute(String requestId, CloudOrchestrationRequest request) throws ApiException {

        // Create ECOMP Managing Environment object in A&AI
        aaiHelper
                .createOperationalEnvironment(aaiClientObjectBuilder.buildAAIOperationalEnvironment("ACTIVE", request));

        // Call client to publish to DMaap
        try {
            logger.debug("1 {}", request.getOperationalEnvironmentId());
            logger.debug("2 {}", request.getRequestDetails().getRequestInfo().getInstanceName());
            logger.debug("3 {}",
                    request.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString());
            logger.debug("4 {}", request.getRequestDetails().getRequestParameters().getTenantContext());
            logger.debug("5 {}", request.getRequestDetails().getRequestParameters().getWorkloadContext());


            dmaapClient.dmaapPublishOperationalEnvRequest(request.getOperationalEnvironmentId(),
                    request.getRequestDetails().getRequestInfo().getInstanceName(),
                    request.getRequestDetails().getRequestParameters().getOperationalEnvironmentType().toString(),
                    request.getRequestDetails().getRequestParameters().getTenantContext(),
                    request.getRequestDetails().getRequestParameters().getWorkloadContext(), "Create");
        } catch (Exception e) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.DataError).build();
            ValidateException validateException =
                    new ValidateException.Builder("Could not publish DMaap", HttpStatus.SC_BAD_REQUEST,
                            ErrorNumbers.SVC_BAD_PARAMETER).cause(e).errorInfo(errorLoggerInfo).build();
            requestDb.updateInfraFailureCompletion(e.getMessage(), requestId, request.getOperationalEnvironmentId());
            throw validateException;
        }
        // Update request database
        requestDb.updateInfraSuccessCompletion("SUCCESSFULLY Created ECOMP OperationalEnvironment.", requestId,
                request.getOperationalEnvironmentId());

    }
}
