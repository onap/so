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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.*;

@Component
public class ExecuteBuildingBlockBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteBuildingBlockBuilder.class);

    private static final String VNF = "Vnf";
    private static final String PNF = "Pnf";
    private static final String VFMODULE = "VfModule";
    private static final String NETWORK = "Network";
    private static final String HEALTH_CHECK = "HealthCheckBB";
    private static final String UPGRADE_CNF = "UpgradeVfModuleBB";

    protected List<ExecuteBuildingBlock> buildExecuteBuildingBlockList(List<OrchestrationFlow> orchFlows,
            List<Resource> originalResourceList, String requestId, String apiVersion, String resourceId,
            String requestAction, String vnfType, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, boolean replaceVnf) {
        List<Resource> resourceList = getOnlyRootResourceList(originalResourceList);

        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        boolean ascendingOrder = requestAction.equals(CREATE_INSTANCE) || requestAction.equals(ASSIGN_INSTANCE)
                || requestAction.equals(ACTIVATE_INSTANCE);

        ExecutionPlan plan = ExecutionPlan.build(resourceList, ascendingOrder);

        logger.info("Orchestration Flows");
        for (OrchestrationFlow orchFlow : orchFlows) {
            String flowDetails = new ToStringBuilder(this).append("id", orchFlow.getId())
                    .append("action", orchFlow.getAction()).append("sequenceNumber", orchFlow.getSequenceNumber())
                    .append("flowName", orchFlow.getFlowName()).append("flowVersion", orchFlow.getFlowVersion())
                    .append("bpmnAction", orchFlow.getBpmnAction()).append("bpmnScope", orchFlow.getBpmnScope())
                    .toString();
            logger.info("Flow: {}", flowDetails);
            buildExecuteBuildingBlockListPlan(orchFlow, plan, requestId, apiVersion, resourceId, requestAction, vnfType,
                    workflowResourceIds, requestDetails, replaceVnf);
        }

        plan.flushBlocksFromCache(flowsToExecute);

        return flowsToExecute;
    }

    protected void buildExecuteBuildingBlockListPlan(OrchestrationFlow flow, ExecutionPlan plan, String requestId,
            String apiVersion, String resourceId, String requestAction, String vnfType,
            WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails, boolean replaceVnf) {

        List<ExecuteBuildingBlock> mainFlows = buildExecuteBuildingBlockListRaw(flow, plan.getResource(), requestId,
                apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, replaceVnf);

        plan.pushBlockToCache(mainFlows);

        for (ExecutionGroup nestedGroup : plan.getNestedExecutions()) {
            for (ExecutionPlan nestedPlan : nestedGroup.getNestedExecutions()) {
                buildExecuteBuildingBlockListPlan(flow, nestedPlan, requestId, apiVersion, resourceId, requestAction,
                        vnfType, workflowResourceIds, requestDetails, replaceVnf);
            }
            if (nestedGroup.getCacheSize() > 0)
                plan.changeCurrentGroup(nestedGroup);
        }
    }

    private List<ExecuteBuildingBlock> buildExecuteBuildingBlockListRaw(OrchestrationFlow orchFlow, Resource resource,
            String requestId, String apiVersion, String resourceId, String requestAction, String vnfType,
            WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails, boolean replaceVnf) {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        if (orchFlow.getFlowName().contains(CHILD_SERVICE)) {
            if (WorkflowType.SERVICE.equals(resource.getResourceType()) && resource.hasParent()) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.SERVICE, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
            }
        } else if (orchFlow.getFlowName().contains(SERVICE) || (orchFlow.getFlowName().contains(CONTROLLER)
                && (SERVICE).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
            if (!replaceVnf) {
                workflowResourceIds.setServiceInstanceId(resourceId);
            }
            if (!resource.hasParent()) {
                addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.SERVICE, orchFlow, requestId,
                        apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false,
                        false);
            }
        } else if (orchFlow.getFlowName().contains(VNF)
                || (orchFlow.getFlowName().contains(CONTROLLER) && (VNF).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VNF, orchFlow, requestId, apiVersion,
                    resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if ((orchFlow.getFlowName().equalsIgnoreCase(HEALTH_CHECK))
                && (VNF).equalsIgnoreCase(orchFlow.getBpmnScope())) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VNF, orchFlow, requestId, apiVersion,
                    resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if ((orchFlow.getFlowName().equalsIgnoreCase(UPGRADE_CNF))
                && (VNF).equalsIgnoreCase(orchFlow.getBpmnScope())) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VNF, orchFlow, requestId, apiVersion,
                    resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if (orchFlow.getFlowName().contains(PNF)
                || (orchFlow.getFlowName().contains(CONTROLLER) && (PNF).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.PNF, orchFlow, requestId, apiVersion,
                    resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if (orchFlow.getFlowName().contains(NETWORK) && !orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.NETWORK, orchFlow, requestId,
                    apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VIRTUAL_LINK, orchFlow, requestId,
                    apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, true, false);
        } else if (orchFlow.getFlowName().contains(VFMODULE) || (orchFlow.getFlowName().contains(CONTROLLER)
                && (VFMODULE).equalsIgnoreCase(orchFlow.getBpmnScope()))) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VFMODULE, orchFlow, requestId,
                    apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if (orchFlow.getFlowName().contains(VOLUMEGROUP)) {
            if (requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                    || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS)) {
                logger.debug("Replacing workflow resource id by volume group id");
                resourceId = workflowResourceIds.getVolumeGroupId();
            }
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.VOLUMEGROUP, orchFlow, requestId,
                    apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, false);
        } else if (orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.NETWORKCOLLECTION, orchFlow,
                    requestId, apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails,
                    false, false);
        } else if (orchFlow.getFlowName().contains(CONFIGURATION)) {
            addBuildingBlockToExecuteBBList(flowsToExecute, resource, WorkflowType.CONFIGURATION, orchFlow, requestId,
                    apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, false, true);
        } else {
            flowsToExecute
                    .add(buildExecuteBuildingBlock(orchFlow, requestId, null, apiVersion, resourceId, requestAction,
                            false, vnfType, workflowResourceIds, requestDetails, false, null, null, false, null));
        }
        return flowsToExecute;
    }

    protected List<Resource> getOnlyRootResourceList(List<Resource> resourceList) {
        return resourceList.stream().filter(x -> countResourceOnTheResourceList(x, resourceList) == 1)
                .collect(Collectors.toList());
    }

    protected int countResourceOnTheResourceList(Resource resource, List<Resource> resourceList) {
        int count = resourceList.stream()
                .mapToInt(x -> (x.equals(resource) ? 1 : 0) + countResourceOnTheResourceList(resource, x.getChildren()))
                .reduce(0, Integer::sum);
        return count;
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

    private void addBuildingBlockToExecuteBBList(List<ExecuteBuildingBlock> flowsToExecute, Resource resource,
            WorkflowType workflowType, OrchestrationFlow orchFlow, String requestId, String apiVersion,
            String resourceId, String requestAction, String vnfType, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, boolean isVirtualLink, boolean isConfiguration) {

        if (resource == null || !resource.getResourceType().equals(workflowType))
            return;
        flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resource, apiVersion, resourceId,
                requestAction, false, vnfType, workflowResourceIds, requestDetails, isVirtualLink,
                resource.getVirtualLinkKey(), null, isConfiguration, null));
    }

    protected ConfigurationResourceKeys getConfigurationResourceKeys(Resource resource, String vnfcName) {
        ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
        Optional.ofNullable(vnfcName).ifPresent(configurationResourceKeys::setVnfcName);
        configurationResourceKeys.setCvnfcCustomizationUUID(resource.getCvnfModuleCustomizationId());
        configurationResourceKeys.setVfModuleCustomizationUUID(resource.getVfModuleCustomizationId());
        configurationResourceKeys.setVnfResourceCustomizationUUID(resource.getVnfCustomizationId());
        return configurationResourceKeys;
    }
}
