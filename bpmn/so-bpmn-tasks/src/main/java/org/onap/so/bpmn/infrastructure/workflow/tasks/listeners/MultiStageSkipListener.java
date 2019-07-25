/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.db.PostCompletionRequestsDbListener;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulator;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MultiStageSkipListener implements FlowManipulator, PostCompletionRequestsDbListener {

    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;

    @Autowired
    private CatalogDbClient catalogDbClient;

    private static final String G_MULTI_STAGE_DESIGN = "multiStageDesign";

    @Override
    public boolean shouldRunFor(String currentBBName, boolean isFirst, BuildingBlockExecution execution) {
        return ((boolean) execution.getVariable(BBConstants.G_ALACARTE)) && "AssignVfModuleBB".equals(currentBBName)
                && isFirst;
    }


    @Override
    public boolean shouldRunFor(BuildingBlockExecution execution) {

        return (boolean) Optional.ofNullable(execution.getVariable(G_MULTI_STAGE_DESIGN)).orElse(false);
    }

    @Override
    public void run(List<ExecuteBuildingBlock> flowsToExecute, ExecuteBuildingBlock currentBB,
            BuildingBlockExecution execution) {
        String vfModuleId = currentBB.getResourceId();
        String vnfId = currentBB.getWorkflowResourceIds().getVnfId();
        org.onap.aai.domain.yang.VfModule vfModule = bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId);
        if (vfModule == null) {
            org.onap.aai.domain.yang.GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(vnfId);
            if (vnf != null) {
                VnfResourceCustomization vnfCust = catalogDbClient
                        .getVnfResourceCustomizationByModelCustomizationUUID(vnf.getModelCustomizationId());
                if (vnfCust != null && vnfCust.getMultiStageDesign() != null
                        && vnfCust.getMultiStageDesign().equalsIgnoreCase("true")) {
                    flowsToExecute.retainAll(Collections.singletonList(currentBB));
                    execution.setVariable(G_MULTI_STAGE_DESIGN, Boolean.valueOf(vnfCust.getMultiStageDesign()));
                }
            }
        }

    }

    @Override
    public void run(InfraActiveRequests request, BuildingBlockExecution execution) {
        request.setFlowStatus("Successfully completed Assign Building Block only due to multi-stage-design VNF");
    }
}
