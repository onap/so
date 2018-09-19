/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.BaseTest;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class ExecuteBuildlingBlockRainyDayTest extends BaseTest {
	@Autowired
	private ExecuteBuildingBlockRainyDay executeBuildingBlockRainyDay;
	
	private ServiceInstance serviceInstance;
	private Customer customer; //will build service sub
	private GenericVnf vnf;
	private BuildingBlock buildingBlock;
	private ExecuteBuildingBlock executeBuildingBlock;
	private static final String ASTERISK = "*";
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		customer = setCustomer();
		vnf = setGenericVnf();
		
		buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName("AssignServiceInstanceBB");
		
		executeBuildingBlock = new ExecuteBuildingBlock();
		executeBuildingBlock.setBuildingBlock(buildingBlock);
		
		delegateExecution.setVariable("gBBInput", gBBInput);
		delegateExecution.setVariable("WorkflowException", new WorkflowException("", 7000, ""));
		delegateExecution.setVariable("buildingBlock", executeBuildingBlock);
		delegateExecution.setVariable("lookupKeyMap", lookupKeyMap);
	}
	
	@Test
	public void setRetryTimerTest() throws Exception{
		delegateExecution.setVariable("retryCount", 2);
		executeBuildingBlockRainyDay.setRetryTimer(delegateExecution);
		assertEquals("PT25M",delegateExecution.getVariable("RetryDuration"));
	}
	
	@Test
	public void setRetryTimerExceptionTest() {
		expectedException.expect(BpmnError.class);
		DelegateExecution execution = mock(DelegateExecution.class);
		when(execution.getVariable(eq("retryCount"))).thenThrow(Exception.class);
		executeBuildingBlockRainyDay.setRetryTimer(execution);
	}
	
	@Test
	public void queryRainyDayTableExists() throws Exception{
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
		vnf.setVnfType("vnft1");
		
		RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
		rainyDayHandlerStatus.setErrorCode("7000");
		rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
		rainyDayHandlerStatus.setServiceType("st1");
		rainyDayHandlerStatus.setVnfType("vnft1");
		rainyDayHandlerStatus.setPolicy("Rollback");
		rainyDayHandlerStatus.setWorkStep(ASTERISK);
		
		doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", "st1", "vnft1", "7000", "*");
		
		executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution);
		
		assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableDefault() throws Exception{
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
		vnf.setVnfType("vnft1");

		RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
		rainyDayHandlerStatus.setErrorCode(ASTERISK);
		rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
		rainyDayHandlerStatus.setServiceType(ASTERISK);
		rainyDayHandlerStatus.setVnfType(ASTERISK);
		rainyDayHandlerStatus.setPolicy("Rollback");
		rainyDayHandlerStatus.setWorkStep(ASTERISK);
		
		doReturn(null).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", "st1", "vnft1", "7000", ASTERISK);
		doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", ASTERISK, ASTERISK, ASTERISK, ASTERISK);
		
		executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution);
		
		assertEquals("Rollback", delegateExecution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableDoesNotExist() throws Exception{
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
		vnf.setVnfType("vnft1");

		doReturn(null).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));

		executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution);
		
		assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableExceptionTest() {
		doThrow(Exception.class).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
		
		executeBuildingBlockRainyDay.queryRainyDayTable(delegateExecution);
		
		assertEquals("Abort", delegateExecution.getVariable("handlingCode"));
	}
}
