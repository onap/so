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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class CreateVfModuleBBTest extends BaseBPMNTest{
	@Test
	public void sunnyDayCreateVfModule_Test() throws InterruptedException {
		mockSubprocess("VnfAdapter", "Mocked VnfAdapter", "GenericStub");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("CreateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("CreateVfModuleBB_Start",
				"QueryVnf",
				"QueryVfModule",
				"CreateVfModule",
				"VnfAdapter",
				"UpdateVfModuleStatus",
				"CreateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayCreateVfModuleSDNCQueryVnfError_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(sdncQueryTasks).queryVnf(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("CreateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("CreateVfModuleBB_Start", "QueryVnf")
				.hasNotPassed("QueryVfModule", "CreateVfModule", "VnfAdapter", "UpdateVfModuleStatus", "CreateVfModuleBB_End");
		assertThat(pi).isEnded();
	}

	@Test
	public void rainyDayCreateVfModuleSDNCQueryVnfModuleError_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(sdncQueryTasks).queryVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("CreateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("CreateVfModuleBB_Start", "QueryVnf", "QueryVfModule")
				.hasNotPassed("CreateVfModule", "VnfAdapter", "UpdateVfModuleStatus", "CreateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayCreateVfModuleVnfAdapterCreateError_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(vnfAdapterCreateTasks).createVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("CreateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("CreateVfModuleBB_Start", "QueryVnf", "QueryVfModule", "CreateVfModule")
				.hasNotPassed("VnfAdapter", "UpdateVfModuleStatus", "CreateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayCreateVfModuleUpdateVfModuleStatusError_Test() throws Exception {
		mockSubprocess("VnfAdapter", "Mocked VnfAdapter", "GenericStub");
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiUpdateTasks).updateOrchestrationStatusCreatedVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("CreateVfModuleBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("CreateVfModuleBB_Start", "QueryVnf", "QueryVfModule", "CreateVfModule", "VnfAdapter", "UpdateVfModuleStatus")
				.hasNotPassed("CreateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
}
