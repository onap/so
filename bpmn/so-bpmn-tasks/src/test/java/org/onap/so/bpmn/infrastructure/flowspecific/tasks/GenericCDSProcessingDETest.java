/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.AbstractCDSProcessingBBUtils;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.exception.ExceptionBuilder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import java.util.Arrays;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

@RunWith(Parameterized.class)
@ContextConfiguration(
        classes = {ExceptionBuilder.class, GenericCDSProcessingDE.class, AbstractCDSProcessingBBUtils.class})
public class GenericCDSProcessingDETest extends BaseTaskTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    private static final String DOWNLOAD_ACTION = "downloadNeSw";
    private static final String ACTIVATE_ACTION = "activateNeSw";
    private static final String PRECHECK_ACTION = "precheck";
    private static final String POSTCHECK_ACTION = "postcheck";
    private static final String ASSIGN_ACTION = "config-assign";
    private static final String DEPLOY_ACTION = "config-deploy";

    private String description;
    private String action;
    private String scope;
    private String expectedJson;

    public GenericCDSProcessingDETest(String desc, String action, String scope, String expectedJson) {
        this.description = desc;
        this.action = action;
        this.scope = scope;
        this.expectedJson = expectedJson;

    }

    @Parameterized.Parameters(name = "index {0}")
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                {"Test JSON for action:" + ACTIVATE_ACTION + " scope:pnf", ACTIVATE_ACTION, "pnf",
                        buildExpectedJson(ACTIVATE_ACTION, "pnf")},
                {"Test JSON for action:" + DOWNLOAD_ACTION + " scope:pnf", DOWNLOAD_ACTION, "pnf",
                        buildExpectedJson(DOWNLOAD_ACTION, "pnf")},
                {"Test JSON for action:" + ASSIGN_ACTION + " scope:pnf", ASSIGN_ACTION, "pnf",
                        buildExpectedJson(ASSIGN_ACTION, "pnf")},
                {"Test JSON for action:" + DEPLOY_ACTION + " scope:pnf", DEPLOY_ACTION, "pnf",
                        buildExpectedJson(DEPLOY_ACTION, "pnf")},
                {"Test JSON for action:" + PRECHECK_ACTION + " scope:pnf", PRECHECK_ACTION, "pnf",
                        buildExpectedJson(PRECHECK_ACTION, "pnf")},
                {"Test JSON for action:" + POSTCHECK_ACTION + " scope:pnf", POSTCHECK_ACTION, "pnf",
                        buildExpectedJson(POSTCHECK_ACTION, "pnf")},});
    }

    private static String buildExpectedJson(String action, String scope) {
        final String EXPECTED_SW_JSON = "{\"" + action + "-request\":" + "{\"" + action + "-" + "properties\":"
                + "{\"service-instance-id\":\"test_service_id\","
                + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\","
                + "\"target-software-version\":\"demo-sw-ver2.0.0\"," + "\"pnf-name\":\"PNFDemo\","
                + "\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"}," + "\"resolution-key\":\"PNFDemo\""
                + "}" + "}";
        final String EXPECTED_CONFIG_JSON = "{\"" + action + "-request\":" + "{\"" + action + "-" + "properties\":"
                + "{\"service-instance-id\":\"test_service_id\","
                + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\"," + "\"pnf-name\":\"PNFDemo\","
                + "\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"}," + "\"resolution-key\":\"PNFDemo\""
                + "}" + "}";

        switch (action) {
            case ACTIVATE_ACTION:
            case DOWNLOAD_ACTION:
            case PRECHECK_ACTION:
            case POSTCHECK_ACTION:
                return EXPECTED_SW_JSON;
            case ASSIGN_ACTION:
            case DEPLOY_ACTION:
                return EXPECTED_CONFIG_JSON;
            default:
                return "";
        }

    }

    private static String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static String TEST_PROCESS_KEY = "processKey1";
    private static String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private static String TEST_SOFTWARE_VERSION = "demo-sw-ver2.0.0";

    @Mock
    private ExceptionBuilder exceptionBuilder;

    @Mock
    private AbstractCDSProcessingBBUtils cdsDispather;


    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() {
        // given
        execution.setVariable("testProcessKey", TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        execution.setVariable("target-software-version", TEST_SOFTWARE_VERSION);

        execution.setVariable("scope", this.scope);
        execution.setVariable("action", this.action);
    }

    @Test
    public void testExecution_validPnf_action_executionObjectCreated() {
        try {

            // given
            ControllerContext controllerContext = new ControllerContext();
            controllerContext.setExecution(execution);
            controllerContext.setControllerActor("cds");
            controllerContext.setControllerAction(this.action);
            controllerContext.setControllerScope(this.scope);
            ControllerRunnable<DelegateExecution> controllerRunnable =
                    new GenericCDSProcessingDE(exceptionBuilder, cdsDispather);
            doNothing().when(cdsDispather).constructExecutionServiceInputObject(execution);
            doNothing().when(cdsDispather).sendRequestToCDSClient(execution);

            // when
            Boolean isUnderstandable = controllerRunnable.understand(controllerContext);
            Boolean isReady = controllerRunnable.ready(controllerContext);
            controllerRunnable.prepare(controllerContext);
            controllerRunnable.run(controllerContext);

            // verify
            assertEquals(isUnderstandable, true);
            assertEquals(isReady, true);
            Object executionObject = execution.getVariable(EXECUTION_OBJECT);
            assertThat(executionObject).isNotNull();
            assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
            checkCDSPropertiesBean((AbstractCDSPropertiesBean) executionObject, expectedJson, this.action);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown" + e.getMessage());
        }
    }

    private void checkCDSPropertiesBean(AbstractCDSPropertiesBean executionObject, String expectedRequestObjJson,
            String action) {
        assertThat(executionObject.getBlueprintName()).matches(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(executionObject.getBlueprintVersion()).matches(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(executionObject.getRequestId()).matches(TEST_MSO_REQUEST_ID);
        assertThat(executionObject.getSubRequestId()).isNotBlank();
        assertThat(executionObject.getMode()).matches("sync");
        assertThat(executionObject.getActionName()).matches(action);
        assertThat(executionObject.getOriginatorId()).matches("SO");

        assertThat(executionObject.getRequestObject()).isNotNull();
        String requestObject = executionObject.getRequestObject();

        JSONAssert.assertEquals(requestObject, expectedRequestObjJson, false);
    }
}
