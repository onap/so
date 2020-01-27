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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SkipCDSBuildingBlockListenerTest {
    private static final String VNF_SCOPE = "VNF";
    private static final String VF_SCOPE = "VFModule";
    private static final String VFMODULEID = "vfModuleId";
    private static final String MODELUUID = "modelUuid";
    private static final String VNF_ACTION1 = "config-assign";
    private static final String VNF_ACTION2 = "config-deploy";
    private static final String VNF_ACTION3 = "configAssign";
    private static final String VNF_ACTION4 = "configDeploy";
    private static final String VF_ACTION1 = "configAssign";
    private static final String VF_ACTION2 = "configDeploy";
    private static final String MODELCUSTOMIZATIONUUID = "modelCustomizationUUID";

    private ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
    private RequestDetails reqDetail = new RequestDetails();
    private BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
    @Mock
    private CatalogDbClient catalogDbClient;
    @InjectMocks
    private SkipCDSBuildingBlockListener skipCDSBuildingBlockListener;

    @Test
    public void testTrigger() {
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        assertTrue("should be triggered",
                skipCDSBuildingBlockListener.shouldRunFor("ControllerExecutionBB", true, execution));
        assertFalse("should not be triggered",
                skipCDSBuildingBlockListener.shouldRunFor("ControllerExecutionBB2", true, execution));
    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VNF_SCOPE, VNF_ACTION1);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);

    }

    @Test
    public void testProcessForSkipVnfConfigDeployAction() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VNF_SCOPE, VNF_ACTION2);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);
    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction1() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VNF_SCOPE, VNF_ACTION3);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);

    }

    @Test
    public void testProcessForSkipVnfConfigAssignAction2() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VNF_SCOPE, VNF_ACTION4);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);

    }

    @Test
    public void testProcessForSkipVfModuleConfigAssignAction() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VF_SCOPE, VF_ACTION1);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);

    }

    @Test
    public void testProcessForSkipVfModuleConfigDeployAction() {
        List<ExecuteBuildingBlock> flowsToExecute = createflowsToExecute();
        setScopeAndAction(VF_SCOPE, VF_ACTION2);
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, execution);

    }

    private void setScopeAndAction(String scope, String action) {
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        buildingBlock.setKey(MODELCUSTOMIZATIONUUID);
        executeBuildingBlock.setBuildingBlock(buildingBlock);

    }

    private List<ExecuteBuildingBlock> createflowsToExecute() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        ModelInfo model = new ModelInfo();
        model.setModelUuid(MODELUUID);
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);
        executeBuildingBlock.setResourceId(VFMODULEID);
        flowsToExecute.add(executeBuildingBlock);
        return flowsToExecute;
    }

}
