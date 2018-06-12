package org.openecomp.mso.bpmn.servicedecomposition.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.entities.BuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class ExecuteBuildlingBlockRainyDayTest extends BaseTest {
	@Autowired
	private ExecuteBuildingBlockRainyDay executeBuildingBlockRainyDay;
	
	private DelegateExecution execution;
	private ServiceInstance serviceInstance;
	private Customer customer; //will build service sub
	private GenericVnf vnf;
	private WorkflowException workflowException;
	private BuildingBlock buildingBlock;
	private ExecuteBuildingBlock executeBuildingBlock;
	
	@Before
	public void before() {
		execution = new ExecutionImpl();
		
		serviceInstance = setServiceInstance();
		customer = setCustomer();
		vnf = setGenericVnf();
		
		workflowException = new WorkflowException("", 7000, "");
		
		buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName("AssignServiceInstanceBB");
		buildingBlock.setSequenceNumber(0);
		
		executeBuildingBlock = new ExecuteBuildingBlock();
		executeBuildingBlock.setBuildingBlock(buildingBlock);
		
		execution.setVariable("gBBInput", gBBInput);
		execution.setVariable("WorkflowException", workflowException);
		execution.setVariable("buildingBlock", executeBuildingBlock);
	}
	
	@Test
	public void setRetryTimerTest() throws Exception{
		execution.setVariable("retryCount", 2);
		executeBuildingBlockRainyDay.setRetryTimer(execution);
		assertEquals("PT25M",execution.getVariable("RetryDuration"));
	}
	
	@Test
	public void setRetryTimerExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		executeBuildingBlockRainyDay.setRetryTimer(null);
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
		rainyDayHandlerStatus.setWorkStep("ASTERISK");
		
		doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", "st1", "vnft1", "7000", "*");
		
		executeBuildingBlockRainyDay.queryRainyDayTable(execution);
		
		assertEquals("Rollback", execution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableDefault() throws Exception{
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
		vnf.setVnfType("vnft1");

		RainyDayHandlerStatus rainyDayHandlerStatus = new RainyDayHandlerStatus();
		rainyDayHandlerStatus.setErrorCode("ASTERISK");
		rainyDayHandlerStatus.setFlowName("AssignServiceInstanceBB");
		rainyDayHandlerStatus.setServiceType("ASTERISK");
		rainyDayHandlerStatus.setVnfType("ASTERISK");
		rainyDayHandlerStatus.setPolicy("Rollback");
		rainyDayHandlerStatus.setWorkStep("ASTERISK");
		
		doReturn(null).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", "st1", "vnft1", "7000", "ASTERISK");
		doReturn(rainyDayHandlerStatus).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep("AssignServiceInstanceBB", "ASTERISK", "ASTERISK", "ASTERISK", "ASTERISK");
		
		executeBuildingBlockRainyDay.queryRainyDayTable(execution);
		
		assertEquals("Rollback", execution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableDoesNotExist() throws Exception{
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		serviceInstance.getModelInfoServiceInstance().setServiceType("st1");
		vnf.setVnfType("vnft1");

		doReturn(null).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));

		executeBuildingBlockRainyDay.queryRainyDayTable(execution);
		
		assertEquals("Abort", execution.getVariable("handlingCode"));
	}
	
	@Test
	public void queryRainyDayTableExceptionTest() {
		doThrow(Exception.class).when(MOCK_catalogDbClient).getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(isA(String.class), isA(String.class), isA(String.class), isA(String.class), isA(String.class));
		
		executeBuildingBlockRainyDay.queryRainyDayTable(execution);
		
		assertEquals("Abort", execution.getVariable("handlingCode"));
	}
}
