/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.audit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Optional;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.LInterface;
import org.onap.so.audit.beans.AuditInventory;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class AuditStackServiceDataTest extends AuditStackServiceData {

	@InjectMocks
	AuditStackServiceData auditStackService = new AuditStackServiceData();

	@Mock
	HeatStackAudit heatStackAuditMock;

	@Mock
	Environment mockEnv;

	@Mock
	ExternalTask mockExternalTask;

	@Mock
	ExternalTaskService mockExternalTaskService;

	AuditInventory auditInventory = new AuditInventory();

	@Before
	public void setup() {
		auditInventory.setCloudOwner("cloudOwner");
		auditInventory.setCloudRegion("cloudRegion");
		auditInventory.setTenantId("tenantId");
		auditInventory.setHeatStackName("stackName");
		MockitoAnnotations.initMocks(this);
		doReturn(auditInventory).when(mockExternalTask).getVariable("auditInventory");
		doReturn("6000").when(mockEnv).getProperty("mso.workflow.topics.retryMultiplier","6000");
		doReturn("aasdfasdf").when(mockExternalTask).getId();
	}

	@Test
	public void execute_external_task_audit_success_Test() {
		doReturn(true).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId", "stackName");
		auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
		Mockito.verify(mockExternalTaskService).complete(mockExternalTask);
	}

	@Test
	public void execute_external_task_audit_first_failure_Test() {
		doReturn(false).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId", "stackName");
		doReturn(null).when(mockExternalTask).getRetries();
		auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);
		Mockito.verify(mockExternalTaskService).handleFailure(mockExternalTask,
				"Unable to find all VServers and L-Interaces in A&AI",
				"Unable to find all VServers and L-Interaces in A&AI", 8, 10000L);
	}

	@Test
	public void execute_external_task_audit_intermediate_failure_Test() {
		doReturn(false).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId", "stackName");
		doReturn(6).when(mockExternalTask).getRetries();
		auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);		
		Mockito.verify(mockExternalTaskService).handleFailure(mockExternalTask,
				"Unable to find all VServers and L-Interaces in A&AI",
				"Unable to find all VServers and L-Interaces in A&AI", 5, 12000L);

	}

	@Test
	public void execute_external_task_audit_final_failure_Test() {
		doReturn(false).when(heatStackAuditMock).auditHeatStack("cloudRegion", "cloudOwner", "tenantId", "stackName");
		doReturn(1).when(mockExternalTask).getRetries();
		auditStackService.executeExternalTask(mockExternalTask, mockExternalTaskService);		
		Mockito.verify(mockExternalTaskService).handleBpmnError(mockExternalTask,
				"AuditAAIInventoryFailure", "Number of Retries Exceeded auditing inventory");
	}

	@Test
	public void retry_sequence_calculation_Test() {
		long firstRetry = auditStackService.calculateRetryDelay(8);
		assertEquals(6000L, firstRetry);
		long secondRetry = auditStackService.calculateRetryDelay(7);
		assertEquals(6000L, secondRetry);
		long thirdRetry = auditStackService.calculateRetryDelay(6);
		assertEquals(12000L, thirdRetry);
		long fourthRetry = auditStackService.calculateRetryDelay(5);
		assertEquals(18000L, fourthRetry);
		long fifthRetry = auditStackService.calculateRetryDelay(4);
		assertEquals(30000L, fifthRetry);
		long sixRetry = auditStackService.calculateRetryDelay(3);
		assertEquals(48000L, sixRetry);
		long seventhRetry = auditStackService.calculateRetryDelay(2);
		assertEquals(78000L, seventhRetry);
		long eigthRetry = auditStackService.calculateRetryDelay(1);
		assertEquals(120000L, eigthRetry);
	}

	@Test
	public void retry_sequence_Test() {
		long firstRetry = auditStackService.calculateRetryDelay(8);
		assertEquals(6000L, firstRetry);
		long secondRetry = auditStackService.calculateRetryDelay(7);
		assertEquals(6000L, secondRetry);
		long thirdRetry = auditStackService.calculateRetryDelay(6);
		assertEquals(12000L, thirdRetry);
		long fourthRetry = auditStackService.calculateRetryDelay(5);
		assertEquals(18000L, fourthRetry);
		long fifthRetry = auditStackService.calculateRetryDelay(4);
		assertEquals(30000L, fifthRetry);
		long sixRetry = auditStackService.calculateRetryDelay(3);
		assertEquals(48000L, sixRetry);
		long seventhRetry = auditStackService.calculateRetryDelay(2);
		assertEquals(78000L, seventhRetry);
		long eigthRetry = auditStackService.calculateRetryDelay(1);
		assertEquals(120000L, eigthRetry);
	}
}
