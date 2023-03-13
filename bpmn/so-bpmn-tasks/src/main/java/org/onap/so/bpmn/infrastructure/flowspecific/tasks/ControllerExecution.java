/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Optional;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_NAME;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_VERSION;


@Component
public class ControllerExecution {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExecution.class);
    private static final String CONTROLLER_ACTOR = "actor";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String SCOPE = "scope";
    private static final String ACTION = "action";
    private static final String BBNAME = "bbName";
    private static final String MSO_REQUEST_ID = "msoRequestId";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * Setting Controller Actor, Scope and Action Variables in BuildingBlockExecution object
     * 
     * @param execution - BuildingBlockExecution object
     */
    public void setControllerActorScopeAction(BuildingBlockExecution execution) {

        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();

        String scope = Optional.ofNullable(buildingBlock.getBpmnScope()).orElseThrow(
                () -> new NullPointerException("BPMN Scope is NULL in the orchestration_flow_reference table "));
        String action = Optional.ofNullable(buildingBlock.getBpmnAction()).orElseThrow(
                () -> new NullPointerException("BPMN Action is NULL in the orchestration_flow_reference table "));
        String controllerActor;

        try {
            if (String.valueOf(scope).equals("pnf")) {
                Pnf pnf = getPnf(execution);
                String pnfModelUUID = pnf.getModelInfoPnf().getModelCustomizationUuid();
                PnfResourceCustomization pnfResourceCustomization =
                        catalogDbClient.getPnfResourceCustomizationByModelCustomizationUUID(pnfModelUUID);

                controllerActor = Optional.ofNullable(pnfResourceCustomization.getControllerActor()).orElse("APPC");
                execution.setVariable(MSO_REQUEST_ID,
                        execution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId());
                execution.setVariable(PRC_BLUEPRINT_VERSION, pnfResourceCustomization.getBlueprintVersion());
                execution.setVariable(PRC_BLUEPRINT_NAME, pnfResourceCustomization.getBlueprintName());
            } else if ("service".equalsIgnoreCase(scope)) {
                GeneralBuildingBlock gbb = execution.getGeneralBuildingBlock();
                ModelInfoServiceInstance modelInfoServiceInstance =
                        gbb.getServiceInstance().getModelInfoServiceInstance();
                controllerActor = Optional.ofNullable(modelInfoServiceInstance.getControllerActor()).orElse("CDS");
            } else if ("nssi".equalsIgnoreCase(scope)) {
                GeneralBuildingBlock gbb = execution.getGeneralBuildingBlock();
                ModelInfoServiceInstance modelInfoServiceInstance =
                        gbb.getServiceInstance().getModelInfoServiceInstance();
                logger.info(">>>> modelInfoServiceInstance: {}", modelInfoServiceInstance);
                ModelInfoServiceInstance modelInfoServiceInstance1 = execution.getVariable("nssiModelInfo");
                logger.info(">>>> ex1: {}", modelInfoServiceInstance1);
                controllerActor = Optional.ofNullable(modelInfoServiceInstance.getControllerActor()).orElse("CDS");
            } else {
                GenericVnf genericVnf = getGenericVnf(execution);
                String modelUuid = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();
                VnfResourceCustomization vnfResourceCustomization =
                        catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(modelUuid);

                controllerActor = Optional.ofNullable(vnfResourceCustomization.getControllerActor()).orElse("APPC");
            }

            execution.setVariable(SCOPE, scope);
            execution.setVariable(ACTION, action);
            execution.setVariable(CONTROLLER_ACTOR, controllerActor);

            logger.debug("Executing Controller Execution for ControllerActor: {}, Scope: {} , Action: {}",
                    controllerActor, scope, action);
        } catch (Exception ex) {
            logger.error("An exception occurred while fetching Controller Actor,Scope and Action ", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * Selecting bbName from BBNameSelectionReference and setting the value in a variable of BuildingBlockExecution
     * 
     * @param execution - BuildingBlockExecution object
     */
    public void selectBB(BuildingBlockExecution execution) {
        try {

            String controllerActor = execution.getVariable(CONTROLLER_ACTOR);
            String action = Optional.of((String) execution.getVariable(ACTION)).get();
            String scope = Optional.of((String) execution.getVariable(SCOPE)).get();
            BBNameSelectionReference bbNameSelectionReference =
                    catalogDbClient.getBBNameSelectionReference(controllerActor, scope, action);
            String bbName = bbNameSelectionReference.getBbName();
            execution.setVariable(BBNAME, bbName);
            logger.debug(" Executing {} BPMN", bbName);
        } catch (Exception ex) {
            logger.error("An exception occurred while getting bbname from catalogdb ", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);

        }

    }

    private Pnf getPnf(BuildingBlockExecution buildingBlockExecution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.PNF);
    }

    private GenericVnf getGenericVnf(BuildingBlockExecution buildingBlockExecution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey(buildingBlockExecution, ResourceKey.GENERIC_VNF_ID);
    }
}
