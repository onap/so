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

import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.IS_CHILD_PROCESS;
import static org.onap.so.bpmn.infrastructure.service.composition.ServiceCompositionConstants.PARENT_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.ASSIGN_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CONTROLLER;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.DELETE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.FABRIC_CONFIGURATION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.RECREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCERETAINASSIGNMENTS;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.UPDATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.HEALTH_CHECK;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.UPGRADE_CNF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aai.domain.yang.Vnfc;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aaiclient.client.aai.AAIObjectName;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.NetworkSliceSubnetEBBLoader;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.PnfEBBLoader;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.ServiceEBBLoader;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.VnfEBBLoader;
import org.onap.so.bpmn.infrastructure.workflow.tasks.excpetion.VnfcMultipleRelationshipException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.utils.WorkflowResourceIdsUtils;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIEntityNotFoundException;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.serviceinstancebeans.InstanceDirection;

@Component
public class WorkflowAction {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAction.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_INSTANCES = "serviceInstances";
    private static final String VF_MODULES = "vfModules";
    private static final String VNF_TYPE = "vnfType";
    private static final String CONFIGURATION = "Configuration";
    private static final String SUPPORTEDTYPES =
            "vnfs|pnfs|cnfs|vfModules|networks|networkCollections|volumeGroups|serviceInstances|instanceGroups|NetworkSliceSubnet";
    private static final String HOMINGSOLUTION = "Homing_Solution";
    private static final String SERVICE_TYPE_TRANSPORT = "TRANSPORT";
    private static final String SERVICE_TYPE_BONDING = "BONDING";
    private static final String CLOUD_OWNER = "DEFAULT";
    private static final String CREATENETWORKBB = "CreateNetworkBB";
    private static final String ACTIVATENETWORKBB = "ActivateNetworkBB";
    private static final String VOLUMEGROUP_DELETE_PATTERN = "(Un|De)(.*)Volume(.*)";
    private static final String VOLUMEGROUP_CREATE_PATTERN = "(A|C)(.*)Volume(.*)";
    private static final String DEFAULT_CLOUD_OWNER = "org.onap.so.cloud-owner";
    private static final String HOMING = "homing";

    @Autowired
    protected BBInputSetup bbInputSetup;
    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;
    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private Environment environment;
    @Autowired
    private AaiResourceIdValidator aaiResourceIdValidator;
    @Autowired
    private ExecuteBuildingBlockBuilder executeBuildingBlockBuilder;
    @Autowired
    private VnfEBBLoader vnfEBBLoader;
    @Autowired
    private PnfEBBLoader pnfEBBLoader;
    @Autowired
    private ServiceEBBLoader serviceEBBLoader;
    @Autowired
    private NetworkSliceSubnetEBBLoader networkSliceSubnetEBBLoader;

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    public void setBbInputSetup(BBInputSetup bbInputSetup) {
        this.bbInputSetup = bbInputSetup;
    }

    public void selectExecutionList(DelegateExecution execution) throws Exception {
        try {
            fillExecutionDefault(execution);
            final String bpmnRequest = (String) execution.getVariable(BBConstants.G_BPMN_REQUEST);
            ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);

            final String requestId = (String) execution.getVariable(BBConstants.G_REQUEST_ID);

            String uri = (String) execution.getVariable(BBConstants.G_URI);
            boolean isResume = isUriResume(uri);

            final boolean isALaCarte = (boolean) execution.getVariable(BBConstants.G_ALACARTE);
            Resource resource = getResource(bbInputSetupUtils, isResume, isALaCarte, uri, requestId);

            WorkflowResourceIds workflowResourceIds = populateResourceIdsFromApiHandler(execution);
            RequestDetails requestDetails = sIRequest.getRequestDetails();
            String requestAction = (String) execution.getVariable(BBConstants.G_ACTION);
            String resourceId = getResourceId(resource, requestAction, requestDetails, workflowResourceIds);
            WorkflowType resourceType = resource.getResourceType();

            String serviceInstanceId = getServiceInstanceId(execution, resourceId, resourceType);

            fillExecution(execution, requestDetails.getRequestInfo().getSuppressRollback(), resourceId, resourceType);
            List<ExecuteBuildingBlock> flowsToExecute;
            if (isRequestMacroServiceResume(isALaCarte, resourceType, requestAction, serviceInstanceId)) {
                String errorMessage = "Could not resume Macro flow. Error loading execution path.";
                flowsToExecute = loadExecuteBuildingBlocks(execution, requestId, errorMessage);
            } else if (isALaCarte && isResume) {
                String errorMessage =
                        "Could not resume request with request Id: " + requestId + ". No flowsToExecute was found";
                flowsToExecute = loadExecuteBuildingBlocks(execution, requestId, errorMessage);
            } else {
                String vnfType = (String) execution.getVariable(VNF_TYPE);
                String cloudOwner = getCloudOwner(requestDetails.getCloudConfiguration());
                List<OrchestrationFlow> orchFlows =
                        (List<OrchestrationFlow>) execution.getVariable(BBConstants.G_ORCHESTRATION_FLOW);
                final String apiVersion = (String) execution.getVariable(BBConstants.G_APIVERSION);
                final String serviceType =
                        Optional.ofNullable((String) execution.getVariable(BBConstants.G_SERVICE_TYPE)).orElse("");
                if (isALaCarte) {
                    flowsToExecute = loadExecuteBuildingBlocksForAlaCarte(orchFlows, execution, requestAction,
                            resourceType, cloudOwner, serviceType, sIRequest, requestId, workflowResourceIds,
                            requestDetails, resourceId, vnfType, apiVersion);
                } else {
                    flowsToExecute = loadExecuteBuildingBlocksForMacro(sIRequest, resourceType, requestAction,
                            execution, serviceInstanceId, resourceId, workflowResourceIds, orchFlows, cloudOwner,
                            serviceType, requestId, apiVersion, vnfType, requestDetails);
                }
            }
            // If the user set "Homing_Solution" to "none", disable homing, else if "Homing_Solution" is specified,
            // enable it.
            if (sIRequest.getRequestDetails().getRequestParameters() != null
                    && sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
                List<Map<String, Object>> userParams =
                        sIRequest.getRequestDetails().getRequestParameters().getUserParams();
                for (Map<String, Object> params : userParams) {
                    if (params.containsKey(HOMINGSOLUTION)) {
                        execution.setVariable(HOMING, !"none".equals(params.get(HOMINGSOLUTION)));
                    }
                }
            }

            if (CollectionUtils.isEmpty(flowsToExecute)) {
                throw new IllegalStateException("Macro did not come up with a valid execution path.");
            }

            List<String> flowNames = new ArrayList<>();
            logger.info("List of BuildingBlocks to execute:");

            flowsToExecute.forEach(ebb -> {
                logger.info(ebb.getBuildingBlock().getBpmnFlowName());
                flowNames.add(ebb.getBuildingBlock().getBpmnFlowName());
            });

            if (!isResume) {
                bbInputSetupUtils.persistFlowExecutionPath(requestId, flowsToExecute);
            }
            setExecutionVariables(execution, flowsToExecute, flowNames);

        } catch (Exception ex) {
            if (!(execution.hasVariable("WorkflowException")
                    || execution.hasVariable("WorkflowExceptionExceptionMessage"))) {
                buildAndThrowException(execution, "Exception while setting execution list. ", ex);
            } else {
                throw ex;
            }
        }
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocksForAlaCarte(List<OrchestrationFlow> orchFlows,
            DelegateExecution execution, String requestAction, WorkflowType resourceType, String cloudOwner,
            String serviceType, ServiceInstancesRequest sIRequest, String requestId,
            WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails, String resourceId, String vnfType,
            String apiVersion) throws Exception {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        if (orchFlows == null || orchFlows.isEmpty()) {
            orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, true, cloudOwner,
                    serviceType);
        }
        Resource resourceKey = getResourceKey(sIRequest, resourceType);

        ReplaceInstanceRelatedInformation replaceInfo = new ReplaceInstanceRelatedInformation();
        if ((requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS))
                && resourceType.equals(WorkflowType.VFMODULE)) {
            logger.debug("Build a BB list for replacing BB modules");
            ConfigBuildingBlocksDataObject cbbdo = createConfigBuildingBlocksDataObject(execution, sIRequest, requestId,
                    workflowResourceIds, requestDetails, requestAction, resourceId, vnfType, orchFlows, apiVersion,
                    resourceKey, replaceInfo);
            orchFlows = getVfModuleReplaceBuildingBlocks(cbbdo);

            createBuildingBlocksForOrchFlows(execution, sIRequest, requestId, workflowResourceIds, requestDetails,
                    requestAction, resourceId, flowsToExecute, vnfType, orchFlows, apiVersion, resourceKey,
                    replaceInfo);
        } else {
            if (isConfiguration(orchFlows) && !requestAction.equalsIgnoreCase(CREATE_INSTANCE)) {
                addConfigBuildingBlocksToFlowsToExecuteList(execution, sIRequest, requestId, workflowResourceIds,
                        requestDetails, requestAction, resourceId, flowsToExecute, vnfType, apiVersion, resourceKey,
                        replaceInfo, orchFlows);
            }
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().contains(FABRIC_CONFIGURATION))
                    .collect(Collectors.toList());

            for (OrchestrationFlow orchFlow : orchFlows) {
                ExecuteBuildingBlock ebb = executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, requestId,
                        resourceKey, apiVersion, resourceId, requestAction, true, vnfType, workflowResourceIds,
                        requestDetails, false, null, null, false, replaceInfo);
                flowsToExecute.add(ebb);
            }
        }
        return flowsToExecute;
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocksForMacro(ServiceInstancesRequest sIRequest,
            WorkflowType resourceType, String requestAction, DelegateExecution execution, String serviceInstanceId,
            String resourceId, WorkflowResourceIds workflowResourceIds, List<OrchestrationFlow> orchFlows,
            String cloudOwner, String serviceType, String requestId, String apiVersion, String vnfType,
            RequestDetails requestDetails) throws IOException, VrfBondingServiceException {
        List<ExecuteBuildingBlock> flowsToExecute;
        List<Resource> resourceList = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();

        if (resourceType == WorkflowType.SERVICE || isVNFCreate(resourceType, requestAction)
                || isPNFCreate(resourceType, requestAction)) {
            resourceList = serviceEBBLoader.getResourceListForService(sIRequest, requestAction, execution,
                    serviceInstanceId, resourceId, aaiResourceIds);
        } else if (resourceType == WorkflowType.NETWORK_SLICE_SUBNET) {
            resourceList = networkSliceSubnetEBBLoader.setNetworkSliceSubnetResource(resourceId);
        } else if (isPNFDelete(resourceType, requestAction)) {
            pnfEBBLoader.traverseAAIPnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(), resourceId,
                    aaiResourceIds);
        } else if (isPNFUpdate(resourceType, requestAction)) {
            pnfEBBLoader.traverseAAIPnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(), resourceId,
                    aaiResourceIds);
        } else if (resourceType == WorkflowType.VNF
                && (DELETE_INSTANCE.equalsIgnoreCase(requestAction) || REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                        || (RECREATE_INSTANCE.equalsIgnoreCase(requestAction)))) {
            vnfEBBLoader.traverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else if (resourceType == WorkflowType.VNF && UPDATE_INSTANCE.equalsIgnoreCase(requestAction)) {
            vnfEBBLoader.customTraverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else if (resourceType == WorkflowType.VNF && HEALTH_CHECK.equalsIgnoreCase(requestAction)) {
            vnfEBBLoader.customTraverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else if (resourceType == WorkflowType.VNF && UPGRADE_CNF.equalsIgnoreCase(requestAction)) {
            vnfEBBLoader.customTraverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else {
            buildAndThrowException(execution, "Current Macro Request is not supported");
        }
        StringBuilder foundObjects = new StringBuilder();
        for (WorkflowType type : WorkflowType.values()) {
            foundObjects.append(type).append(" - ")
                    .append((int) resourceList.stream().filter(x -> type.equals(x.getResourceType())).count())
                    .append("    ");
        }
        logger.info("Found {}", foundObjects);

        if (orchFlows == null || orchFlows.isEmpty()) {
            orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, false, cloudOwner,
                    serviceType);
        }
        boolean vnfReplace = false;
        if (resourceType.equals(WorkflowType.VNF) && (REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                || REPLACEINSTANCERETAINASSIGNMENTS.equalsIgnoreCase(requestAction))) {
            vnfReplace = true;
        }
        flowsToExecute = executeBuildingBlockBuilder.buildExecuteBuildingBlockList(orchFlows, resourceList, requestId,
                apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, vnfReplace);
        if (serviceEBBLoader.isNetworkCollectionInTheResourceList(resourceList)) {
            logger.info("Sorting for Vlan Tagging");
            flowsToExecute = sortExecutionPathByObjectForVlanTagging(flowsToExecute, requestAction);
        }
        logger.info("Building Block Execution Order");
        for (ExecuteBuildingBlock block : flowsToExecute) {
            Resource res = resourceList.stream()
                    .filter(resource -> resource.getResourceId() == block.getBuildingBlock().getKey()).findAny()
                    .orElse(null);
            String log = "Block: " + block.getBuildingBlock().getBpmnFlowName();
            if (res != null) {
                log += ", Resource: " + res.getResourceType() + "[" + res.getResourceId() + "]";
                log += ", Priority: " + res.getProcessingPriority();
                if (res.getResourceType() == WorkflowType.VFMODULE)
                    log += ", Base: " + res.isBaseVfModule();
            }
            if (block.getBuildingBlock().getBpmnScope() != null)
                log += ", Scope: " + block.getBuildingBlock().getBpmnScope();
            if (block.getBuildingBlock().getBpmnAction() != null)
                log += ", Action: " + block.getBuildingBlock().getBpmnAction();
            logger.info(log);
        }

        RelatedInstanceList[] instanceList = sIRequest.getRequestDetails().getRelatedInstanceList();
        execution.setVariable(IS_CHILD_PROCESS, Boolean.FALSE);
        if (instanceList != null) {
            for (RelatedInstanceList relatedInstanceList : instanceList) {
                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                if (InstanceDirection.source.equals(relatedInstance.getInstanceDirection())) {
                    execution.setVariable(IS_CHILD_PROCESS, Boolean.TRUE);
                    execution.setVariable(PARENT_CORRELATION_ID, requestDetails.getRequestInfo().getCorrelator());
                }
            }
        }

        // By default, enable homing at VNF level for CREATE_INSTANCE and ASSIGNINSTANCE
        if (resourceType == WorkflowType.SERVICE
                && (requestAction.equals(CREATE_INSTANCE) || requestAction.equals(ASSIGN_INSTANCE))
                && resourceList.stream().anyMatch(x -> WorkflowType.VNF.equals(x.getResourceType()))) {
            execution.setVariable(HOMING, true);
            execution.setVariable("calledHoming", false);
        }
        if (resourceType == WorkflowType.SERVICE && (requestAction.equalsIgnoreCase(ASSIGN_INSTANCE)
                || requestAction.equalsIgnoreCase(CREATE_INSTANCE))) {
            generateResourceIds(flowsToExecute, resourceList, serviceInstanceId);
        } else {
            updateResourceIdsFromAAITraversal(flowsToExecute, resourceList, aaiResourceIds, serviceInstanceId);
        }
        execution.setVariable("resources", resourceList);
        return flowsToExecute;
    }

    private boolean isVNFCreate(WorkflowType resourceType, String requestAction) {
        return resourceType == WorkflowType.VNF && CREATE_INSTANCE.equalsIgnoreCase(requestAction);
    }


    private boolean isPNFCreate(WorkflowType resourceType, String requestAction) {
        return resourceType == WorkflowType.PNF && CREATE_INSTANCE.equalsIgnoreCase(requestAction);
    }


    private boolean isPNFDelete(WorkflowType resourceType, String requestAction) {
        return resourceType == WorkflowType.PNF && DELETE_INSTANCE.equalsIgnoreCase(requestAction);
    }

    private boolean isPNFUpdate(WorkflowType resourceType, String requestAction) {
        return resourceType == WorkflowType.PNF && UPDATE_INSTANCE.equalsIgnoreCase(requestAction);
    }

    private void setExecutionVariables(DelegateExecution execution, List<ExecuteBuildingBlock> flowsToExecute,
            List<String> flowNames) {
        execution.setVariable("flowNames", flowNames);
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        execution.setVariable("retryCount", 0);
        execution.setVariable("isRollback", false);
        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("isRollbackComplete", false);
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocks(DelegateExecution execution, String requestId,
            String errorMessage) {
        List<ExecuteBuildingBlock> flowsToExecute;
        flowsToExecute = bbInputSetupUtils.loadOriginalFlowExecutionPath(requestId);
        if (flowsToExecute == null) {
            buildAndThrowException(execution, errorMessage);
        }
        return flowsToExecute;
    }

    private ConfigBuildingBlocksDataObject createConfigBuildingBlocksDataObject(DelegateExecution execution,
            ServiceInstancesRequest sIRequest, String requestId, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, String requestAction, String resourceId, String vnfType,
            List<OrchestrationFlow> orchFlows, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo) {

        return new ConfigBuildingBlocksDataObject().setsIRequest(sIRequest).setOrchFlows(orchFlows)
                .setRequestId(requestId).setResourceKey(resourceKey).setApiVersion(apiVersion).setResourceId(resourceId)
                .setRequestAction(requestAction).setaLaCarte(true).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestDetails(requestDetails).setExecution(execution)
                .setReplaceInformation(replaceInfo);
    }

    private void createBuildingBlocksForOrchFlows(DelegateExecution execution, ServiceInstancesRequest sIRequest,
            String requestId, WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails,
            String requestAction, String resourceId, List<ExecuteBuildingBlock> flowsToExecute, String vnfType,
            List<OrchestrationFlow> orchFlows, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo) throws Exception {

        for (OrchestrationFlow orchFlow : orchFlows) {
            if (orchFlow.getFlowName().contains(CONFIGURATION)) {
                List<OrchestrationFlow> configOrchFlows = new ArrayList<>();
                configOrchFlows.add(orchFlow);
                addConfigBuildingBlocksToFlowsToExecuteList(execution, sIRequest, requestId, workflowResourceIds,
                        requestDetails, requestAction, resourceId, flowsToExecute, vnfType, apiVersion, resourceKey,
                        replaceInfo, configOrchFlows);
            } else {
                ExecuteBuildingBlock ebb = executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, requestId,
                        resourceKey, apiVersion, resourceId, requestAction, true, vnfType, workflowResourceIds,
                        requestDetails, false, null, null, false, replaceInfo);
                flowsToExecute.add(ebb);
            }
        }
    }

    private void addConfigBuildingBlocksToFlowsToExecuteList(DelegateExecution execution,
            ServiceInstancesRequest sIRequest, String requestId, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, String requestAction, String resourceId,
            List<ExecuteBuildingBlock> flowsToExecute, String vnfType, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo, List<OrchestrationFlow> configOrchFlows) throws Exception {

        ConfigBuildingBlocksDataObject cbbdo = createConfigBuildingBlocksDataObject(execution, sIRequest, requestId,
                workflowResourceIds, requestDetails, requestAction, resourceId, vnfType, configOrchFlows, apiVersion,
                resourceKey, replaceInfo);
        List<ExecuteBuildingBlock> configBuildingBlocks = getConfigBuildingBlocks(cbbdo);
        flowsToExecute.addAll(configBuildingBlocks);
    }

    private Resource getResourceKey(ServiceInstancesRequest sIRequest, WorkflowType resourceType) {
        String resourceId = "";
        ModelInfo modelInfo = sIRequest.getRequestDetails().getModelInfo();
        if (modelInfo != null) {
            if (modelInfo.getModelType().equals(ModelType.service)) {
                resourceId = modelInfo.getModelVersionId();
            } else {
                resourceId = modelInfo.getModelCustomizationId();
            }
        }
        return new Resource(resourceType, resourceId, true, null);
    }

    private String getCloudOwner(CloudConfiguration cloudConfiguration) {
        if (cloudConfiguration != null && cloudConfiguration.getCloudOwner() != null) {
            return cloudConfiguration.getCloudOwner();
        }
        logger.warn("cloud owner value not found in request details, it will be set as default");
        return environment.getProperty(DEFAULT_CLOUD_OWNER);
    }

    protected <T> List<T> getRelatedResourcesInVfModule(String vnfId, String vfModuleId, Class<T> resultClass,
            AAIObjectName name) {
        List<T> vnfcs = new ArrayList<>();
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        AAIResultWrapper vfModuleResultsWrapper = bbInputSetupUtils.getAAIResourceDepthOne(uri);
        Optional<Relationships> relationshipsOp = vfModuleResultsWrapper.getRelationships();
        if (relationshipsOp.isEmpty()) {
            logger.debug("No relationships were found for vfModule in AAI");
        } else {
            Relationships relationships = relationshipsOp.get();
            List<AAIResultWrapper> vnfcResultWrappers = relationships.getByType(name);
            for (AAIResultWrapper vnfcResultWrapper : vnfcResultWrappers) {
                Optional<T> vnfcOp = vnfcResultWrapper.asBean(resultClass);
                vnfcOp.ifPresent(vnfcs::add);
            }
        }
        return vnfcs;
    }

    protected <T> T getRelatedResourcesInVnfc(Vnfc vnfc, Class<T> resultClass, AAIObjectName name)
            throws VnfcMultipleRelationshipException {
        T configuration = null;
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfc.getVnfcName()));
        AAIResultWrapper vnfcResultsWrapper = bbInputSetupUtils.getAAIResourceDepthOne(uri);
        Optional<Relationships> relationshipsOp = vnfcResultsWrapper.getRelationships();
        if (relationshipsOp.isEmpty()) {
            logger.debug("No relationships were found for VNFC in AAI");
        } else {
            Relationships relationships = relationshipsOp.get();
            List<AAIResultWrapper> configurationResultWrappers =
                    this.getResultWrappersFromRelationships(relationships, name);
            if (configurationResultWrappers.size() > 1) {
                throw new VnfcMultipleRelationshipException(vnfc.getVnfcName());
            }
            if (!configurationResultWrappers.isEmpty()) {
                Optional<T> configurationOp = configurationResultWrappers.get(0).asBean(resultClass);
                if (configurationOp.isPresent()) {
                    configuration = configurationOp.get();
                }
            }
        }
        return configuration;
    }

    protected List<AAIResultWrapper> getResultWrappersFromRelationships(Relationships relationships,
            AAIObjectName name) {
        return relationships.getByType(name);
    }

    protected boolean isConfiguration(List<OrchestrationFlow> orchFlows) {
        for (OrchestrationFlow flow : orchFlows) {
            if (flow.getFlowName().contains(CONFIGURATION) && !"ConfigurationScaleOutBB".equals(flow.getFlowName())) {
                return true;
            }
        }
        return false;
    }

    protected List<ExecuteBuildingBlock> getConfigBuildingBlocks(ConfigBuildingBlocksDataObject dataObj)
            throws AAIEntityNotFoundException, VnfcMultipleRelationshipException {

        List<ExecuteBuildingBlock> flowsToExecuteConfigs = new ArrayList<>();
        List<OrchestrationFlow> result = dataObj.getOrchFlows().stream()
                .filter(item -> item.getFlowName().contains(FABRIC_CONFIGURATION)).collect(Collectors.toList());
        String vnfId = dataObj.getWorkflowResourceIds().getVnfId();
        String vfModuleId = dataObj.getWorkflowResourceIds().getVfModuleId();

        String vnfCustomizationUUID = bbInputSetupUtils.getAAIGenericVnf(vnfId).getModelCustomizationId();
        String vfModuleCustomizationUUID;
        org.onap.aai.domain.yang.VfModule aaiVfModule = bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId);

        if (aaiVfModule == null) {
            logger.error("No matching VfModule is found in Generic-Vnf in AAI for vnfId: {} and vfModuleId : {}", vnfId,
                    vfModuleId);
            throw new AAIEntityNotFoundException("No matching VfModule is found in Generic-Vnf in AAI for vnfId: "
                    + vnfId + " and vfModuleId : " + vfModuleId);
        } else {
            vfModuleCustomizationUUID = aaiVfModule.getModelCustomizationId();
        }
        String replaceVfModuleCustomizationUUID = "";
        String replaceVnfModuleCustomizationUUID = "";
        boolean isReplace = false;
        if (dataObj.getRequestAction().equalsIgnoreCase("replaceInstance")
                || dataObj.getRequestAction().equalsIgnoreCase("replaceInstanceRetainAssignments")) {
            for (RelatedInstanceList relatedInstList : dataObj.getRequestDetails().getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                    replaceVnfModuleCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                }
            }
            replaceVfModuleCustomizationUUID = dataObj.getRequestDetails().getModelInfo().getModelCustomizationId();
            isReplace = true;
        }

        List<org.onap.aai.domain.yang.Vnfc> vnfcs =
                getRelatedResourcesInVfModule(vnfId, vfModuleId, org.onap.aai.domain.yang.Vnfc.class, Types.VNFC);
        for (org.onap.aai.domain.yang.Vnfc vnfc : vnfcs) {
            WorkflowResourceIds workflowIdsCopy = SerializationUtils.clone(dataObj.getWorkflowResourceIds());
            org.onap.aai.domain.yang.Configuration configuration =
                    getRelatedResourcesInVnfc(vnfc, org.onap.aai.domain.yang.Configuration.class, Types.CONFIGURATION);
            if (configuration == null) {
                logger.warn(String.format("No configuration found for VNFC %s in AAI", vnfc.getVnfcName()));
                continue;
            }
            workflowIdsCopy.setConfigurationId(configuration.getConfigurationId());
            for (OrchestrationFlow orchFlow : result) {
                if (!isReplace || (isReplace && (orchFlow.getFlowName().contains("Delete")))) {
                    if (!isReplace) {
                        dataObj.getResourceKey().setVfModuleCustomizationId(vfModuleCustomizationUUID);
                        dataObj.getResourceKey().setVnfCustomizationId(vnfCustomizationUUID);
                    } else {
                        if (orchFlow.getFlowName().contains("Delete")) {
                            dataObj.getResourceKey().setVfModuleCustomizationId(vfModuleCustomizationUUID);
                            dataObj.getResourceKey().setVnfCustomizationId(vnfCustomizationUUID);
                        } else {
                            dataObj.getResourceKey().setVfModuleCustomizationId(replaceVfModuleCustomizationUUID);
                            dataObj.getResourceKey().setVnfCustomizationId(replaceVnfModuleCustomizationUUID);
                        }
                    }
                    dataObj.getResourceKey().setCvnfModuleCustomizationId(vnfc.getModelCustomizationId());
                    String vnfcName = vnfc.getVnfcName();
                    if (vnfcName == null || vnfcName.isEmpty()) {
                        buildAndThrowException(dataObj.getExecution(), "Exception in create execution list "
                                + ": VnfcName does not exist or is null while there is a configuration for the vfModule",
                                new Exception("Vnfc and Configuration do not match"));
                    }
                    ExecuteBuildingBlock ebb =
                            executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, dataObj.getRequestId(),
                                    dataObj.getResourceKey(), dataObj.getApiVersion(), dataObj.getResourceId(),
                                    dataObj.getRequestAction(), dataObj.isaLaCarte(), dataObj.getVnfType(),
                                    workflowIdsCopy, dataObj.getRequestDetails(), false, null, vnfcName, true, null);
                    flowsToExecuteConfigs.add(ebb);
                }
            }
        }
        return flowsToExecuteConfigs;
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
        logger.error(msg, ex);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg + ex.getMessage());
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg + ex.getMessage());
    }

    protected List<OrchestrationFlow> getVfModuleReplaceBuildingBlocks(ConfigBuildingBlocksDataObject dataObj)
            throws Exception {

        String vnfId = dataObj.getWorkflowResourceIds().getVnfId();
        String vfModuleId = dataObj.getWorkflowResourceIds().getVfModuleId();

        logger.debug("BUILDING REPLACE LIST");

        boolean volumeGroupExisted = false;
        boolean volumeGroupWillExist = false;
        boolean keepVolumeGroup = false;

        boolean rebuildVolumeGroups = false;
        if (dataObj.getRequestDetails().getRequestParameters() != null
                && dataObj.getRequestDetails().getRequestParameters().getRebuildVolumeGroups() != null) {
            rebuildVolumeGroups = dataObj.getRequestDetails().getRequestParameters().getRebuildVolumeGroups();
        }
        String volumeGroupName = "";
        Optional<VolumeGroup> volumeGroupFromVfModule =
                bbInputSetupUtils.getRelatedVolumeGroupFromVfModule(vnfId, vfModuleId);
        if (volumeGroupFromVfModule.isPresent()) {
            String volumeGroupId = volumeGroupFromVfModule.get().getVolumeGroupId();
            volumeGroupName = volumeGroupFromVfModule.get().getVolumeGroupName();
            logger.debug("Volume group id of the existing volume group is: {}", volumeGroupId);
            volumeGroupExisted = true;
            dataObj.getWorkflowResourceIds().setVolumeGroupId(volumeGroupId);
            dataObj.getReplaceInformation().setOldVolumeGroupName(volumeGroupName);
        }

        List<OrchestrationFlow> orchFlows = dataObj.getOrchFlows();
        VfModuleCustomization vfModuleCustomization = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(
                dataObj.getRequestDetails().getModelInfo().getModelCustomizationUuid());
        if (vfModuleCustomization != null && vfModuleCustomization.getVfModule() != null
                && vfModuleCustomization.getVfModule().getVolumeHeatTemplate() != null
                && vfModuleCustomization.getVolumeHeatEnv() != null) {
            volumeGroupWillExist = true;
            if (!volumeGroupExisted) {
                String newVolumeGroupId = UUID.randomUUID().toString();
                dataObj.getWorkflowResourceIds().setVolumeGroupId(newVolumeGroupId);
                dataObj.getReplaceInformation().setOldVolumeGroupName(volumeGroupName);
                logger.debug("newVolumeGroupId: {}", newVolumeGroupId);
            }
        }

        if (volumeGroupExisted && volumeGroupWillExist && !rebuildVolumeGroups) {
            keepVolumeGroup = true;
        }

        if (!volumeGroupExisted || keepVolumeGroup) {
            logger.debug("Filtering out deletion of volume groups");
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().matches(VOLUMEGROUP_DELETE_PATTERN))
                    .collect(Collectors.toList());
        }
        if (!volumeGroupWillExist || keepVolumeGroup) {
            logger.debug("Filtering out creation of volume groups");
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().matches(VOLUMEGROUP_CREATE_PATTERN))
                    .collect(Collectors.toList());
        }

        return orchFlows;
    }

    private void updateResourceIdsFromAAITraversal(List<ExecuteBuildingBlock> flowsToExecute,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds, String serviceInstanceId) {
        for (Pair<WorkflowType, String> pair : aaiResourceIds) {
            logger.debug("{}, {}", pair.getValue0(), pair.getValue1());
        }
        Map<Resource, String> resourceInstanceIds = new HashMap<>();
        Arrays.stream(WorkflowType.values()).forEach(type -> resourceList.stream()
                .filter(resource -> type.equals(resource.getResourceType())
                        && !(WorkflowType.SERVICE.equals(type) && !resource.hasParent()))
                .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource,
                        retrieveAAIResourceId(aaiResourceIds, type), null, serviceInstanceId, resourceInstanceIds)));
    }

    private String retrieveAAIResourceId(List<Pair<WorkflowType, String>> aaiResourceIds, WorkflowType resource) {
        String id = null;
        for (int i = 0; i < aaiResourceIds.size(); i++) {
            if (aaiResourceIds.get(i).getValue0() == resource) {
                id = aaiResourceIds.get(i).getValue1();
                aaiResourceIds.remove(i);
                break;
            }
        }
        return id;
    }

    private void generateResourceIds(List<ExecuteBuildingBlock> flowsToExecute, List<Resource> resourceList,
            String serviceInstanceId) {
        Map<Resource, String> resourceInstanceIds = new HashMap<>();
        Arrays.stream(WorkflowType.values())
                .forEach(type -> resourceList.stream()
                        .filter(resource -> resource.hasParent() && type.equals(resource.getResourceType()))
                        .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource, null,
                                resource.getVirtualLinkKey(), serviceInstanceId, resourceInstanceIds)));
    }

    protected void updateWorkflowResourceIds(List<ExecuteBuildingBlock> flowsToExecute, WorkflowType resourceType,
            Resource resource, String id, String virtualLinkKey, String serviceInstanceId,
            Map<Resource, String> resourceInstanceIds) {
        String key = resource.getResourceId();
        String resourceId = id;
        if (resourceId == null) {
            resourceId = UUID.randomUUID().toString();
        }
        resourceInstanceIds.put(resource, resourceId);
        Set<String> assignedFlows = new LinkedHashSet<>();
        for (ExecuteBuildingBlock ebb : flowsToExecute) {
            String resourceTypeStr = resourceType.toString();
            String flowName = ebb.getBuildingBlock().getBpmnFlowName();
            String scope = StringUtils.defaultString(ebb.getBuildingBlock().getBpmnScope());
            String action = StringUtils.defaultString(ebb.getBuildingBlock().getBpmnAction());

            if (key != null && key.equalsIgnoreCase(ebb.getBuildingBlock().getKey())
                    && isFlowAssignable(assignedFlows, ebb, resourceType, flowName + action)
                    && (flowName.contains(resourceTypeStr)
                            || (flowName.contains(CONTROLLER) && resourceTypeStr.equalsIgnoreCase(scope)))) {
                WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
                workflowResourceIds.setServiceInstanceId(serviceInstanceId);
                Resource parent = resource.getParent();
                if (resource.hasParent() && resourceInstanceIds.containsKey(parent)) {
                    WorkflowResourceIdsUtils.setResourceIdByWorkflowType(workflowResourceIds, parent.getResourceType(),
                            resourceInstanceIds.get(parent));
                }
                if (resource.hasParent() && WorkflowType.SERVICE.equals(resourceType)
                        && WorkflowType.SERVICE.equals(parent.getResourceType())) {
                    String childServiceInstanceId = resource.isGenerated() ? resourceId : resource.getResourceId();
                    workflowResourceIds.setChildServiceInstanceId(childServiceInstanceId);
                    workflowResourceIds.setChildServiceInstanceName(resource.getInstanceName());
                } else {
                    WorkflowResourceIdsUtils.setInstanceNameByWorkflowType(workflowResourceIds, resourceType,
                            resource.getInstanceName());
                    WorkflowResourceIdsUtils.setResourceIdByWorkflowType(workflowResourceIds, resourceType, resourceId);
                }
                ebb.setWorkflowResourceIds(workflowResourceIds);
                assignedFlows.add(flowName + action);
            }
            if (virtualLinkKey != null && ebb.getBuildingBlock().isVirtualLink()
                    && virtualLinkKey.equalsIgnoreCase(ebb.getBuildingBlock().getVirtualLinkKey())) {
                WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
                workflowResourceIds.setServiceInstanceId(serviceInstanceId);
                workflowResourceIds.setNetworkId(resourceId);
                ebb.setWorkflowResourceIds(workflowResourceIds);
            }
        }
    }

    private boolean isFlowAssignable(Set<String> assignedFlows, ExecuteBuildingBlock ebb, WorkflowType resourceType,
            String assignedFlowName) {
        String id = WorkflowType.SERVICE.equals(resourceType)
                ? StringUtils.defaultString(ebb.getWorkflowResourceIds().getChildServiceInstanceId())
                : WorkflowResourceIdsUtils.getResourceIdByWorkflowType(ebb.getWorkflowResourceIds(), resourceType);
        return !assignedFlows.contains(assignedFlowName) && id.isEmpty();
    }

    protected WorkflowResourceIds populateResourceIdsFromApiHandler(DelegateExecution execution) {
        return WorkflowResourceIdsUtils.getWorkflowResourceIdsFromExecution(execution);
    }

    protected Resource extractResourceIdAndTypeFromUri(String uri) {
        Pattern patt = Pattern.compile("[vV]\\d+.*?(?:(?:/(?<type>" + SUPPORTEDTYPES
                + ")(?:/(?<id>[^/]+))?)(?:/(?<action>[^/]+))?(?:/resume)?)?$");
        Matcher m = patt.matcher(uri);
        boolean generated = false;

        if (m.find()) {
            logger.debug("found match on {} : {} ", uri, m);
            String type = m.group("type");
            String id = m.group("id");
            String action = m.group("action");
            if (type == null) {
                throw new IllegalArgumentException("Uri could not be parsed. No type found. " + uri);
            }
            if (action == null) {
                if (type.equals(SERVICE_INSTANCES) && (id == null || "assign".equals(id))) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                } else if (type.equals(VF_MODULES) && "scaleOut".equals(id)) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                }
            } else {
                if (action.matches(SUPPORTEDTYPES)) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                    type = action;
                }
            }
            return new Resource(WorkflowType.fromString(convertTypeFromPlural(type)), id, generated, null);
        } else {
            throw new IllegalArgumentException("Uri could not be parsed: " + uri);
        }
    }

    protected String convertTypeFromPlural(String type) {
        if (!type.matches(SUPPORTEDTYPES) || type.equals("NetworkSliceSubnet")) {
            return type;
        } else {
            if (type.equals(SERVICE_INSTANCES)) {
                return SERVICE;
            } else {
                return type.substring(0, 1).toUpperCase() + type.substring(1, type.length() - 1);
            }
        }
    }

    protected List<ExecuteBuildingBlock> sortExecutionPathByObjectForVlanTagging(List<ExecuteBuildingBlock> orchFlows,
            String requestAction) {
        List<ExecuteBuildingBlock> sortedOrchFlows = new ArrayList<>();
        if (requestAction.equals(CREATE_INSTANCE)) {
            for (ExecuteBuildingBlock ebb : orchFlows) {
                if (ebb.getBuildingBlock().getBpmnFlowName().equals("AssignNetworkBB")) {
                    String key = ebb.getBuildingBlock().getKey();
                    boolean isVirtualLink = Boolean.TRUE.equals(ebb.getBuildingBlock().isVirtualLink());
                    String virtualLinkKey = ebb.getBuildingBlock().getVirtualLinkKey();
                    sortedOrchFlows.add(ebb);
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                        if (isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                                && ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                        if (isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)
                                && ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                } else if (ebb.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                        || ebb.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)) {
                    continue;
                } else if (!"".equals(ebb.getBuildingBlock().getBpmnFlowName())) {
                    sortedOrchFlows.add(ebb);
                }
            }
        } else if (requestAction.equals("deleteInstance")) {
            for (ExecuteBuildingBlock ebb : orchFlows) {
                if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeactivateNetworkBB")) {
                    sortedOrchFlows.add(ebb);
                    String key = ebb.getBuildingBlock().getKey();
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (ebb2.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (ebb2.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                } else if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
                        || ebb.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")) {
                    continue;
                } else if (!ebb.getBuildingBlock().getBpmnFlowName().equals("")) {
                    sortedOrchFlows.add(ebb);
                }
            }
        }
        return sortedOrchFlows;
    }

    protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
            WorkflowType resourceName, boolean aLaCarte, String cloudOwner) {
        return this.queryNorthBoundRequestCatalogDb(execution, requestAction, resourceName, aLaCarte, cloudOwner, "");
    }

    protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
            WorkflowType resourceName, boolean aLaCarte, String cloudOwner, String serviceType) {
        List<OrchestrationFlow> listToExecute = new ArrayList<>();
        NorthBoundRequest northBoundRequest;
        if (serviceType.equalsIgnoreCase(SERVICE_TYPE_TRANSPORT)
                || serviceType.equalsIgnoreCase(SERVICE_TYPE_BONDING)) {
            northBoundRequest =
                    catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(
                            requestAction, resourceName.toString(), aLaCarte, cloudOwner, serviceType);
        } else {
            northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(
                    requestAction, resourceName.toString(), aLaCarte, cloudOwner);
        }
        if (northBoundRequest == null) {
            northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(
                    requestAction, resourceName.toString(), aLaCarte, CLOUD_OWNER);
        }
        if (northBoundRequest == null) {
            buildAndThrowException(execution, String.format("The request: %s %s %s is not supported by GR_API.",
                    (aLaCarte ? "AlaCarte" : "Macro"), resourceName, requestAction));
        } else {
            if (northBoundRequest.getIsToplevelflow() != null) {
                execution.setVariable(BBConstants.G_ISTOPLEVELFLOW, northBoundRequest.getIsToplevelflow());
            }
            List<OrchestrationFlow> flows = northBoundRequest.getOrchestrationFlowList();
            if (flows == null) {
                flows = new ArrayList<>();
            } else {
                flows.sort(Comparator.comparingInt(OrchestrationFlow::getSequenceNumber));
            }
            for (OrchestrationFlow flow : flows) {
                if (!flow.getFlowName().contains("BB") && !flow.getFlowName().contains("Activity")) {
                    List<OrchestrationFlow> macroQueryFlows =
                            catalogDbClient.getOrchestrationFlowByAction(flow.getFlowName());
                    listToExecute.addAll(macroQueryFlows);
                } else {
                    listToExecute.add(flow);
                }
            }
        }
        return listToExecute;
    }

    public void handleRuntimeException(DelegateExecution execution) {
        StringBuilder wfeExpMsg = new StringBuilder("Runtime error ");
        String runtimeErrorMessage;
        try {
            String javaExpMsg = (String) execution.getVariable("BPMN_javaExpMsg");
            if (javaExpMsg != null && !javaExpMsg.isEmpty()) {
                wfeExpMsg.append(": ").append(javaExpMsg);
            }
            runtimeErrorMessage = wfeExpMsg.toString();
            logger.error(runtimeErrorMessage);
            execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, runtimeErrorMessage);
        } catch (Exception e) {
            logger.error("Runtime error", e);
            // if runtime message was mulformed
            runtimeErrorMessage = "Runtime error";
        }
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, runtimeErrorMessage);
    }

    protected boolean isUriResume(String uri) {
        return uri.endsWith("/resume");
    }

    protected boolean isRequestMacroServiceResume(boolean aLaCarte, WorkflowType resourceType, String requestAction,
            String serviceInstanceId) {
        return (!aLaCarte && resourceType == WorkflowType.SERVICE
                && (requestAction.equalsIgnoreCase(ASSIGN_INSTANCE) || requestAction.equalsIgnoreCase(CREATE_INSTANCE))
                && (serviceInstanceId != null && serviceInstanceId.trim().length() > 1)
                && (bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId) != null));
    }

    private void fillExecutionDefault(DelegateExecution execution) {
        execution.setVariable("sentSyncResponse", false);
        execution.setVariable(HOMING, false);
        execution.setVariable("calledHoming", false);
        execution.setVariable(BBConstants.G_ISTOPLEVELFLOW, true);
    }

    private void fillExecution(DelegateExecution execution, boolean suppressRollback, String resourceId,
            WorkflowType resourceType) {
        execution.setVariable("suppressRollback", suppressRollback);
        execution.setVariable("resourceId", resourceId);
        execution.setVariable("resourceType", resourceType);
        execution.setVariable("resourceName", resourceType.toString());
    }

    private Resource getResource(BBInputSetupUtils bbInputSetupUtils, boolean isResume, boolean alaCarte, String uri,
            String requestId) {
        if (!alaCarte && isResume) {
            logger.debug("replacing URI {}", uri);
            uri = bbInputSetupUtils.loadOriginalInfraActiveRequestById(requestId).getRequestUrl();
            logger.debug("for RESUME with original value {}", uri);
        }
        return extractResourceIdAndTypeFromUri(uri);
    }

    private String getResourceId(Resource resource, String requestAction, RequestDetails requestDetails,
            WorkflowResourceIds workflowResourceIds) throws Exception {
        if (resource.isGenerated() && requestAction.equalsIgnoreCase("createInstance")
                && requestDetails.getRequestInfo().getInstanceName() != null) {
            return aaiResourceIdValidator.validateResourceIdInAAI(resource.getResourceId(), resource.getResourceType(),
                    requestDetails.getRequestInfo().getInstanceName(), requestDetails, workflowResourceIds);
        } else {
            return resource.getResourceId();
        }
    }

    private String getServiceInstanceId(DelegateExecution execution, String resourceId, WorkflowType resourceType) {
        String serviceInstanceId = (String) execution.getVariable("serviceInstanceId");
        if ((serviceInstanceId == null || serviceInstanceId.isEmpty()) && WorkflowType.SERVICE.equals(resourceType)) {
            serviceInstanceId = resourceId;
        }
        return serviceInstanceId;
    }

}
