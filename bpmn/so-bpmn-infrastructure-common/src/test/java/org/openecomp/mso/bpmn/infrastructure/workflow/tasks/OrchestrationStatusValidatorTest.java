package org.openecomp.mso.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.db.catalog.beans.BuildingBlockDetail;
import org.openecomp.mso.db.catalog.beans.OrchestrationAction;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusValidationDirective;
import org.openecomp.mso.db.catalog.beans.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;

public class OrchestrationStatusValidatorTest extends BaseTaskTest {
	@Autowired
	protected OrchestrationStatusValidator orchestrationStatusValidator;
	
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
