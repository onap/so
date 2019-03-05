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
import static org.mockito.ArgumentMatchers.any;
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

public class DeleteVfModuleBBTest extends BaseBPMNTest{

	@Before
	public void before() {
		variables.put("auditInventoryNeeded", true);
	}

	@Test
	public void sunnyDay() throws InterruptedException, IOException {
		mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
		mockSubprocess("VnfAdapter", "Mocked VnfAdapter", "GenericStub");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("DeleteVfModuleBB", variables);
		List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                .topic("InventoryDeleteAudit", 60L * 1000L).execute();
        while (!tasks.isEmpty()) {
            for (LockedExternalTask task : tasks) {
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }
            tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                    .topic("InventoryDeleteAudit", 60L * 1000L).execute();
        }
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassed("DeleteVfModuleBB_Start","ExclusiveGateway_0xrgzm7","ExclusiveGateway_1yvh16a","Check_Audit",
				"Setup_Audit_Variable","Audit_Inventory","DeleteVfModuleVnfAdapter", "VnfAdapter",
				"DeleteNetworkPolicies", "UpdateVnfIpv4OamAddress", "UpdateVnfManagementV6Address",
				"UpdateVfModuleContrailServiceInstanceFqdn",
				"UpdateVfModuleHeatStackId", "UpdateVfModuleDeleteStatus", "DeleteVfModuleBB_End");
		assertThat(pi).isEnded();
	}

	@Test
	public void rainyDay() throws Exception {
		doThrow(BpmnError.class).when(vnfAdapterDeleteTasks).deleteVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("DeleteVfModuleBB", variables);
		List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                .topic("InventoryDeleteAudit", 60L * 1000L).execute();
        while (!tasks.isEmpty()) {
            for (LockedExternalTask task : tasks) {
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }
            tasks = externalTaskService.fetchAndLock(100, "externalWorkerId")
                    .topic("InventoryDeleteAudit", 60L * 1000L).execute();
        }
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassed("DeleteVfModuleBB_Start", "DeleteVfModuleVnfAdapter")
				.hasNotPassed("VnfAdapter", "DeleteNetworkPolicies", "UpdateVnfIpv4OamAddress", "UpdateVnfManagementV6Address",
						"UpdateVfModuleContrailServiceInstanceFqdn","UpdateVfModuleHeatStackId", "UpdateVfModuleDeleteStatus",
						"DeleteVfModuleBB_End");
		assertThat(pi).isEnded();
	}
}
