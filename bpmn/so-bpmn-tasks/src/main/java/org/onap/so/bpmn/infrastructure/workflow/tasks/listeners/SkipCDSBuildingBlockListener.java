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
import org.apache.logging.log4j.util.Strings;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulator;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class SkipCDSBuildingBlockListener implements FlowManipulator {

    @Autowired
    private CatalogDbClient catalogDbClient;

    private Set<String> vnfActions =
            new HashSet<String>(Arrays.asList("config-assign", "config-deploy", "VnfConfigAssign", "VnfConfigDeploy"));

    private Set<String> vFModuleAction =
            new HashSet<String>(Arrays.asList("VfModuleConfigAssign", "VfModuleConfigDeploy"));

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
        String customizationUUID = currentBB.getBuildingBlock().getKey();
      
        if (Strings.isNotEmpty(customizationUUID)) {
            if (currentBB.getBuildingBlock().getBpmnScope().equalsIgnoreCase("VNF") && (vnfActions.stream()
                    .filter(action -> action.equalsIgnoreCase(currentBB.getBuildingBlock().getBpmnAction())).findFirst()
                    .isPresent())) {
              
                List<VnfResourceCustomization> vnfResourceCustomizations =
                        catalogDbClient.getVnfResourceCustomizationByModelUuid(
                                currentBB.getRequestDetails().getModelInfo().getModelUuid());
                if (!CollectionUtils.isEmpty(vnfResourceCustomizations)) {
                    VnfResourceCustomization vrc = catalogDbClient.findVnfResourceCustomizationInList(customizationUUID,
                            vnfResourceCustomizations);
                    if (null != vrc) {
                        boolean skipConfigVNF = vrc.isSkipPostInstConf();
                        currentSequenceSkipCheck(execution, skipConfigVNF);
                    }

                }
            } else if (currentBB.getBuildingBlock().getBpmnScope().equalsIgnoreCase("VFModule") && (vFModuleAction
                    .stream().filter(action -> action.equalsIgnoreCase(currentBB.getBuildingBlock().getBpmnAction())))
                            .findFirst().isPresent()) {

                VfModuleCustomization vfc =
                        catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(customizationUUID);

                if (null != vfc) {
                    boolean skipVfModule = vfc.isSkipPostInstConf();
                    currentSequenceSkipCheck(execution, skipVfModule);
                }
            }
        }

    }

    private void currentSequenceSkipCheck(BuildingBlockExecution execution, boolean skipModule) {
        if (skipModule) {
            int currentSequence = execution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
            execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, currentSequence + 1);
        }
    }

}
