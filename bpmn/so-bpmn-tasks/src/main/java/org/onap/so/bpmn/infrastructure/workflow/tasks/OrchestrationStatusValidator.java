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
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.exception.OrchestrationStatusValidationException;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private static final String ALACARTE = "aLaCarte";

    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private CatalogDbClient catalogDbClient;


    /**
     * This method validate's the status of the OrchestrationStatus against the buildingBlockDetail ResourceType
     *
     * @param execution
     */
    public void validateOrchestrationStatus(BuildingBlockExecution execution) {
        try {
            OrchestrationStatusValidationDirective previousOrchestrationStatusValidationResult =
                    execution.getVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT);

            execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT, null);

            boolean aLaCarte = (boolean) execution.getVariable(ALACARTE);

            String buildingBlockFlowName = execution.getFlowToBeCalled();

            BuildingBlockDetail buildingBlockDetail = catalogDbClient.getBuildingBlockDetail(buildingBlockFlowName);

            if (buildingBlockDetail == null) {
                throw new OrchestrationStatusValidationException(
                        String.format(BUILDING_BLOCK_DETAIL_NOT_FOUND, buildingBlockFlowName));
            }

            OrchestrationStatus orchestrationStatus;

            switch (buildingBlockDetail.getResourceType()) {
                case SERVICE:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstance =
                            extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    orchestrationStatus = serviceInstance.getOrchestrationStatus();
                    break;
                case VNF:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf genericVnf =
                            extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                    orchestrationStatus = genericVnf.getOrchestrationStatus();
                    break;
                case VF_MODULE:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                            extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                    orchestrationStatus = vfModule.getOrchestrationStatus();
                    break;
                case VOLUME_GROUP:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup =
                            extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
                    orchestrationStatus = volumeGroup.getOrchestrationStatus();
                    break;
                case NETWORK:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network =
                            extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
                    orchestrationStatus = network.getOrchestrationStatus();
                    break;
                case NETWORK_COLLECTION:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInst =
                            extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
                    org.onap.so.bpmn.servicedecomposition.bbobjects.Collection networkCollection =
                            serviceInst.getCollection();
                    orchestrationStatus = networkCollection.getOrchestrationStatus();
                    break;
                case CONFIGURATION:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration configuration =
                            extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
                    orchestrationStatus = configuration.getOrchestrationStatus();
                    break;
                case INSTANCE_GROUP:
                    org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup instanceGroup =
                            extractPojosForBB.extractByKey(execution, ResourceKey.INSTANCE_GROUP_ID);
                    orchestrationStatus = instanceGroup.getOrchestrationStatus();
                    break;
                case NO_VALIDATE:
                    // short circuit and exit method
                    execution.setVariable(ORCHESTRATION_STATUS_VALIDATION_RESULT,
                            OrchestrationStatusValidationDirective.VALIDATION_SKIPPED);
                    return;
                default:
                    // can't currently get here, so not tested. Added in case enum is expanded
                    // without a change to this
                    // code
                    throw new OrchestrationStatusValidationException(
                            String.format(UNKNOWN_RESOURCE_TYPE, buildingBlockFlowName,
                                    buildingBlockDetail.getResourceType(), buildingBlockDetail.getTargetAction()));
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
}
