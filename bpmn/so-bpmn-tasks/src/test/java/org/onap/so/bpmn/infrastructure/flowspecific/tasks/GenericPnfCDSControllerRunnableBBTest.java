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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.GeneratePayloadForCds;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.PayloadGenerationException;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

@RunWith(Parameterized.class)
public class GenericPnfCDSControllerRunnableBBTest extends BaseTaskTest {
    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Spy
    @InjectMocks
    private GenericPnfCDSControllerRunnableBB genericPnfCDSControllerRunnableBB;

    @Mock
    private GeneratePayloadForCds generatePayloadForCds;

    @Mock
    private AbstractCDSProcessingBBUtils abstractCDSProcessingBBUtils;

    private static final String PRECHECK_ACTION = "precheck";
    private static final String DOWNLOAD_ACTION = "downloadNeSw";
    private static final String ACTIVATE_ACTION = "activateNeSw";
    private static final String POSTCHECK_ACTION = "postcheck";

    private String description;
    private String action;
    private String scope;
    private String expectedJson;

    public GenericPnfCDSControllerRunnableBBTest(String description, String action, String scope, String expectedJson) {
        this.description = description;
        this.action = action;
        this.scope = scope;
        this.expectedJson = expectedJson;
    }

    private Pnf preparePnfAndExtractForPnf() throws BBObjectNotFoundException {
        Pnf pnf = new Pnf();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.PNF))).thenReturn(pnf);

        pnf.setPnfName("PNFDemo");
        pnf.setPnfId("5df8b6de-2083-11e7-93ae-92361f002671");
        ModelInfoPnf modelInfoPnf = new ModelInfoPnf();
        modelInfoPnf.setModelCustomizationUuid("9acb3a83-8a52-412c-9a45-901764938144");
        modelInfoPnf.setModelInstanceName("test_service_id");
        modelInfoPnf.setModelUuid("6bc0b04d-1873-4721-b53d-6615225b2a28");
        pnf.setModelInfoPnf(modelInfoPnf);

        return pnf;
    }

    private static String buildExpectedJson(String action, String scope) {
        return "{\"" + action + "-request\":" + "{\"" + action + "-" + "properties\":"
                + "{\"service-instance-id\":\"test_service_id\","
                + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\","
                + "\"target-software-version\":\"demo-sw-ver2.0.0\"," + "\"pnf-name\":\"PNFDemo\","
                + "\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"}," + "\"resolution-key\":\"PNFDemo\""
                + "}" + "}";
    }

    @Parameterized.Parameters(name = "index {0}")
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                {"Test JSON for action:" + PRECHECK_ACTION + " scope:pnf", PRECHECK_ACTION, "pnf",
                        buildExpectedJson(PRECHECK_ACTION, "pnf")},
                {"Test JSON for action:" + DOWNLOAD_ACTION + " scope:pnf", DOWNLOAD_ACTION, "pnf",
                        buildExpectedJson(DOWNLOAD_ACTION, "pnf")},
                {"Test JSON for action:" + ACTIVATE_ACTION + " scope:pnf", ACTIVATE_ACTION, "pnf",
                        buildExpectedJson(ACTIVATE_ACTION, "pnf")},
                {"Test JSON for action:" + POSTCHECK_ACTION + " scope:pnf", POSTCHECK_ACTION, "pnf",
                        buildExpectedJson(POSTCHECK_ACTION, "pnf")},});
    }

    @Test
    public void testExecution_validPnf_action_executionObjectCreated()
            throws PayloadGenerationException, BBObjectNotFoundException {
        doReturn("config-assign").when(genericPnfCDSControllerRunnableBB).getAction(any());
        doReturn("MSORequestId").when(genericPnfCDSControllerRunnableBB).getMSORequestId(any());
        doReturn("BlueprintName").when(genericPnfCDSControllerRunnableBB).getBlueprintName(any());
        doReturn("BlueprintVersion").when(genericPnfCDSControllerRunnableBB).getBlueprintVersion(any());
        preparePnfAndExtractForPnf();



        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(execution);
        controllerContext.setControllerActor("cds");
        controllerContext.setControllerAction(this.action);
        controllerContext.setControllerScope(this.scope);
        AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();
        doNothing().when(abstractCDSProcessingBBUtils).constructExecutionServiceInputObject(execution);
        doNothing().when(abstractCDSProcessingBBUtils).sendRequestToCDSClient(execution);
        doReturn(abstractCDSPropertiesBean).when(generatePayloadForCds).buildCdsPropertiesBean(execution);

        boolean isUnderstandable = genericPnfCDSControllerRunnableBB.understand(controllerContext);
        boolean isReady = genericPnfCDSControllerRunnableBB.ready(controllerContext);
        genericPnfCDSControllerRunnableBB.prepare(controllerContext);
        genericPnfCDSControllerRunnableBB.run(controllerContext);

        assertTrue(isUnderstandable);
        assertTrue(isReady);
        Object executionObject = execution.getVariable(EXECUTION_OBJECT);
        assertThat(executionObject).isNotNull();
        assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
    }
}
