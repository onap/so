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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;

public class DeactivateNetworkBBTest extends BaseBPMNTest{
	@Test
	public void sunnyDayDeactivateNetworkBB_Test() throws InterruptedException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("DeactivateNetworkBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("DeactivateNetworkBB_Start", "DeactivateNetworkSDNC", "DeactivateNetworkAAI", "DeactivateNetworkBB_End");
		assertThat(pi).isEnded();
	}

	@Test
	public void rainyDayDeactivateNetworkBB_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(sdncDeactivateTasks).deactivateNetwork(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("DeactivateNetworkBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("DeactivateNetworkBB_Start", "DeactivateNetworkSDNC")
				.hasNotPassed("DeactivateNetworkAAI", "DeactivateNetworkBB_End");
		assertThat(pi).isEnded();
	}
}
