package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class UnassignVolumeGroupBBTest extends BaseBPMNTest {
	@Test
	public void sunnyDayUnassignVolumeGroup_Test() throws InterruptedException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnassignVolumeGroupBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("UnassignVolumeGroupBB_Start", "UnassignVolumeGroup", "UnassignVolumeGroupBB_End");
		assertThat(pi).isEnded();
	}
	
	@Test
	public void rainyDayUnassignVolumeGroup_Test() throws InterruptedException {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiDeleteTasks).deleteVolumeGroup(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnassignVolumeGroupBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("UnassignVolumeGroupBB_Start", "UnassignVolumeGroup")
				.hasNotPassed("UnassignVolumeGroupBB_End");
		assertThat(pi).isEnded();
	}

}
