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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;

@RunWith(MockitoJUnitRunner.class)
public class SkipCDSBuildingBlockListenerTest {
    private static final String VNF_SCOPE = "vnf";
    private static final String VF_SCOPE = "vfModule";
    private static final String vfModuleId = "vfModuleId";
    private static final String modelUuid = "modelUuid";
    private static final String VNF_ACTION1 = "config-assign";
    private static final String VNF_ACTION2 = "config-deploy";
    private static final String VNF_ACTION3 = "configAssign";
    private static final String VNF_ACTION4 = "configDeploy";
    private static final String VF_ACTION1 = "configAssign";
    private static final String VF_ACTION2 = "configDeploy";
    private static final String modelCustomizationUUID = "modelCustomizationUUID";

    private ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();

    @Mock
    private CatalogDbClient catalogDbClient;
    @InjectMocks
    private SkipCDSBuildingBlockListener skipCDSBuildingBlockListener;

    @Test
    public void testTrigger() {
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        assertTrue("should be triggered",
                skipCDSBuildingBlockListener.shouldRunFor("ControllerExecutionBB", true, execution));
        assertFalse("should be triggered",
                skipCDSBuildingBlockListener.shouldRunFor("ControllerExecutionBB2", true, execution));
    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction() {

        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);

        setScopeAndAction(VNF_SCOPE, VNF_ACTION1);
        assertEquals("vnf", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("config-assign", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        List<VnfResourceCustomization> vnfResourceCustomizations =
                catalogDbClient.getVnfResourceCustomizationByModelUuid(reqDetail.getModelInfo().getModelUuid());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);

        VnfResourceCustomization vnfCust = new VnfResourceCustomization();
        vnfCust.setModelCustomizationUUID(modelCustomizationUUID);

        vnfCust.setSkipPostInstConf(true);
        assertTrue("should be triggered", vnfCust.isSkipPostInstConf());
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomizations)).thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }

    @Test
    public void testProcessForSkipVnfConfigDeployAction() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);
        setScopeAndAction(VNF_SCOPE, VNF_ACTION2);
        assertEquals("vnf", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("config-deploy", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        List<VnfResourceCustomization> vnfResourceCustomizations =
                catalogDbClient.getVnfResourceCustomizationByModelUuid(reqDetail.getModelInfo().getModelUuid());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        VnfResourceCustomization vnfCust = new VnfResourceCustomization();
        vnfCust.setModelCustomizationUUID(modelCustomizationUUID);


        vnfCust.setSkipPostInstConf(true);
        assertTrue("should be triggered", vnfCust.isSkipPostInstConf());
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomizations)).thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction1() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);
        setScopeAndAction(VNF_SCOPE, VNF_ACTION3);
        assertEquals("vnf", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("configAssign", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        List<VnfResourceCustomization> vnfResourceCustomizations =
                catalogDbClient.getVnfResourceCustomizationByModelUuid(reqDetail.getModelInfo().getModelUuid());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        VnfResourceCustomization vnfCust = new VnfResourceCustomization();
        vnfCust.setModelCustomizationUUID(modelCustomizationUUID);


        vnfCust.setSkipPostInstConf(true);
        assertTrue("should be triggered", vnfCust.isSkipPostInstConf());
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomizations)).thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction2() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);
        setScopeAndAction(VNF_SCOPE, VNF_ACTION4);
        assertEquals("vnf", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("configDeploy", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        List<VnfResourceCustomization> vnfResourceCustomizations =
                catalogDbClient.getVnfResourceCustomizationByModelUuid(reqDetail.getModelInfo().getModelUuid());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        VnfResourceCustomization vnfCust = new VnfResourceCustomization();
        vnfCust.setModelCustomizationUUID(modelCustomizationUUID);


        vnfCust.setSkipPostInstConf(true);
        assertTrue("should be triggered", vnfCust.isSkipPostInstConf());
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomizations)).thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }



    @Test
    public void testProcessForSkipVfModuleConfigAssignAction() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);
        setScopeAndAction(VF_SCOPE, VF_ACTION1);
        assertEquals("vfModule", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("configAssign", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        VfModuleCustomization vfCust = new VfModuleCustomization();
        vfCust.setSkipPostInstConf(true);

        assertTrue("should be triggered", vfCust.isSkipPostInstConf());
        when(catalogDbClient
                .getVfModuleCustomizationByModelCuztomizationUUID(executeBuildingBlock.getBuildingBlock().getKey()))
                        .thenReturn(vfCust);
        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }

    @Test
    public void testProcessForSkipVfModuleConfigDeployAction() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(modelUuid);
        RequestDetails reqDetail = new RequestDetails();
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);

        flowsToExecute.add(executeBuildingBlock);
        flowsToExecute.get(0).setResourceId(vfModuleId);
        setScopeAndAction(VF_SCOPE, VF_ACTION2);
        assertEquals("vfModule", executeBuildingBlock.getBuildingBlock().getBpmnScope());
        assertEquals("configDeploy", executeBuildingBlock.getBuildingBlock().getBpmnAction());

        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);

        VfModuleCustomization vfCust = new VfModuleCustomization();
        vfCust.setSkipPostInstConf(true);

        assertTrue("should be triggered", vfCust.isSkipPostInstConf());
        when(catalogDbClient
                .getVfModuleCustomizationByModelCuztomizationUUID(executeBuildingBlock.getBuildingBlock().getKey()))
                        .thenReturn(vfCust);
        skipCDSBuildingBlockListener.run(flowsToExecute, flowsToExecute.get(0), execution);

    }

    private void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        buildingBlock.setKey(modelCustomizationUUID);
        executeBuildingBlock.setBuildingBlock(buildingBlock);

    }

}
