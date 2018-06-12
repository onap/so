package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class ActivateNetworkCollectionBBTest extends BaseBPMNTest{
    @Test
    public void sunnyDayActivateNetworkCollection_Test() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateNetworkCollectionBB",variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("activateNetworkCollection_startEvent","Activate_Network_Collection_AAI_ServiceTask","activateNetworkCollection_EndEvent");     
        assertThat(pi).isEnded();
    }

	@Test
	public void rainyDayActivateNetworkCollection_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiUpdateTasks).updateOrchestrationStatusActiveNetworkCollection(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ActivateNetworkCollectionBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("activateNetworkCollection_startEvent")
				.hasNotPassed("activateNetworkCollection_EndEvent");
		assertThat(pi).isEnded().hasVariables("gBuildingBlockExecution");
	}
}