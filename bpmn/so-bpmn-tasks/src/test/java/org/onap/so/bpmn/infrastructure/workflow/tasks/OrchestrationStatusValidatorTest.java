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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.onap.so.db.catalog.beans.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class OrchestrationStatusValidatorTest extends BaseTaskTest {
	@InjectMocks
	protected OrchestrationStatusValidator orchestrationStatusValidator = new OrchestrationStatusValidator();
	

	
	@Test
	public void test_validateOrchestrationStatus() throws Exception {
		String flowToBeCalled = "AssignServiceInstanceBB";
		setServiceInstance().setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		
		execution.setVariable("flowToBeCalled", flowToBeCalled);
		
		BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
		buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
		buildingBlockDetail.setId(1);
		buildingBlockDetail.setResourceType(ResourceType.SERVICE);
		buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);
		
		OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = new OrchestrationStatusStateTransitionDirective();
		orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.CONTINUE);
		orchestrationStatusStateTransitionDirective.setId(1);
		orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
		orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient).getOrchestrationStatusStateTransitionDirective(ResourceType.SERVICE, OrchestrationStatus.PRECREATED, OrchestrationAction.ASSIGN);
		
		orchestrationStatusValidator.validateOrchestrationStatus(execution);
		
		assertEquals(OrchestrationStatusValidationDirective.CONTINUE, execution.getVariable("orchestrationStatusValidationResult"));
	}
	
	@Test
	public void test_validateOrchestrationStatusConfiguration() throws Exception {
		lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
		String flowToBeCalled = "UnassignFabricConfigurationBB";  
		ServiceInstance si = setServiceInstance();
		List<Configuration> configurations = new ArrayList<>();
		Configuration config = new Configuration();
		
		si.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		config.setConfigurationId("configurationId");
		config.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		configurations.add(config);
		si.setConfigurations(configurations);
		
		execution.setVariable("flowToBeCalled", flowToBeCalled);
		
		BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
		buildingBlockDetail.setBuildingBlockName("UnassignFabricConfigurationBB");
		buildingBlockDetail.setId(1);
		buildingBlockDetail.setResourceType(ResourceType.CONFIGURATION);
		buildingBlockDetail.setTargetAction(OrchestrationAction.UNASSIGN);
		
		doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);
		
		OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = new OrchestrationStatusStateTransitionDirective();
		orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.SILENT_SUCCESS);
		orchestrationStatusStateTransitionDirective.setId(1);
		orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.CONFIGURATION);
		orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.UNASSIGN);
		
		doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient).getOrchestrationStatusStateTransitionDirective(ResourceType.CONFIGURATION, OrchestrationStatus.PRECREATED, OrchestrationAction.UNASSIGN);
		
		orchestrationStatusValidator.validateOrchestrationStatus(execution);
		
		assertEquals(OrchestrationStatusValidationDirective.SILENT_SUCCESS, execution.getVariable("orchestrationStatusValidationResult"));
	}
	
	@Test
	public void test_validateOrchestrationStatus_buildingBlockDetailNotFound() throws Exception {
		expectedException.expect(BpmnError.class);
		
		String flowToBeCalled = "AssignServiceInstanceBB";
		
		execution.setVariable("flowToBeCalled", flowToBeCalled);
		
		doReturn(null).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);
		
		orchestrationStatusValidator.validateOrchestrationStatus(execution);
	}
	
	@Test
	public void test_validateOrchestrationStatus_orchestrationValidationFail() throws Exception {
		expectedException.expect(BpmnError.class);
		
		String flowToBeCalled = "AssignServiceInstanceBB";
		
		execution.setVariable("flowToBeCalled", flowToBeCalled);
		
		BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
		buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
		buildingBlockDetail.setId(1);
		buildingBlockDetail.setResourceType(ResourceType.SERVICE);
		buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);
		
		OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = new OrchestrationStatusStateTransitionDirective();
		orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.FAIL);
		orchestrationStatusStateTransitionDirective.setId(1);
		orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
		orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient).getOrchestrationStatusStateTransitionDirective(ResourceType.SERVICE, OrchestrationStatus.PRECREATED, OrchestrationAction.ASSIGN);
		
		orchestrationStatusValidator.validateOrchestrationStatus(execution);
	}
	
	@Test
	public void test_validateOrchestrationStatus_orchestrationValidationNotFound() throws Exception {
		expectedException.expect(BpmnError.class);
		
		String flowToBeCalled = "AssignServiceInstanceBB";
		
		execution.setVariable("flowToBeCalled", flowToBeCalled);
		
		BuildingBlockDetail buildingBlockDetail = new BuildingBlockDetail();
		buildingBlockDetail.setBuildingBlockName("AssignServiceInstanceBB");
		buildingBlockDetail.setId(1);
		buildingBlockDetail.setResourceType(ResourceType.SERVICE);
		buildingBlockDetail.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(buildingBlockDetail).when(catalogDbClient).getBuildingBlockDetail(flowToBeCalled);
		
		OrchestrationStatusStateTransitionDirective orchestrationStatusStateTransitionDirective = new OrchestrationStatusStateTransitionDirective();
		orchestrationStatusStateTransitionDirective.setFlowDirective(OrchestrationStatusValidationDirective.FAIL);
		orchestrationStatusStateTransitionDirective.setId(1);
		orchestrationStatusStateTransitionDirective.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		orchestrationStatusStateTransitionDirective.setResourceType(ResourceType.SERVICE);
		orchestrationStatusStateTransitionDirective.setTargetAction(OrchestrationAction.ASSIGN);
		
		doReturn(orchestrationStatusStateTransitionDirective).when(catalogDbClient).getOrchestrationStatusStateTransitionDirective(ResourceType.NETWORK, OrchestrationStatus.PRECREATED, OrchestrationAction.ASSIGN);
		
		orchestrationStatusValidator.validateOrchestrationStatus(execution);
	}
}
