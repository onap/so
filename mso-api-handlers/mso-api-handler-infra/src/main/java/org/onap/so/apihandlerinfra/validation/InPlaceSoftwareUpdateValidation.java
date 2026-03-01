/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.apihandlerinfra.validation;

import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;

public class InPlaceSoftwareUpdateValidation implements ValidationRule {
    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        RequestParameters requestParameters = info.getSir().getRequestDetails().getRequestParameters();
        CloudConfiguration cloudConfiguration = info.getSir().getRequestDetails().getCloudConfiguration();
        RequestInfo requestInfo = info.getSir().getRequestDetails().getRequestInfo();

        if (cloudConfiguration == null) {
            throw new ValidationException("cloudConfiguration", true);
        } else if (empty(cloudConfiguration.getLcpCloudRegionId())) {
            throw new ValidationException("lcpCloudRegionId", true);
        } else if (empty(cloudConfiguration.getTenantId())) {
            throw new ValidationException("tenantId", true);
        }
        if (requestInfo == null) {
            throw new ValidationException("requestInfo", true);
        } else if (empty(requestInfo.getRequestorId())) {
            throw new ValidationException("requestorId", true);
        } else if (empty(requestInfo.getSource())) {
            throw new ValidationException("source", true);
        }
        if (requestParameters == null) {
            throw new ValidationException("requestParameters", true);
        }
        return info;
    }
}
