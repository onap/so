/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.client.exception;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.logging.filter.base.ONAPComponents;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionBuilderUnitTest {
    @Mock
    private BuildingBlockExecution buildingBlockExecution;

    @InjectMocks
    @Spy
    private ExceptionBuilder exceptionBuilder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Exception e = new Exception("failure message");
    private DelegateExecution execution;

    @Before
    public void setup() {
        execution = new DelegateExecutionFake();
    }


    @Test
    public void buildAndThrowWorkflowExceptionTest() {
        String expectedErrorMessage =
                "Exception in org.onap.so.client.exception.ExceptionBuilder.buildAndThrowWorkflowException failure message";
        doNothing().when(exceptionBuilder).buildAndThrowWorkflowException(execution, 7000, expectedErrorMessage,
                ONAPComponents.SDNC);

        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e, ONAPComponents.SDNC);

        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(execution, 7000, expectedErrorMessage,
                ONAPComponents.SDNC);
    }

    @Test
    public void buildAndThrowWorkflowExceptionBuildingBlockExecutionTest() {
        String expectedErrorMessage =
                "Exception in org.onap.so.client.exception.ExceptionBuilder.buildAndThrowWorkflowException failure message";
        doNothing().when(exceptionBuilder).buildAndThrowWorkflowException(buildingBlockExecution, 7000,
                expectedErrorMessage, ONAPComponents.SDNC);

        exceptionBuilder.buildAndThrowWorkflowException(buildingBlockExecution, 7000, e, ONAPComponents.SDNC);

        verify(exceptionBuilder, times(1)).buildAndThrowWorkflowException(buildingBlockExecution, 7000,
                expectedErrorMessage, ONAPComponents.SDNC);
    }

    @Test
    public void buildAndThrowWorkflowExceptionWithErrorMessageTest() {
        doReturn("Process key").when(exceptionBuilder).getProcessKey(execution);

        thrown.expect(BpmnError.class);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getMessage(), ONAPComponents.SDNC);
    }

    @Test
    public void buildAndThrowWorkflowExceptionWithWorkStepTest() {
        doReturn("Process key").when(exceptionBuilder).getProcessKey(execution);

        try {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getMessage(), ONAPComponents.SDNC,
                    "WORKSTEP");
        } catch (BpmnError e) {
        }
        WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
        assertEquals("WORKSTEP", workflowException.getWorkStep());
    }
}
