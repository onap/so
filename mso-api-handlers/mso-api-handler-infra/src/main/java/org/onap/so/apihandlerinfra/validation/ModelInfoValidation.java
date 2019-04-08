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
import org.onap.so.apihandlerinfra.TestApi;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.utils.UUIDChecker;

public class ModelInfoValidation implements ValidationRule {
    private static boolean empty(String s) {
        return (s == null || s.trim().isEmpty());
    }

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        ModelInfo modelInfo = info.getSir().getRequestDetails().getModelInfo();
        RequestParameters requestParameters = info.getReqParameters();
        String requestScope = info.getRequestScope();
        Actions action = info.getAction();
        int reqVersion = info.getReqVersion();
        Boolean aLaCarteFlag = info.getALaCarteFlag();

        if (!requestScope.equals(ModelType.instanceGroup.toString())) {

            if (!empty(modelInfo.getModelNameVersionId())) {
                modelInfo.setModelVersionId(modelInfo.getModelNameVersionId());
            }
            // modelCustomizationId is required when usePreLoad is false for v4 and higher for VF Module Create
            if (requestParameters != null && reqVersion >= 4 && requestScope.equalsIgnoreCase(ModelType.vfModule.name())
                    && action == Action.createInstance && !requestParameters.isUsePreload()) {
                if (!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())) {
                    throw new ValidationException("modelCustomizationId");
                }
            }

            // modelCustomizationId is required for v5 and higher for VF Module Replace
            if (requestParameters != null && reqVersion > 4 && requestScope.equalsIgnoreCase(ModelType.vfModule.name())
                    && action == Action.replaceInstance) {
                if (!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())) {
                    throw new ValidationException("modelCustomizationId");
                }
            }

            // modelCustomizationId or modelCustomizationName are required for VNF Replace
            if (requestParameters != null && reqVersion > 4 && requestScope.equalsIgnoreCase(ModelType.vnf.name())
                    && action == Action.replaceInstance || action == Action.recreateInstance) {
                if (!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())
                        && modelInfo.getModelCustomizationName() == null) {
                    throw new ValidationException("modelCustomizationId or modelCustomizationName");
                }
            }

            // is required for serviceInstance delete macro when aLaCarte=false (v3)
            // create and updates except for network (except v4)
            if (empty(modelInfo.getModelInvariantId()) && ((reqVersion > 2 && (aLaCarteFlag != null && !aLaCarteFlag)
                    && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.deleteInstance)
                    || !(reqVersion < 4 && requestScope.equalsIgnoreCase(ModelType.network.name()))
                            && (action == Action.createInstance || action == Action.updateInstance
                                    || action == Action.enablePort || action == Action.disablePort
                                    || action == Action.addRelationships || action == Action.removeRelationships
                                    || (requestScope.equalsIgnoreCase(ModelType.configuration.name())
                                            && (action == Action.activateInstance
                                                    || action == Action.deactivateInstance))))) {
                throw new ValidationException("modelInvariantId");
            }
            if (empty(modelInfo.getModelInvariantId())
                    && (requestScope.equalsIgnoreCase(ModelType.vfModule.name()) && action == Action.scaleOut)) {
                throw new ValidationException("modelInvariantId");
            }
            if (empty(modelInfo.getModelInvariantId())
                    && (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action == Action.recreateInstance)) {
                throw new ValidationException("modelInvariantId", true);
            }
            if (!empty(modelInfo.getModelInvariantId()) && !UUIDChecker.isValidUUID(modelInfo.getModelInvariantId())) {
                throw new ValidationException("modelInvariantId format");
            }

            if (reqVersion >= 4 && !(requestScope.equalsIgnoreCase(ModelType.configuration.name()))
                    && empty(modelInfo.getModelName())
                    && (action == Action.createInstance || action == Action.updateInstance
                            || action == Action.addRelationships || action == Action.removeRelationships
                            || action == Action.recreateInstance
                            || ((action == Action.deleteInstance || action == Action.scaleOut)
                                    && (requestScope.equalsIgnoreCase(ModelType.vfModule.name()))))) {
                throw new ValidationException("modelName", true);
            }

            if (empty(modelInfo.getModelVersion()) && !(requestScope.equalsIgnoreCase(ModelType.configuration.name()))
                    && (!(reqVersion < 4 && requestScope.equalsIgnoreCase(ModelType.network.name()))
                            && (action == Action.createInstance || action == Action.updateInstance
                                    || action == Action.addRelationships || action == Action.removeRelationships
                                    || action == Action.scaleOut))) {
                throw new ValidationException("modelVersion");
            }

            if (empty(modelInfo.getModelVersion())
                    && (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action == Action.recreateInstance)) {
                throw new ValidationException("modelVersion", true);
            }

            // is required for serviceInstance delete macro when aLaCarte=false in v4
            if (reqVersion >= 4 && empty(modelInfo.getModelVersionId()) && (((aLaCarteFlag != null && !aLaCarteFlag)
                    && requestScope.equalsIgnoreCase(ModelType.service.name()) && action == Action.deleteInstance)
                    || (action == Action.createInstance || action == Action.updateInstance
                            || action == Action.enablePort || action == Action.disablePort
                            || action == Action.addRelationships || action == Action.removeRelationships
                            || (requestScope.equalsIgnoreCase(ModelType.configuration.name())
                                    && (action == Action.activateInstance || action == Action.deactivateInstance))))) {
                throw new ValidationException("modelVersionId");
            }
            if (empty(modelInfo.getModelVersionId())
                    && (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action == Action.recreateInstance)) {
                throw new ValidationException("modelVersionId", true);
            }
            if (empty(modelInfo.getModelVersionId())
                    && (requestScope.equalsIgnoreCase(ModelType.vfModule.name()) && action == Action.scaleOut)) {
                throw new ValidationException("modelVersionId");
            }

            if (requestScope.equalsIgnoreCase(ModelType.vnf.name()) && action != Action.deleteInstance
                    && empty(modelInfo.getModelCustomizationName())) {
                if (!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId())) {
                    throw new ValidationException("modelCustomizationId or modelCustomizationName");
                }
            }

            if (reqVersion >= 4 && (!UUIDChecker.isValidUUID(modelInfo.getModelCustomizationId()))
                    && (requestScope.equalsIgnoreCase(ModelType.network.name())
                            || requestScope.equalsIgnoreCase(ModelType.configuration.name()))
                    && (action == Action.updateInstance || action == Action.createInstance)) {
                throw new ValidationException("modelCustomizationId");
            }
            if (empty(modelInfo.getModelCustomizationId()) && requestScope.equalsIgnoreCase(ModelType.vfModule.name())
                    && action == Action.scaleOut && !(requestParameters.getTestApi() == TestApi.VNF_API.name()
                            && requestParameters.isUsePreload() == true)) {
                throw new ValidationException("modelCustomizationId");
            }
        } else {
            if (empty(modelInfo.getModelVersionId()) && action == Action.createInstance) {
                throw new ValidationException("modelVersionId", true);
            }
        }
        return info;
    }
}
