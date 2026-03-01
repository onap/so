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

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeactivateVnfOperationalEnvironment {

    @Autowired
    private AAIClientHelper aaiHelper;
    @Autowired
    private RequestsDBHelper requestDb;

    public void execute(String requestId, CloudOrchestrationRequest request) throws ApiException {
        String operationalEnvironmentId = request.getOperationalEnvironmentId();

        OperationalEnvironment aaiOpEnv = getAAIOperationalEnvironment(operationalEnvironmentId);
        if (aaiOpEnv != null) {
            String operationalEnvironmentStatus = aaiOpEnv.getOperationalEnvironmentStatus();

            if (StringUtils.isBlank(operationalEnvironmentStatus)) {
                String error =
                        "OperationalEnvironmentStatus is null on OperationalEnvironmentId: " + operationalEnvironmentId;
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.DataError).build();
                throw new ValidateException.Builder(error, HttpStatus.SC_BAD_REQUEST,
                        ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
            }

            if (operationalEnvironmentStatus.equalsIgnoreCase("ACTIVE")) {

                aaiOpEnv.setOperationalEnvironmentStatus("INACTIVE");
                aaiHelper.updateAaiOperationalEnvironment(operationalEnvironmentId, aaiOpEnv);

            } else if (!operationalEnvironmentStatus.equalsIgnoreCase("INACTIVE")) {
                String error =
                        "Invalid OperationalEnvironmentStatus on OperationalEnvironmentId: " + operationalEnvironmentId;
                ErrorLoggerInfo errorLoggerInfo =
                        new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, ErrorCode.DataError).build();
                ValidateException validateException = new ValidateException.Builder(error, HttpStatus.SC_BAD_REQUEST,
                        ErrorNumbers.SVC_DETAILED_SERVICE_ERROR).errorInfo(errorLoggerInfo).build();
                requestDb.updateInfraFailureCompletion(error, requestId, operationalEnvironmentId);
                throw validateException;
            }

            requestDb.updateInfraSuccessCompletion("SUCCESSFULLY Deactivated OperationalEnvironment", requestId,
                    operationalEnvironmentId);
        }
    }

    private OperationalEnvironment getAAIOperationalEnvironment(String operationalEnvironmentId) {
        AAIResultWrapper aaiResult = aaiHelper.getAaiOperationalEnvironment(operationalEnvironmentId);
        Optional<OperationalEnvironment> operationalEnvironmentOpt = aaiResult.asBean(OperationalEnvironment.class);
        return operationalEnvironmentOpt.isPresent() ? operationalEnvironmentOpt.get() : null;
    }
}
