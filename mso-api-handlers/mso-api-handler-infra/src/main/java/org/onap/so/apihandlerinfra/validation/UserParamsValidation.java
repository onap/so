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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.onap.so.apihandlerinfra.Action;
import org.onap.so.apihandlerinfra.Actions;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;

public class UserParamsValidation implements ValidationRule {
    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        Service validate = info.getUserParams();
        if (validate.getModelInfo() == null) {
            throw new ValidationException("modelInfo in userParams", true);
        } else if (validate.getModelInfo().getModelType() == null) {
            throw new ValidationException("modelType in userParams service modelInfo", true);
        } else if (validate.getModelInfo().getModelVersionId() == null) {
            throw new ValidationException("modelVersionId in userParams service modelInfo", true);
        }
        modelInfoValidation(info.getSir().getRequestDetails().getModelInfo(), validate.getModelInfo());
        if (validate.getInstanceName() != null && info.getRequestInfo().getInstanceName() != null) {
            instanceNameValidation(info, validate);
        }

        Actions action = info.getAction();
        Map<String, Set<String>> vnfCustomIdToInstanceNames = new HashMap<>();
        Map<String, Set<String>> vfModuleCustomIdToInstanceNames = new HashMap<>();
        for (Vnfs vnf : validate.getResources().getVnfs()) {
            if (vnf.getModelInfo() == null) {
                throw new ValidationException("modelInfo in userParams vnf resources", true);
            } else if (vnf.getModelInfo().getModelCustomizationId() == null) {
                throw new ValidationException("modelCustomizationId in userParams vnf resources", true);
            } else if (vnf.getModelInfo().getModelVersionId() == null) {
                throw new ValidationException("modelVersionId in userParams vnf resources", true);
            }
            if (vnf.getCloudConfiguration() == null) {
                throw new ValidationException("cloudConfiguration in userParams vnf resources", true);
            }
            if (action == Action.createInstance || action == Action.assignInstance) {
                if (vnf.getPlatform() == null) {
                    throw new ValidationException("platform in userParams vnf resources", true);
                }
                if (vnf.getProductFamilyId() == null) {
                    throw new ValidationException("productFamilyId in userParams vnf resources", true);
                }
            }
            if (vnf.getPlatform() != null && vnf.getPlatform().getPlatformName() == null) {
                throw new ValidationException("platformName in userParams vnf resources", true);
            }

            String vnfCustomizationId = vnf.getModelInfo().getModelCustomizationId();
            vnfCustomIdToInstanceNames.putIfAbsent(vnfCustomizationId, new HashSet<>());
            String vnfInstanceName = StringUtils.defaultString(vnf.getInstanceName());
            Set<String> vnfVisitedInstanceNames = vnfCustomIdToInstanceNames.get(vnfCustomizationId);
            if (!vnfVisitedInstanceNames.add(vnfInstanceName)) {
                throw new ValidationException(
                        "instanceName: same instanceName with same modelCustomizationId in userParams vnf resources",
                        true);
            }
            if (vnf.getVfModules().isEmpty()) {
                throw new ValidationException("vfModules in userParams vnf resources", true);
            }

            for (VfModules vfModule : vnf.getVfModules()) {
                if (vfModule.getModelInfo() == null) {
                    throw new ValidationException("modelInfo in userParams vfModules resources", true);
                } else if (vfModule.getModelInfo().getModelCustomizationId() == null) {
                    throw new ValidationException("modelCustomizationId in userParams vfModule resources", true);
                } else if (vfModule.getModelInfo().getModelVersionId() == null) {
                    throw new ValidationException("modelVersionId in userParams vfModule resources", true);
                }

                String vfModulecustomizationId = vfModule.getModelInfo().getModelCustomizationId();
                vfModuleCustomIdToInstanceNames.putIfAbsent(vfModulecustomizationId, new HashSet<>());
                String vfModuleInstanceName = StringUtils.defaultString(vfModule.getInstanceName());
                Set<String> vfModuleVisitedInstanceNames = vfModuleCustomIdToInstanceNames.get(vfModulecustomizationId);
                if (!vfModuleVisitedInstanceNames.add(vfModuleInstanceName)) {
                    throw new ValidationException(
                            "instanceName: same instanceName with same modelCustomizationId in userParams vfModule resources",
                            true);
                }
            }
        }
        validateDuplicateInstanceNames(vnfCustomIdToInstanceNames, "vnf");
        validateDuplicateInstanceNames(vfModuleCustomIdToInstanceNames, "vfModule");

        Map<String, Set<String>> pnfCustomIdToInstanceNames = new HashMap<>();

        for (Pnfs pnf : validate.getResources().getPnfs()) {
            if (pnf.getModelInfo() == null) {
                throw new ValidationException("modelInfo in userParams pnf resources", true);
            } else if (pnf.getModelInfo().getModelCustomizationId() == null) {
                throw new ValidationException("modelCustomizationId in userParams pnf resources", true);
            } else if (pnf.getModelInfo().getModelVersionId() == null) {
                throw new ValidationException("modelVersionId in userParams pnf resources", true);
            }
            String pnfCustomizationId = pnf.getModelInfo().getModelCustomizationId();
            pnfCustomIdToInstanceNames.putIfAbsent(pnfCustomizationId, new HashSet<>());
            String pnfInstanceName = StringUtils.defaultString(pnf.getInstanceName());
            Set<String> pnfVisitedInstanceNames = pnfCustomIdToInstanceNames.get(pnfCustomizationId);
            if (!pnfVisitedInstanceNames.add(pnfInstanceName)) {
                throw new ValidationException(
                        "instanceName: same instanceName with same modelCustomizationId in userParams pnf resources",
                        true);
            }
        }

        validateDuplicateInstanceNames(pnfCustomIdToInstanceNames, "pnf");

        List<Networks> validateNetworks = new ArrayList<>();
        validateNetworks = validate.getResources().getNetworks();
        if (validateNetworks != null) {
            for (Networks networks : validateNetworks) {
                if (networks.getModelInfo() == null) {
                    throw new ValidationException("modelInfo in userParams network resources", true);
                } else if (networks.getModelInfo().getModelCustomizationId() == null) {
                    throw new ValidationException("modelCustomizationId in userParams network resources", true);
                } else if (networks.getModelInfo().getModelVersionId() == null) {
                    throw new ValidationException("modelVersionId in userParams network resources", true);
                }
                if (networks.getCloudConfiguration() == null) {
                    throw new ValidationException("cloudConfiguration in userParams network resources", true);
                }
            }
        }
        return info;
    }

    public void instanceNameValidation(ValidationInformation info, Service validate) throws ValidationException {
        if (!info.getRequestInfo().getInstanceName().equals(validate.getInstanceName())) {
            throw new ValidationException("instanceName in requestInfo", "instanceName in userParams service");
        }
    }

    public void modelInfoValidation(ModelInfo info, ModelInfo userParamInfo) throws ValidationException {
        if (!info.getModelType().equals(userParamInfo.getModelType())) {
            throw new ValidationException("modelType in modelInfo", "modelType in userParams service");
        }
        if ((info.getModelInvariantId() != null && userParamInfo.getModelInvariantId() != null)
                && (!info.getModelInvariantId().equals(userParamInfo.getModelInvariantId()))) {
            throw new ValidationException("modelInvariantId in modelInfo", "modelInvariantId in userParams service");
        }
        if (!info.getModelVersionId().equals(userParamInfo.getModelVersionId())) {
            throw new ValidationException("modelVersionId in modelInfo", "modelVersionId in userParams service");
        }
        if ((info.getModelName() != null && userParamInfo.getModelName() != null)
                && (!info.getModelName().equals(userParamInfo.getModelName()))) {
            throw new ValidationException("modelName in modelInfo", "modelName in userParams service");
        }
        if ((info.getModelVersion() != null && userParamInfo.getModelVersion() != null)
                && (!info.getModelVersion().equals(userParamInfo.getModelVersion()))) {
            throw new ValidationException("modelVersion in modelInfo", "modelVersion in userParams service");
        }
        if ((info.getModelCustomizationId() != null && userParamInfo.getModelCustomizationId() != null)
                && (!info.getModelCustomizationId().equals(userParamInfo.getModelCustomizationId()))) {
            throw new ValidationException("modelCustomizationId in modelInfo",
                    "modelCustomizationId in userParams service");
        }
    }

    private void validateDuplicateInstanceNames(Map<String, Set<String>> duplicateValidator, String type)
            throws ValidationException {
        Set<String> allInstanceNames = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : duplicateValidator.entrySet()) {
            Set<String> instanceNames = entry.getValue();
            if (instanceNames.size() > 1 && instanceNames.contains(""))
                throw new ValidationException(String.format(
                        "instanceName: instanceName is missing or empty with same modelCustomizationId in userParams %s resources",
                        type), true);

            for (String instanceName : instanceNames) {
                if (!instanceName.isBlank() && !allInstanceNames.add(instanceName)) {
                    throw new ValidationException(String.format(
                            "instanceName: same instanceName but different modelCustomizationId (instanceName should be unique)  in userParams %s resources",
                            type), true);
                }
            }
        }
    }
}
