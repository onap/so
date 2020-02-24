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
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Spy;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
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

    private static String buildExpectedJson(String action, String scope) {
        JSONObject jsonSubSubObject = new JSONObject();
        jsonSubSubObject.put("service-instance-id", "test_service_id");
        jsonSubSubObject.put("pnf-customization-uuid", "9acb3a83-8a52-412c-9a45-901764938144");
        jsonSubSubObject.put("pnf-id", "5df8b6de-2083-11e7-93ae-92361f002671");
        jsonSubSubObject.put("target-software-version", "demo-sw-ver2.0.0");
        jsonSubSubObject.put("pnf-name", "PNFDemo");
        jsonSubSubObject.put("service-model-uuid", "6bc0b04d-1873-4721-b53d-6615225b2a28");

        JSONObject jsonSubObject = new JSONObject();
        jsonSubObject.put(action + "-properties", jsonSubSubObject);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(action + "-request", jsonSubObject);

        return jsonObject.toString();
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
    public void testExecution_validPnf_action_executionObjectCreated() {
        AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();
        doNothing().when(abstractCDSProcessingBBUtils).constructExecutionServiceInputObject(execution);
        doNothing().when(abstractCDSProcessingBBUtils).sendRequestToCDSClient(execution);
        doReturn(abstractCDSPropertiesBean).when(genericPnfCDSControllerRunnableBB)
                .prepareAndSetCdsPropertyBean(execution);

        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(execution);
        controllerContext.setControllerActor("cds");
        controllerContext.setControllerAction(this.action);
        controllerContext.setControllerScope(this.scope);

        Boolean isUnderstandable = genericPnfCDSControllerRunnableBB.understand(controllerContext);
        Boolean isReady = genericPnfCDSControllerRunnableBB.ready(controllerContext);
        genericPnfCDSControllerRunnableBB.prepare(controllerContext);
        genericPnfCDSControllerRunnableBB.run(controllerContext);

        assertTrue(isUnderstandable);
        assertTrue(isReady);
        Object executionObject = execution.getVariable(EXECUTION_OBJECT);
        assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
        assertThat(executionObject).isNotNull();
    }
}
