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

import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.apihandlerinfra.Constants;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestInfo;

public class RequestInfoValidation implements ValidationRule {
    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        RequestInfo requestInfo = info.getSir().getRequestDetails().getRequestInfo();
        int reqVersion = info.getReqVersion();
        String requestScope = info.getRequestScope();
        Actions action = info.getAction();
        Boolean aLaCarteFlag = info.getALaCarteFlag();

        // required for all operations in V4
        if (empty(requestInfo.getRequestorId()) && reqVersion >= 4) {
            throw new ValidationException("requestorId");
        }

        if (empty(requestInfo.getSource())) {
            throw new ValidationException("source");
        }
        if (!empty(requestInfo.getInstanceName())) {
            if (!requestInfo.getInstanceName().matches(Constants.VALID_INSTANCE_NAME_FORMAT)) {
                throw new ValidationException("instanceName format");
            }
        }
        if (empty(requestInfo.getProductFamilyId())) {
            // Mandatory for vnf Create(aLaCarte=true), Network Create(aLaCarte=true) and network update
            // Mandatory for macro request create service instance
            if ((requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action == Action.createInstance)
                    || (requestScope.equalsIgnoreCase(ModelType.network.name())
                            && (action == Action.createInstance || action == Action.updateInstance))
                    || (reqVersion > 3 && (aLaCarteFlag != null && !aLaCarteFlag)
                            && requestScope.equalsIgnoreCase(ModelType.service.name())
                            && action == Action.createInstance)) {
                throw new ValidationException("productFamilyId");
            }
        }
        return info;
    }
}
