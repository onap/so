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
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelType;

public class CloudConfigurationValidation implements ValidationRule {

    private static final String Cloud_Configuration = "cloudConfiguration";

    public boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        CloudConfiguration cloudConfiguration = info.getSir().getRequestDetails().getCloudConfiguration();
        String requestScope = info.getRequestScope();
        int reqVersion = info.getReqVersion();
        Actions action = info.getAction();
        Boolean aLaCarteFlag = info.getALaCarteFlag();

        if (!requestScope.equals(ModelType.instanceGroup.toString())) {
            if (cloudConfiguration == null && reqVersion >= 5 && (aLaCarteFlag != null && aLaCarteFlag)) {
                if ((!requestScope.equalsIgnoreCase(ModelType.service.name())
                        && !requestScope.equalsIgnoreCase(ModelType.configuration.name()))
                        && (action == Action.createInstance || action == Action.deleteInstance
                                || action == Action.updateInstance)) {
                    throw new ValidationException(Cloud_Configuration);
                }
                if ((requestScope.equalsIgnoreCase(ModelType.vnf.name())
                        || requestScope.equalsIgnoreCase(ModelType.vfModule.name()))
                        && action == Action.replaceInstance) {
                    throw new ValidationException(Cloud_Configuration);
                }
                if (requestScope.equalsIgnoreCase(ModelType.configuration.name())
                        && (action == Action.enablePort || action == Action.disablePort
                                || action == Action.activateInstance || action == Action.deactivateInstance)) {
                    throw new ValidationException(Cloud_Configuration);
                }
                if (requestScope.equalsIgnoreCase(ModelType.vfModule.name())
                        && (action == Action.deactivateAndCloudDelete || action == Action.scaleOut)) {
                    throw new ValidationException(Cloud_Configuration);
                }
                if (requestScope.equals(ModelType.vnf.name()) && action == Action.recreateInstance) {
                    throw new ValidationException(Cloud_Configuration, true);
                }
            }
        }

        if (cloudConfiguration == null && ((aLaCarteFlag != null && !aLaCarteFlag)
                && requestScope.equalsIgnoreCase(ModelType.service.name()) && reqVersion < 5)) {
            throw new ValidationException(Cloud_Configuration);
        }

        if (cloudConfiguration != null) {
            if (empty(cloudConfiguration.getLcpCloudRegionId())) {
                throw new ValidationException("lcpCloudRegionId");
            }
            if (empty(cloudConfiguration.getTenantId())
                    && !(requestScope.equalsIgnoreCase(ModelType.configuration.name()))) {
                throw new ValidationException("tenantId");
            }
        }
        return info;
    }
}
