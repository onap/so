/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 *
 * ================================================================================
 * Copyright (c) 2020 Ericsson. All rights reserved
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

package org.onap.so.bpmn.infrastructure.service.level;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.infrastructure.service.level.impl.PrepareServiceUpgradeDE;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.onap.so.bpmn.infrastructure.service.level.AbstractServiceUpgradeDE.PNF_HC_PARAMS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrepareServiceUpgradeDE.class, ExceptionBuilder.class})
public class PrepareServiceUpgradeDETest {

    private static final String TEST_PNF_SCOPE = "pnf";
    private static final String TEST_PROCESS_KEY = "testProcessKey";
    private static final String PROCESS_KEY_VALUE = "testProcessKeyValue";
    private Map<String, String> pnfHealthCheckTestParams = new HashMap<>();

    @Autowired
    private PrepareServiceUpgradeDE prepareServiceUpgradeDE;

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DelegateExecution execution = new DelegateExecutionFake();
    private DelegateExecution invalidExecution = new DelegateExecutionFake();


    @Before
    public void setUpPnfUpgradeTest() {
        pnfHealthCheckTestParams.put("TEST_SERVICE_MODEL_INFO", "d4c6855e-3be2-5dtu-9390-c999a38829bc");
        pnfHealthCheckTestParams.put("TEST_SERVICE_INSTANCE_NAME", "test_service_id");
        pnfHealthCheckTestParams.put("TEST_PNF_CORRELATION_ID", "pnfCorrelationId");
        pnfHealthCheckTestParams.put("TEST_MODEL_UUID", "6bc0b04d-1873-4721-b53d-6615225b2a28");
        pnfHealthCheckTestParams.put("TEST_PNF_UUID", "c93g70d9-8de3-57f1-7de1-f5690ac2b005");
        pnfHealthCheckTestParams.put("TEST_PRC_BLUEPRINT_NAME", "serviceUpgrade");
        pnfHealthCheckTestParams.put("TEST_PRC_BLUEPRINT_VERSION", "1.0.2");
        pnfHealthCheckTestParams.put("TEST_PRC_CUSTOMIZATION_UUID", "PRC_customizationUuid");
        pnfHealthCheckTestParams.put("TEST_RESOURCE_CUSTOMIZATION_UUID_PARAM", "9acb3a83-8a52-412c-9a45-901764938144");
        pnfHealthCheckTestParams.put("TEST_PRC_INSTANCE_NAME", "Demo_pnf");
        pnfHealthCheckTestParams.put("TEST_PRC_CONTROLLER_ACTOR", "cds");
        pnfHealthCheckTestParams.put("TEST_REQUEST_PAYLOAD", "test_payload");

        for (String param : PNF_HC_PARAMS) {
            execution.setVariable(param, pnfHealthCheckTestParams.get("TEST_" + param));
        }
        execution.setVariable("CONTROLLER_SCOPE", TEST_PNF_SCOPE);
        execution.setVariable(TEST_PROCESS_KEY, PROCESS_KEY_VALUE);

        invalidExecution.setVariables(execution.getVariables());
    }

    @Test
    public void executePnfUpgradeSuccessTest() throws Exception {
        prepareServiceUpgradeDE.execute(execution);
        // Expect the pnf health check workflow to be set in to execution if validation is successful
        assertEquals("GenericPnfHealthCheck", execution.getVariable("healthCheckWorkflow"));
    }

    @Test
    public void validateSuccessParamsForPnfTest() {
        boolean isValid = prepareServiceUpgradeDE.validateParamsWithScope(execution, TEST_PNF_SCOPE);
        assertTrue(isValid);
    }

    @Test
    public void validateFailureParamsForPnfTest() throws Exception {
        invalidExecution.removeVariable("PNF_UUID");
        invalidExecution.setVariable("PRC_BLUEPRINT_NAME", null);
        boolean isValid = prepareServiceUpgradeDE.validateParamsWithScope(invalidExecution, TEST_PNF_SCOPE);
        assertFalse(isValid);
        // BPMN exception is thrown in case of validation failure or invalid execution
        thrown.expect(BpmnError.class);
        prepareServiceUpgradeDE.execute(invalidExecution);
    }

}


