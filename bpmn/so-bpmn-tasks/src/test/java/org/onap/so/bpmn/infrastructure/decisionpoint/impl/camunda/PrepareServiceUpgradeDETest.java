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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.onap.so.BaseBPMNTest;
import org.onap.so.client.exception.ExceptionBuilder;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class PrepareServiceUpgradeDETest extends BaseBPMNTest {

    private static final String TEST_PNF_SCOPE = "pnf";
    private static final String TEST_PNF_NAME = "Demo_pnf";
    private static final String TEST_PNF_ID = "c93g70d9-8de3-57f1-7de1-f5690ac2b005";
    private static final String TEST_PNF_IPV4_ADDR = "192.168.10.14";
    private static final String TEST_SERVICE_INST_ID = "d4c6855e-3be2-5dtu-9390-c999a38829bc";
    private static final String TEST_PROCESS_KEY = "testProcessKey";
    private static final String PROCESS_KEY_VALUE = "testProcessKeyValue";

    @InjectMocks
    private PrepareServiceUpgradeDE prepareServiceUpgradeDE = new PrepareServiceUpgradeDE();

    @InjectMocks
    @Spy
    private ExceptionBuilder exceptionBuilder;

    @InjectMocks
    @Spy
    private RuntimeServiceImpl runtimeService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DelegateExecution execution = new DelegateExecutionFake();
    private DelegateExecution invalidExecution = new DelegateExecutionFake();
    private ProcessInstanceWithVariables piVariables;
    private VariableMap vMap;

    @Before
    public void setUpPnfUpgradeTest() {
        execution.setVariable("CONTROLLER_SCOPE", TEST_PNF_SCOPE);
        execution.setVariable("PNF_NAME", TEST_PNF_NAME);
        execution.setVariable("PNF_ID", TEST_PNF_ID);
        execution.setVariable("PNF_IPV4_ADDR", TEST_PNF_IPV4_ADDR);
        execution.setVariable("SERVICE_INST_ID", TEST_SERVICE_INST_ID);
        execution.setVariable(TEST_PROCESS_KEY, PROCESS_KEY_VALUE);

        invalidExecution.setVariables(execution.getVariables());

        // Mocking successful execution of pnf health check workflow
        ExecutionEntity healthCheckExecution = mock(ExecutionEntity.class);
        vMap = new VariableMapImpl().putValue("HEALTH_CHECK_RESULT", "success");
        piVariables = new ProcessInstanceWithVariablesImpl(healthCheckExecution, vMap);
        ProcessInstantiationBuilder piBuilder = mock(ProcessInstantiationBuilder.class);

        doReturn(piBuilder).when(runtimeService).createProcessInstanceByKey("GenericPnfHealthCheck");
        doReturn(piBuilder).when(piBuilder).setVariables(any());
        doReturn(piVariables).when(piBuilder).executeWithVariablesInReturn();
    }

    @Test
    public void executePnfUpgradeSuccessTest() throws Exception {
        prepareServiceUpgradeDE.execute(execution);
        assertEquals("success", execution.getVariable("HEALTH_CHECK_RESULT"));
    }

    @Test
    public void validateSuccessParamsForPnfTest() {
        boolean isValid = prepareServiceUpgradeDE.validateParamsForPnf(execution);
        assertTrue(isValid);
    }

    @Test
    public void validateFailureParamsForPnfTest() throws Exception {
        invalidExecution.setVariable("PNF_ID", "");
        boolean isValid = prepareServiceUpgradeDE.validateParamsForPnf(invalidExecution);
        assertFalse(isValid);
        thrown.expect(BpmnError.class);
        prepareServiceUpgradeDE.execute(invalidExecution);
    }



}
