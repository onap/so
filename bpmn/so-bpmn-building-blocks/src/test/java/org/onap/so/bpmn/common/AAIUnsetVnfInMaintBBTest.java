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

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.BaseTest;

public class AAIUnsetVnfInMaintBBTest extends BaseTest {
	
	@Test
	@Ignore
	public void sunnyDayAAISetVnftInMaintBBTest() throws InterruptedException, IOException {		
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("AAIUnsetVnfInMaintBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_AAIUnsetVnfInMaintBB", "Task_UnsetInMaint", "End_AAIUnsetVnfInMaintBB");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayAAISetVnfInMaintBBTest() {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiFlagTasks).modifyVnfInMaintFlag(any(BuildingBlockExecution.class), any(boolean.class));
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("AAIUnsetVnfInMaintBB", variables);
		assertThat(processInstance).isNotNull();
		assertThat(processInstance).isStarted()
			.hasPassedInOrder("Start_AAIUnsetVnfInMaintBB", "Task_UnsetInMaint")
			.hasNotPassed("End_AAIUnsetVnfInMaintBB");
		assertThat(processInstance).isEnded();
	}

	
}
