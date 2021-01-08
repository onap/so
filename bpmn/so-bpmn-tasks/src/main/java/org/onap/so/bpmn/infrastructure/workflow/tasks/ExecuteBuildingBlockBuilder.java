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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.ASSIGNINSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CONFIGURATION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CONTROLLER;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.NETWORKCOLLECTION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCERETAINASSIGNMENTS;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.VOLUMEGROUP;

@Component
public class ExecuteBuildingBlockBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteBuildingBlockBuilder.class);

    private static final String VNF = "Vnf";
    private static final String PNF = "Pnf";
    private static final String VFMODULE = "VfModule";
    private static final String NETWORK = "Network";

    protected List<ExecuteBuildingBlock> buildExecuteBuildingBlockList(List<OrchestrationFlow> orchFlows,
            List<Resource> resourceList, String requestId, String apiVersion, String resourceId, String requestAction,
            String vnfType, WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails,
            boolean replaceVnf) {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        for (OrchestrationFlow orchFlow : orchFlows) {
            if (orchFlow.getFlowName().contains(SERVICE)) {
                if (!replaceVnf) {
                    workflowResourceIds.setServiceInstanceId(resourceId);
                }
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.SERVICE, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
            } else if (orchFlow.getFlowName().contains(VNF) || (orchFlow.getFlowName().contains(CONTROLLER)
                    && (VNF).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.VNF, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
            } else if (orchFlow.getFlowName().contains(PNF) || (orchFlow.getFlowName().contains(CONTROLLER)
                    && (PNF).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.PNF, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
            } else if (orchFlow.getFlowName().contains(NETWORK)
                    && !orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.NETWORK, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.VIRTUAL_LINK, orchFlow,
                        requestId, apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails,
                        true, false);
            } else if (orchFlow.getFlowName().contains(VFMODULE) || (orchFlow.getFlowName().contains(CONTROLLER)
                    && (VFMODULE).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
                List<Resource> vfModuleResourcesSorted;
                if (requestAction.equals(CREATE_INSTANCE) || requestAction.equals(ASSIGNINSTANCE)
                        || requestAction.equals("activateInstance")) {
                    vfModuleResourcesSorted = sortVfModulesByBaseFirst(resourceList.stream()
                            .filter(x -> WorkflowType.VFMODULE == x.getResourceType()).collect(Collectors.toList()));
                } else {
                    vfModuleResourcesSorted = sortVfModulesByBaseLast(resourceList.stream()
                            .filter(x -> WorkflowType.VFMODULE == x.getResourceType()).collect(Collectors.toList()));
                }
                for (Resource resource : vfModuleResourcesSorted) {
                    flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resource, apiVersion, resourceId,
                            requestAction, false, vnfType, workflowResourceIds, requestDetails, false, null, null,
                            false, null));
                }
            } else if (orchFlow.getFlowName().contains(VOLUMEGROUP)) {
                if (requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                        || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS)) {
                    logger.debug("Replacing workflow resource id by volume group id");
                    resourceId = workflowResourceIds.getVolumeGroupId();
                }
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.VOLUMEGROUP, orchFlow,
                        requestId, apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails,
                        false, false);
            } else if (orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.NETWORKCOLLECTION, orchFlow,
                        requestId, apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails,
                        false, false);
            } else if (orchFlow.getFlowName().contains(CONFIGURATION)) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resourceList, WorkflowType.CONFIGURATION, orchFlow,
                        requestId, apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails,
                        false, true);
            } else {
                flowsToExecute
                        .add(buildExecuteBuildingBlock(orchFlow, requestId, null, apiVersion, resourceId, requestAction,
                                false, vnfType, workflowResourceIds, requestDetails, false, null, null, false, null));
            }
        }
        return flowsToExecute;
    }

    protected ExecuteBuildingBlock buildExecuteBuildingBlock(OrchestrationFlow orchFlow, String requestId,
            Resource resource, String apiVersion, String resourceId, String requestAction, boolean aLaCarte,
            String vnfType, WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails,
            boolean isVirtualLink, String virtualLinkKey, String vnfcName, boolean isConfiguration,
            ReplaceInstanceRelatedInformation replaceInfo) {

        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName(orchFlow.getFlowName()).setMsoId(UUID.randomUUID().toString())
                        .setIsVirtualLink(isVirtualLink).setVirtualLinkKey(virtualLinkKey)
                        .setKey(Optional.ofNullable(resource).map(Resource::getResourceId).orElse(""));
        Optional.ofNullable(orchFlow.getBpmnAction()).ifPresent(buildingBlock::setBpmnAction);
        Optional.ofNullable(orchFlow.getBpmnScope()).ifPresent(buildingBlock::setBpmnScope);
        String oldVolumeGroupName = "";
        if (replaceInfo != null) {
            oldVolumeGroupName = replaceInfo.getOldVolumeGroupName();
        }
        if (resource != null
                && (orchFlow.getFlowName().contains(VOLUMEGROUP) && (requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                        || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS)))) {
            logger.debug("Setting resourceId to volume group id for volume group flow on replace");
            resourceId = workflowResourceIds.getVolumeGroupId();
        }

        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock().setApiVersion(apiVersion)
                .setaLaCarte(aLaCarte).setRequestAction(requestAction).setResourceId(resourceId).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestId(requestId).setBuildingBlock(buildingBlock)
                .setRequestDetails(requestDetails).setOldVolumeGroupName(oldVolumeGroupName);

        if (resource != null && (isConfiguration || resource.getResourceType().equals(WorkflowType.CONFIGURATION))) {
            ConfigurationResourceKeys configurationResourceKeys = getConfigurationResourceKeys(resource, vnfcName);
            executeBuildingBlock.setConfigurationResourceKeys(configurationResourceKeys);
        }
        return executeBuildingBlock;
    }

    protected List<Resource> sortVfModulesByBaseFirst(List<Resource> vfModuleResources) {
        int count = 0;
        for (Resource resource : vfModuleResources) {
            if (resource.isBaseVfModule()) {
                Collections.swap(vfModuleResources, 0, count);
                break;
            }
            count++;
        }
        return vfModuleResources;
    }

    protected List<Resource> sortVfModulesByBaseLast(List<Resource> vfModuleResources) {
        int count = 0;
        for (Resource resource : vfModuleResources) {
            if (resource.isBaseVfModule()) {
                Collections.swap(vfModuleResources, vfModuleResources.size() - 1, count);
                break;
            }
            count++;
        }
        return vfModuleResources;
    }

    private void addBuildingBlockToExecuteBBList(List<ExecuteBuildingBlock> flowsToExecute, List<Resource> resourceList,
            WorkflowType workflowType, OrchestrationFlow orchFlow, String requestId, String apiVersion,
            String resourceId, String requestAction, String vnfType, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, boolean isVirtualLink, boolean isConfiguration) {

        resourceList.stream().filter(resource -> resource.getResourceType().equals(workflowType))
                .forEach(resource -> flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resource,
                        apiVersion, resourceId, requestAction, false, vnfType, workflowResourceIds, requestDetails,
                        isVirtualLink, resource.getVirtualLinkKey(), null, isConfiguration, null)));
    }

    private ConfigurationResourceKeys getConfigurationResourceKeys(Resource resource, String vnfcName) {
        ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
        Optional.ofNullable(vnfcName).ifPresent(configurationResourceKeys::setVnfcName);
        configurationResourceKeys.setCvnfcCustomizationUUID(resource.getCvnfModuleCustomizationId());
        configurationResourceKeys.setVfModuleCustomizationUUID(resource.getVfModuleCustomizationId());
        configurationResourceKeys.setVnfResourceCustomizationUUID(resource.getVnfCustomizationId());
        return configurationResourceKeys;
    }


}
