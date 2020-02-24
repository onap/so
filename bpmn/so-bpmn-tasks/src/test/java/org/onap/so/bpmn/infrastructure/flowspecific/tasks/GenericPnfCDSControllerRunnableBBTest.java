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

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BBObjectNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.EXECUTION_OBJECT;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_NAME;
import static org.onap.so.client.cds.PayloadConstants.PRC_BLUEPRINT_VERSION;
import static org.onap.so.client.cds.PayloadConstants.SCOPE;

@RunWith(MockitoJUnitRunner.class)
public class GenericPnfCDSControllerRunnableBBTest extends BaseTaskTest {

    @InjectMocks
    private GenericPnfCDSControllerRunnableBB genericPnfCDSControllerRunnableBB;

    private ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();

    @Test
    public void prepareTest() throws BBObjectNotFoundException {
        // given
        preparePnfAndExtractForPnf();

        execution.setVariable(PRC_BLUEPRINT_NAME, "blueprint_name");
        execution.setVariable(PRC_BLUEPRINT_VERSION, "blueprint_version");
        execution.setVariable(MSO_REQUEST_ID, "mso_request_id");
        execution.setVariable(SCOPE, "scope");

        controllerContext.setExecution(execution);

        // when
        genericPnfCDSControllerRunnableBB.prepare(controllerContext);

        // then
        final AbstractCDSPropertiesBean abstractCDSPropertiesBean = execution.getVariable(EXECUTION_OBJECT);
        final JSONObject actionProperties = new JSONObject(abstractCDSPropertiesBean.getRequestObject())
                .getJSONObject("action-request").getJSONObject("action-properties");

        assertThat(abstractCDSPropertiesBean).isNotNull();
        assertThat(abstractCDSPropertiesBean).isInstanceOf(AbstractCDSPropertiesBean.class);
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isNotNull();
        assertThat(abstractCDSPropertiesBean.getRequestObject()).isInstanceOf(String.class);

        assertEquals("blueprint_name", abstractCDSPropertiesBean.getBlueprintName());
        assertEquals("blueprint_version", abstractCDSPropertiesBean.getBlueprintVersion());
        assertEquals("mso_request_id", abstractCDSPropertiesBean.getRequestId());
        assertEquals("action", abstractCDSPropertiesBean.getActionName());
        assertEquals("sync", abstractCDSPropertiesBean.getMode());
        assertEquals("SO", abstractCDSPropertiesBean.getOriginatorId());

        assertEquals("5df8b6de-2083-11e7-93ae-92361f002671", actionProperties.get("pnf-id"));
        assertEquals("test_service_id", actionProperties.get("service-instance-id"));
        assertEquals("6bc0b04d-1873-4721-b53d-6615225b2a28", actionProperties.get("service-model-uuid"));
        assertEquals("PNFDemo", actionProperties.get("pnf-name"));
        assertEquals("9acb3a83-8a52-412c-9a45-901764938144", actionProperties.get("pnf-customization-uuid"));

    }

    private void preparePnfAndExtractForPnf() throws BBObjectNotFoundException {
        Pnf pnf = new Pnf();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.PNF))).thenReturn(pnf);

        pnf.setPnfName("PNFDemo");
        pnf.setPnfId("5df8b6de-2083-11e7-93ae-92361f002671");
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        modelInfoPnf.setModelCustomizationUuid("9acb3a83-8a52-412c-9a45-901764938144");
        modelInfoPnf.setModelInstanceName("test_service_id");
        modelInfoPnf.setModelUuid("6bc0b04d-1873-4721-b53d-6615225b2a28");
        pnf.setModelInfoPnf(modelInfoPnf);
    }
}
