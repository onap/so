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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;

@RunWith(MockitoJUnitRunner.class)
public class MultiStageSkipListenerTest {

    @Mock
    private CatalogDbClient catalogDbClient;

    @Mock
    private BBInputSetupUtils bbInputSetupUtils;

    @InjectMocks
    private MultiStageSkipListener multiStageSkipListener;

    @Test
    public void testTrigger() {
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_ALACARTE, true);

        assertTrue("should be triggered", multiStageSkipListener.shouldRunFor("AssignVfModuleBB", true, execution));

        execution.setVariable(BBConstants.G_ALACARTE, false);
        assertFalse("should not be triggered",
                multiStageSkipListener.shouldRunFor("AssignVfModuleBB", true, execution));

        execution.setVariable(BBConstants.G_ALACARTE, true);
        assertFalse("should not be triggered",
                multiStageSkipListener.shouldRunFor("AssignVfModuleBB2", true, execution));

        execution.setVariable("multiStageDesign", true);
        assertTrue("should be triggered", multiStageSkipListener.shouldRunFor(execution));

        execution.setVariable("multiStageDesign", false);
        assertFalse("should not be triggered", multiStageSkipListener.shouldRunFor(execution));

        execution.setVariable("multiStageDesign", null);
        assertFalse("should not be triggered", multiStageSkipListener.shouldRunFor(execution));

    }

    @Test
    public void testProcessMultiStageSkip() {
        String vfModuleId = "vfModuleId";
        String vnfId = "vnfId";
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("serviceInstanceId");
        workflowResourceIds.setVnfId(vnfId);
        BuildingBlock bb = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
        ebb.setResourceId(vfModuleId);
        ebb.setBuildingBlock(bb);
        ebb.setWorkflowResourceIds(workflowResourceIds);
        flowsToExecute.add(ebb);
        flowsToExecute.add(new ExecuteBuildingBlock());
        flowsToExecute.add(new ExecuteBuildingBlock());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setVfModuleId(vfModuleId);
        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setModelCustomizationId("modelCustomizationUUID");
        VnfResourceCustomization vnfCust = new VnfResourceCustomization();
        vnfCust.setModelCustomizationUUID("modelCustomizationUUID");
        vnfCust.setMultiStageDesign("true");
        when(catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(vnf.getModelCustomizationId()))
                .thenReturn(vnfCust);
        when(bbInputSetupUtils.getAAIVfModule(eq(vnfId), eq(vfModuleId))).thenReturn(null);
        when(bbInputSetupUtils.getAAIGenericVnf(eq(vnfId))).thenReturn(vnf);

        multiStageSkipListener.run(flowsToExecute, flowsToExecute.get(0), execution);
        assertEquals("Flows should only have Assign", flowsToExecute.size(), 1);
        assertEquals("Flows should only have Assign", flowsToExecute.get(0).getBuildingBlock().getBpmnFlowName(),
                "AssignVfModuleBB");
    }

    @Test
    public void postCompletionRequestsDbListenerTest() {
        InfraActiveRequests request = new InfraActiveRequests();
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        multiStageSkipListener.run(request, execution);

        assertEquals("Successfully completed Assign Building Block only due to multi-stage-design VNF",
                request.getFlowStatus());
    }
}
