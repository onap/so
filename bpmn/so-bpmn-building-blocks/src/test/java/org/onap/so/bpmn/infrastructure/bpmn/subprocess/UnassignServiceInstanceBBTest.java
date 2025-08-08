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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;

public class UnassignServiceInstanceBBTest extends BaseBPMNTest {
    @Test
    public void sunnyDayUnassignServiceInstanceSDNC() {
        mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
        BuildingBlockExecution bbe = new DelegateExecutionImpl(new ExecutionImpl());
        variables.put("gBuildingBlockExecution", bbe);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnassignServiceInstanceBB", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("Start_UnassignServiceInstanceBB",
                "Task_SdncUnassignServiceInstance", "CallActivity_sdncHandlerCall", "Task_AAIDeleteServiceInstance",
                "End_UnassignServiceInstanceBB");
        assertThat(pi).isEnded();
    }
}
