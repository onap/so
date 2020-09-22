/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.Vnfc;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.AAIObjectName;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.infrastructure.workflow.tasks.utils.WorkflowResourceIdsUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.DuplicateNameException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.MultipleObjectsFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAIEntityNotFoundException;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowAction {

    private static final String WORKFLOW_ACTION_ERROR_MESSAGE = "WorkflowActionErrorMessage";
    private static final String SERVICE_INSTANCES = "serviceInstances";
    private static final String SERVICE_INSTANCE = "serviceInstance";
    private static final String VF_MODULES = "vfModules";
    private static final String WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI =
            "WorkflowAction was unable to verify if the instance name already exist in AAI.";
    private static final String VNF_TYPE = "vnfType";
    private static final String SERVICE = "Service";
    private static final String VNF = "Vnf";
    private static final String PNF = "Pnf";
    private static final String VFMODULE = "VfModule";
    private static final String VOLUMEGROUP = "VolumeGroup";
    private static final String NETWORK = "Network";
    private static final String NETWORKCOLLECTION = "NetworkCollection";
    private static final String CONFIGURATION = "Configuration";
    private static final String ASSIGNINSTANCE = "assignInstance";
    private static final String CREATEINSTANCE = "createInstance";
    private static final String REPLACEINSTANCE = "replaceInstance";
    private static final String REPLACEINSTANCERETAINASSIGNMENTS = "replaceInstanceRetainAssignments";
    private static final String USERPARAMSERVICE = "service";
    private static final String SUPPORTEDTYPES =
            "vnfs|vfModules|networks|networkCollections|volumeGroups|serviceInstances|instanceGroups";
    private static final String HOMINGSOLUTION = "Homing_Solution";
    private static final String FABRIC_CONFIGURATION = "FabricConfiguration";
    private static final String SERVICE_TYPE_TRANSPORT = "TRANSPORT";
    private static final String SERVICE_TYPE_BONDING = "BONDING";
    private static final String CLOUD_OWNER = "DEFAULT";
    private static final Logger logger = LoggerFactory.getLogger(WorkflowAction.class);
    private static final String NAME_EXISTS_WITH_DIFF_VERSION_ID = "(%s) and different version id (%s)";
    private static final String NAME_EXISTS_MULTIPLE =
            "(%s) and multiple combination of model-version-id + service-type + global-customer-id";
    private static final String NAME_EXISTS_WITH_DIFF_COMBINATION =
            "(%s) and global-customer-id (%s), service-type (%s), model-version-id (%s)";
    private static final String NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID =
            "(%s), same parent and different customization id (%s)";
    private static final String NAME_EXISTS_WITH_DIFF_PARENT = "(%s) id (%s) and different parent relationship";
    private static final String CREATENETWORKBB = "CreateNetworkBB";
    private static final String ACTIVATENETWORKBB = "ActivateNetworkBB";
    private static final String VOLUMEGROUP_DELETE_PATTERN = "(Un|De)(.*)Volume(.*)";
    private static final String VOLUMEGROUP_CREATE_PATTERN = "(A|C)(.*)Volume(.*)";
    private static final String CONTROLLER = "Controller";
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
    private AAIConfigurationResources aaiConfigurationResources;
    @Autowired
    private WorkflowActionExtractResourcesAAI workflowActionUtils;
    @Autowired
    private VrfValidation vrfValidation;
    @Autowired
    private Environment environment;

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    public void setBbInputSetup(BBInputSetup bbInputSetup) {
        this.bbInputSetup = bbInputSetup;
    }

    public void selectExecutionList(DelegateExecution execution) throws Exception {
        try {
            final String bpmnRequest = (String) execution.getVariable(BBConstants.G_BPMN_REQUEST);
            ServiceInstancesRequest sIRequest =
                    new ObjectMapper().readValue(bpmnRequest, ServiceInstancesRequest.class);

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
            List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

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
                    if (orchFlows == null || orchFlows.isEmpty()) {
                        orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, true,
                                cloudOwner, serviceType);
                    }
                    Resource resourceKey = getResourceKey(sIRequest, resourceType);

                    ReplaceInstanceRelatedInformation replaceInfo = new ReplaceInstanceRelatedInformation();
                    if ((requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                            || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS))
                            && resourceType.equals(WorkflowType.VFMODULE)) {
                        logger.debug("Build a BB list for replacing BB modules");
                        ConfigBuildingBlocksDataObject cbbdo = createConfigBuildingBlocksDataObject(execution,
                                sIRequest, requestId, workflowResourceIds, requestDetails, requestAction, resourceId,
                                vnfType, orchFlows, apiVersion, resourceKey, replaceInfo);
                        orchFlows = getVfModuleReplaceBuildingBlocks(cbbdo);

                        createBuildingBlocksForOrchFlows(execution, sIRequest, requestId, workflowResourceIds,
                                requestDetails, requestAction, resourceId, flowsToExecute, vnfType, orchFlows,
                                apiVersion, resourceKey, replaceInfo);
                    } else {
                        if (isConfiguration(orchFlows) && !requestAction.equalsIgnoreCase(CREATEINSTANCE)) {
                            addConfigBuildingBlocksToFlowsToExecuteList(execution, sIRequest, requestId,
                                    workflowResourceIds, requestDetails, requestAction, resourceId, flowsToExecute,
                                    vnfType, apiVersion, resourceKey, replaceInfo, orchFlows);
                        }
                        orchFlows =
                                orchFlows.stream().filter(item -> !item.getFlowName().contains(FABRIC_CONFIGURATION))
                                        .collect(Collectors.toList());

                        for (OrchestrationFlow orchFlow : orchFlows) {
                            ExecuteBuildingBlock ebb = buildExecuteBuildingBlock(orchFlow, requestId, resourceKey,
                                    apiVersion, resourceId, requestAction, true, vnfType, workflowResourceIds,
                                    requestDetails, false, null, null, false, replaceInfo);
                            flowsToExecute.add(ebb);
                        }
                    }
                } else {
                    boolean foundRelated = false;
                    boolean containsService = false;
                    List<Resource> resourceList = new ArrayList<>();
                    List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
                    if (resourceType == WorkflowType.SERVICE && requestAction.equalsIgnoreCase(ASSIGNINSTANCE)) {
                        // SERVICE-MACRO-ASSIGN will always get user params with a
                        // service.
                        if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
                            containsService = isContainsService(sIRequest);
                            if (containsService) {
                                traverseUserParamsService(execution, resourceList, sIRequest, requestAction);
                            }
                        } else {
                            buildAndThrowException(execution,
                                    "Service-Macro-Assign request details must contain user params with a service");
                        }
                    } else if (resourceType == WorkflowType.SERVICE && requestAction.equalsIgnoreCase(CREATEINSTANCE)) {
                        // SERVICE-MACRO-CREATE will get user params with a service,
                        // a service with a network, a service with a
                        // networkcollection, OR an empty service.
                        // If user params is just a service or null and macro
                        // queries the SI and finds a VNF, macro fails.

                        if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
                            containsService = isContainsService(sIRequest);
                        }
                        if (containsService) {
                            foundRelated = traverseUserParamsService(execution, resourceList, sIRequest, requestAction);
                        }
                        if (!foundRelated) {
                            traverseCatalogDbService(execution, sIRequest, resourceList, aaiResourceIds);
                        }
                    } else if (resourceType == WorkflowType.SERVICE
                            && ("activateInstance".equalsIgnoreCase(requestAction)
                                    || "unassignInstance".equalsIgnoreCase(requestAction)
                                    || "deleteInstance".equalsIgnoreCase(requestAction)
                                    || requestAction.equalsIgnoreCase("activate" + FABRIC_CONFIGURATION))) {
                        // SERVICE-MACRO-ACTIVATE, SERVICE-MACRO-UNASSIGN, and
                        // SERVICE-MACRO-DELETE
                        // Will never get user params with service, macro will have
                        // to query the SI in AAI to find related instances.
                        traverseAAIService(execution, resourceList, resourceId, aaiResourceIds);
                    } else if (resourceType == WorkflowType.SERVICE
                            && "deactivateInstance".equalsIgnoreCase(requestAction)) {
                        resourceList.add(new Resource(WorkflowType.SERVICE, "", false));
                    } else if (resourceType == WorkflowType.VNF && (REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                            || ("recreateInstance".equalsIgnoreCase(requestAction)))) {
                        traverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                                workflowResourceIds.getVnfId(), aaiResourceIds);
                    } else if (resourceType == WorkflowType.VNF && "updateInstance".equalsIgnoreCase(requestAction)) {
                        customTraverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                                workflowResourceIds.getVnfId(), aaiResourceIds);
                    } else {
                        buildAndThrowException(execution, "Current Macro Request is not supported");
                    }
                    StringBuilder foundObjects = new StringBuilder();
                    for (WorkflowType type : WorkflowType.values()) {
                        foundObjects.append(type).append(" - ").append(
                                (int) resourceList.stream().filter(x -> type.equals(x.getResourceType())).count())
                                .append("    ");
                    }
                    logger.info("Found {}", foundObjects);

                    if (orchFlows == null || orchFlows.isEmpty()) {
                        orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, isALaCarte,
                                cloudOwner, serviceType);
                    }
                    boolean vnfReplace = false;
                    if (resourceType.equals(WorkflowType.VNF) && (REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                            || REPLACEINSTANCERETAINASSIGNMENTS.equalsIgnoreCase(requestAction))) {
                        vnfReplace = true;
                    }
                    flowsToExecute = buildExecuteBuildingBlockList(orchFlows, resourceList, requestId, apiVersion,
                            resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, vnfReplace);
                    if (isNetworkCollectionInTheResourceList(resourceList)) {
                        logger.info("Sorting for Vlan Tagging");
                        flowsToExecute = sortExecutionPathByObjectForVlanTagging(flowsToExecute, requestAction);
                    }
                    // By default, enable homing at VNF level for CREATEINSTANCE and ASSIGNINSTANCE
                    if (resourceType == WorkflowType.SERVICE
                            && (requestAction.equals(CREATEINSTANCE) || requestAction.equals(ASSIGNINSTANCE))
                            && resourceList.stream().anyMatch(x -> WorkflowType.VNF.equals(x.getResourceType()))) {
                        execution.setVariable(HOMING, true);
                        execution.setVariable("calledHoming", false);
                    }
                    if (resourceType == WorkflowType.SERVICE && (requestAction.equalsIgnoreCase(ASSIGNINSTANCE)
                            || requestAction.equalsIgnoreCase(CREATEINSTANCE))) {
                        generateResourceIds(flowsToExecute, resourceList, serviceInstanceId);
                    } else {
                        updateResourceIdsFromAAITraversal(flowsToExecute, resourceList, aaiResourceIds,
                                serviceInstanceId);
                    }
                }
            }
            // If the user set "Homing_Solution" to "none", disable homing, else if "Homing_Solution" is specified,
            // enable it.
            if (sIRequest.getRequestDetails().getRequestParameters() != null
                    && sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
                List<Map<String, Object>> userParams = getListOfUserParams(sIRequest);
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

    private void setExecutionVariables(DelegateExecution execution, List<ExecuteBuildingBlock> flowsToExecute,
            List<String> flowNames) {
        execution.setVariable("flowNames", flowNames);
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        execution.setVariable("retryCount", 0);
        execution.setVariable("isRollback", false);
        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("isRollbackComplete", false);
    }

    private boolean isContainsService(ServiceInstancesRequest sIRequest) {
        boolean containsService;
        List<Map<String, Object>> userParams = getListOfUserParams(sIRequest);
        containsService = userParams.stream().anyMatch(param -> param.containsKey(USERPARAMSERVICE));
        return containsService;
    }

    private List<Map<String, Object>> getListOfUserParams(ServiceInstancesRequest sIRequest) {
        return sIRequest.getRequestDetails().getRequestParameters().getUserParams();
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
                ExecuteBuildingBlock ebb = buildExecuteBuildingBlock(orchFlow, requestId, resourceKey, apiVersion,
                        resourceId, requestAction, true, vnfType, workflowResourceIds, requestDetails, false, null,
                        null, false, replaceInfo);
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
        return new Resource(resourceType, resourceId, true);
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
        if (!relationshipsOp.isPresent()) {
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

    protected <T> T getRelatedResourcesInVnfc(Vnfc vnfc, Class<T> resultClass, AAIObjectName name) throws Exception {
        T configuration = null;
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfc.getVnfcName()));
        AAIResultWrapper vnfcResultsWrapper = bbInputSetupUtils.getAAIResourceDepthOne(uri);
        Optional<Relationships> relationshipsOp = vnfcResultsWrapper.getRelationships();
        if (!relationshipsOp.isPresent()) {
            logger.debug("No relationships were found for VNFC in AAI");
        } else {
            Relationships relationships = relationshipsOp.get();
            List<AAIResultWrapper> configurationResultWrappers =
                    this.getResultWrappersFromRelationships(relationships, name);
            if (configurationResultWrappers.size() > 1) {
                String multipleRelationshipsError =
                        "Multiple relationships exist from VNFC " + vnfc.getVnfcName() + " to Configurations";
                throw new Exception(multipleRelationshipsError);
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
            throws Exception {

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
                dataObj.getResourceKey().setVfModuleCustomizationId(vfModuleCustomizationUUID);
                dataObj.getResourceKey().setCvnfModuleCustomizationId(vnfc.getModelCustomizationId());
                dataObj.getResourceKey().setVnfCustomizationId(vnfCustomizationUUID);
                String vnfcName = vnfc.getVnfcName();
                if (vnfcName == null || vnfcName.isEmpty()) {
                    buildAndThrowException(dataObj.getExecution(), "Exception in create execution list "
                            + ": VnfcName does not exist or is null while there is a configuration for the vfModule",
                            new Exception("Vnfc and Configuration do not match"));
                }
                ExecuteBuildingBlock ebb = buildExecuteBuildingBlock(orchFlow, dataObj.getRequestId(),
                        dataObj.getResourceKey(), dataObj.getApiVersion(), dataObj.getResourceId(),
                        dataObj.getRequestAction(), dataObj.isaLaCarte(), dataObj.getVnfType(), workflowIdsCopy,
                        dataObj.getRequestDetails(), false, null, vnfcName, true, null);
                flowsToExecuteConfigs.add(ebb);
            }
        }
        return flowsToExecuteConfigs;
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
            logger.debug("Volume group id of the existing volume group is: " + volumeGroupId);
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
                logger.debug("newVolumeGroupId: " + newVolumeGroupId);
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

    private void updateResourceIdsFromAAITraversal(List<ExecuteBuildingBlock> flowsToExecute,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds, String serviceInstanceId) {
        for (Pair<WorkflowType, String> pair : aaiResourceIds) {
            logger.debug(pair.getValue0() + ", " + pair.getValue1());
        }

        Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE))
                .forEach(type -> resourceList.stream().filter(resource -> type.equals(resource.getResourceType()))
                        .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource.getResourceId(),
                                retrieveAAIResourceId(aaiResourceIds, type), null, serviceInstanceId)));
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
        Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE))
                .forEach(type -> resourceList.stream().filter(resource -> type.equals(resource.getResourceType()))
                        .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource.getResourceId(),
                                null, resource.getVirtualLinkKey(), serviceInstanceId)));
    }

    protected void updateWorkflowResourceIds(List<ExecuteBuildingBlock> flowsToExecute, WorkflowType resourceType,
            String key, String id, String virtualLinkKey, String serviceInstanceId) {
        String resourceId = id;
        if (resourceId == null) {
            resourceId = UUID.randomUUID().toString();
        }
        for (ExecuteBuildingBlock ebb : flowsToExecute) {
            if (key != null && key.equalsIgnoreCase(ebb.getBuildingBlock().getKey()) && (ebb.getBuildingBlock()
                    .getBpmnFlowName().contains(resourceType.toString())
                    || (ebb.getBuildingBlock().getBpmnFlowName().contains(CONTROLLER)
                            && ebb.getBuildingBlock().getBpmnScope().equalsIgnoreCase(resourceType.toString())))) {
                WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
                workflowResourceIds.setServiceInstanceId(serviceInstanceId);
                WorkflowResourceIdsUtils.setResourceIdByWorkflowType(workflowResourceIds, resourceType, resourceId);
                ebb.setWorkflowResourceIds(workflowResourceIds);
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

    protected CollectionResourceCustomization findCatalogNetworkCollection(DelegateExecution execution,
            org.onap.so.db.catalog.beans.Service service) {
        CollectionResourceCustomization networkCollection = null;
        int count = 0;
        for (CollectionResourceCustomization collectionCust : service.getCollectionResourceCustomizations()) {
            if (catalogDbClient.getNetworkCollectionResourceCustomizationByID(
                    collectionCust.getModelCustomizationUUID()) != null) {
                networkCollection = collectionCust;
                count++;
            }
        }
        if (count == 0) {
            return null;
        } else if (count > 1) {
            buildAndThrowException(execution,
                    "Found multiple Network Collections in the Service model, only one per Service is supported.");
        }
        return networkCollection;
    }

    protected void traverseCatalogDbService(DelegateExecution execution, ServiceInstancesRequest sIRequest,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds)
            throws JsonProcessingException, VrfBondingServiceException {
        String modelUUID = sIRequest.getRequestDetails().getModelInfo().getModelVersionId();
        org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(modelUUID);

        if (service == null) {
            buildAndThrowException(execution, "Could not find the service model in catalog db.");
        } else {
            resourceList.add(new Resource(WorkflowType.SERVICE, service.getModelUUID(), false));
            RelatedInstance relatedVpnBinding =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.vpnBinding);
            RelatedInstance relatedLocalNetwork =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.network);

            if (relatedVpnBinding != null && relatedLocalNetwork != null) {
                traverseVrfConfiguration(aaiResourceIds, resourceList, service, relatedVpnBinding, relatedLocalNetwork);
            } else {
                traverseNetworkCollection(execution, resourceList, service);
            }
        }
    }

    protected void traverseVrfConfiguration(List<Pair<WorkflowType, String>> aaiResourceIds,
            List<Resource> resourceList, org.onap.so.db.catalog.beans.Service service,
            RelatedInstance relatedVpnBinding, RelatedInstance relatedLocalNetwork)
            throws VrfBondingServiceException, JsonProcessingException {
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork =
                bbInputSetupUtils.getAAIL3Network(relatedLocalNetwork.getInstanceId());
        vrfValidation.vrfServiceValidation(service);
        vrfValidation.vrfCatalogDbChecks(service);
        vrfValidation.aaiVpnBindingValidation(relatedVpnBinding.getInstanceId(),
                bbInputSetupUtils.getAAIVpnBinding(relatedVpnBinding.getInstanceId()));
        vrfValidation.aaiNetworkValidation(relatedLocalNetwork.getInstanceId(), aaiLocalNetwork);
        vrfValidation.aaiSubnetValidation(aaiLocalNetwork);
        vrfValidation.aaiAggregateRouteValidation(aaiLocalNetwork);
        vrfValidation.aaiRouteTargetValidation(aaiLocalNetwork);
        String existingAAIVrfConfiguration = getExistingAAIVrfConfiguration(relatedVpnBinding, aaiLocalNetwork);
        if (existingAAIVrfConfiguration != null) {
            aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, existingAAIVrfConfiguration));
        }
        resourceList.add(new Resource(WorkflowType.CONFIGURATION,
                service.getConfigurationCustomizations().get(0).getModelCustomizationUUID(), false));

    }

    protected String getExistingAAIVrfConfiguration(RelatedInstance relatedVpnBinding,
            org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws JsonProcessingException, VrfBondingServiceException {
        Optional<Relationships> relationshipsOp = new AAIResultWrapper(
                new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiLocalNetwork)).getRelationships();
        if (relationshipsOp.isPresent()) {
            List<AAIResultWrapper> configurationsRelatedToLocalNetwork =
                    relationshipsOp.get().getByType(Types.CONFIGURATION);
            if (configurationsRelatedToLocalNetwork.size() > 1) {
                throw new VrfBondingServiceException(
                        "Network: " + aaiLocalNetwork.getNetworkId() + " has more than 1 configuration related to it");
            }
            if (configurationsRelatedToLocalNetwork.size() == 1) {
                AAIResultWrapper configWrapper = configurationsRelatedToLocalNetwork.get(0);
                Optional<Configuration> relatedConfiguration = configWrapper.asBean(Configuration.class);
                if (relatedConfiguration.isPresent() && vrfConfigurationAlreadyExists(relatedVpnBinding,
                        relatedConfiguration.get(), configWrapper)) {
                    return relatedConfiguration.get().getConfigurationId();
                }
            }
        }
        return null;
    }

    protected boolean vrfConfigurationAlreadyExists(RelatedInstance relatedVpnBinding, Configuration vrfConfiguration,
            AAIResultWrapper configWrapper) throws VrfBondingServiceException {
        if ("VRF-ENTRY".equalsIgnoreCase(vrfConfiguration.getConfigurationType())) {
            Optional<Relationships> relationshipsConfigOp = configWrapper.getRelationships();
            if (relationshipsConfigOp.isPresent()) {
                Optional<VpnBinding> relatedInfraVpnBindingOp =
                        workflowActionUtils.extractRelationshipsVpnBinding(relationshipsConfigOp.get());
                if (relatedInfraVpnBindingOp.isPresent()) {
                    VpnBinding relatedInfraVpnBinding = relatedInfraVpnBindingOp.get();
                    if (!relatedInfraVpnBinding.getVpnId().equalsIgnoreCase(relatedVpnBinding.getInstanceId())) {
                        throw new VrfBondingServiceException("Configuration: " + vrfConfiguration.getConfigurationId()
                                + " is not connected to the same vpn binding id provided in request: "
                                + relatedVpnBinding.getInstanceId());
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void traverseNetworkCollection(DelegateExecution execution, List<Resource> resourceList,
            org.onap.so.db.catalog.beans.Service service) {
        if (isVnfCustomizationsInTheService(service)) {
            buildAndThrowException(execution,
                    "Cannot orchestrate Service-Macro-Create without user params with a vnf. Please update ASDC model for new macro orchestration support or add service_recipe records to route to old macro flows");
        }
        if (isPnfCustomizationsInTheService(service)) {
            buildAndThrowException(execution,
                    "Cannot orchestrate Service-Macro-Create without user params with a pnf. Please update ASDC model for new macro orchestration support or add service_recipe records to route to old macro flows");
        }
        List<CollectionResourceCustomization> customizations = service.getCollectionResourceCustomizations();
        if (customizations.isEmpty()) {
            logger.debug("No Collections found. CollectionResourceCustomization list is empty.");
        } else {
            CollectionResourceCustomization collectionResourceCustomization =
                    findCatalogNetworkCollection(execution, service);
            traverseNetworkCollectionResourceCustomization(resourceList, collectionResourceCustomization);
        }
        traverseNetworkCollectionCustomization(resourceList, service);
    }

    private void traverseNetworkCollectionResourceCustomization(List<Resource> resourceList,
            CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomizationShouldNotBeProcessed(resourceList, collectionResourceCustomization))
            return;
        int minNetworks = 0;
        org.onap.so.db.catalog.beans.InstanceGroup instanceGroup =
                collectionResourceCustomization.getCollectionResource().getInstanceGroup();
        CollectionResourceInstanceGroupCustomization collectionInstCust = null;
        if (!instanceGroup.getCollectionInstanceGroupCustomizations().isEmpty()) {
            for (CollectionResourceInstanceGroupCustomization collectionInstanceGroupTemp : instanceGroup
                    .getCollectionInstanceGroupCustomizations()) {
                if (collectionInstanceGroupTemp.getModelCustomizationUUID()
                        .equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
                    collectionInstCust = collectionInstanceGroupTemp;
                    break;
                }
            }
            if (interfaceNetworkQuantityIsAvailableInCollection(collectionInstCust)) {
                minNetworks = collectionInstCust.getSubInterfaceNetworkQuantity();
            }
        }
        logger.debug("minNetworks: {}", minNetworks);
        CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                getCollectionNetworkResourceCustomization(collectionResourceCustomization, instanceGroup);
        for (int i = 0; i < minNetworks; i++) {
            if (collectionNetworkResourceCust != null) {
                Resource resource = new Resource(WorkflowType.VIRTUAL_LINK,
                        collectionNetworkResourceCust.getModelCustomizationUUID(), false);
                resource.setVirtualLinkKey(Integer.toString(i));
                resourceList.add(resource);
            }
        }
    }

    private CollectionNetworkResourceCustomization getCollectionNetworkResourceCustomization(
            CollectionResourceCustomization collectionResourceCustomization, InstanceGroup instanceGroup) {
        CollectionNetworkResourceCustomization collectionNetworkResourceCust = null;
        for (CollectionNetworkResourceCustomization collectionNetworkTemp : instanceGroup
                .getCollectionNetworkResourceCustomizations()) {
            if (collectionNetworkTemp.getNetworkResourceCustomization().getModelCustomizationUUID()
                    .equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
                collectionNetworkResourceCust = collectionNetworkTemp;
                break;
            }
        }
        return collectionNetworkResourceCust;
    }

    private boolean collectionResourceCustomizationShouldNotBeProcessed(List<Resource> resourceList,
            CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomization == null) {
            logger.debug("No Network Collection Customization found");
            return true;
        }
        resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                collectionResourceCustomization.getModelCustomizationUUID(), false));
        logger.debug("Found a network collection");
        if (collectionResourceCustomization.getCollectionResource() == null) {
            logger.debug("No Network Collection found. collectionResource is null");
            return true;
        }
        if (collectionResourceCustomization.getCollectionResource().getInstanceGroup() == null) {
            logger.debug("No Instance Group found for network collection.");
            return true;
        }
        String toscaNodeType =
                collectionResourceCustomization.getCollectionResource().getInstanceGroup().getToscaNodeType();
        if (!toscaNodeTypeHasNetworkCollection(toscaNodeType)) {
            logger.debug("Instance Group tosca node type does not contain NetworkCollection:  {}", toscaNodeType);
            return true;
        }
        return false;
    }

    private boolean interfaceNetworkQuantityIsAvailableInCollection(
            CollectionResourceInstanceGroupCustomization collectionInstCust) {
        return collectionInstCust != null && collectionInstCust.getSubInterfaceNetworkQuantity() != null;
    }

    private boolean toscaNodeTypeHasNetworkCollection(String toscaNodeType) {
        return toscaNodeType != null && toscaNodeType.contains(NETWORKCOLLECTION);
    }

    private void traverseNetworkCollectionCustomization(List<Resource> resourceList,
            org.onap.so.db.catalog.beans.Service service) {
        if (isNetworkCollectionInTheResourceList(resourceList)) {
            return;
        }
        if (service.getNetworkCustomizations() == null) {
            logger.debug("No networks were found on this service model");
            return;
        }
        for (int i = 0; i < service.getNetworkCustomizations().size(); i++) {
            resourceList.add(new Resource(WorkflowType.NETWORK,
                    service.getNetworkCustomizations().get(i).getModelCustomizationUUID(), false));
        }
    }

    private boolean isNetworkCollectionInTheResourceList(List<Resource> resourceList) {
        return resourceList.stream().anyMatch(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType());
    }

    private boolean isVnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getVnfCustomizations() == null || service.getVnfCustomizations().isEmpty());
    }

    private boolean isPnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getPnfCustomizations() == null || service.getPnfCustomizations().isEmpty());
    }

    protected void traverseAAIService(DelegateExecution execution, List<Resource> resourceList, String resourceId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(resourceId);
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                    bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            resourceList.add(new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false));
            traverseServiceInstanceMSOVnfs(resourceList, aaiResourceIds, serviceInstanceMSO);
            traverseServiceInstanceMSOPnfs(resourceList, aaiResourceIds, serviceInstanceMSO);
            if (serviceInstanceMSO.getNetworks() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network : serviceInstanceMSO
                        .getNetworks()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.NETWORK, network.getNetworkId()));
                    resourceList.add(new Resource(WorkflowType.NETWORK, network.getNetworkId(), false));
                }
            }
            if (serviceInstanceMSO.getCollection() != null) {
                logger.debug("found networkcollection");
                aaiResourceIds
                        .add(new Pair<>(WorkflowType.NETWORKCOLLECTION, serviceInstanceMSO.getCollection().getId()));
                resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                        serviceInstanceMSO.getCollection().getId(), false));
            }
            if (serviceInstanceMSO.getConfigurations() != null) {
                for (Configuration config : serviceInstanceMSO.getConfigurations()) {
                    Optional<org.onap.aai.domain.yang.Configuration> aaiConfig =
                            aaiConfigurationResources.getConfiguration(config.getConfigurationId());
                    if (aaiConfig.isPresent() && aaiConfig.get().getRelationshipList() != null) {
                        for (Relationship relationship : aaiConfig.get().getRelationshipList().getRelationship()) {
                            if (relationship.getRelatedTo().contains("vnfc")
                                    || relationship.getRelatedTo().contains("vpn-binding")) {
                                aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, config.getConfigurationId()));
                                resourceList.add(
                                        new Resource(WorkflowType.CONFIGURATION, config.getConfigurationId(), false));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in traverseAAIService", ex);
            buildAndThrowException(execution,
                    "Could not find existing Service Instance or related Instances to execute the request on.");
        }
    }

    private void traverseServiceInstanceMSOVnfs(List<Resource> resourceList,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getVnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
            resourceList.add(new Resource(WorkflowType.VNF, vnf.getVnfId(), false));
            traverseVnfModules(resourceList, aaiResourceIds, vnf);
            if (vnf.getVolumeGroups() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                    resourceList.add(new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false));
                }
            }
        }
    }

    private void traverseServiceInstanceMSOPnfs(List<Resource> resourceList,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getPnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf : serviceInstanceMSO.getPnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.PNF, pnf.getPnfId()));
            resourceList.add(new Resource(WorkflowType.PNF, pnf.getPnfId(), false));
        }
    }

    private void traverseVnfModules(List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf) {
        if (vnf.getVfModules() == null) {
            return;
        }
        for (VfModule vfModule : vnf.getVfModules()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
            Resource resource = new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false);
            resource.setBaseVfModule(vfModule.getModelInfoVfModule().getIsBaseBoolean());
            resourceList.add(resource);
        }
    }

    private void traverseAAIVnf(DelegateExecution execution, List<Resource> resourceList, String serviceId,
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

    private void customTraverseAAIVnf(DelegateExecution execution, List<Resource> resourceList, String serviceId,
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

    protected boolean traverseUserParamsService(DelegateExecution execution, List<Resource> resourceList,
            ServiceInstancesRequest sIRequest, String requestAction) throws IOException {
        boolean foundRelated = false;
        boolean foundVfModuleOrVG = false;
        String vnfCustomizationUUID = "";
        String vfModuleCustomizationUUID = "";
        if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
            List<Map<String, Object>> userParams = getListOfUserParams(sIRequest);
            for (Map<String, Object> params : userParams) {
                if (params.containsKey(USERPARAMSERVICE)) {
                    ObjectMapper obj = new ObjectMapper();
                    String input = obj.writeValueAsString(params.get(USERPARAMSERVICE));
                    Service validate = obj.readValue(input, Service.class);
                    resourceList.add(
                            new Resource(WorkflowType.SERVICE, validate.getModelInfo().getModelVersionId(), false));
                    if (validate.getResources().getVnfs() != null) {
                        for (Vnfs vnf : validate.getResources().getVnfs()) {
                            resourceList.add(new Resource(WorkflowType.VNF,
                                    vnf.getModelInfo().getModelCustomizationId(), false));
                            foundRelated = true;
                            if (vnf.getModelInfo() != null && vnf.getModelInfo().getModelCustomizationUuid() != null) {
                                vnfCustomizationUUID = vnf.getModelInfo().getModelCustomizationUuid();
                            }
                            if (vnf.getVfModules() != null) {
                                for (VfModules vfModule : vnf.getVfModules()) {
                                    VfModuleCustomization vfModuleCustomization =
                                            catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(
                                                    vfModule.getModelInfo().getModelCustomizationUuid());
                                    if (vfModuleCustomization != null) {

                                        if (vfModuleCustomization.getVfModule() != null
                                                && vfModuleCustomization.getVfModule().getVolumeHeatTemplate() != null
                                                && vfModuleCustomization.getVolumeHeatEnv() != null) {
                                            resourceList.add(new Resource(WorkflowType.VOLUMEGROUP,
                                                    vfModuleCustomization.getModelCustomizationUUID(), false));
                                            foundVfModuleOrVG = true;
                                        }

                                        if (vfModuleCustomization.getVfModule() != null
                                                && vfModuleCustomization.getVfModule().getModuleHeatTemplate() != null
                                                && vfModuleCustomization.getHeatEnvironment() != null) {
                                            foundVfModuleOrVG = true;
                                            Resource resource = new Resource(WorkflowType.VFMODULE,
                                                    vfModuleCustomization.getModelCustomizationUUID(), false);
                                            resource.setBaseVfModule(
                                                    vfModuleCustomization.getVfModule().getIsBase() != null
                                                            && vfModuleCustomization.getVfModule().getIsBase());
                                            resourceList.add(resource);
                                            if (vfModule.getModelInfo() != null
                                                    && vfModule.getModelInfo().getModelCustomizationUuid() != null) {
                                                vfModuleCustomizationUUID =
                                                        vfModule.getModelInfo().getModelCustomizationUuid();
                                            }
                                            if (!vnfCustomizationUUID.isEmpty()
                                                    && !vfModuleCustomizationUUID.isEmpty()) {
                                                List<CvnfcConfigurationCustomization> configs =
                                                        traverseCatalogDbForConfiguration(
                                                                validate.getModelInfo().getModelVersionId(),
                                                                vnfCustomizationUUID, vfModuleCustomizationUUID);
                                                for (CvnfcConfigurationCustomization config : configs) {
                                                    Resource configResource = new Resource(WorkflowType.CONFIGURATION,
                                                            config.getConfigurationResource().getModelUUID(), false);
                                                    resource.setVnfCustomizationId(
                                                            vnf.getModelInfo().getModelCustomizationId());
                                                    resource.setVfModuleCustomizationId(
                                                            vfModule.getModelInfo().getModelCustomizationId());
                                                    resourceList.add(configResource);
                                                }
                                            }
                                        }
                                        if (!foundVfModuleOrVG) {
                                            buildAndThrowException(execution,
                                                    "Could not determine if vfModule was a vfModule or volume group. Heat template and Heat env are null");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (validate.getResources().getPnfs() != null) {
                        for (Pnfs pnf : validate.getResources().getPnfs()) {
                            resourceList.add(new Resource(WorkflowType.PNF,
                                    pnf.getModelInfo().getModelCustomizationId(), false));
                            foundRelated = true;
                        }
                    }
                    if (validate.getResources().getNetworks() != null) {
                        for (Networks network : validate.getResources().getNetworks()) {
                            resourceList.add(new Resource(WorkflowType.NETWORK,
                                    network.getModelInfo().getModelCustomizationId(), false));
                            foundRelated = true;
                        }
                        if (requestAction.equals(CREATEINSTANCE)) {
                            String networkColCustId = queryCatalogDBforNetworkCollection(execution, sIRequest);
                            if (networkColCustId != null) {
                                resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION, networkColCustId, false));
                                foundRelated = true;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return foundRelated;
    }

    protected List<CvnfcConfigurationCustomization> traverseCatalogDbForConfiguration(String serviceModelUUID,
            String vnfCustomizationUUID, String vfModuleCustomizationUUID) {
        List<CvnfcConfigurationCustomization> configurations = new ArrayList<>();
        try {
            List<CvnfcCustomization> cvnfcCustomizations = catalogDbClient.getCvnfcCustomization(serviceModelUUID,
                    vnfCustomizationUUID, vfModuleCustomizationUUID);
            for (CvnfcCustomization cvnfc : cvnfcCustomizations) {
                for (CvnfcConfigurationCustomization customization : cvnfc.getCvnfcConfigurationCustomization()) {
                    if (customization.getConfigurationResource().getToscaNodeType().contains(FABRIC_CONFIGURATION)) {
                        configurations.add(customization);
                    }
                }
            }
            logger.debug("found {} fabric configuration(s)", configurations.size());
            return configurations;
        } catch (Exception ex) {
            logger.error("Error in finding configurations", ex);
            return configurations;
        }
    }

    protected String queryCatalogDBforNetworkCollection(DelegateExecution execution,
            ServiceInstancesRequest sIRequest) {
        org.onap.so.db.catalog.beans.Service service =
                catalogDbClient.getServiceByID(sIRequest.getRequestDetails().getModelInfo().getModelVersionId());
        if (service != null) {
            CollectionResourceCustomization networkCollection = this.findCatalogNetworkCollection(execution, service);
            if (networkCollection != null) {
                return networkCollection.getModelCustomizationUUID();
            }
        }
        return null;
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
            return new Resource(WorkflowType.fromString(convertTypeFromPlural(type)), id, generated);
        } else {
            throw new IllegalArgumentException("Uri could not be parsed: " + uri);
        }
    }

    protected String validateResourceIdInAAI(String generatedResourceId, WorkflowType type, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws Exception {
        try {
            if ("SERVICE".equalsIgnoreCase(type.toString())) {
                return validateServiceResourceIdInAAI(generatedResourceId, instanceName, reqDetails);
            } else if ("NETWORK".equalsIgnoreCase(type.toString())) {
                return validateNetworkResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("VNF".equalsIgnoreCase(type.toString())) {
                return validateVnfResourceIdInAAI(generatedResourceId, instanceName, reqDetails, workflowResourceIds);
            } else if ("VFMODULE".equalsIgnoreCase(type.toString())) {
                return validateVfModuleResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("VOLUMEGROUP".equalsIgnoreCase(type.toString())) {
                return validateVolumeGroupResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            } else if ("CONFIGURATION".equalsIgnoreCase(type.toString())) {
                return validateConfigurationResourceIdInAAI(generatedResourceId, instanceName, reqDetails,
                        workflowResourceIds);
            }
            return generatedResourceId;
        } catch (DuplicateNameException dne) {
            throw dne;
        } catch (Exception ex) {
            logger.error(WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI, ex);
            throw new IllegalStateException(
                    WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI);
        }
    }

    protected String convertTypeFromPlural(String type) {
        if (!type.matches(SUPPORTEDTYPES)) {
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
        if (requestAction.equals(CREATEINSTANCE)) {
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

    private void addBuildingBlockToExecuteBBList(List<ExecuteBuildingBlock> flowsToExecute, List<Resource> resourceList,
            WorkflowType workflowType, OrchestrationFlow orchFlow, String requestId, String apiVersion,
            String resourceId, String requestAction, String vnfType, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, boolean isVirtualLink, boolean isConfiguration) {

        resourceList.stream().filter(resource -> resource.getResourceType().equals(workflowType))
                .forEach(resource -> flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resource,
                        apiVersion, resourceId, requestAction, false, vnfType, workflowResourceIds, requestDetails,
                        isVirtualLink, resource.getVirtualLinkKey(), null, isConfiguration, null)));
    }

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
                if (requestAction.equals(CREATEINSTANCE) || requestAction.equals(ASSIGNINSTANCE)
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

    private ConfigurationResourceKeys getConfigurationResourceKeys(Resource resource, String vnfcName) {
        ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
        Optional.ofNullable(vnfcName).ifPresent(configurationResourceKeys::setVnfcName);
        configurationResourceKeys.setCvnfcCustomizationUUID(resource.getCvnfModuleCustomizationId());
        configurationResourceKeys.setVfModuleCustomizationUUID(resource.getVfModuleCustomizationId());
        configurationResourceKeys.setVnfResourceCustomizationUUID(resource.getVnfCustomizationId());
        return configurationResourceKeys;
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
            if (flows == null)
                flows = new ArrayList<>();
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

    protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
        logger.error(msg, ex);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg + ex.getMessage());
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg + ex.getMessage());
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
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
                && (requestAction.equalsIgnoreCase(ASSIGNINSTANCE) || requestAction.equalsIgnoreCase(CREATEINSTANCE))
                && (serviceInstanceId != null && serviceInstanceId.trim().length() > 1)
                && (bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId) != null));
    }

    protected String validateServiceResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails) throws DuplicateNameException {
        String globalCustomerId = reqDetails.getSubscriberInfo().getGlobalSubscriberId();
        String serviceType = reqDetails.getRequestParameters().getSubscriptionServiceType();
        if (instanceName != null) {
            Optional<ServiceInstance> serviceInstanceAAI =
                    bbInputSetupUtils.getAAIServiceInstanceByName(globalCustomerId, serviceType, instanceName);
            if (serviceInstanceAAI.isPresent()) {
                if (serviceInstanceAAI.get().getModelVersionId()
                        .equalsIgnoreCase(reqDetails.getModelInfo().getModelVersionId())) {
                    return serviceInstanceAAI.get().getServiceInstanceId();
                } else {
                    throw new DuplicateNameException(SERVICE_INSTANCE, String.format(NAME_EXISTS_WITH_DIFF_VERSION_ID,
                            instanceName, reqDetails.getModelInfo().getModelVersionId()));
                }
            } else {
                ServiceInstances aaiServiceInstances =
                        bbInputSetupUtils.getAAIServiceInstancesGloballyByName(instanceName);
                if (aaiServiceInstances != null) {
                    if (aaiServiceInstances.getServiceInstance() != null
                            && !aaiServiceInstances.getServiceInstance().isEmpty()) {
                        if (aaiServiceInstances.getServiceInstance().size() > 1) {
                            throw new DuplicateNameException(SERVICE_INSTANCE,
                                    String.format(NAME_EXISTS_MULTIPLE, instanceName));
                        } else {
                            ServiceInstance si = aaiServiceInstances.getServiceInstance().stream().findFirst().get();
                            Map<String, String> keys =
                                    bbInputSetupUtils.getURIKeysFromServiceInstance(si.getServiceInstanceId());

                            throw new DuplicateNameException(SERVICE_INSTANCE, String.format(
                                    NAME_EXISTS_WITH_DIFF_COMBINATION, instanceName,
                                    keys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId),
                                    keys.get(
                                            AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType),
                                    si.getModelVersionId()));
                        }
                    }
                }
            }
        }
        return generatedResourceId;
    }

    protected String validateNetworkResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds)
            throws DuplicateNameException, MultipleObjectsFoundException {
        Optional<L3Network> network = bbInputSetupUtils
                .getRelatedNetworkByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
        if (network.isPresent()) {
            if (network.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return network.get().getNetworkId();
            } else {
                throw new DuplicateNameException("l3Network", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, network.get().getModelCustomizationId()));
            }
        }
        if (bbInputSetupUtils.existsAAINetworksGloballyByName(instanceName)) {
            throw new DuplicateNameException("l3Network", String.format(NAME_EXISTS_WITH_DIFF_PARENT, instanceName,
                    workflowResourceIds.getServiceInstanceId()));
        }
        return generatedResourceId;
    }

    protected String validateVnfResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<GenericVnf> vnf = bbInputSetupUtils
                .getRelatedVnfByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
        if (vnf.isPresent()) {
            if (vnf.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return vnf.get().getVnfId();
            } else {
                throw new DuplicateNameException("generic-vnf", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, vnf.get().getModelCustomizationId()));
            }
        }
        GenericVnfs vnfs = bbInputSetupUtils.getAAIVnfsGloballyByName(instanceName);
        if (vnfs != null) {
            throw new DuplicateNameException("generic-vnf",
                    String.format(NAME_EXISTS_WITH_DIFF_PARENT, instanceName, vnfs.getGenericVnf().get(0).getVnfId()));
        }
        return generatedResourceId;
    }

    protected String validateVfModuleResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
        if (vnf != null && vnf.getVfModules() != null) {
            for (org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()) {
                if (vfModule.getVfModuleName().equalsIgnoreCase(instanceName)) {
                    if (vfModule.getModelCustomizationId()
                            .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                        return vfModule.getVfModuleId();
                    } else {
                        throw new DuplicateNameException("vfModule",
                                String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID, instanceName,
                                        reqDetails.getModelInfo().getModelCustomizationId()));
                    }
                }
            }
        }
        if (bbInputSetupUtils.existsAAIVfModuleGloballyByName(instanceName)) {
            throw new DuplicateNameException("vfModule", instanceName);
        }
        return generatedResourceId;
    }

    protected String validateVolumeGroupResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<VolumeGroup> volumeGroup =
                bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(workflowResourceIds.getVnfId(), instanceName);
        if (volumeGroup.isPresent()) {
            if (volumeGroup.get().getVfModuleModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return volumeGroup.get().getVolumeGroupId();
            } else {
                throw new DuplicateNameException("volumeGroup", volumeGroup.get().getVolumeGroupName());
            }
        }
        if (bbInputSetupUtils.existsAAIVolumeGroupGloballyByName(instanceName)) {
            throw new DuplicateNameException("volumeGroup", instanceName);
        }
        return generatedResourceId;
    }

    protected String validateConfigurationResourceIdInAAI(String generatedResourceId, String instanceName,
            RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws DuplicateNameException {
        Optional<org.onap.aai.domain.yang.Configuration> configuration =
                bbInputSetupUtils.getRelatedConfigurationByNameFromServiceInstance(
                        workflowResourceIds.getServiceInstanceId(), instanceName);
        if (configuration.isPresent()) {
            if (configuration.get().getModelCustomizationId()
                    .equalsIgnoreCase(reqDetails.getModelInfo().getModelCustomizationId())) {
                return configuration.get().getConfigurationId();
            } else {
                throw new DuplicateNameException("configuration", String.format(NAME_EXISTS_WITH_DIFF_CUSTOMIZATION_ID,
                        instanceName, configuration.get().getConfigurationId()));
            }
        }
        if (bbInputSetupUtils.existsAAIConfigurationGloballyByName(instanceName)) {
            throw new DuplicateNameException("configuration", instanceName);
        }
        return generatedResourceId;
    }

    private void fillExecution(DelegateExecution execution, boolean suppressRollback, String resourceId,
            WorkflowType resourceType) {
        execution.setVariable("sentSyncResponse", false);
        execution.setVariable(HOMING, false);
        execution.setVariable("calledHoming", false);
        execution.setVariable(BBConstants.G_ISTOPLEVELFLOW, true);
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
            return validateResourceIdInAAI(resource.getResourceId(), resource.getResourceType(),
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
