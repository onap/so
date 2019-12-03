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
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerExecution {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExecution.class);
    private static final String CONTROLLER_ACTOR = "controller_actor";
    private static final String BUILDING_BLOCK = "buildingBlock";
    private static final String SCOPE = "scope";
    private static final String ACTION = "action";
    private static final String BBNAME = "bbName";
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    private String scope;
    private String action;


    /**
     * Setting Controller Actor, Scope and Action Variables in BuildingBlockExecution object
     * 
     * @param execution
     */
    public void setControllerActorScopeAction(BuildingBlockExecution execution) {
        try {
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String modleUuid = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();
            VnfResourceCustomization vnfResourceCustomization =
                    catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(modleUuid);

            // Fetching Controller Actor at VNF level if null then Actor is set as APPC
            String controller_actor = Optional.ofNullable(vnfResourceCustomization.getControllerActor()).orElse("APPC");
            ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
            BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();
            this.scope = Optional.ofNullable(buildingBlock.getBpmnScope()).orElseThrow(
                    () -> new NullPointerException("BPMN Scope is NULL in the orchestration_flow_reference table "));
            this.action = Optional.ofNullable(buildingBlock.getBpmnAction()).orElseThrow(
                    () -> new NullPointerException("BPMN Action is NULL in the orchestration_flow_reference table "));
            execution.setVariable(SCOPE, scope);
            execution.setVariable(ACTION, action);
            execution.setVariable(CONTROLLER_ACTOR, controller_actor);
            logger.debug("Executing Controller Execution for ControllerActor:" + controller_actor + ", Scope:" + scope
                    + ", Action:" + action);

        } catch (Exception ex) {
            logger.error("An exception occurred while fetching Controller Actor,Scope and Action ", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * Selecting bbName from BBNameSelectionReference and setting the value in a variable of BuildingBlockExecution
     * 
     * @param execution
     */
    public void selectBB(BuildingBlockExecution execution) {
        try {

            String actor = execution.getVariable(CONTROLLER_ACTOR);
            action = Optional.of((String) execution.getVariable(ACTION)).get();
            scope = Optional.of((String) execution.getVariable(SCOPE)).get();
            BBNameSelectionReference bbNameSelectionReference =
                    catalogDbClient.getBBNameSelectionReference(actor, this.scope, this.action);
            String bbName = bbNameSelectionReference.getBB_NAME();
            execution.setVariable(BBNAME, bbName);
            logger.debug(" Executing " + bbName + " BPMN");
        } catch (Exception ex) {
            logger.error("An exception occurred while getting bbname from catalogdb ", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);

        }

    }
}
