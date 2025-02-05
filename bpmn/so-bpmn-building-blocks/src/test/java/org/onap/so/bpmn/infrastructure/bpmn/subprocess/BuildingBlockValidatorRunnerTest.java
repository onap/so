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

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;

public class BuildingBlockValidatorRunnerTest extends BaseBPMNTest {
    @Test
    public void sunnyDayActivateNetwork_Test() throws InterruptedException {
        variables.put("flowToBeCalled", "CreateVolumeGroupBB");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("BuildingBlockValidatorRunnerTest", variables);
        assertThat(pi).isNotNull();

        execute(job());
        execute(job());
        assertThat(pi).isStarted().hasPassedInOrder("ServiceTask_1", "ServiceTask_2", "ServiceTask_3");
        assertThat(pi).isEnded();
    }
}
