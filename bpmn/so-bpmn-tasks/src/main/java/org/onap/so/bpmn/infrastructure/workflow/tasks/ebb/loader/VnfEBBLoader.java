/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
 * ================================================================================
 * Modifications Copyright (c) 2021 Orange
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;

@Component
public class VnfEBBLoader {

    private static final Logger logger = LoggerFactory.getLogger(VnfEBBLoader.class);

    private final BBInputSetupUtils bbInputSetupUtils;
    private final BBInputSetup bbInputSetup;
    private final WorkflowActionExtractResourcesAAI workflowActionUtils;
    private final ExceptionBuilder exceptionBuilder;

    VnfEBBLoader(BBInputSetupUtils bbInputSetupUtils, BBInputSetup bbInputSetup,
            @Qualifier("WorkflowActionExtractResourcesAAI") WorkflowActionExtractResourcesAAI workflowActionUtils,
            ExceptionBuilder exceptionBuilder) {
        this.bbInputSetupUtils = bbInputSetupUtils;
        this.bbInputSetup = bbInputSetup;
        this.workflowActionUtils = workflowActionUtils;
        this.exceptionBuilder = exceptionBuilder;
    }


    public void traverseAAIVnf(DelegateExecution execution, List<Resource> resourceList, String serviceId, String vnfId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI =
                    bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
            ServiceInstance serviceInstanceMSO = bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            Resource serviceResource =
                    new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false, null);
            resourceList.add(serviceResource);
            if (serviceInstanceMSO.getVnfs() != null) {
                findVnfWithGivenId(serviceInstanceMSO, vnfId, aaiResourceIds, resourceList, serviceResource, execution);
            }
        } catch (Exception ex) {
            logger.error("Exception in traverseAAIVnf", ex);
            buildAndThrowException(execution,
                    "Could not find existing Vnf or related Instances to execute the request on.");
        }
    }

    public void customTraverseAAIVnf(DelegateExecution execution, List<Resource> resourceList, String serviceId,
            String vnfId, List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI =
                    bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
            ServiceInstance serviceInstanceMSO = bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            Resource serviceResource =
                    new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false, null);
            resourceList.add(serviceResource);
            if (serviceInstanceMSO.getVnfs() != null) {
                findVnfWithGivenIdAndAddCustomizationUUID(serviceInstanceMSO, vnfId, aaiResourceIds, resourceList,
                        serviceResource, execution);
            }
        } catch (Exception ex) {
            logger.error("Exception in customTraverseAAIVnf", ex);
            buildAndThrowException(execution,
                    "Could not find existing Vnf or related Instances to execute the request on.");
        }

    }

    private void findVnfWithGivenId(ServiceInstance serviceInstanceMSO, String vnfId,
            List<Pair<WorkflowType, String>> aaiResourceIds, List<Resource> resourceList, Resource serviceResource,
            DelegateExecution execution) {
        for (GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
            if (vnf.getVnfId().equals(vnfId)) {
                aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
                Resource vnfResource = new Resource(WorkflowType.VNF, vnf.getVnfId(), false, serviceResource);
                org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = bbInputSetupUtils.getAAIGenericVnf(vnfId);
                vnfResource.setModelCustomizationId(aaiGenericVnf.getModelCustomizationId());
                vnfResource.setModelVersionId(aaiGenericVnf.getModelVersionId());
                resourceList.add(vnfResource);
                processVfModules(vnf, aaiResourceIds, resourceList, vnfResource, execution);
                processVolumeGroups(vnf, aaiResourceIds, resourceList, vnfResource);
                break;
            }
        }
    }

    private void findVnfWithGivenIdAndAddCustomizationUUID(ServiceInstance serviceInstanceMSO, String vnfId,
            List<Pair<WorkflowType, String>> aaiResourceIds, List<Resource> resourceList, Resource serviceResource,
            DelegateExecution execution) {
        for (GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
            if (vnf.getVnfId().equals(vnfId)) {
                aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
                org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = bbInputSetupUtils.getAAIGenericVnf(vnfId);
                Resource vnfResource =
                        new Resource(WorkflowType.VNF, aaiGenericVnf.getModelCustomizationId(), false, serviceResource);
                vnfResource.setModelCustomizationId(aaiGenericVnf.getModelCustomizationId());
                vnfResource.setModelVersionId(aaiGenericVnf.getModelVersionId());
                resourceList.add(vnfResource);
                processVfModules(vnf, aaiResourceIds, resourceList, vnfResource, execution);
                processVolumeGroups(vnf, aaiResourceIds, resourceList, vnfResource);
                break;
            }
        }
    }

    private void findConfigurationsInsideVfModule(DelegateExecution execution,
            org.onap.aai.domain.yang.VfModule aaiVfModule, List<Resource> resourceList, Resource vfModuleResource,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            AAIResultWrapper vfModuleWrapper = new AAIResultWrapper(
                    new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiVfModule));
            Optional<Relationships> relationshipsOp;
            relationshipsOp = vfModuleWrapper.getRelationships();
            if (relationshipsOp.isPresent()) {
                relationshipsOp = workflowActionUtils.extractRelationshipsVnfc(relationshipsOp.get());
                addConfigToResources(relationshipsOp, resourceList, vfModuleResource, aaiResourceIds);
            }
        } catch (Exception ex) {
            logger.error("Exception in findConfigurationsInsideVfModule", ex);
            buildAndThrowException(execution, "Failed to find Configuration object from the vfModule.");
        }
    }

    private void processVfModules(GenericVnf vnf, List<Pair<WorkflowType, String>> aaiResourceIds,
            List<Resource> resourceList, Resource vnfResource, DelegateExecution execution) {
        if (vnf.getVfModules() != null) {
            for (VfModule vfModule : vnf.getVfModules()) {
                aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
                Resource vfModuleResource =
                        new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false, vnfResource);
                org.onap.aai.domain.yang.VfModule aaiVfModule =
                        bbInputSetupUtils.getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId());
                vfModuleResource.setModelInvariantId(aaiVfModule.getModelInvariantId());
                vfModuleResource.setModelCustomizationId(aaiVfModule.getModelCustomizationId());
                vfModuleResource.setBaseVfModule(aaiVfModule.isIsBaseVfModule());
                resourceList.add(vfModuleResource);
                findConfigurationsInsideVfModule(execution, aaiVfModule, resourceList, vfModuleResource,
                        aaiResourceIds);
            }
        }
    }

    private void processVolumeGroups(GenericVnf vnf, List<Pair<WorkflowType, String>> aaiResourceIds,
            List<Resource> resourceList, Resource vnfResource) {
        if (vnf.getVolumeGroups() != null) {
            for (VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                aaiResourceIds.add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                resourceList.add(
                        new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false, vnfResource));
            }
        }
    }

    private void addConfigToResources(Optional<Relationships> relationshipsOp, List<Resource> resourceList,
            Resource vfModuleResource, List<Pair<WorkflowType, String>> aaiResourceIds) {
        if (relationshipsOp.isPresent()) {
            Optional<Configuration> config =
                    workflowActionUtils.extractRelationshipsConfiguration(relationshipsOp.get());
            if (config.isPresent()) {
                aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, config.get().getConfigurationId()));
                resourceList.add(new Resource(WorkflowType.CONFIGURATION, config.get().getConfigurationId(), false,
                        vfModuleResource));
            }
        }
    }

    private void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }
}
