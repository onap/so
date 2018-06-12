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

package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class AssignVnfBBTest extends BaseBPMNTest {
	@Test
	public void sunnyDayAssignVnfBBTest() throws InterruptedException, IOException {
		variables.put("callHoming", true);
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("AssignVnfBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_AssignVnfBB", "Task_CreateVnf", "Task_createPlatform", "Task_createLineOfBusiness", "Task_createInstanceGroups",
				"Task_callHoming", "Task_SDNCAdapterVnfTopologyAssign", "Task_UpdateVnfOrchestrationStatusAssigned",
				"End_AssignVnfBB");
		assertThat(pi).isEnded();
	}

	@Test
	public void rainyDayAssignVnfBBTest() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiCreateTasks)
				.createVnf(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("AssignVnfBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_AssignVnfBB", "Task_CreateVnf").hasNotPassed(
				"Task_createPlatform", "Task_createLineOfBusiness", "Task_createInstanceGroups", "Task_SDNCAdapterVnfTopologyAssign",
				"Task_UpdateVnfOrchestrationStatusAssigned", "End_AssignVnfBB");
		assertThat(pi).isEnded();
	}
}
