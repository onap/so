/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.OrchestrationStatusValidationException;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import java.util.EnumSet;
import java.util.Set;

@Component
public class OrchestrationStatusValidator {
    private static final Logger logger = LoggerFactory.getLogger(OrchestrationStatusValidator.class);

    private static final String BUILDING_BLOCK_DETAIL_NOT_FOUND =
            "Building Block (%s) not set up in Orchestration_Status_Validation table in CatalogDB.";
    private static final String UNKNOWN_RESOURCE_TYPE =
            "Building Block (%s) not set up correctly in Orchestration_Status_Validation table in CatalogDB. ResourceType=(%s), TargetAction=(%s)";
    private static final String ORCHESTRATION_VALIDATION_FAIL =
            "Orchestration Status Validation failed. ResourceType=(%s), TargetAction=(%s), OrchestrationStatus=(%s)";
    private static final String ORCHESTRATION_STATUS_VALIDATION_RESULT = "orchestrationStatusValidationResult";
    private static final String RESOURCE_EXIST_STATUS_MESSAGE =
            "The %s was found to already exist, thus no new %s was created in the cloud via this request";
    private static final String RESOURCE_NOT_EXIST_STATUS_MESSAGE =
            "The %s was not found, thus no %s was deleted in the cloud via this request";
    private static final Set<ResourceType> cloudResources =
            EnumSet.of(ResourceType.VF_MODULE, ResourceType.VOLUME_GROUP, ResourceType.NETWORK);

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    RequestsDbClient requestDBClient;


    /**
     * This method validate's the status of the OrchestrationStatus against the buildingBlockDetail ResourceType
     */
    public void validateOrchestrationStatus(BuildingBlockExecution execution) {
        try {
            execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT, null);
            String buildingBlockFlowName = execution.getFlowToBeCalled();
            BuildingBlockDetail buildingBlockDetail = catalogDbClient.getBuildingBlockDetail(buildingBlockFlowName);

            if (buildingBlockDetail == null) {
                throw new OrchestrationStatusValidationException(
                        String.format(BUILDING_BLOCK_DETAIL_NOT_FOUND, buildingBlockFlowName));
            }

            OrchestrationStatus orchestrationStatus =
                    getOrchestrationStatus(execution, buildingBlockFlowName, buildingBlockDetail);
            if (buildingBlockDetail.getResourceType().equals(ResourceType.NO_VALIDATE)) {
                return;
            }

            if (orchestrationStatus == null) {
                throw new OrchestrationStatusValidationException(
                        "The resource's orchstration status is null. Cannot perform task on a null orchestration status");
            }
            OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = catalogDbClient
                    .getOrchestrationStatusStateTransitionDirective(buildingBlockDetail.getResourceType(),
                            orchestrationStatus, buildingBlockDetail.getTargetAction());

            if (orchestrationStatusStateTransitionDirective
                    .getFlowDirective() == OrchestrationStatusValidationDirective.FAIL) {
                throw new OrchestrationStatusValidationException(
                        String.format(ORCHESTRATION_VALIDATION_FAIL, buildingBlockDetail.getResourceType(),
                                buildingBlockDetail.getTargetAction(), orchestrationStatus));
            }

            execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT,
                    orchestrationStatusStateTransitionDirective.getFlowDirective());

            if (buildingBlockFlowName.matches("Create(.*)|Delete(.*)") && orchestrationStatusStateTransitionDirective
                    .getFlowDirective() == OrchestrationStatusValidationDirective.SILENT_SUCCESS) {

                updatedResourceStatus(execution, buildingBlockDetail);
            }

        } catch (BBObjectNotFoundException ex) {
            logger.error(
                    "Error occurred for bb object notfound in OrchestrationStatusValidator validateOrchestrationStatus ",
                    ex);
            if (execution.getFlowToBeCalled().contains("Unassign")) {
                execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT,
                        OrchestrationStatusValidationDirective.SILENT_SUCCESS);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, ex);
            }
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e);
        }
    }

    private OrchestrationStatus getOrchestrationStatus(BuildingBlockExecution execution, String buildingBlockFlowName,
            BuildingBlockDetail buildingBlockDetail)
            throws BBObjectNotFoundException, OrchestrationStatusValidationException {
        OrchestrationStatus orchestrationStatus = null;

        switch (buildingBlockDetail.getResourceType()) {
            case SERVICE:
                ServiceInstance serviceInstance =
                        extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                orchestrationStatus = serviceInstance.getOrchestrationStatus();
                break;
            case VNF:
                GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                orchestrationStatus = genericVnf.getOrchestrationStatus();
                break;
            case VF_MODULE:
                VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                orchestrationStatus = vfModule.getOrchestrationStatus();
                break;
            case VOLUME_GROUP:
                VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
                orchestrationStatus = volumeGroup.getOrchestrationStatus();
                break;
            case NETWORK:
                L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
                orchestrationStatus = network.getOrchestrationStatus();
                break;
            case NETWORK_COLLECTION:
                Collection networkCollection = getNetworkCollection(execution);
                orchestrationStatus = networkCollection.getOrchestrationStatus();
                break;
            case CONFIGURATION:
                Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
                orchestrationStatus = configuration.getOrchestrationStatus();
                break;
            case INSTANCE_GROUP:
                InstanceGroup instanceGroup = extractPojosForBB.extractByKey(execution, ResourceKey.INSTANCE_GROUP_ID);
                orchestrationStatus = instanceGroup.getOrchestrationStatus();
                break;
            case NO_VALIDATE:
                // short circuit and exit method
                execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT,
                        OrchestrationStatusValidationDirective.VALIDATION_SKIPPED);
                break;
            default:
                // can't currently get here, so not tested. Added in case enum is expanded
                // without a change to this
                // code
                throw new OrchestrationStatusValidationException(
                        String.format(UNKNOWN_RESOURCE_TYPE, buildingBlockFlowName,
                                buildingBlockDetail.getResourceType(), buildingBlockDetail.getTargetAction()));
        }
        return orchestrationStatus;
    }

    private Collection getNetworkCollection(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        ServiceInstance serviceInst = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        return serviceInst.getCollection();
    }

    private void updatedResourceStatus(BuildingBlockExecution execution, BuildingBlockDetail buildingBlockDetail) {

        if (cloudResources.contains(buildingBlockDetail.getResourceType())) {
            String resource = buildingBlockDetail.getResourceType().toString();

            String resourceId = execution.getLookupMap()
                    .get(ResourceKey.valueOf(buildingBlockDetail.getResourceType().getResourceKey()));

            String resourceStatusMessage = RESOURCE_NOT_EXIST_STATUS_MESSAGE;
            if (execution.getFlowToBeCalled().matches("Create(.*)")) {
                resourceStatusMessage = RESOURCE_EXIST_STATUS_MESSAGE;
            }

            updateRequestsDb(resourceId, String.format(resourceStatusMessage, resource, resource));
        }

    }

    private void updateRequestsDb(String requestId, String resourceStatusMessage) {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId(requestId);
        request.setResourceStatusMessage(resourceStatusMessage);
        try {
            requestDBClient.patchInfraActiveRequests(request);
        } catch (HttpClientErrorException e) {
            logger.warn("Unable to update active request resource status");
        }
    }
}
