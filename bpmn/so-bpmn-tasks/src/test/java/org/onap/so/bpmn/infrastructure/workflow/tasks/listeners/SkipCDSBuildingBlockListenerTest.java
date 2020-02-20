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
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class SkipCDSBuildingBlockListenerTest {

    private static final String VNF_SCOPE = "VNF";
    private static final String VF_SCOPE = "VFModule";
    private static final String TEST_MODELUUID = "123456789";
    private static final String VNF_TEST_ACTION = "VnfConfigAssign";
    private static final String VFModule_TEST_ACTION = "VfModuleConfigAssign";
    private static final String MODELCUSTOMIZATIONUUID = "123456789";
    private static final String BBNAME = "ControllerExecutionBB";
    private static final boolean ISFIRST = true;

    private int actual;
    private List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
    private List<VnfResourceCustomization> vnfResourceCustomization;
    private List<VfModuleCustomization> vfModuleCustomization;
    private ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
    private RequestDetails reqDetail = new RequestDetails();
    private BuildingBlockExecution buildingBlockExecution = new DelegateExecutionImpl(new DelegateExecutionFake());
    private VnfResourceCustomization vnfCust = new VnfResourceCustomization();
    private VfModuleCustomization vfCust = new VfModuleCustomization();
    private BuildingBlock buildingBlock = new BuildingBlock();

    @InjectMocks
    private SkipCDSBuildingBlockListener skipCDSBuildingBlockListener;
    @Mock
    private CatalogDbClient catalogDbClient;

    @Before
    public void before() {
        ModelInfo model = new ModelInfo();
        model.setModelUuid(TEST_MODELUUID);
        reqDetail.setModelInfo(model);
        executeBuildingBlock.setRequestDetails(reqDetail);
    }

    @Test
    public void testTrigger() {
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        skipCDSBuildingBlockListener.shouldRunFor(BBNAME, ISFIRST, execution);
        assertEquals("ControllerExecutionBB", BBNAME);
    }

    @Test
    public void testProcessForVNFToSkipCDSBB() {
        // given
        setBuildingBlockAndCurrentSequence(VNF_SCOPE, VNF_TEST_ACTION, 0);
        vnfResourceCustomization = getVnfResourceCustomizationList(true);

        when(catalogDbClient.getVnfResourceCustomizationByModelUuid(
                executeBuildingBlock.getRequestDetails().getModelInfo().getModelUuid()))
                        .thenReturn(vnfResourceCustomization);
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomization)).thenReturn(vnfCust);

        // when
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        // then
        actual = buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
        assertEquals(1, actual);

    }

    @Test
    public void testProcessForVNFNotToSkipCDSBB() {
        // given
        setBuildingBlockAndCurrentSequence(VNF_SCOPE, VNF_TEST_ACTION, 0);
        vnfResourceCustomization = getVnfResourceCustomizationList(false);

        when(catalogDbClient.getVnfResourceCustomizationByModelUuid(
                executeBuildingBlock.getRequestDetails().getModelInfo().getModelUuid()))
                        .thenReturn(vnfResourceCustomization);
        when(catalogDbClient.findVnfResourceCustomizationInList(executeBuildingBlock.getBuildingBlock().getKey(),
                vnfResourceCustomization)).thenReturn(vnfCust);

        // when
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        // then
        actual = buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
        assertEquals(0, actual);

    }


    @Test
    public void testProcessForVFToSkipCDSBB() {
        // given
        setBuildingBlockAndCurrentSequence(VF_SCOPE, VFModule_TEST_ACTION, 0);
        vfModuleCustomization = getVfModuleCustomizationList(true);

        when(catalogDbClient
                .getVfModuleCustomizationByModelCuztomizationUUID(executeBuildingBlock.getBuildingBlock().getKey()))
                        .thenReturn(vfCust);

        // when
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        // then
        actual = buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
        assertEquals(1, actual);

    }

    @Test
    public void testProcessForVFNotToSkipCDSBB() {
        // given
        setBuildingBlockAndCurrentSequence(VF_SCOPE, VFModule_TEST_ACTION, 0);
        vfModuleCustomization = getVfModuleCustomizationList(false);

        when(catalogDbClient
                .getVfModuleCustomizationByModelCuztomizationUUID(executeBuildingBlock.getBuildingBlock().getKey()))
                        .thenReturn(vfCust);

        // when
        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        // then
        actual = buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
        assertEquals(0, actual);

    }

    /**
     * setting scope action in buildingBlock and BB current sequence in BuildingBlockExecution
     *
     * @param scope
     * @param action
     * @param squence
     */
    private void setBuildingBlockAndCurrentSequence(String scope, String action, int sequence) {
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        buildingBlock.setBpmnFlowName("ControllerExecutionBB");
        buildingBlock.setKey(MODELCUSTOMIZATIONUUID);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BBConstants.G_CURRENT_SEQUENCE, sequence);

    }

    private List<VnfResourceCustomization> getVnfResourceCustomizationList(boolean setSkippost) {
        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        vnfCust.setModelCustomizationUUID(MODELCUSTOMIZATIONUUID);
        vnfCust.setSkipPostInstConf(setSkippost);
        vnfResourceCustomizations.add(vnfCust);
        return vnfResourceCustomizations;
    }

    private List<VfModuleCustomization> getVfModuleCustomizationList(boolean setSkippost) {
        List<VfModuleCustomization> vfModuleCustomizations = new ArrayList<>();
        vfCust.setModelCustomizationUUID(MODELCUSTOMIZATIONUUID);
        vfCust.setSkipPostInstConf(setSkippost);
        vfModuleCustomizations.add(vfCust);
        return vfModuleCustomizations;
    }

}
