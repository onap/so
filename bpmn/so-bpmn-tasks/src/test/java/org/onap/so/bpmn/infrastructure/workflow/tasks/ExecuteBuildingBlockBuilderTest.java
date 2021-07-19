/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
 * ================================================================================
 * Modifications Copyright (c) 2021 Orange
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

import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ExecuteBuildingBlockBuilderTest {

    final private ExecuteBuildingBlockBuilder executeBBBuilder = new ExecuteBuildingBlockBuilder();

    @Test
    public void verifyLackOfNullPointerExceptionForNullResource() {
        ExecuteBuildingBlock result = null;
        try {
            result = executeBBBuilder.buildExecuteBuildingBlock(new OrchestrationFlow(), null, null, null, null, null,
                    false, null, null, null, false, null, null, true, null);
        } catch (NullPointerException e) {
            fail("NullPointerException should not be thrown when 'resource' is null");
        }
        assertNotNull(result);
    }

    @Test
    public void getConfigurationResourceKeysTest() {
        String vnfcName = "vnfc";
        String vfModuleCustomizationId = "1a2b3c4e5d";
        String cvnfModuleCustomizationId = "2b1a3c";
        String vnfCustomizationId = "zz12aa";

        Resource resource = new Resource(WorkflowType.SERVICE, "123", true, null);

        resource.setCvnfModuleCustomizationId(vfModuleCustomizationId);
        resource.setCvnfModuleCustomizationId(cvnfModuleCustomizationId);
        resource.setVnfCustomizationId(vnfCustomizationId);

        ConfigurationResourceKeys confResourceKeys = executeBBBuilder.getConfigurationResourceKeys(resource, vnfcName);

        assertNotNull(confResourceKeys);
        assertEquals(vnfcName, confResourceKeys.getVnfcName());
        assertEquals(cvnfModuleCustomizationId, confResourceKeys.getCvnfcCustomizationUUID());
        assertEquals(vnfCustomizationId, confResourceKeys.getVnfResourceCustomizationUUID());

    }

    private Resource getResource(List<Resource> resources, ExecuteBuildingBlock block) {
        Resource res =
                resources.stream().filter(resource -> resource.getResourceId() == block.getBuildingBlock().getKey())
                        .findAny().orElse(null);
        return res;
    }

    private void buildExecuteBuildingBlockListOrderOfVnf(int vnfCount, boolean addPnf, boolean priorities) {
        WorkflowResourceIds wfIds = new WorkflowResourceIds();
        wfIds.setServiceInstanceId("1");
        List<Resource> resources = new ArrayList<>();
        Resource r1 = new Resource(WorkflowType.SERVICE, "1", false, null);
        resources.add(r1);
        for (int i = 0; i < vnfCount; i++) {
            Resource r2 = new Resource(WorkflowType.VNF, Integer.toString(2 + i * 3), false, r1);
            Resource r3 = new Resource(WorkflowType.VFMODULE, Integer.toString(3 + i * 3), false, r2);
            Resource r4 = new Resource(WorkflowType.VFMODULE, Integer.toString(4 + i * 3), false, r2);
            r4.setBaseVfModule(true);
            resources.add(r2);
            resources.add(r3);
            resources.add(r4);
            if (priorities) {
                r2.setProcessingPriority(vnfCount - i);
                r3.setProcessingPriority(1);
                r4.setProcessingPriority(2);
            }
        }
        if (addPnf) {
            Resource r5 = new Resource(WorkflowType.PNF, Integer.toString(5 + (vnfCount - 1) * 3), false, r1);
            resources.add(r5);
        }

        OrchestrationFlow f1 = new OrchestrationFlow();
        f1.setFlowName("AssignServiceInstanceBB");
        OrchestrationFlow f2 = new OrchestrationFlow();
        f2.setFlowName("AssignVnfBB");
        OrchestrationFlow f3 = new OrchestrationFlow();
        f3.setFlowName("AssignVfModuleBB");
        OrchestrationFlow f4 = new OrchestrationFlow();
        f4.setFlowName("ControllerExecutionBB");
        f4.setBpmnAction("config-assign");
        f4.setBpmnScope("vnf");
        OrchestrationFlow f5 = new OrchestrationFlow();
        f5.setFlowName("AssignPnfBB");
        OrchestrationFlow f6 = new OrchestrationFlow();
        f6.setFlowName("WaitForPnfReadyBB");
        OrchestrationFlow f7 = new OrchestrationFlow();
        f7.setFlowName("ControllerExecutionBB");
        f7.setBpmnAction("config-assign");
        f7.setBpmnScope("pnf");
        OrchestrationFlow f8 = new OrchestrationFlow();
        f8.setFlowName("ControllerExecutionBB");
        f8.setBpmnAction("config-deploy");
        f8.setBpmnScope("pnf");
        OrchestrationFlow f9 = new OrchestrationFlow();
        f9.setFlowName("ActivatePnfBB");
        OrchestrationFlow f10 = new OrchestrationFlow();
        f10.setFlowName("CreateVfModuleBB");
        OrchestrationFlow f11 = new OrchestrationFlow();
        f11.setFlowName("ActivateVfModuleBB");
        OrchestrationFlow f12 = new OrchestrationFlow();
        f12.setFlowName("ControllerExecutionBB");
        f12.setBpmnAction("config-deploy");
        f12.setBpmnScope("vnf");
        OrchestrationFlow f13 = new OrchestrationFlow();
        f13.setFlowName("ActivateVnfBB");
        OrchestrationFlow f14 = new OrchestrationFlow();
        f14.setFlowName("ActivateServiceInstanceBB");

        List<OrchestrationFlow> flows = Arrays.asList(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14);
        for (int i = 0; i < flows.size(); i++) {
            OrchestrationFlow flow = flows.get(i);
            flow.setId(i + 1);
            flow.setSequenceNumber(i + 1);
            flow.setFlowVersion(1.0);
            flow.setAction("Service-Macro-Create");
        }

        List<ExecuteBuildingBlock> flowsToExecute = executeBBBuilder.buildExecuteBuildingBlockList(flows, resources,
                "1", "1", "1", WorkflowActionConstants.CREATE_INSTANCE, "type", wfIds, null, false);

        for (ExecuteBuildingBlock block : flowsToExecute) {
            String name = block.getBuildingBlock().getBpmnFlowName();
            if (name.equals("ControllerExecutionBB"))
                name += " [" + block.getBuildingBlock().getBpmnScope() + ", " + block.getBuildingBlock().getBpmnAction()
                        + "]";
            System.out.println(name);
        }
        assertNotNull(flowsToExecute);
        assertEquals((addPnf ? 7 : 2) + vnfCount * 4L + vnfCount * 2L * 3, flowsToExecute.size());

        for (ExecuteBuildingBlock block : flowsToExecute)
            assertNotNull(getResource(resources, block));

        if (priorities) {
            if (vnfCount > 1)
                assertEquals("5", getResource(resources, flowsToExecute.get(1)).getResourceId());
            else
                assertEquals("3", getResource(resources, flowsToExecute.get(2)).getResourceId());
        } else {
            if (vnfCount > 1)
                assertEquals("2", getResource(resources, flowsToExecute.get(1)).getResourceId());
            else
                assertEquals("4", getResource(resources, flowsToExecute.get(2)).getResourceId());
        }

        // AssignVfModuleBB
        assertEquals(f3.getFlowName(), flowsToExecute.get(2).getBuildingBlock().getBpmnFlowName());
        // ControllerExecutionBB
        assertEquals(f4.getFlowName(), flowsToExecute.get(4).getBuildingBlock().getBpmnFlowName());
        if (vnfCount > 1) {
            // AssignVnfBB
            if (addPnf)
                assertEquals(f2.getFlowName(), flowsToExecute.get(5).getBuildingBlock().getBpmnFlowName());
            else
                assertEquals(f2.getFlowName(), flowsToExecute.get(11).getBuildingBlock().getBpmnFlowName());
        }
        int pnfOffset = -5;
        if (addPnf) {
            pnfOffset = (vnfCount - 1) * 4;
            // AssignPnfBB
            assertEquals(f5.getFlowName(), flowsToExecute.get(5 + pnfOffset).getBuildingBlock().getBpmnFlowName());
            // WaitForPnfReadyBB
            assertEquals(f6.getFlowName(), flowsToExecute.get(6 + pnfOffset).getBuildingBlock().getBpmnFlowName());
            // ControllerExecutionBB
            assertEquals(f7.getFlowName(), flowsToExecute.get(7 + pnfOffset).getBuildingBlock().getBpmnFlowName());
            // ControllerExecutionBB
            assertEquals(f8.getFlowName(), flowsToExecute.get(8 + pnfOffset).getBuildingBlock().getBpmnFlowName());
            // ActivatePnfBB
            assertEquals(f9.getFlowName(), flowsToExecute.get(9 + pnfOffset).getBuildingBlock().getBpmnFlowName());
        }
        // CreateVfModuleBB
        assertEquals(f10.getFlowName(), flowsToExecute.get(10 + pnfOffset).getBuildingBlock().getBpmnFlowName());
        // ActivateVfModuleBB
        assertEquals(f11.getFlowName(), flowsToExecute.get(11 + pnfOffset).getBuildingBlock().getBpmnFlowName());
        // ControllerExecutionBB
        assertEquals(f12.getFlowName(), flowsToExecute.get(14 + pnfOffset).getBuildingBlock().getBpmnFlowName());
        // ActivateVnfBB
        assertEquals(f13.getFlowName(), flowsToExecute.get(15 + pnfOffset).getBuildingBlock().getBpmnFlowName());
    }

    @Test
    public void buildExecuteBuildingBlockListSortByBaseVfModuleTest() {
        buildExecuteBuildingBlockListOrderOfVnf(1, false, false);
    }

    @Test
    public void buildExecuteBuildingBlockListSortByPriorityTest() {
        buildExecuteBuildingBlockListOrderOfVnf(1, false, true);
    }

    @Test
    public void buildExecuteBuildingBlockListGroupByResourceTypeAndSortByPriorityTest() {
        buildExecuteBuildingBlockListOrderOfVnf(2, false, true);
    }

    @Test
    public void buildExecuteBuildingBlockListGroupByResourceTypeTest() {
        buildExecuteBuildingBlockListOrderOfVnf(2, false, false);
    }

    @Test
    public void buildExecuteBuildingBlockListGroupByResourceTypeVnfAndPnfTest() {
        buildExecuteBuildingBlockListOrderOfVnf(2, true, false);
    }
}
