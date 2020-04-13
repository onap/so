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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.VnfAdapterVfModuleResources;
import org.onap.so.client.orchestration.VnfAdapterVolumeGroupResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterDeleteTasks {

    private static final String VNFREST_REQUEST = "VNFREST_Request";

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;
    @Autowired
    private VnfAdapterVfModuleResources vnfAdapterVfModuleResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;

    public void deleteVolumeGroup(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);

            DeleteVolumeGroupRequest deleteVolumeGroupRequest = vnfAdapterVolumeGroupResources.deleteVolumeGroupRequest(
                    gBBInput.getRequestContext(), gBBInput.getCloudRegion(), serviceInstance, volumeGroup);
            execution.setVariable(VNFREST_REQUEST, deleteVolumeGroupRequest.toXmlString());
            execution.setVariable("deleteVolumeGroupRequest", "true");
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void deleteVfModule(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            DeleteVfModuleRequest deleteVfModuleRequest = vnfAdapterVfModuleResources.deleteVfModuleRequest(
                    gBBInput.getRequestContext(), gBBInput.getCloudRegion(), serviceInstance, genericVnf, vfModule);
            execution.setVariable(VNFREST_REQUEST, deleteVfModuleRequest.toXmlString());
            execution.setVariable("deleteVfModuleRequest", "true");
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
