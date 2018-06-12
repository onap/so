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

public class ActivateVfModuleBBTest extends BaseBPMNTest{
	@Test
	public void sunnyDay() throws InterruptedException, IOException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("ActivateVfModuleBB_Start", "ActivateVfModule",
				"UpdateVfModuleActiveStatus", "ActivateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDay() throws Exception {
		doThrow(BpmnError.class).when(aaiUpdateTasks).updateOrchestrationStatusActivateVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("ActivateVfModuleBB_Start", "ActivateVfModule", "UpdateVfModuleActiveStatus")
				.hasNotPassed("ActivateVfModuleBB_End");
		assertThat(pi).isEnded().hasVariables("gBuildingBlockExecution");
	
	}
}
