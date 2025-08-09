/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.PreFlowManipulator;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkipCDSBuildingBlockListener implements PreFlowManipulator {

    private static Logger logger = LoggerFactory.getLogger(SkipCDSBuildingBlockListener.class);

    @Autowired
    private CatalogDbClient catalogDbClient;

    private Set<String> vnfActions = new HashSet<String>(Arrays.asList("config-assign", "config-deploy",
            "VnfConfigAssign", "VnfConfigDeploy", "config-upgrade-assign", "config-upgrade-deploy"));

    private Set<String> vFModuleAction =
            new HashSet<String>(Arrays.asList("VfModuleConfigAssign", "VfModuleConfigDeploy"));

    private Set<String> pnfActions =
            new HashSet<>(Arrays.asList("config-assign", "config-deploy", "PnfConfigAssign", "PnfConfigDeploy"));

    @Override
    public boolean shouldRunFor(String currentBBName, boolean isFirst, BuildingBlockExecution execution) {

        return "ControllerExecutionBB".equals(currentBBName);
    }

    /**
     * Skip the CDS Building block according to the Skip Flag.
     *
     * @param flowsToExecute - List of ExecuteBuildingBlock object.
     * @param execution - BuildingBlockExecution object
     * @param currentBB - ExecuteBuildingBlock object
     *
     */
    @Override
    public void run(List<ExecuteBuildingBlock> flowsToExecute, ExecuteBuildingBlock currentBB,
            BuildingBlockExecution execution) {
        String resourceKey = currentBB.getBuildingBlock().getKey();
        List<Resource> resources = execution.getVariable("resources");
        Resource resource = resources.stream().filter(r -> resourceKey.equals(r.getResourceId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resource not found for key:" + resourceKey));

        String scope = currentBB.getBuildingBlock().getBpmnScope();

        if ("SERVICE".equalsIgnoreCase(scope)) {
            Service service = catalogDbClient.getServiceByID(resource.getModelVersionId());
            currentSequenceSkipCheck(execution, service.getSkipPostInstConf());
        } else if ("VNF".equalsIgnoreCase(scope) && containsIgnoreCaseAction(currentBB, vnfActions)) {
            VnfResourceCustomization vrc = catalogDbClient
                    .getVnfResourceCustomizationByModelCustomizationUUID(resource.getModelCustomizationId());
            if (vrc != null) {
                logger.debug("getSkipPostInstConf value: {}", vrc.getSkipPostInstConf());
                boolean skipConfigVNF = vrc.getSkipPostInstConf();
                currentSequenceSkipCheck(execution, skipConfigVNF);
            }
        } else if (currentBB.getBuildingBlock().getBpmnScope().equalsIgnoreCase("VFModule")
                && containsIgnoreCaseAction(currentBB, vFModuleAction)) {
            VfModuleCustomization vfc = catalogDbClient
                    .getVfModuleCustomizationByModelCuztomizationUUID(resource.getModelCustomizationId());
            if (null != vfc) {
                logger.debug("getSkipPostInstConf value: {}", vfc.getSkipPostInstConf().booleanValue());
                boolean skipVfModule = vfc.getSkipPostInstConf();
                currentSequenceSkipCheck(execution, skipVfModule);
            }
        } else if (currentBB.getBuildingBlock().getBpmnScope().equalsIgnoreCase("PNF")
                && containsIgnoreCaseAction(currentBB, pnfActions)) {
            PnfResourceCustomization pnfResourceCustomization = catalogDbClient
                    .getPnfResourceCustomizationByModelCustomizationUUID(resource.getModelCustomizationId());

            if (null != pnfResourceCustomization) {
                logger.debug("getSkipPostInstConf value: {}", pnfResourceCustomization.getSkipPostInstConf());
                boolean skipConfigPNF = pnfResourceCustomization.getSkipPostInstConf();
                currentSequenceSkipCheck(execution, skipConfigPNF);
            }
        }
    }

    private boolean containsIgnoreCaseAction(ExecuteBuildingBlock currentBB, Set<String> actions) {
        return actions.stream().filter(action -> action.equalsIgnoreCase(currentBB.getBuildingBlock().getBpmnAction()))
                .findFirst().isPresent();
    }


    private void currentSequenceSkipCheck(BuildingBlockExecution execution, boolean skipModule) {
        if (skipModule) {
            int currentSequence = execution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
            execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, currentSequence + 1);
        }
    }

}
