package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class UnassignNetworkBBTest  extends BaseBPMNTest {
    @Test
    public void sunnyDayAssignNetwork_Test() throws InterruptedException {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnassignNetworkBB",variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("Start_UnassignNetworkBB","Task_GetL3NetworkById","Task_VfModuleRelatioship","Task_GetCloudRegionVersion","Task_SNDCUnAssign","Task_DeleteNetwork","End_UnassignNetworkBB");     
        assertThat(pi).isEnded();
    }

	@Test
	public void rainyDayAssignNetwork_Test() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(unassignNetworkBB).checkRelationshipRelatedTo(any(BuildingBlockExecution.class), eq("vf-module"));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnassignNetworkBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("Start_UnassignNetworkBB", "Task_GetL3NetworkById", "Task_VfModuleRelatioship")
				.hasNotPassed("End_UnassignNetworkBB");
		assertThat(pi).isEnded();
	}
}