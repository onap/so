/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;

@RunWith(MockitoJUnitRunner.class)
public class SkipConfigVnfListenerTest {

    private static final String MODEL_UUID = "modelUuidTest";
    private static final String VNF_CUSTOMIZATION_UUID = "vnfCustomizationUUIDTest";
    private static final String G_CURRENT_SEQUENCE = "gCurrentSequence";

    private DelegateExecution execution;
    @Mock
    private CatalogDbClient catalogDbClientMock;
    @InjectMocks
    private SkipConfigVnfListener testedObject;
    private BuildingBlockExecution buildingBlockExecution;

    @Before
    public void setup() {
        execution = new DelegateExecutionFake();
        buildingBlockExecution = new DelegateExecutionImpl(execution);
    }

    @Test
    public void shouldRunFor_ConfigAssignVnfBB() {
        assertThat(testedObject.shouldRunFor("ConfigAssignVnfBB", true, null)).isTrue();
    }

    @Test
    public void shouldRunFor_ConfigDeployVnfBB() {
        assertThat(testedObject.shouldRunFor("ConfigDeployVnfBB", true, null)).isTrue();
    }

    @Test
    public void shouldNotRunFor_notConfigBB() {
        assertThat(testedObject.shouldRunFor("BBtest", true, null)).isFalse();
    }

    @Test
    public void skipVnfSuccessful_sequenceIncremented() {
        // given
        execution.setVariable(G_CURRENT_SEQUENCE, 0);
        List<VnfResourceCustomization> vnfResourceCustomizations = createVnfResourceCustomizationList();
        when(catalogDbClientMock.getVnfResourceCustomizationByModelUuid(MODEL_UUID))
                .thenReturn(vnfResourceCustomizations);
        when(catalogDbClientMock.findVnfResourceCustomizationInList(VNF_CUSTOMIZATION_UUID, vnfResourceCustomizations))
                .thenReturn(createVnfResourceCustomization());
        // when
        testedObject.run(null, createExecuteBuildingBlock(), buildingBlockExecution);
        // then
        assertThat((int) execution.getVariable(G_CURRENT_SEQUENCE)).isEqualTo(1);
    }

    private ExecuteBuildingBlock createExecuteBuildingBlock() {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelUuid(MODEL_UUID);
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setModelInfo(modelInfo);

        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
        BuildingBlock buildingBlock = new BuildingBlock();
        buildingBlock.setKey(VNF_CUSTOMIZATION_UUID);
        executeBuildingBlock.setBuildingBlock(buildingBlock);
        executeBuildingBlock.setRequestDetails(requestDetails);
        return executeBuildingBlock;
    }

    private List<VnfResourceCustomization> createVnfResourceCustomizationList() {
        VnfResourceCustomization vnfResourceCustomization2 = new VnfResourceCustomization();
        vnfResourceCustomization2.setSkipPostInstConf(false);
        return new ArrayList<>(Arrays.asList(createVnfResourceCustomization(), vnfResourceCustomization2));
    }

    private VnfResourceCustomization createVnfResourceCustomization() {
        VnfResourceCustomization vnfResourceCustomization = new VnfResourceCustomization();
        vnfResourceCustomization.setSkipPostInstConf(true);
        return vnfResourceCustomization;
    }

}
