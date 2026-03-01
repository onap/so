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

import java.util.Map;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.utils.UUIDChecker;

public class InstanceIdMapValidation implements ValidationRule {

    private static final String Service_InstanceId = "serviceInstanceId";
    private static final String Vnf_InstanceId = "vnfInstanceId";
    private static final String PNF_NAME = "pnfName";
    private static final String vfModule_InstanceId = "vfModuleInstanceId";

    private static final String volume_Group_InstanceId = "volumeGroupInstanceId";
    private static final String Network_Instance_Id = "networkInstanceId";
    private static final String Configuration_Instance_Id = "configurationInstanceId";

    @Override
    public ValidationInformation validate(ValidationInformation info) throws ValidationException {
        Map<String, String> instanceIdMap = info.getInstanceIdMap();
        ServiceInstancesRequest sir = info.getSir();
        if (instanceIdMap != null) {
            if (instanceIdMap.get(Service_InstanceId) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(Service_InstanceId))) {
                    throw new ValidationException(Service_InstanceId, true);
                }
                sir.setServiceInstanceId(instanceIdMap.get(Service_InstanceId));
            }

            if (instanceIdMap.get(Vnf_InstanceId) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(Vnf_InstanceId))) {
                    throw new ValidationException(Vnf_InstanceId, true);
                }
                sir.setVnfInstanceId(instanceIdMap.get(Vnf_InstanceId));
            }

            if (instanceIdMap.get(vfModule_InstanceId) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(vfModule_InstanceId))) {
                    throw new ValidationException(vfModule_InstanceId, true);
                }
                sir.setVfModuleInstanceId(instanceIdMap.get(vfModule_InstanceId));
            }

            if (instanceIdMap.get(volume_Group_InstanceId) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(volume_Group_InstanceId))) {
                    throw new ValidationException(volume_Group_InstanceId, true);
                }
                sir.setVolumeGroupInstanceId(instanceIdMap.get(volume_Group_InstanceId));
            }

            if (instanceIdMap.get(Network_Instance_Id) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(Network_Instance_Id))) {
                    throw new ValidationException(Network_Instance_Id, true);
                }
                sir.setNetworkInstanceId(instanceIdMap.get(Network_Instance_Id));
            }

            if (instanceIdMap.get(Configuration_Instance_Id) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(Configuration_Instance_Id))) {
                    throw new ValidationException(Configuration_Instance_Id, true);
                }
                sir.setConfigurationId(instanceIdMap.get(Configuration_Instance_Id));
            }

            if (instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID) != null) {
                if (!UUIDChecker.isValidUUID(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID))) {
                    throw new ValidationException(CommonConstants.INSTANCE_GROUP_INSTANCE_ID, true);
                }
                sir.setInstanceGroupId(instanceIdMap.get(CommonConstants.INSTANCE_GROUP_INSTANCE_ID));
            }

            if (instanceIdMap.get(PNF_NAME) != null) {
                sir.setPnfName(instanceIdMap.get(PNF_NAME));
            }
        }
        return info;
    }
}
