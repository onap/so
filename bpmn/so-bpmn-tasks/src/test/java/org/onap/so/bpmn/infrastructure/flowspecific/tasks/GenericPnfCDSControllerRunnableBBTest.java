/*
 * ============LICENSE_START======================================================= Copyright (C) 2020 Nokia. All rights
 * reserved. ================================================================================ Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BBObjectNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.EXECUTION_OBJECT;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_NAME;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_VERSION;
import static org.onap.so.client.cds.PayloadConstants.SCOPE;

@RunWith(MockitoJUnitRunner.class)
public class GenericPnfCDSControllerRunnableBBTest {

    @Mock
    private ExtractPojosForBB extractPojosForBB;

    @InjectMocks
    private GenericPnfCDSControllerRunnableBB genericPnfCDSControllerRunnableBB;

    private ControllerContext<BuildingBlockExecution> controllerContext;
    private BuildingBlockExecution execution;

    private final static String blueprintName = "blueprint_name";
    private final static String blueprintVersion = "blueprint_version";
    private final static String msoRequestId = "mso_request_id";
    private final static String pnfID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private final static String serviceInstanceID = "test_service_id";
    private final static String pnfName = "PNFDemo";
    private final static String serviceModelUUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private final static String pnfCustomizationUUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private final static String action = "action";

    @Before
    public void setUp() {
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
        BuildingBlock buildingBlock = new BuildingBlock();

        buildingBlock.setBpmnAction(action);
        executeBuildingBlock.setBuildingBlock(buildingBlock);

        execution = new DelegateExecutionImpl(new ExecutionImpl());
        execution.setVariable("buildingBlock", executeBuildingBlock);
        execution.setVariable(PRC_BLUEPRINT_NAME, blueprintName);
        execution.setVariable(PRC_BLUEPRINT_VERSION, blueprintVersion);
        execution.setVariable(MSO_REQUEST_ID, msoRequestId);
        execution.setVariable(SCOPE, "scope");

        controllerContext = new ControllerContext<>();
        controllerContext.setExecution(execution);
    }

    @Test
    public void prepareTest() throws BBObjectNotFoundException {
        // given
        preparePnfAndExtractForPnf();

        // when
        genericPnfCDSControllerRunnableBB.prepare(controllerContext);

        // then
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean = execution.getVariable(EXECUTION_OBJECT);
        final JSONObject actionProperties = new JSONObject(abstractCDSPropertiesBean.getRequestObject())
                .getJSONObject("action-request").getJSONObject("action-properties");

        assertThat(abstractCDSPropertiesBean).isNotNull();
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isNotNull();
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isInstanceOf(String.class);

        assertEquals(blueprintName, abstractCDSPropertiesBean.getBlueprintName());
        assertEquals(blueprintVersion, abstractCDSPropertiesBean.getBlueprintVersion());
        assertEquals(msoRequestId, abstractCDSPropertiesBean.getRequestId());
        assertEquals(action, abstractCDSPropertiesBean.getActionName());
        assertEquals("sync", abstractCDSPropertiesBean.getMode());
        assertEquals("SO", abstractCDSPropertiesBean.getOriginatorId());

        assertEquals(pnfID, actionProperties.get("pnf-id"));
        assertEquals(serviceInstanceID, actionProperties.get("service-instance-id"));
        assertEquals(serviceModelUUID, actionProperties.get("service-model-uuid"));
        assertEquals(pnfName, actionProperties.get("pnf-name"));
        assertEquals(pnfCustomizationUUID, actionProperties.get("pnf-customization-uuid"));

    }

    private void preparePnfAndExtractForPnf() throws BBObjectNotFoundException {
        Pnf pnf = new Pnf();

        pnf.setPnfName(pnfName);
        pnf.setPnfId(pnfID);
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        modelInfoPnf.setModelCustomizationUuid(pnfCustomizationUUID);
        modelInfoPnf.setModelInstanceName(serviceInstanceID);
        modelInfoPnf.setModelUuid(serviceModelUUID);
        pnf.setModelInfoPnf(modelInfoPnf);

        when(extractPojosForBB.extractByKey(eq(execution), eq(ResourceKey.PNF))).thenReturn(pnf);
    }
}
