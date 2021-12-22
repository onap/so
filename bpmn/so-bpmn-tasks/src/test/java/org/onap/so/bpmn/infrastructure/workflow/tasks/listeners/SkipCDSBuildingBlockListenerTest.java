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
import java.util.UUID;
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
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.RequestDetails;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SkipCDSBuildingBlockListenerTest {

    private static final String SERVICE_SCOPE = "service";
    private static final String VNF_SCOPE = "VNF";
    private static final String VF_SCOPE = "VFModule";
    private static final String PNF_SCOPE = "pnf";
    private static final String VNF_TEST_ACTION = "VnfConfigAssign";
    private static final String VFModule_TEST_ACTION = "VfModuleConfigAssign";
    private static final String PNFModule_TEST_ACTION = "config-assign";
    private static final String SERVICE_MODEL_VERSION_ID = UUID.randomUUID().toString();
    private static final String VNF_MODEL_CUSTOMIZATION_ID = UUID.randomUUID().toString();
    private static final String VF_MODULE_CUSTOMIZATION_ID = UUID.randomUUID().toString();
    private static final String PNF_CUSTOMIZATION_ID = UUID.randomUUID().toString();
    private static final String BBNAME = "ControllerExecutionBB";
    private static final boolean ISFIRST = true;

    private List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
    private ExecuteBuildingBlock executeBuildingBlock;
    private RequestDetails reqDetail = new RequestDetails();
    private BuildingBlockExecution buildingBlockExecution = new DelegateExecutionImpl(new DelegateExecutionFake());
    private VnfResourceCustomization vnfCust;
    private VfModuleCustomization vfCust;
    private PnfResourceCustomization pnfResourceCustomization;
    private BuildingBlock buildingBlock;

    @InjectMocks
    private SkipCDSBuildingBlockListener skipCDSBuildingBlockListener;
    @Mock
    private CatalogDbClient catalogDbClient;

    @Before
    public void before() {
        executeBuildingBlock = new ExecuteBuildingBlock();
        buildingBlock = new BuildingBlock();

        ArrayList<Resource> resources = new ArrayList<>();
        Resource service = new Resource(WorkflowType.SERVICE, SERVICE_MODEL_VERSION_ID, false, null);
        service.setModelVersionId(SERVICE_MODEL_VERSION_ID);
        resources.add(service);
        Resource vnf = new Resource(WorkflowType.VNF, VNF_MODEL_CUSTOMIZATION_ID, false, null);
        vnf.setModelCustomizationId(VNF_MODEL_CUSTOMIZATION_ID);
        resources.add(vnf);
        Resource vfModule = new Resource(WorkflowType.VFMODULE, VF_MODULE_CUSTOMIZATION_ID, false, null);
        vfModule.setModelCustomizationId(VF_MODULE_CUSTOMIZATION_ID);
        resources.add(vfModule);
        Resource pnf = new Resource(WorkflowType.PNF, PNF_CUSTOMIZATION_ID, false, null);
        pnf.setModelCustomizationId(PNF_CUSTOMIZATION_ID);
        resources.add(pnf);
        buildingBlockExecution.setVariable("resources", resources);

        vnfCust = new VnfResourceCustomization();
        vfCust = new VfModuleCustomization();
        pnfResourceCustomization = new PnfResourceCustomization();
    }

    @Test
    public void testTrigger() {
        BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
        skipCDSBuildingBlockListener.shouldRunFor(BBNAME, ISFIRST, execution);
        assertEquals("ControllerExecutionBB", BBNAME);
    }

    @Test
    public void testSkipCDSforService() {
        setBuildingBlockAndCurrentSequence(SERVICE_SCOPE, "service-config-assign", SERVICE_MODEL_VERSION_ID, 0);
        when(catalogDbClient.getServiceByID(SERVICE_MODEL_VERSION_ID)).thenReturn(new Service());

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(1, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    @Test
    public void testProcessForVNFToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(VNF_SCOPE, VNF_TEST_ACTION, VNF_MODEL_CUSTOMIZATION_ID, 0);
        when(catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(VNF_MODEL_CUSTOMIZATION_ID))
                .thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(1, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    @Test
    public void testProcessForVNFNotToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(VNF_SCOPE, VNF_TEST_ACTION, VNF_MODEL_CUSTOMIZATION_ID, 0);
        vnfCust.setSkipPostInstConf(false);
        when(catalogDbClient.getVnfResourceCustomizationByModelCustomizationUUID(VNF_MODEL_CUSTOMIZATION_ID))
                .thenReturn(vnfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(0, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }


    @Test
    public void testProcessForVFToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(VF_SCOPE, VFModule_TEST_ACTION, VF_MODULE_CUSTOMIZATION_ID, 0);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(VF_MODULE_CUSTOMIZATION_ID))
                .thenReturn(vfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(1, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    @Test
    public void testProcessForVFNotToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(VF_SCOPE, VFModule_TEST_ACTION, VF_MODULE_CUSTOMIZATION_ID, 0);
        vfCust.setSkipPostInstConf(false);
        when(catalogDbClient
                .getVfModuleCustomizationByModelCuztomizationUUID(executeBuildingBlock.getBuildingBlock().getKey()))
                        .thenReturn(vfCust);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(0, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    @Test
    public void testProcessForPNFToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(PNF_SCOPE, PNFModule_TEST_ACTION, PNF_CUSTOMIZATION_ID, 0);
        when(catalogDbClient.getPnfResourceCustomizationByModelCustomizationUUID(PNF_CUSTOMIZATION_ID))
                .thenReturn(pnfResourceCustomization);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(1, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    @Test
    public void testProcessForPNFNotToSkipCDSBB() {
        setBuildingBlockAndCurrentSequence(PNF_SCOPE, PNFModule_TEST_ACTION, PNF_CUSTOMIZATION_ID, 0);
        pnfResourceCustomization.setSkipPostInstConf(false);
        when(catalogDbClient.getPnfResourceCustomizationByModelCustomizationUUID(PNF_CUSTOMIZATION_ID))
                .thenReturn(pnfResourceCustomization);

        skipCDSBuildingBlockListener.run(flowsToExecute, executeBuildingBlock, buildingBlockExecution);

        assertEquals(0, (int) buildingBlockExecution.getVariable(BBConstants.G_CURRENT_SEQUENCE));
    }

    /**
     * setting scope action in buildingBlock and BB current sequence in BuildingBlockExecution
     *
     * @param scope
     * @param action
     * @param squence
     */
    private void setBuildingBlockAndCurrentSequence(String scope, String action, String key, int sequence) {
        buildingBlock.setBpmnScope(scope);
        buildingBlock.setBpmnAction(action);
        buildingBlock.setBpmnFlowName("ControllerExecutionBB");
        buildingBlock.setKey(key);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        buildingBlockExecution.setVariable(BBConstants.G_CURRENT_SEQUENCE, sequence);
    }

}
