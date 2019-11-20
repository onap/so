/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;


public class GenericControllerExecutionDETest extends BaseBPMNTest {

    @Test
    public void testExecution_validInput_expectedExecution() {

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("GenericControllerExecutionDE", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("Start_ControllerExecutionDE", "Call_ControllerExecutionDE",
                "End_ControllerExecutionDE");
        assertThat(pi).isEnded();
    }

    @Test
    public void testExecution_failedExecution_exceptionThrown() {
        doThrow(new BpmnError("7000", "TESTING ERRORS")).when(controllerExecutionDE)
                .execute(any(DelegateExecution.class));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("GenericControllerExecutionDE", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("Start_ControllerExecutionDE", "Call_ControllerExecutionDE")
                .hasNotPassed("End_ControllerExecutionDE");
        assertThat(pi).isEnded();
    }
}
