/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionExtractResourcesAAI;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;

@Component
public class VnfEBBLoader {

    private static final Logger logger = LoggerFactory.getLogger(VnfEBBLoader.class);

    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;
    @Autowired
    protected BBInputSetup bbInputSetup;
    @Autowired
    private WorkflowActionExtractResourcesAAI workflowActionUtils;
    @Autowired
    private ExceptionBuilder exceptionBuilder;


    public void traverseAAIVnf(DelegateExecution execution, List<Resource> resourceList, String serviceId, String vnfId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                    bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            resourceList.add(new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false));
            if (serviceInstanceMSO.getVnfs() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
                    if (vnf.getVnfId().equals(vnfId)) {
                        aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
                        resourceList.add(new Resource(WorkflowType.VNF, vnf.getVnfId(), false));
                        if (vnf.getVfModules() != null) {
                            for (VfModule vfModule : vnf.getVfModules()) {
                                aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
                                resourceList.add(new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false));
                                findConfigurationsInsideVfModule(execution, vnf.getVnfId(), vfModule.getVfModuleId(),
                                        resourceList, aaiResourceIds);
                            }
                        }
                        if (vnf.getVolumeGroups() != null) {
                            for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf
                                    .getVolumeGroups()) {
                                aaiResourceIds
                                        .add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                                resourceList.add(
                                        new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false));
                            }
                        }
                        break;
                    }
                }
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
            ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                    bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            resourceList.add(new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false));
            if (serviceInstanceMSO.getVnfs() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
                    if (vnf.getVnfId().equals(vnfId)) {
                        aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));

                        String vnfCustomizationUUID =
                                bbInputSetupUtils.getAAIGenericVnf(vnfId).getModelCustomizationId();
                        resourceList.add(new Resource(WorkflowType.VNF, vnfCustomizationUUID, false));

                        if (vnf.getVfModules() != null) {
                            for (VfModule vfModule : vnf.getVfModules()) {
                                aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
                                resourceList.add(new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false));
                                findConfigurationsInsideVfModule(execution, vnf.getVnfId(), vfModule.getVfModuleId(),
                                        resourceList, aaiResourceIds);
                            }
                        }
                        if (vnf.getVolumeGroups() != null) {
                            for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf
                                    .getVolumeGroups()) {
                                aaiResourceIds
                                        .add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                                resourceList.add(
                                        new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false));
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in customTraverseAAIVnf", ex);
            buildAndThrowException(execution,
                    "Could not find existing Vnf or related Instances to execute the request on.");
        }

    }

    private void findConfigurationsInsideVfModule(DelegateExecution execution, String vnfId, String vfModuleId,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            org.onap.aai.domain.yang.VfModule aaiVfModule = bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId);
            AAIResultWrapper vfModuleWrapper = new AAIResultWrapper(
                    new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiVfModule));
            Optional<Relationships> relationshipsOp;
            relationshipsOp = vfModuleWrapper.getRelationships();
            if (relationshipsOp.isPresent()) {
                relationshipsOp = workflowActionUtils.extractRelationshipsVnfc(relationshipsOp.get());
                if (relationshipsOp.isPresent()) {
                    Optional<Configuration> config =
                            workflowActionUtils.extractRelationshipsConfiguration(relationshipsOp.get());
                    if (config.isPresent()) {
                        aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, config.get().getConfigurationId()));
                        resourceList.add(
                                new Resource(WorkflowType.CONFIGURATION, config.get().getConfigurationId(), false));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in findConfigurationsInsideVfModule", ex);
            buildAndThrowException(execution, "Failed to find Configuration object from the vfModule.");
        }
    }

    private void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }
}
