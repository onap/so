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

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

import java.io.IOException;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;

public class ActivateVnfBBTest extends BaseBPMNTest{
	@Test
	public void sunnyDay() throws InterruptedException, IOException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateVnfBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_ActivateVnfBB", "Task_SDNCAdapterVnfTopologyActivate",
				"Task_ActivateOrchestrationStatusVnf", "End_ActivateVnfBB");
		assertThat(pi).isEnded();
	}

}
