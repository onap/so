package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

/**
 * Unit test cases for DeActivateServiceInstanceTest.bpmn
 */
public class DeactivateServiceInstanceBBTest extends BaseBPMNTest{
	@Test
	public void sunnyDayDeactivateServiceInstanceSDNC() throws InterruptedException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("DeactivateServiceInstanceBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_DeactivateServiceInstanceBB", "Task_DeactivateServiceInstance_SDNC", "Task_DeactivateServiceInstance_AAI", "End_DeactivateServiceInstanceBB");
		assertThat(pi).isEnded();
	}
}
