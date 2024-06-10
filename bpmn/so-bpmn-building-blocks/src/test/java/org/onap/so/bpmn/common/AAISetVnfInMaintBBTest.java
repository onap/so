/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;

public class AAISetVnfInMaintBBTest extends BaseBPMNTest {


    @Test
    public void sunnyDayAAISetVnfInMaintBBTest() throws InterruptedException, IOException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("AAISetVnfInMaintBB", variables);
        assertThat(pi).isNotNull().isStarted().hasPassedInOrder("Start_AAISetVnfInMaintBB", "Task_SetInMaint",
                "End_AAISetVnfInMaintBB");
    }

    @Test
    public void rainyDayAAISetVnfInMaintBBTest() {
        doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiFlagTasks)
                .modifyVnfInMaintFlag(any(BuildingBlockExecution.class), any(boolean.class));

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("AAISetVnfInMaintBB", variables);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance).isStarted().hasPassedInOrder("Start_AAISetVnfInMaintBB", "Task_SetInMaint")
                .hasNotPassed("End_AAISetVnfInMaintBB");
        assertThat(processInstance).isEnded();
    }


}
