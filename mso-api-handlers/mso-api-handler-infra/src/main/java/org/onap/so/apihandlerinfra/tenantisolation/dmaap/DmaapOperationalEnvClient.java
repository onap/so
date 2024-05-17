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

package org.onap.so.apihandlerinfra.tenantisolation.dmaap;

import java.util.ServiceLoader;
import org.apache.http.HttpStatus;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DmaapOperationalEnvClient {

    @Autowired
    private ServiceLoader.Provider<OperationalEnvironmentPublisher> dmaapPublisher;

    protected String buildRequest(String operationalEnvironmentId, String operationalEnvironmentName,
            String operationalEnvironmentType, String tenantContext, String workloadContext, String action)
            throws ApiException {
        final CreateEcompOperationEnvironmentBean operationalEnv = new CreateEcompOperationEnvironmentBean();
        operationalEnv.withOperationalEnvironmentId(operationalEnvironmentId)
                .withOperationalEnvironmentName(operationalEnvironmentName)
                .withOperationalEnvironmentType(operationalEnvironmentType).withTenantContext(tenantContext)
                .withWorkloadContext(workloadContext).withaction(action);
        try {
            return this.getJson(operationalEnv);
        } catch (JsonProcessingException ex) {
            ErrorLoggerInfo errorLoggerInfo =
                    new ErrorLoggerInfo.Builder(MessageEnum.APIH_REQUEST_VALIDATION_ERROR, ErrorCode.SchemaError)
                            .build();
            ValidateException validateException =
                    new ValidateException.Builder("Mapping of request to JSON object failed : " + ex.getMessage(),
                            HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER).cause(ex)
                                    .errorInfo(errorLoggerInfo).build();

            throw validateException;
        }
    }

    protected String getJson(CreateEcompOperationEnvironmentBean obj) throws JsonProcessingException {

        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);

    }

    public void dmaapPublishOperationalEnvRequest(String operationalEnvironmentId, String operationalEnvironmentName,
            String operationalEnvironmentType, String tenantContext, String workloadContext, String action)
            throws ApiException {

        String request = this.buildRequest(operationalEnvironmentId, operationalEnvironmentName,
                operationalEnvironmentType, tenantContext, workloadContext, action);
        dmaapPublisher.get().send(request);

    }

}
