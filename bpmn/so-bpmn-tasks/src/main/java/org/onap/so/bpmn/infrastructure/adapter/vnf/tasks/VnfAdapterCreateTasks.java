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

import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.VnfAdapterVfModuleResources;
import org.onap.so.client.orchestration.VnfAdapterVolumeGroupResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterCreateTasks {
    private static final Logger logger = LoggerFactory.getLogger(VnfAdapterCreateTasks.class);
    public static final String SDNCQUERY_RESPONSE = "SDNCQueryResponse_";
    private static final String VNFREST_REQUEST = "VNFREST_Request";

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;
    @Autowired
    private VnfAdapterVfModuleResources vnfAdapterVfModuleResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;

    /**
     * This method is used for creating the request for the VolumeGroup.
     *
     * @param execution
     * @return
     */
    public void createVolumeGroupRequest(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0);

            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            VfModule vfModule;
            String sdncVfModuleQueryResponse = null;
            try {
                vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                if (vfModule.getSelflink() != null && !vfModule.getSelflink().isEmpty()) {
                    sdncVfModuleQueryResponse = execution.getVariable(SDNCQUERY_RESPONSE + vfModule.getVfModuleId());
                } else {
                    throw new Exception("Vf Module " + vfModule.getVfModuleId()
                            + " exists in gBuildingBlock but does not have a selflink value");
                }
            } catch (BBObjectNotFoundException bbException) {
                logger.error("Exception occurred", bbException);
                // If there is not a vf module in the general building block (in aLaCarte case),
                // we will not retrieve
                // the SDNCQueryResponse and proceed as normal without throwing an error
            }

            CreateVolumeGroupRequest createVolumeGroupRequest = vnfAdapterVolumeGroupResources.createVolumeGroupRequest(
                    gBBInput.getRequestContext(), gBBInput.getCloudRegion(), gBBInput.getOrchContext(), serviceInstance,
                    genericVnf, volumeGroup, sdncVfModuleQueryResponse);
            execution.setVariable(VNFREST_REQUEST, createVolumeGroupRequest.toXmlString());
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }


    /**
     * This method is used for creating the request for the VfModule.
     *
     * @param execution
     * @return
     */
    public void createVfModule(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            ServiceInstance serviceInstance =
                    gBBInput.getCustomer().getServiceSubscription().getServiceInstances().get(0);
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            VolumeGroup volumeGroup = null;
            try {
                volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            } catch (BBObjectNotFoundException bbException) {
                logger.info("Volume Group not found in GBB. ");
            }
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            RequestContext requestContext = gBBInput.getRequestContext();
            OrchestrationContext orchestrationContext = gBBInput.getOrchContext();
            String sdncVfModuleQueryResponse = execution.getVariable(SDNCQUERY_RESPONSE + vfModule.getVfModuleId());
            String sdncVnfQueryResponse = execution.getVariable(SDNCQUERY_RESPONSE + genericVnf.getVnfId());

            CreateVfModuleRequest createVfModuleRequest = vnfAdapterVfModuleResources.createVfModuleRequest(
                    requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, vfModule,
                    volumeGroup, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
            execution.setVariable(VNFREST_REQUEST, createVfModuleRequest.toXmlString());
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

}
