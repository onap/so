/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.Vnfc;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.common.listener.db.RequestsDbListenerRunner;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulatorListenerRunner;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.Configuration;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowActionBBTasks {

    private static final String G_CURRENT_SEQUENCE = "gCurrentSequence";
    private static final String G_REQUEST_ID = "mso-request-id";
    private static final String G_ALACARTE = "aLaCarte";
    private static final String G_ACTION = "requestAction";
    private static final String RETRY_COUNT = "retryCount";
    private static final String FABRIC_CONFIGURATION = "FabricConfiguration";
    private static final String ADD_FABRIC_CONFIGURATION_BB = "AddFabricConfigurationBB";
    private static final String COMPLETED = "completed";
    private static final String HANDLINGCODE = "handlingCode";
    private static final String ROLLBACKTOCREATED = "RollbackToCreated";
    protected String maxRetries = "mso.rainyDay.maxRetries";
    private static final Logger logger = LoggerFactory.getLogger(WorkflowActionBBTasks.class);

    @Autowired
    private RequestsDbClient requestDbclient;
    @Autowired
    private WorkflowAction workflowAction;
    @Autowired
    private WorkflowActionBBFailure workflowActionBBFailure;
    @Autowired
    private Environment environment;
    @Autowired
    private BBInputSetupUtils bbInputSetupUtils;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private FlowManipulatorListenerRunner flowManipulatorListenerRunner;
    @Autowired
    private RequestsDbListenerRunner requestsDbListener;

    public void selectBB(DelegateExecution execution) {
        List<ExecuteBuildingBlock> flowsToExecute =
                (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        execution.setVariable("MacroRollback", false);

        flowManipulatorListenerRunner.modifyFlows(flowsToExecute, new DelegateExecutionImpl(execution));
        int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);

        ExecuteBuildingBlock ebb = flowsToExecute.get(currentSequence);

        execution.setVariable("buildingBlock", ebb);
        currentSequence++;
        if (currentSequence >= flowsToExecute.size()) {
            execution.setVariable(COMPLETED, true);
        } else {
            execution.setVariable(COMPLETED, false);
        }
        execution.setVariable(G_CURRENT_SEQUENCE, currentSequence);
    }

    public void updateFlowStatistics(DelegateExecution execution) {
        try {
            int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
            if (currentSequence > 1) {
                InfraActiveRequests request = this.getUpdatedRequest(execution, currentSequence);
                requestDbclient.updateInfraActiveRequests(request);
            }
        } catch (Exception ex) {
            logger.warn(
                    "Bpmn Flow Statistics was unable to update Request Db with the new completion percentage. Competion percentage may be invalid.",
                    ex);
        }
    }

    protected InfraActiveRequests getUpdatedRequest(DelegateExecution execution, int currentSequence) {
        List<ExecuteBuildingBlock> flowsToExecute =
                (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        String requestId = (String) execution.getVariable(G_REQUEST_ID);
        InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
        ExecuteBuildingBlock completedBB = flowsToExecute.get(currentSequence - 2);
        ExecuteBuildingBlock nextBB = flowsToExecute.get(currentSequence - 1);
        int completedBBs = currentSequence - 1;
        int totalBBs = flowsToExecute.size();
        int remainingBBs = totalBBs - completedBBs;
        String statusMessage = this.getStatusMessage(completedBB.getBuildingBlock().getBpmnFlowName(),
                nextBB.getBuildingBlock().getBpmnFlowName(), completedBBs, remainingBBs);
        Long percentProgress = this.getPercentProgress(completedBBs, totalBBs);
        request.setFlowStatus(statusMessage);
        request.setProgress(percentProgress);
        request.setLastModifiedBy("CamundaBPMN");
        return request;
    }

    protected Long getPercentProgress(int completedBBs, int totalBBs) {
        double ratio = (completedBBs / (totalBBs * 1.0));
        int percentProgress = (int) (ratio * 95);
        return new Long(percentProgress + 5);
    }

    protected String getStatusMessage(String completedBB, String nextBB, int completedBBs, int remainingBBs) {
        return "Execution of " + completedBB + " has completed successfully, next invoking " + nextBB
                + " (Execution Path progress: BBs completed = " + completedBBs + "; BBs remaining = " + remainingBBs
                + ").";
    }

    public void sendSyncAck(DelegateExecution execution) {
        final String requestId = (String) execution.getVariable(G_REQUEST_ID);
        final String resourceId = (String) execution.getVariable("resourceId");
        ServiceInstancesResponse serviceInstancesResponse = new ServiceInstancesResponse();
        RequestReferences requestRef = new RequestReferences();
        requestRef.setInstanceId(resourceId);
        requestRef.setRequestId(requestId);
        serviceInstancesResponse.setRequestReferences(requestRef);
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = "";
        try {
            jsonRequest = mapper.writeValueAsString(serviceInstancesResponse);
        } catch (JsonProcessingException e) {
            workflowAction.buildAndThrowException(execution,
                    "Could not marshall ServiceInstancesRequest to Json string to respond to API Handler.", e);
        }
        WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
        callbackResponse.setStatusCode(200);
        callbackResponse.setMessage("Success");
        callbackResponse.setResponse(jsonRequest);
        String processKey = execution.getProcessEngineServices().getRepositoryService()
                .getProcessDefinition(execution.getProcessDefinitionId()).getKey();
        WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId,
                callbackResponse);
        logger.info("Successfully sent sync ack.");
        updateInstanceId(execution);
    }

    public void sendErrorSyncAck(DelegateExecution execution) {
        final String requestId = (String) execution.getVariable(G_REQUEST_ID);
        try {
            ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
            String errorMsg = (String) execution.getVariable("WorkflowActionErrorMessage");
            if (errorMsg == null) {
                errorMsg = "WorkflowAction failed unexpectedly.";
            }
            String processKey = exceptionBuilder.getProcessKey(execution);
            String buildworkflowException =
                    "<aetgt:WorkflowException xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"><aetgt:ErrorMessage>"
                            + errorMsg
                            + "</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException>";
            WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
            callbackResponse.setStatusCode(500);
            callbackResponse.setMessage("Fail");
            callbackResponse.setResponse(buildworkflowException);
            WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId,
                    callbackResponse);
            execution.setVariable("sentSyncResponse", true);
        } catch (Exception ex) {
            logger.error(" Sending Sync Error Activity Failed. {}", ex.getMessage(), ex);
        }
    }

    public void updateRequestStatusToComplete(DelegateExecution execution) {
        try {
            final String requestId = (String) execution.getVariable(G_REQUEST_ID);
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
            final String action = (String) execution.getVariable(G_ACTION);
            final boolean aLaCarte = (boolean) execution.getVariable(G_ALACARTE);
            final String resourceName = (String) execution.getVariable("resourceName");
            String statusMessage = (String) execution.getVariable("StatusMessage");
            String macroAction = "";
            if (statusMessage == null) {
                if (aLaCarte) {
                    macroAction = "ALaCarte-" + resourceName + "-" + action + " request was executed correctly.";
                } else {
                    macroAction = "Macro-" + resourceName + "-" + action + " request was executed correctly.";
                }
            } else {
                macroAction = statusMessage;
            }
            execution.setVariable("finalStatusMessage", macroAction);
            Timestamp endTime = new Timestamp(System.currentTimeMillis());
            request.setEndTime(endTime);
            request.setFlowStatus("Successfully completed all Building Blocks");
            request.setStatusMessage(macroAction);
            request.setProgress(Long.valueOf(100));
            request.setRequestStatus("COMPLETE");
            request.setLastModifiedBy("CamundaBPMN");
            requestsDbListener.post(request, new DelegateExecutionImpl(execution));
            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception ex) {
            workflowAction.buildAndThrowException(execution, "Error Updating Request Database", ex);
        }
    }

    public void checkRetryStatus(DelegateExecution execution) {
        String handlingCode = (String) execution.getVariable(HANDLINGCODE);
        String requestId = (String) execution.getVariable(G_REQUEST_ID);
        String retryDuration = (String) execution.getVariable("RetryDuration");
        int retryCount = (int) execution.getVariable(RETRY_COUNT);
        int envMaxRetries;
        try {
            envMaxRetries = Integer.parseInt(this.environment.getProperty(maxRetries));
        } catch (Exception ex) {
            logger.error("Could not read maxRetries from config file. Setting max to 5 retries", ex);
            envMaxRetries = 5;
        }
        int nextCount = retryCount + 1;
        if ("Retry".equals(handlingCode)) {
            workflowActionBBFailure.updateRequestErrorStatusMessage(execution);
            try {
                InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
                request.setRetryStatusMessage(
                        "Retry " + nextCount + "/" + envMaxRetries + " will be started in " + retryDuration);
                requestDbclient.updateInfraActiveRequests(request);
            } catch (Exception ex) {
                logger.warn("Failed to update Request Db Infra Active Requests with Retry Status", ex);
            }
            if (retryCount < envMaxRetries) {
                int currSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
                execution.setVariable(G_CURRENT_SEQUENCE, currSequence - 1);
                execution.setVariable(RETRY_COUNT, nextCount);
            } else {
                workflowAction.buildAndThrowException(execution,
                        "Exceeded maximum retries. Ending flow with status Abort");
            }
        } else {
            execution.setVariable(RETRY_COUNT, 0);
        }
    }

    /**
     * Rollback will only handle Create/Activate/Assign Macro flows. Execute layer will rollback the flow its currently
     * working on.
     */
    public void rollbackExecutionPath(DelegateExecution execution) {
        if (!(boolean) execution.getVariable("isRollback")) {
            List<ExecuteBuildingBlock> flowsToExecute =
                    (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
            List<ExecuteBuildingBlock> rollbackFlows = new ArrayList();
            int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
            int listSize = flowsToExecute.size();
            for (int i = listSize - 1; i >= 0; i--) {
                if (i > currentSequence - 1) {
                    flowsToExecute.remove(i);
                } else {
                    String flowName = flowsToExecute.get(i).getBuildingBlock().getBpmnFlowName();
                    if (flowName.startsWith("Assign")) {
                        flowName = flowName.replaceFirst("Assign", "Unassign");
                    } else if (flowName.startsWith("Create")) {
                        flowName = flowName.replaceFirst("Create", "Delete");
                    } else if (flowName.startsWith("Activate")) {
                        flowName = flowName.replaceFirst("Activate", "Deactivate");
                    } else if (flowName.startsWith("Add")) {
                        flowName = flowName.replaceFirst("Add", "Delete");
                    } else {
                        continue;
                    }
                    flowsToExecute.get(i).getBuildingBlock().setBpmnFlowName(flowName);
                    rollbackFlows.add(flowsToExecute.get(i));
                }
            }

            String handlingCode = (String) execution.getVariable(HANDLINGCODE);
            List<ExecuteBuildingBlock> rollbackFlowsFiltered = new ArrayList<>();
            rollbackFlowsFiltered.addAll(rollbackFlows);
            if ("RollbackToAssigned".equals(handlingCode) || ROLLBACKTOCREATED.equals(handlingCode)) {
                for (int i = 0; i < rollbackFlows.size(); i++) {
                    if (rollbackFlows.get(i).getBuildingBlock().getBpmnFlowName().contains("Unassign") && !rollbackFlows
                            .get(i).getBuildingBlock().getBpmnFlowName().contains("FabricConfiguration")) {
                        rollbackFlowsFiltered.remove(rollbackFlows.get(i));
                    } else if (rollbackFlows.get(i).getBuildingBlock().getBpmnFlowName().contains("Delete")
                            && ROLLBACKTOCREATED.equals(handlingCode)) {
                        rollbackFlowsFiltered.remove(rollbackFlows.get(i));
                    }
                }
            }

            workflowActionBBFailure.updateRequestErrorStatusMessage(execution);
            if (rollbackFlows.isEmpty())
                execution.setVariable("isRollbackNeeded", false);
            else
                execution.setVariable("isRollbackNeeded", true);
            execution.setVariable("flowsToExecute", rollbackFlowsFiltered);
            execution.setVariable(HANDLINGCODE, "PreformingRollback");
            execution.setVariable("isRollback", true);
            execution.setVariable(G_CURRENT_SEQUENCE, 0);
            execution.setVariable(RETRY_COUNT, 0);
        } else {
            workflowAction.buildAndThrowException(execution,
                    "Rollback has already been called. Cannot rollback a request that is currently in the rollback state.");
        }
    }

    protected void updateInstanceId(DelegateExecution execution) {
        try {
            String requestId = (String) execution.getVariable(G_REQUEST_ID);
            String resourceId = (String) execution.getVariable("resourceId");
            WorkflowType resourceType = (WorkflowType) execution.getVariable("resourceType");
            InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
            if (resourceType == WorkflowType.SERVICE) {
                request.setServiceInstanceId(resourceId);
            } else if (resourceType == WorkflowType.VNF) {
                request.setVnfId(resourceId);
            } else if (resourceType == WorkflowType.VFMODULE) {
                request.setVfModuleId(resourceId);
            } else if (resourceType == WorkflowType.VOLUMEGROUP) {
                request.setVolumeGroupId(resourceId);
            } else if (resourceType == WorkflowType.NETWORK) {
                request.setNetworkId(resourceId);
            } else if (resourceType == WorkflowType.CONFIGURATION) {
                request.setConfigurationId(resourceId);
            } else if (resourceType == WorkflowType.INSTANCE_GROUP) {
                request.setInstanceGroupId(resourceId);
            }
            setInstanceName(resourceId, resourceType, request);
            request.setLastModifiedBy("CamundaBPMN");
            requestDbclient.updateInfraActiveRequests(request);
        } catch (Exception ex) {
            logger.error("Exception in updateInstanceId", ex);
            workflowAction.buildAndThrowException(execution, "Failed to update Request db with instanceId");
        }
    }

    public void postProcessingExecuteBB(DelegateExecution execution) {
        List<ExecuteBuildingBlock> flowsToExecute =
                (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        String handlingCode = (String) execution.getVariable(HANDLINGCODE);
        final boolean aLaCarte = (boolean) execution.getVariable(G_ALACARTE);
        int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
        ExecuteBuildingBlock ebb = flowsToExecute.get(currentSequence - 1);
        String bbFlowName = ebb.getBuildingBlock().getBpmnFlowName();
        if ("ActivateVfModuleBB".equalsIgnoreCase(bbFlowName) && aLaCarte && "Success".equalsIgnoreCase(handlingCode)) {
            postProcessingExecuteBBActivateVfModule(execution, ebb, flowsToExecute);
        }
    }

    protected void postProcessingExecuteBBActivateVfModule(DelegateExecution execution, ExecuteBuildingBlock ebb,
            List<ExecuteBuildingBlock> flowsToExecute) {
        try {
            String serviceInstanceId = ebb.getWorkflowResourceIds().getServiceInstanceId();
            String vnfId = ebb.getWorkflowResourceIds().getVnfId();
            String vfModuleId = ebb.getResourceId();
            ebb.getWorkflowResourceIds().setVfModuleId(vfModuleId);
            String serviceModelUUID =
                    bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId).getModelVersionId();
            String vnfCustomizationUUID = bbInputSetupUtils.getAAIGenericVnf(vnfId).getModelCustomizationId();
            String vfModuleCustomizationUUID =
                    bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId).getModelCustomizationId();
            List<Vnfc> vnfcs =
                    workflowAction.getRelatedResourcesInVfModule(vnfId, vfModuleId, Vnfc.class, AAIObjectType.VNFC);
            logger.debug("Vnfc Size: {}", vnfcs.size());
            for (Vnfc vnfc : vnfcs) {
                String modelCustomizationId = vnfc.getModelCustomizationId();
                logger.debug("Processing Vnfc: {}", modelCustomizationId);
                CvnfcConfigurationCustomization fabricConfig = catalogDbClient.getCvnfcCustomization(serviceModelUUID,
                        vnfCustomizationUUID, vfModuleCustomizationUUID, modelCustomizationId);
                if (fabricConfig != null && fabricConfig.getConfigurationResource() != null
                        && fabricConfig.getConfigurationResource().getToscaNodeType() != null
                        && fabricConfig.getConfigurationResource().getToscaNodeType().contains(FABRIC_CONFIGURATION)) {
                    String configurationId = getConfigurationId(vnfc);
                    ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
                    configurationResourceKeys.setCvnfcCustomizationUUID(modelCustomizationId);
                    configurationResourceKeys.setVfModuleCustomizationUUID(vfModuleCustomizationUUID);
                    configurationResourceKeys.setVnfResourceCustomizationUUID(vnfCustomizationUUID);
                    configurationResourceKeys.setVnfcName(vnfc.getVnfcName());
                    ExecuteBuildingBlock addConfigBB = getExecuteBBForConfig(ADD_FABRIC_CONFIGURATION_BB, ebb,
                            configurationId, configurationResourceKeys);
                    flowsToExecute.add(addConfigBB);
                    flowsToExecute.stream()
                            .forEach(executeBB -> logger.info("Flows to Execute After Post Processing: {}",
                                    executeBB.getBuildingBlock().getBpmnFlowName()));
                    execution.setVariable("flowsToExecute", flowsToExecute);
                    execution.setVariable(COMPLETED, false);
                } else {
                    logger.debug("No cvnfcCustomization found for customizationId: {}", modelCustomizationId);
                }
            }
        } catch (EntityNotFoundException e) {
            logger.debug("Will not be running Fabric Config Building Blocks", e);
        } catch (Exception e) {
            String errorMessage = "Error occurred in post processing of Vf Module create";
            execution.setVariable(HANDLINGCODE, ROLLBACKTOCREATED);
            execution.setVariable("WorkflowActionErrorMessage", errorMessage);
            logger.error(errorMessage, e);
        }
    }

    protected String getConfigurationId(Vnfc vnfc) {
        List<Configuration> configurations =
                workflowAction.getRelatedResourcesInVnfc(vnfc, Configuration.class, AAIObjectType.CONFIGURATION);
        if (!configurations.isEmpty()) {
            Configuration configuration = configurations.get(0);
            return configuration.getConfigurationId();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    protected ExecuteBuildingBlock getExecuteBBForConfig(String bbName, ExecuteBuildingBlock ebb,
            String configurationId, ConfigurationResourceKeys configurationResourceKeys) {
        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName(bbName).setMsoId(UUID.randomUUID().toString());

        WorkflowResourceIds workflowResourceIds = ebb.getWorkflowResourceIds();
        workflowResourceIds.setConfigurationId(configurationId);
        ExecuteBuildingBlock configBB = new ExecuteBuildingBlock().setaLaCarte(ebb.isaLaCarte())
                .setApiVersion(ebb.getApiVersion()).setRequestAction(ebb.getRequestAction())
                .setVnfType(ebb.getVnfType()).setRequestId(ebb.getRequestId())
                .setRequestDetails(ebb.getRequestDetails()).setBuildingBlock(buildingBlock)
                .setWorkflowResourceIds(workflowResourceIds).setConfigurationResourceKeys(configurationResourceKeys);
        return configBB;
    }

    protected void setInstanceName(String resourceId, WorkflowType resourceType, InfraActiveRequests request) {
        logger.debug("Setting instanceName in infraActiveRequest");
        try {
            if (resourceType == WorkflowType.SERVICE && request.getServiceInstanceName() == null) {
                ServiceInstance service = bbInputSetupUtils.getAAIServiceInstanceById(resourceId);
                if (service != null) {
                    request.setServiceInstanceName(service.getServiceInstanceName());
                }
            } else if (resourceType == WorkflowType.VNF && request.getVnfName() == null) {
                GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(resourceId);
                if (vnf != null) {
                    request.setVnfName(vnf.getVnfName());
                }
            } else if (resourceType == WorkflowType.VFMODULE && request.getVfModuleName() == null) {
                VfModule vfModule = bbInputSetupUtils.getAAIVfModule(request.getVnfId(), resourceId);
                if (vfModule != null) {
                    request.setVfModuleName(vfModule.getVfModuleName());
                }
            } else if (resourceType == WorkflowType.VOLUMEGROUP && request.getVolumeGroupName() == null) {
                Optional<VolumeGroup> volumeGroup =
                        bbInputSetupUtils.getRelatedVolumeGroupByIdFromVnf(request.getVnfId(), resourceId);
                if (volumeGroup.isPresent()) {
                    request.setVolumeGroupName(volumeGroup.get().getVolumeGroupName());
                }
            } else if (resourceType == WorkflowType.NETWORK && request.getNetworkName() == null) {
                L3Network network = bbInputSetupUtils.getAAIL3Network(resourceId);
                if (network != null) {
                    request.setNetworkName(network.getNetworkName());
                }
            } else if (resourceType == WorkflowType.CONFIGURATION && request.getConfigurationName() == null) {
                org.onap.aai.domain.yang.Configuration configuration =
                        bbInputSetupUtils.getAAIConfiguration(resourceId);
                if (configuration != null) {
                    request.setConfigurationName(configuration.getConfigurationName());
                }
            } else if (resourceType == WorkflowType.INSTANCE_GROUP && request.getInstanceGroupName() == null) {
                InstanceGroup instanceGroup = bbInputSetupUtils.getAAIInstanceGroup(resourceId);
                if (instanceGroup != null) {
                    request.setInstanceGroupName(instanceGroup.getInstanceGroupName());
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in setInstanceName", ex);
        }
    }
}
