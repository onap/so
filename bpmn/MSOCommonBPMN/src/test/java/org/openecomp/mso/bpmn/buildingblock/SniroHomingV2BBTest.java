package org.openecomp.mso.bpmn.buildingblock;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;

public class SniroHomingV2BBTest extends BaseTest{

	@Test
	@Ignore
	public void testHomingV2_success(){
		mockSubprocess("ReceiveWorkflowMessage", "Mock ReceiveWorkflowMessage", "GenericStub");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingV2", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("start", "callSniro", "callReceiveAsync", "processSolution", "end");
		assertThat(pi).isEnded();
	}

	@Test
	@Ignore
	public void testHomingV2_error_bpmnError(){

		doThrow(new BpmnError("MSOWorkflowException")).when(sniroHoming).callSniro(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingV2", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("start", "catchBpmnError", "processBpmnError", "endBpmnError")
				.hasNotPassed("callReceiveAsync");
		assertThat(pi).isEnded();
	}

	@Test
	public void testHomingV2_error_javaException(){

		doThrow(new RuntimeException("Test")).when(sniroHoming).callSniro(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingV2", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("start", "catchJavaException", "processJavaException", "endJavaException")
				.hasNotPassed("callReceiveAsync");
		assertThat(pi).isEnded();
	}

}
