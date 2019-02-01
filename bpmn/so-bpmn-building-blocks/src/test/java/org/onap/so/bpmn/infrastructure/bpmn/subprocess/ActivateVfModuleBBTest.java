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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;

public class ActivateVfModuleBBTest extends BaseBPMNTest{
	
	@Before
	public void before() {
		variables.put("vfModuleActivateTimerDuration", "PT2S");
		variables.put("auditInventoryNeeded", "true");
	}

	@Test
	public void sunnyDay() throws InterruptedException, IOException {
		mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateVfModuleBB", variables);
		List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                .topic("InventoryAudit", 60L * 1000L).execute();
        while (!tasks.isEmpty()) {
            for (LockedExternalTask task : tasks) {
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }
            tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                    .topic("InventoryAudit", 60L * 1000L).execute();
        }
		
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("ActivateVfModuleBB_Start","ExclusiveGateway_1v8bmbu","Setup_AAI_Inventory_Audit", "Audit_AAI_Inventory", "ActivateVfModule", "CallActivity_sdncHandler",
				"UpdateVfModuleActiveStatus", "ActivateVfModuleBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDay() throws Exception {
		mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
		doThrow(BpmnError.class).when(aaiUpdateTasks).updateOrchestrationStatusActivateVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateVfModuleBB", variables);
		List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                .topic("InventoryAudit", 60L * 1000L).execute();
        while (!tasks.isEmpty()) {
            for (LockedExternalTask task : tasks) {
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }
            tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                    .topic("InventoryAudit", 60L * 1000L).execute();
        }

		assertThat(pi).isNotNull().isStarted()
				.hasPassedInOrder("ActivateVfModuleBB_Start","ExclusiveGateway_1v8bmbu","Setup_AAI_Inventory_Audit", "Audit_AAI_Inventory", "ActivateVfModule", "UpdateVfModuleActiveStatus")
				.hasNotPassed("ActivateVfModuleBB_End");
	}
}
